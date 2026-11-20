package br.fatec.hobby_hub.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Cartão de cobrança da assinatura. Só guarda o necessário para exibir: nunca o número completo nem o CVV
@Entity
@Table(name = "tb_cartoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cartao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false, length = 4)
    private String ultimosDigitos;

    @Column(nullable = false)
    private String nomeTitular;

    // Formato MM/AA
    @Column(nullable = false, length = 5)
    private String validade;
}
