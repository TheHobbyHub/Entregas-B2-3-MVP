-- Aulas de exemplo: categorias, ateliês, instrutores e 4 aulas em São Paulo.
-- Roda toda vez que a API sobe, mas só cadastra o que ainda não existe (pelo nome/título): não apaga nem duplica nada.
-- Para apagar e recriar as aulas do zero, use o script manual db/dados-exemplo-aulas.sql.

-- Ícones da família MaterialCommunityIcons do @expo/vector-icons
INSERT INTO tb_categorias (nome, icone)
SELECT v.nome, v.icone FROM (VALUES
    ('Cerâmica & Argila', 'flower-outline'),
    ('Pintura & Aquarela', 'brush'),
    ('Marcenaria', 'hammer'),
    ('Gastronomia', 'chef-hat'),
    ('Costura & Bordado', 'needle')
) AS v(nome, icone)
WHERE NOT EXISTS (SELECT 1 FROM tb_categorias c WHERE c.nome = v.nome);

INSERT INTO tb_enderecos (logradouro, numero, complemento, bairro, cidade, estado, cep)
SELECT v.* FROM (VALUES
    ('Rua Girassol', '123', NULL, 'Vila Madalena', 'São Paulo', 'SP', '05433000'),
    ('Alameda dos Ipês', '450', 'Sala 2', 'Pinheiros', 'São Paulo', 'SP', '05422010'),
    ('Rua Cardoso de Almeida', '980', NULL, 'Perdizes', 'São Paulo', 'SP', '05013001'),
    ('Rua Augusta', '1500', 'Loja 3', 'Consolação', 'São Paulo', 'SP', '01304001')
) AS v(logradouro, numero, complemento, bairro, cidade, estado, cep)
WHERE NOT EXISTS (SELECT 1 FROM tb_enderecos e WHERE e.logradouro = v.logradouro AND e.numero = v.numero);

INSERT INTO tb_parceiros (nome, endereco_id)
SELECT v.nome, (SELECT e.id FROM tb_enderecos e WHERE e.logradouro = v.logradouro AND e.numero = v.numero LIMIT 1)
FROM (VALUES
    ('Ateliê Luz', 'Rua Girassol', '123'),
    ('Espaço Criar', 'Alameda dos Ipês', '450'),
    ('Estúdio Oficina Viva', 'Rua Cardoso de Almeida', '980'),
    ('Cozinha Afeto', 'Rua Augusta', '1500')
) AS v(nome, logradouro, numero)
WHERE NOT EXISTS (SELECT 1 FROM tb_parceiros p WHERE p.nome = v.nome);

INSERT INTO tb_instrutores (nome, especialidade, foto_url, parceiro_id)
SELECT v.nome, v.especialidade, NULL, (SELECT p.id FROM tb_parceiros p WHERE p.nome = v.parceiro LIMIT 1)
FROM (VALUES
    ('Sofia Martins', 'Mestre Ceramista', 'Ateliê Luz'),
    ('Clara Mendes', 'Artista Botânica', 'Espaço Criar'),
    ('Rafael Costa', 'Marceneiro', 'Estúdio Oficina Viva'),
    ('Mariana Santos', 'Chef Confeiteira', 'Cozinha Afeto')
) AS v(nome, especialidade, parceiro)
WHERE NOT EXISTS (SELECT 1 FROM tb_instrutores i WHERE i.nome = v.nome);

INSERT INTO tb_aulas (titulo, descricao, categoria_id, subcategoria, nivel, duracao_minutos, imagem_url, destaque, parceiro_id, instrutor_id)
SELECT v.titulo, v.descricao,
       (SELECT c.id FROM tb_categorias c WHERE c.nome = v.categoria LIMIT 1),
       v.subcategoria, v.nivel, v.duracao, NULL, v.destaque,
       (SELECT p.id FROM tb_parceiros p WHERE p.nome = v.parceiro LIMIT 1),
       (SELECT i.id FROM tb_instrutores i WHERE i.nome = v.instrutor LIMIT 1)
FROM (VALUES
    ('Cerâmica Iniciante: Torno & Modelagem',
     'Nesta aula você aprenderá técnicas básicas de modelagem manual, acabamento no torno, queima e introdução à esmaltação. Todo o material essencial está incluído e você leva sua peça final para casa após o ciclo no forno.',
     'Cerâmica & Argila', 'Torno & Modelagem', 'Básico', 120, true, 'Ateliê Luz', 'Sofia Martins'),
    ('Aquarela Botânica & Estudo de Cores',
     'Aprenda a observar e pintar folhas e flores em aquarela, explorando mistura de cores, transparências e camadas.',
     'Pintura & Aquarela', 'Aquarela', 'Básico', 90, true, 'Espaço Criar', 'Clara Mendes'),
    ('Marcenaria Básica & Pequenos Objetos',
     'Introdução ao uso seguro de ferramentas manuais e elétricas, com a construção de um pequeno objeto em madeira.',
     'Marcenaria', 'Pequenos Objetos', 'Intermediário', 180, false, 'Estúdio Oficina Viva', 'Rafael Costa'),
    ('Pães de Fermentação Natural',
     'Do levain ao forno: prepare seu próprio pão de fermentação natural e entenda cada etapa do processo.',
     'Gastronomia', 'Panificação', 'Básico', 150, false, 'Cozinha Afeto', 'Mariana Santos')
) AS v(titulo, descricao, categoria, subcategoria, nivel, duracao, destaque, parceiro, instrutor)
WHERE NOT EXISTS (SELECT 1 FROM tb_aulas a WHERE a.titulo = v.titulo);

-- Planos que dão acesso a cada aula.
-- Cerâmica e Pães: todos os planos | Aquarela: a partir do segundo plano mais barato | Marcenaria: só o mais caro
INSERT INTO tb_aulas_planos (aula_id, plano_id)
SELECT a.id, p.id
FROM tb_aulas a
CROSS JOIN tb_planos p
WHERE (a.titulo IN ('Cerâmica Iniciante: Torno & Modelagem', 'Pães de Fermentação Natural')
       OR (a.titulo = 'Aquarela Botânica & Estudo de Cores'
           AND p.id <> (SELECT id FROM tb_planos ORDER BY preco_mensal ASC LIMIT 1))
       OR (a.titulo = 'Marcenaria Básica & Pequenos Objetos'
           AND p.id = (SELECT id FROM tb_planos ORDER BY preco_mensal DESC LIMIT 1)))
  AND NOT EXISTS (SELECT 1 FROM tb_aulas_planos ap WHERE ap.aula_id = a.id AND ap.plano_id = p.id);

-- Só na primeira vez (quando as aulas ainda não têm horário nenhum): horários de demonstração relativos à data
-- atual, no fuso de Brasília (o container do Postgres usa UTC), e aulas já concluídas para quem tem assinatura
-- ativa, para a aba "Concluídas" ter conteúdo. Os horários semanais até 08/2027 ficam em 04-horarios-recorrentes.sql.
WITH novos_horarios AS (
    INSERT INTO tb_horarios_aula (aula_id, data_hora, capacidade)
    SELECT a.id, h.inicio, h.capacidade
    FROM tb_aulas a
    JOIN (
        SELECT v.titulo, date_trunc('day', NOW() AT TIME ZONE 'America/Sao_Paulo') + v.deslocamento AS inicio, v.capacidade
        FROM (VALUES
            -- Cerâmica: um horário lotado (capacidade 1)
            ('Cerâmica Iniciante: Torno & Modelagem', INTERVAL '2 days 10 hours', 1),
            ('Cerâmica Iniciante: Torno & Modelagem', INTERVAL '2 days 18 hours', 8),
            ('Cerâmica Iniciante: Torno & Modelagem', INTERVAL '4 days 19 hours', 8),
            ('Cerâmica Iniciante: Torno & Modelagem', INTERVAL '6 days 14 hours', 8),
            ('Aquarela Botânica & Estudo de Cores', INTERVAL '3 days 19 hours', 10),
            ('Aquarela Botânica & Estudo de Cores', INTERVAL '5 days 10 hours', 10),
            -- Começa durante a Cerâmica das 19h do mesmo dia: útil para testar o conflito de horário
            ('Aquarela Botânica & Estudo de Cores', INTERVAL '4 days 20 hours', 10),
            ('Marcenaria Básica & Pequenos Objetos', INTERVAL '4 days 14 hours', 6),
            ('Marcenaria Básica & Pequenos Objetos', INTERVAL '7 days 9 hours', 6),
            ('Pães de Fermentação Natural', INTERVAL '3 days 9 hours', 12),
            ('Pães de Fermentação Natural', INTERVAL '8 days 15 hours', 12),
            -- Já passaram: viram o histórico de aulas concluídas
            ('Cerâmica Iniciante: Torno & Modelagem', INTERVAL '-3 days' + INTERVAL '14 hours', 8),
            ('Aquarela Botânica & Estudo de Cores', INTERVAL '-6 days' + INTERVAL '19 hours', 8)
        ) AS v(titulo, deslocamento, capacidade)
        UNION ALL
        -- Começa em menos de 24h: permite agendar, mas não reagendar; cancelar não devolve o crédito
        SELECT 'Cerâmica Iniciante: Torno & Modelagem', date_trunc('hour', NOW() AT TIME ZONE 'America/Sao_Paulo') + INTERVAL '5 hours', 10
    ) AS h ON h.titulo = a.titulo
    WHERE NOT EXISTS (SELECT 1 FROM tb_horarios_aula)
    RETURNING id, aula_id, data_hora
)
INSERT INTO tb_agendamentos (usuario_id, horario_id, data_agendamento, creditos_pagos, status)
SELECT ass.usuario_id, nh.id, nh.data_hora - INTERVAL '5 days', 1, 'AGENDADO'
FROM novos_horarios nh
JOIN tb_assinaturas ass ON ass.status = 'ATIVA'
JOIN tb_aulas_planos ap ON ap.aula_id = nh.aula_id AND ap.plano_id = ass.plano_id
WHERE nh.data_hora < NOW() AT TIME ZONE 'America/Sao_Paulo';
