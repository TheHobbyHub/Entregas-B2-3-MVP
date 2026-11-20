-- Horários semanais para todas as aulas de exemplo, de amanhã até 31/08/2027.
-- Roda toda vez que a API sobe, depois das aulas (03-aulas-exemplo.sql). Horários que já existem não são duplicados.
-- Para estender o período, troque a data final no generate_series.

INSERT INTO tb_horarios_aula (aula_id, data_hora, capacidade)
SELECT a.id, dia + g.hora, g.capacidade
FROM tb_aulas a
JOIN (VALUES
    -- dia_semana: 1 = segunda ... 7 = domingo
    ('Cerâmica Iniciante: Torno & Modelagem', 2, INTERVAL '19 hours', 8),
    ('Cerâmica Iniciante: Torno & Modelagem', 6, INTERVAL '10 hours', 8),
    ('Aquarela Botânica & Estudo de Cores',   3, INTERVAL '19 hours', 10),
    ('Aquarela Botânica & Estudo de Cores',   6, INTERVAL '14 hours', 10),
    ('Marcenaria Básica & Pequenos Objetos',  4, INTERVAL '18 hours', 6),
    ('Marcenaria Básica & Pequenos Objetos',  7, INTERVAL '9 hours',  6),
    ('Pães de Fermentação Natural',           5, INTERVAL '18 hours', 12),
    ('Pães de Fermentação Natural',           7, INTERVAL '14 hours', 12)
) AS g(titulo, dia_semana, hora, capacidade) ON g.titulo = a.titulo
-- Datas no fuso de Brasília (o container do Postgres usa UTC)
CROSS JOIN generate_series(
    date_trunc('day', NOW() AT TIME ZONE 'America/Sao_Paulo') + INTERVAL '1 day',
    TIMESTAMP '2027-08-31',
    INTERVAL '1 day'
) AS dia
WHERE EXTRACT(ISODOW FROM dia) = g.dia_semana
  AND NOT EXISTS (
      SELECT 1 FROM tb_horarios_aula h WHERE h.aula_id = a.id AND h.data_hora = dia + g.hora
  );
