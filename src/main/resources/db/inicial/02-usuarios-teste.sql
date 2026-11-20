-- Usuários de teste do HobbyHub (senha de ambos: Hobby@2026).
--   assinante@hobbyhub.com : Plano Plus ativo, cartão cadastrado e 3 faturas no histórico
--   visitante@hobbyhub.com : conta sem assinatura, para testar o fluxo de escolha de plano
-- Roda toda vez que a API sobe, depois dos planos (01-planos.sql). O que já existe não é duplicado.

-- Hash BCrypt de "Hobby@2026" (o mesmo formato que a API grava no cadastro)
INSERT INTO tb_usuarios (nome, sobrenome, email, cpf, telefone, data_nascimento, senha, status, saldo_creditos)
SELECT v.nome, v.sobrenome, v.email, v.cpf, v.telefone, v.nascimento::date,
       '$2a$10$C3rgmqDgwWdfKkLWhpF69uw1Rc9oyl6KH18bq7bx4Frfx7Jumpn5y', 'ATIVO', v.saldo
FROM (VALUES
    ('Aluno', 'Assinante', 'assinante@hobbyhub.com', '47209381597', '11987654321', '1998-05-10', 4),
    ('Aluno', 'Visitante', 'visitante@hobbyhub.com', '83615027426', '11912345678', '2000-09-22', 0)
) AS v(nome, sobrenome, email, cpf, telefone, nascimento, saldo)
WHERE NOT EXISTS (SELECT 1 FROM tb_usuarios u WHERE u.email = v.email OR u.cpf = v.cpf);

-- Assinatura do Plano Plus: o ciclo atual começou há 10 dias
INSERT INTO tb_assinaturas (usuario_id, plano_id, data_inicio, data_vencimento, status, cancelamento_agendado)
SELECT u.id, p.id, CURRENT_DATE - 10, (CURRENT_DATE - 10) + INTERVAL '1 month', 'ATIVA', false
FROM tb_usuarios u
JOIN tb_planos p ON p.nome = 'Plano Plus'
WHERE u.email = 'assinante@hobbyhub.com'
  AND NOT EXISTS (SELECT 1 FROM tb_assinaturas a WHERE a.usuario_id = u.id);

-- Cartão de cobrança (o pagamento é simulado; só os 4 últimos dígitos são guardados)
INSERT INTO tb_cartoes (usuario_id, ultimos_digitos, nome_titular, validade)
SELECT u.id, '4242', 'ALUNO ASSINANTE', '12/30'
FROM tb_usuarios u
WHERE u.email = 'assinante@hobbyhub.com'
  AND NOT EXISTS (SELECT 1 FROM tb_cartoes c WHERE c.usuario_id = u.id);

-- Mensalidades pagas: ciclo atual e os dois anteriores
INSERT INTO tb_faturas (assinatura_id, plano_id, valor, data, tipo, status)
SELECT a.id, a.plano_id, p.preco_mensal, (a.data_inicio - m * INTERVAL '1 month')::date, 'MENSALIDADE', 'PAGO'
FROM tb_assinaturas a
JOIN tb_usuarios u ON u.id = a.usuario_id
JOIN tb_planos p ON p.id = a.plano_id
CROSS JOIN generate_series(0, 2) AS m
WHERE u.email = 'assinante@hobbyhub.com'
  AND NOT EXISTS (SELECT 1 FROM tb_faturas f WHERE f.assinatura_id = a.id);
