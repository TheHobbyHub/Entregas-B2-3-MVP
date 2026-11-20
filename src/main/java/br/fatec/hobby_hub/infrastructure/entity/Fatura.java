package br.fatec.hobby_hub.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tb_faturas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Fatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "assinatura_id", nullable = false)
    private Assinatura assinatura;

    // Plano cobrado nesta fatura (o plano da assinatura pode mudar depois)
    @ManyToOne
    @JoinColumn(name = "plano_id", nullable = false)
    private Plano plano;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDate data;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoFatura tipo;

    // O pagamento é simulado: toda fatura registrada já foi paga
    @Column(nullable = false)
    private String status;
}
