package br.fatec.hobby_hub.services;

import br.fatec.hobby_hub.dto.CartaoRequestDTO;
import br.fatec.hobby_hub.dto.CartaoRespostaDTO;
import br.fatec.hobby_hub.infrastructure.entity.Cartao;
import br.fatec.hobby_hub.infrastructure.entity.Usuario;
import br.fatec.hobby_hub.infrastructure.repository.CartaoRepository;
import br.fatec.hobby_hub.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartaoService {

    private final CartaoRepository cartaoRepository;
    private final UsuarioRepository usuarioRepository;

    public Optional<Cartao> buscar(Long usuarioId) {
        return cartaoRepository.findByUsuarioId(usuarioId);
    }

    // Cada usuário tem um cartão de cobrança: cadastrar um novo substitui o anterior
    @Transactional
    public CartaoRespostaDTO salvar(Long usuarioId, CartaoRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String numero = dto.numero().replaceAll("\\D", "");
        if (numero.length() < 13 || numero.length() > 19) {
            throw new RuntimeException("Número do cartão inválido.");
        }

        String[] partesValidade = dto.validade().split("/");
        YearMonth validade = YearMonth.of(2000 + Integer.parseInt(partesValidade[1]), Integer.parseInt(partesValidade[0]));
        if (validade.isBefore(YearMonth.now())) {
            throw new RuntimeException("Este cartão está vencido.");
        }

        Cartao cartao = cartaoRepository.findByUsuarioId(usuarioId).orElseGet(() -> {
            Cartao novo = new Cartao();
            novo.setUsuario(usuario);
            return novo;
        });
        cartao.setUltimosDigitos(numero.substring(numero.length() - 4));
        cartao.setNomeTitular(dto.nomeTitular().trim());
        cartao.setValidade(dto.validade());

        return new CartaoRespostaDTO(cartaoRepository.save(cartao));
    }
}
