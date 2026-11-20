package br.fatec.hobby_hub.services;

import br.fatec.hobby_hub.dto.AssinaturaRequestDTO;
import br.fatec.hobby_hub.dto.CartaoRespostaDTO;
import br.fatec.hobby_hub.dto.DetalhesAssinaturaDTO;
import br.fatec.hobby_hub.dto.FaturaRespostaDTO;
import br.fatec.hobby_hub.dto.OpcaoTrocaPlanoDTO;
import br.fatec.hobby_hub.dto.ResumoAssinaturaDTO;
import br.fatec.hobby_hub.infrastructure.entity.Assinatura;
import br.fatec.hobby_hub.infrastructure.entity.Fatura;
import br.fatec.hobby_hub.infrastructure.entity.Plano;
import br.fatec.hobby_hub.infrastructure.entity.StatusAgendamento;
import br.fatec.hobby_hub.infrastructure.entity.TipoFatura;
import br.fatec.hobby_hub.infrastructure.entity.Usuario;
import br.fatec.hobby_hub.infrastructure.repository.AgendamentoRepository;
import br.fatec.hobby_hub.infrastructure.repository.AssinaturaRepository;
import br.fatec.hobby_hub.infrastructure.repository.FaturaRepository;
import br.fatec.hobby_hub.infrastructure.repository.PlanoRepository;
import br.fatec.hobby_hub.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AssinaturaService {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AssinaturaRepository assinaturaRepository;
    private final PlanoRepository planoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final FaturaRepository faturaRepository;
    private final CartaoService cartaoService;

    public List<Plano> listarPlanos() {
        return planoRepository.findAll();
    }

    @Transactional
    public String realizarAssinatura(AssinaturaRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        Plano plano = planoRepository.findById(dto.planoId())
                .orElseThrow(() -> new RuntimeException("Plano não encontrado"));

        List<Assinatura> assinaturasExistentes = assinaturaRepository
                .findByUsuarioIdAndStatusIn(usuario.getId(), List.of("ATIVA", "PENDENTE"));

        Assinatura assinatura = null;

        for (Assinatura ass : assinaturasExistentes) {
            if ("ATIVA".equals(ass.getStatus())) {
                throw new RuntimeException("O usuário já possui uma assinatura ativa.");
            }
            if ("PENDENTE".equals(ass.getStatus())) {
                assinatura = ass;
            }
        }

        if (assinatura == null) {
            assinatura = new Assinatura();
            assinatura.setUsuario(usuario);
        }

        assinatura.setPlano(plano);
        assinatura.setDataInicio(LocalDate.now());
        assinatura.setDataVencimento(LocalDate.now().plusMonths(1));

        String statusPagamento = simularGatewayPagamento(dto.metodoPagamento());

        if ("RECUSADO".equals(statusPagamento)) {
            throw new RuntimeException("Pagamento negado pela operadora. Tente utilizar outro método.");
        }

        if ("FALHA_SISTEMA".equals(statusPagamento)) {
            assinatura.setStatus("PENDENTE");
            assinaturaRepository.save(assinatura);
            return "Sistema de pagamento indisponível. Pedido salvo como Pendente. Tente concluir mais tarde.";
        }

        assinatura.setStatus("ATIVA");
        assinaturaRepository.save(assinatura);
        registrarFatura(assinatura, plano, plano.getPrecoMensal(), TipoFatura.MENSALIDADE, LocalDate.now());

        // Se o cartão for inválido, a exceção desfaz toda a assinatura (método transacional)
        if (dto.cartao() != null) {
            cartaoService.salvar(usuario.getId(), dto.cartao());
        }

        // Os créditos não acumulam: o saldo é sempre o valor contratado no plano
        usuario.setSaldoCreditos(plano.getCreditosMensais());
        usuarioRepository.save(usuario);

        return "Assinatura ativada com sucesso!";
    }

    // Fecha os ciclos que terminaram: encerra as assinaturas canceladas, aplica downgrades agendados,
    // registra a fatura de cada renovação e volta o saldo para os créditos do plano
    @Transactional
    public int renovarAssinaturasVencidas() {
        LocalDate hoje = LocalDate.now();
        List<Assinatura> vencidas = assinaturaRepository.findByStatusAndDataVencimentoLessThanEqual("ATIVA", hoje);

        for (Assinatura assinatura : vencidas) {
            Usuario usuario = assinatura.getUsuario();

            if (Boolean.TRUE.equals(assinatura.getCancelamentoAgendado())) {
                assinatura.setStatus("CANCELADA");
                assinaturaRepository.save(assinatura);
                usuario.setSaldoCreditos(0);
                usuarioRepository.save(usuario);
                continue;
            }

            // Se a aplicação ficou desligada por mais de um mês, avança até o ciclo atual (uma fatura por ciclo)
            while (!assinatura.getDataVencimento().isAfter(hoje)) {
                if (assinatura.getPlanoAgendado() != null) {
                    assinatura.setPlano(assinatura.getPlanoAgendado());
                    assinatura.setPlanoAgendado(null);
                }
                assinatura.setDataInicio(assinatura.getDataVencimento());
                assinatura.setDataVencimento(assinatura.getDataVencimento().plusMonths(1));
                registrarFatura(assinatura, assinatura.getPlano(), assinatura.getPlano().getPrecoMensal(),
                        TipoFatura.MENSALIDADE, assinatura.getDataInicio());
            }
            assinaturaRepository.save(assinatura);

            usuario.setSaldoCreditos(assinatura.getPlano().getCreditosMensais());
            usuarioRepository.save(usuario);
        }

        return vencidas.size();
    }

    public Optional<Assinatura> buscarAssinaturaAtiva(Long usuarioId) {
        return assinaturaRepository.findByUsuarioIdAndStatusIn(usuarioId, List.of("ATIVA"))
                .stream()
                .findFirst();
    }

    public Plano buscarPlanoAtivo(Long usuarioId) {
        return buscarAssinaturaAtiva(usuarioId)
                .map(Assinatura::getPlano)
                .orElseThrow(() -> new RuntimeException("Você não possui uma assinatura ativa. Assine um plano para agendar aulas."));
    }

    public ResumoAssinaturaDTO resumo(Long usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);

        return buscarAssinaturaAtiva(usuarioId)
                .map(assinatura -> {
                    long concluidas = agendamentoRepository.countByUsuarioIdAndStatusAndHorarioDataHoraBetween(
                            usuarioId, StatusAgendamento.AGENDADO,
                            assinatura.getDataInicio().atStartOfDay(), LocalDateTime.now());
                    return new ResumoAssinaturaDTO(
                            true,
                            assinatura.getPlano().getId(),
                            assinatura.getPlano().getNome(),
                            assinatura.getPlano().getCreditosMensais(),
                            usuario.getSaldoCreditos(),
                            assinatura.getDataInicio(),
                            assinatura.getDataVencimento(),
                            concluidas
                    );
                })
                .orElseGet(() -> new ResumoAssinaturaDTO(false, null, null, null,
                        usuario.getSaldoCreditos(), null, null, 0L));
    }

    public DetalhesAssinaturaDTO detalhes(Long usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        CartaoRespostaDTO cartao = cartaoService.buscar(usuarioId).map(CartaoRespostaDTO::new).orElse(null);

        return buscarAssinaturaAtiva(usuarioId)
                .map(assinatura -> new DetalhesAssinaturaDTO(
                        true,
                        assinatura.getPlano().getId(),
                        assinatura.getPlano().getNome(),
                        assinatura.getPlano().getPrecoMensal(),
                        assinatura.getPlano().getCreditosMensais(),
                        usuario.getSaldoCreditos(),
                        assinatura.getDataInicio(),
                        assinatura.getDataVencimento(),
                        Boolean.TRUE.equals(assinatura.getCancelamentoAgendado()),
                        assinatura.getPlanoAgendado() != null ? assinatura.getPlanoAgendado().getNome() : null,
                        cartao
                ))
                .orElseGet(() -> new DetalhesAssinaturaDTO(false, null, null, null, null,
                        usuario.getSaldoCreditos(), null, null, false, null, cartao));
    }

    public List<FaturaRespostaDTO> listarFaturas(Long usuarioId) {
        buscarUsuario(usuarioId);
        return faturaRepository.findByAssinaturaUsuarioIdOrderByDataDescIdDesc(usuarioId).stream()
                .map(FaturaRespostaDTO::new)
                .toList();
    }

    // Todos os planos, do mais barato ao mais caro, com o efeito de trocar para cada um
    public List<OpcaoTrocaPlanoDTO> opcoesTroca(Long usuarioId) {
        Assinatura assinatura = buscarAssinaturaParaGerenciar(usuarioId);
        Plano atual = assinatura.getPlano();
        LocalDate hoje = LocalDate.now();

        return planoRepository.findAll(Sort.by("precoMensal")).stream()
                .map(plano -> {
                    if (plano.getId().equals(atual.getId())) {
                        return opcao(plano, "ATUAL", BigDecimal.ZERO, 0, assinatura.getDataInicio());
                    }
                    if (ehUpgrade(atual, plano)) {
                        return opcao(plano, "UPGRADE", valorUpgrade(assinatura, plano, hoje),
                                creditosAdicionais(atual, plano), hoje);
                    }
                    boolean agendado = assinatura.getPlanoAgendado() != null
                            && assinatura.getPlanoAgendado().getId().equals(plano.getId());
                    return opcao(plano, agendado ? "AGENDADO" : "DOWNGRADE", BigDecimal.ZERO, 0,
                            assinatura.getDataVencimento());
                })
                .toList();
    }

    // Upgrade vale na hora, cobrando a diferença proporcional aos dias que faltam e somando a diferença de créditos.
    // Downgrade fica agendado para a próxima renovação. Em ambos, a última escolha substitui um downgrade agendado
    @Transactional
    public String trocarPlano(Long usuarioId, Long planoId) {
        Assinatura assinatura = buscarAssinaturaParaGerenciar(usuarioId);
        if (Boolean.TRUE.equals(assinatura.getCancelamentoAgendado())) {
            throw new RuntimeException("Sua assinatura está cancelada. Reative-a para trocar de plano.");
        }

        Plano atual = assinatura.getPlano();
        Plano novo = planoRepository.findById(planoId)
                .orElseThrow(() -> new RuntimeException("Plano não encontrado"));
        if (novo.getId().equals(atual.getId())) {
            throw new RuntimeException("Você já está neste plano.");
        }

        if (!ehUpgrade(atual, novo)) {
            assinatura.setPlanoAgendado(novo);
            assinaturaRepository.save(assinatura);
            return "Seu plano muda para o " + novo.getNome() + " em " + assinatura.getDataVencimento().format(FORMATO_DATA)
                    + ". Até lá, você continua com o " + atual.getNome() + ".";
        }

        if (cartaoService.buscar(usuarioId).isEmpty()) {
            throw new RuntimeException("Cadastre um cartão em Forma de Pagamento para fazer o upgrade.");
        }

        LocalDate hoje = LocalDate.now();
        BigDecimal valor = valorUpgrade(assinatura, novo, hoje);
        int creditosExtras = creditosAdicionais(atual, novo);

        assinatura.setPlano(novo);
        assinatura.setPlanoAgendado(null);
        assinaturaRepository.save(assinatura);

        if (valor.signum() > 0) {
            registrarFatura(assinatura, novo, valor, TipoFatura.UPGRADE, hoje);
        }

        Usuario usuario = assinatura.getUsuario();
        usuario.setSaldoCreditos(usuario.getSaldoCreditos() + creditosExtras);
        usuarioRepository.save(usuario);

        return "Upgrade concluído! Você já está no " + novo.getNome() + ".";
    }

    @Transactional
    public void desistirDaTroca(Long usuarioId) {
        Assinatura assinatura = buscarAssinaturaParaGerenciar(usuarioId);
        if (assinatura.getPlanoAgendado() == null) {
            throw new RuntimeException("Não há troca de plano agendada.");
        }
        assinatura.setPlanoAgendado(null);
        assinaturaRepository.save(assinatura);
    }

    // A assinatura continua valendo até o vencimento e então não renova. Descarta um downgrade agendado
    @Transactional
    public LocalDate cancelar(Long usuarioId) {
        Assinatura assinatura = buscarAssinaturaParaGerenciar(usuarioId);
        if (Boolean.TRUE.equals(assinatura.getCancelamentoAgendado())) {
            throw new RuntimeException("Sua assinatura já está cancelada.");
        }
        assinatura.setCancelamentoAgendado(true);
        assinatura.setPlanoAgendado(null);
        assinaturaRepository.save(assinatura);
        return assinatura.getDataVencimento();
    }

    @Transactional
    public void reativar(Long usuarioId) {
        Assinatura assinatura = buscarAssinaturaParaGerenciar(usuarioId);
        if (!Boolean.TRUE.equals(assinatura.getCancelamentoAgendado())) {
            throw new RuntimeException("Sua assinatura não está cancelada.");
        }
        assinatura.setCancelamentoAgendado(false);
        assinaturaRepository.save(assinatura);
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    private Assinatura buscarAssinaturaParaGerenciar(Long usuarioId) {
        buscarUsuario(usuarioId);
        return buscarAssinaturaAtiva(usuarioId)
                .orElseThrow(() -> new RuntimeException("Você não possui uma assinatura ativa."));
    }

    private boolean ehUpgrade(Plano atual, Plano novo) {
        return novo.getPrecoMensal().compareTo(atual.getPrecoMensal()) > 0;
    }

    // (preço novo - preço atual) x dias que faltam no ciclo / dias do ciclo
    private BigDecimal valorUpgrade(Assinatura assinatura, Plano novo, LocalDate hoje) {
        long diasCiclo = ChronoUnit.DAYS.between(assinatura.getDataInicio(), assinatura.getDataVencimento());
        if (diasCiclo <= 0) return BigDecimal.ZERO;
        long diasRestantes = Math.clamp(ChronoUnit.DAYS.between(hoje, assinatura.getDataVencimento()), 0, diasCiclo);

        return novo.getPrecoMensal().subtract(assinatura.getPlano().getPrecoMensal())
                .multiply(BigDecimal.valueOf(diasRestantes))
                .divide(BigDecimal.valueOf(diasCiclo), 2, RoundingMode.HALF_UP);
    }

    private int creditosAdicionais(Plano atual, Plano novo) {
        return Math.max(0, novo.getCreditosMensais() - atual.getCreditosMensais());
    }

    private OpcaoTrocaPlanoDTO opcao(Plano plano, String tipo, BigDecimal valor, int creditos, LocalDate dataInicio) {
        return new OpcaoTrocaPlanoDTO(plano.getId(), plano.getNome(), plano.getPrecoMensal(),
                plano.getCreditosMensais(), tipo, valor, creditos, dataInicio);
    }

    private void registrarFatura(Assinatura assinatura, Plano plano, BigDecimal valor, TipoFatura tipo, LocalDate data) {
        Fatura fatura = new Fatura();
        fatura.setAssinatura(assinatura);
        fatura.setPlano(plano);
        fatura.setValor(valor);
        fatura.setData(data);
        fatura.setTipo(tipo);
        fatura.setStatus("PAGO");
        faturaRepository.save(fatura);
    }

    private String simularGatewayPagamento(String inputFrontend) {
        if ("recusado".equalsIgnoreCase(inputFrontend)) return "RECUSADO";
        if ("timeout".equalsIgnoreCase(inputFrontend)) return "FALHA_SISTEMA";
        return "APROVADO";
    }
}
