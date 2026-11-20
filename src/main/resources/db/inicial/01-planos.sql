-- Planos de assinatura. Só cadastra se ainda não houver nenhum plano.
-- Os scripts desta pasta rodam sozinhos toda vez que a API sobe (veja application.properties).

INSERT INTO tb_planos (nome, preco_mensal, creditos_mensais, recomendado)
SELECT * FROM (VALUES
    ('Plano Essencial', 129.90, 2, false),
    ('Plano Plus',      229.90, 4, true),
    ('Plano Premium',   319.90, 6, false)
) AS p
WHERE NOT EXISTS (SELECT 1 FROM tb_planos);
