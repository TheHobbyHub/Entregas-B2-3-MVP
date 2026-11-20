package br.fatec.hobby_hub.controller;

import br.fatec.hobby_hub.dto.AssinaturaRequestDTO;
import br.fatec.hobby_hub.dto.CartaoRequestDTO;
import br.fatec.hobby_hub.dto.CartaoRespostaDTO;
import br.fatec.hobby_hub.dto.DetalhesAssinaturaDTO;
import br.fatec.hobby_hub.dto.FaturaRespostaDTO;
import br.fatec.hobby_hub.dto.OpcaoTrocaPlanoDTO;
import br.fatec.hobby_hub.dto.ResumoAssinaturaDTO;
import br.fatec.hobby_hub.dto.TrocaPlanoRequestDTO;
import br.fatec.hobby_hub.infrastructure.entity.Plano;
import br.fatec.hobby_hub.services.AssinaturaService;
import br.fatec.hobby_hub.services.CartaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/assinaturas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AssinaturaController {

    private final AssinaturaService service;
    private final CartaoService cartaoService;

    @GetMapping("/planos")
    public ResponseEntity<List<Plano>> listarPlanos() {
        return ResponseEntity.ok(service.listarPlanos());
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<ResumoAssinaturaDTO> resumo(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(service.resumo(usuarioId));
    }

    @PostMapping("/assinar")
    public ResponseEntity<Map<String, String>> assinar(@RequestBody @Valid AssinaturaRequestDTO dto) {
        try {
            String resultado = service.realizarAssinatura(dto);
            return ResponseEntity.ok(Map.of("mensagem", resultado));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // Gerenciar Assinatura

    @GetMapping("/usuario/{usuarioId}/detalhes")
    public ResponseEntity<DetalhesAssinaturaDTO> detalhes(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(service.detalhes(usuarioId));
    }

    @GetMapping("/usuario/{usuarioId}/faturas")
    public ResponseEntity<List<FaturaRespostaDTO>> faturas(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(service.listarFaturas(usuarioId));
    }

    @PutMapping("/usuario/{usuarioId}/cartao")
    public ResponseEntity<CartaoRespostaDTO> trocarCartao(@PathVariable Long usuarioId, @RequestBody @Valid CartaoRequestDTO dto) {
        return ResponseEntity.ok(cartaoService.salvar(usuarioId, dto));
    }

    @GetMapping("/usuario/{usuarioId}/opcoes-troca")
    public ResponseEntity<List<OpcaoTrocaPlanoDTO>> opcoesTroca(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(service.opcoesTroca(usuarioId));
    }

    @PostMapping("/usuario/{usuarioId}/trocar-plano")
    public ResponseEntity<Map<String, String>> trocarPlano(@PathVariable Long usuarioId, @RequestBody @Valid TrocaPlanoRequestDTO dto) {
        return ResponseEntity.ok(Map.of("mensagem", service.trocarPlano(usuarioId, dto.planoId())));
    }

    @DeleteMapping("/usuario/{usuarioId}/troca-agendada")
    public ResponseEntity<Map<String, String>> desistirDaTroca(@PathVariable Long usuarioId) {
        service.desistirDaTroca(usuarioId);
        return ResponseEntity.ok(Map.of("mensagem", "Troca de plano desfeita. Você continua no plano atual."));
    }

    @PostMapping("/usuario/{usuarioId}/cancelar")
    public ResponseEntity<Map<String, String>> cancelar(@PathVariable Long usuarioId) {
        LocalDate fim = service.cancelar(usuarioId);
        return ResponseEntity.ok(Map.of("mensagem",
                "Assinatura cancelada. Você mantém o acesso até " + fim.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "."));
    }

    @PostMapping("/usuario/{usuarioId}/reativar")
    public ResponseEntity<Map<String, String>> reativar(@PathVariable Long usuarioId) {
        service.reativar(usuarioId);
        return ResponseEntity.ok(Map.of("mensagem", "Assinatura reativada! Ela será renovada normalmente."));
    }
}
