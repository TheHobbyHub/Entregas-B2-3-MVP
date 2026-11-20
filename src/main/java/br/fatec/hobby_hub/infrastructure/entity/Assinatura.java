package br.fatec.hobby_hub.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "tb_assinaturas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Assinatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "plano_id", nullable = false)
    private Plano plano;

    @Column(nullable = false)
    private LocalDate dataInicio;

    @Column(nullable = false)
    private LocalDate dataVencimento;

    @Column(nullable = false)
    private String status;

    // Downgrade escolhido pelo usuário: passa a valer na próxima renovação
    @ManyToOne
    @JoinColumn(name = "plano_agendado_id")
    private Plano planoAgendado;

    // Cancelada pelo usuário: continua ATIVA até a data de vencimento e não renova.
    // O default no banco permite criar a coluna em tabelas que já têm assinaturas
    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean cancelamentoAgendado = false;
}