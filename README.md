# HobbyHub

## 1. Sobre o sistema

HobbyHub é um aplicativo mobile de assinatura que centraliza a oferta de aulas e experiências criativas — como cerâmica, pintura, música, dança, culinária e artesanato — em um único catálogo digital. A plataforma resolve a fragmentação e a burocracia desse mercado conectando dois públicos principais: pessoas interessadas em descobrir ou desenvolver novos hobbies, que passam a explorar diferentes modalidades de forma prática e flexível usando um saldo mensal de créditos (sem precisar fechar matrículas ou contratos individuais em cada local), e escolas, ateliês e profissionais independentes, que ganham um canal centralizado para divulgar suas atividades, ampliar sua visibilidade e simplificar a gestão de horários, vagas e reservas.

O sistema é dividido em duas partes:

| Parte | Repositório | O que faz |
|---|---|---|
| API (back-end) | este repositório | Regras de negócio, banco de dados e endpoints REST |
| App (front-end) | [TheHobbyHub/hobbyhub-front](https://github.com/TheHobbyHub/hobbyhub-front) | Aplicativo mobile usado pelos alunos |

**Funcionalidades do MVP:** cadastro, login e recuperação de senha; escolha e assinatura de plano; catálogo de aulas com filtros; agendamento, reagendamento e cancelamento de aulas com controle de créditos; perfil e edição de dados; gerenciamento da assinatura (upgrade e downgrade de plano, troca de cartão, histórico de faturamento e cancelamento).

## 2. Integrantes

| Nome completo | RA |
|---|---|
| Lia Noemi Matsubara | 2040482323038 |
| Beatriz Stefani da Silva | 2040482323049 |

**Turno:** Noturno

## 3. Tecnologias e versões

### Back-end (este repositório)

| Tecnologia | Versão |
|---|---|
| Java | 21 |
| Spring Boot (Web MVC, Data JPA, Security, Validation, Mail) | 4.1.1 |
| Maven | 3.9 (via Maven Wrapper `mvnw`) |
| Lombok | gerenciada pelo Spring Boot |
| PostgreSQL | 16 |
| Docker / Docker Compose | Docker Desktop recente (Compose v2) |

### Front-end ([hobbyhub-front](https://github.com/TheHobbyHub/hobbyhub-front))

| Tecnologia | Versão |
|---|---|
| JavaScript | ES2022+ |
| React Native | 0.88.1 |
| React | 19.3.0 |
| Expo SDK | 58 |
| React Navigation | 7 |
| Axios | 1.20 |
| AsyncStorage | 2.2.0 |
| Node.js | 22.13 ou superior |

**Segurança:** as senhas são guardadas com hash BCrypt. O pagamento é **simulado**: nenhum valor é cobrado e só os 4 últimos dígitos do cartão são armazenados.

## 4. Pré-requisitos e execução

### Pré-requisitos

- [Git](https://git-scm.com/downloads)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado e **aberto**
- [Node.js](https://nodejs.org/) 22.13 ou superior (para rodar o app)

> Os comandos abaixo funcionam no PowerShell (Windows) e no terminal do Linux/macOS. No Git Bash, comece os comandos `docker cp` e `docker exec` com `MSYS_NO_PATHCONV=1 ` para o caminho `/tmp/...` não ser convertido.

### 4.1 Subir a API e o banco de dados

```bash
git clone https://github.com/TheHobbyHub/Entregas-B2-3-MVP.git
cd Entregas-B2-3-MVP
docker compose up -d --build
```

Na primeira vez o build leva alguns minutos. A API está pronta quando o log mostrar `Started HobbyHubApplication`:

```bash
docker logs hobbyhub_backend
```

Confira no navegador: http://localhost:8080/aulas deve responder `[]`.

### 4.2 Carregar os dados iniciais

Rode os comandos abaixo **na pasta do repositório**, nesta ordem.

**Planos de assinatura** (Essencial, Plus e Premium):

```bash
docker exec hobbyhub_postgres psql -U hobbyhub_user -d hobbyhub_db -c "INSERT INTO tb_planos (nome, preco_mensal, creditos_mensais, recomendado) SELECT * FROM (VALUES ('Plano Essencial', 129.90, 2, false), ('Plano Plus', 229.90, 4, true), ('Plano Premium', 319.90, 6, false)) AS p WHERE NOT EXISTS (SELECT 1 FROM tb_planos);"
```

**Usuários de teste, aulas de exemplo e horários até 08/2027:**

```bash
docker cp src/main/resources/db/. hobbyhub_postgres:/tmp/db
docker exec hobbyhub_postgres psql -U hobbyhub_user -d hobbyhub_db -f /tmp/db/usuarios-teste.sql
docker exec hobbyhub_postgres psql -U hobbyhub_user -d hobbyhub_db -f /tmp/db/dados-exemplo-aulas.sql
docker exec hobbyhub_postgres psql -U hobbyhub_user -d hobbyhub_db -f /tmp/db/horarios-recorrentes.sql
```

| Script | O que faz |
|---|---|
| `usuarios-teste.sql` | Cria os usuários da seção 5. Pode ser rodado de novo sem duplicar. |
| `dados-exemplo-aulas.sql` | Cria categorias, ateliês, instrutores e 4 aulas em São Paulo, com aulas já concluídas para quem tem assinatura. **Apaga e recria** aulas, horários e agendamentos; usuários, planos e assinaturas são mantidos. |
| `horarios-recorrentes.sql` | Cria horários semanais para as aulas até 31/08/2027. Pode ser rodado de novo sem duplicar. |

Confira em http://localhost:8080/assinaturas/planos: devem aparecer os três planos.

### 4.3 Rodar o app

Em outra pasta (fora deste repositório):

```bash
git clone https://github.com/TheHobbyHub/hobbyhub-front.git
cd hobbyhub-front
npm install
```

**Opção A — no navegador (mais simples):**

```bash
npx expo start --web
```

O app abre no navegador. Na tela **Configuração de Servidor**, informe:

```
http://localhost:8080
```

**Opção B — no celular Android, com o app Expo Go:**

1. Instale o **Expo Go** pela Play Store.
2. Conecte o celular e o computador **na mesma rede Wi-Fi**.
3. Descubra o IP do computador: no Windows, rode `ipconfig` e copie o **Endereço IPv4** do adaptador Wi-Fi (algo como `192.168.0.12`).
4. Rode `npx expo start` e leia o QR Code com o Expo Go.
5. Na tela **Configuração de Servidor**, informe `http://SEU_IP:8080` (por exemplo, `http://192.168.0.12:8080`).

Para trocar o endereço depois, toque na engrenagem ⚙ da tela inicial.

### 4.4 Problemas comuns

- **"Falha ao conectar ao servidor" no celular:** confira se o celular está no mesmo Wi-Fi e se o endereço começa com `http://` e termina com `:8080`. No Windows, libere a porta TCP 8080 no Firewall (Firewall do Windows > Configurações avançadas > Regras de Entrada > Nova Regra > Porta). Redes de faculdade costumam bloquear a comunicação entre aparelhos; nesse caso, use o roteador do celular (hotspot).
- **"Não foi possível carregar os planos":** os planos não foram cadastrados. Refaça o passo 4.2.
- **Nenhum horário para agendar:** rode de novo o `horarios-recorrentes.sql` (passo 4.2).

### 4.5 Parar o sistema

```bash
docker compose down
```

Os dados ficam salvos no volume do Docker. Para subir de novo, use `docker compose up -d`.

## 5. Usuários e senhas de teste

O MVP tem um perfil de acesso: **aluno**. Ateliês, instrutores e aulas são cadastrados pelos scripts e não têm login nesta versão.

| Perfil | E-mail | Senha | Situação |
|---|---|---|---|
| Aluno com assinatura | `assinante@hobbyhub.com` | `Hobby@2026` | Plano Plus ativo com 4 créditos, cartão final 4242 e 3 faturas no histórico. Use para agendar aulas e gerenciar a assinatura. |
| Aluno sem assinatura | `visitante@hobbyhub.com` | `Hobby@2026` | Conta nova, sem plano. Use para testar a escolha de plano e o checkout. |

**Cartão para o checkout (pagamento simulado):** qualquer número com 13 a 19 dígitos e validade futura é aceito, por exemplo:

| Número | Nome impresso | Validade | CVV |
|---|---|---|---|
| `4242 4242 4242 4242` | `ALUNO TESTE` | `12/30` | `123` |

Também é possível criar uma conta nova pela tela **Cadastrar-se**. A senha precisa ter 8 caracteres ou mais, com letra maiúscula, letra minúscula, número e um caractere especial (`@$!%*?&#`).
