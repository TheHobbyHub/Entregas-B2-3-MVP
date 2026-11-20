# HobbyHub

## 1. Sobre o sistema

HobbyHub é um aplicativo mobile de assinatura que centraliza a oferta de aulas e experiências criativas, como cerâmica, pintura, música, dança, culinária e artesanato, em um único catálogo digital. A plataforma resolve a fragmentação e a burocracia desse mercado conectando dois públicos principais: pessoas interessadas em descobrir ou desenvolver novos hobbies, que passam a explorar diferentes modalidades de forma prática e flexível usando um saldo mensal de créditos (sem precisar fechar matrículas ou contratos individuais em cada local), e escolas, ateliês e profissionais independentes, que ganham um canal centralizado para divulgar suas atividades, ampliar sua visibilidade e simplificar a gestão de horários, vagas e reservas.

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

## 4. Pré-requisitos e execução

### Pré-requisitos

- [Git](https://git-scm.com/downloads)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado e **aberto**

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

### 4.2 Rodar o app

**No celular Android, pelo APK**

1. Conecte o celular e o computador que está rodando a API **na mesma rede Wi-Fi**.
2. Descubra o IP do computador na rede:
   - **Windows:** rode `ipconfig` e copie o **Endereço IPv4** do adaptador Wi-Fi.
3. No celular, baixe o APK.
4. Abra o arquivo `.apk` e instale. Se o Android avisar sobre "fontes desconhecidas", permita a instalação pelo navegador. Se já houver uma versão antiga do HobbyHub instalada, desinstale antes.
5. Ao abrir o app, na tela **Configuração de Servidor**, informe `http://SEU_IP:8080`, com o mesmo endereço do passo 2.

## 5. Usuários e senhas de teste

O MVP tem um perfil de acesso: **aluno**. Ateliês, instrutores e aulas são cadastrados automaticamente ao subir a API e não têm login nesta versão.

| Perfil | E-mail | Senha | Situação |
|---|---|---|---|
| Aluno com assinatura | `assinante@hobbyhub.com` | `Hobby@2026` | Plano Plus ativo com 4 créditos e 3 faturas no histórico. Use para agendar aulas e gerenciar a assinatura. |
| Aluno sem assinatura | `visitante@hobbyhub.com` | `Hobby@2026` | Conta nova, sem plano. Use para testar a escolha de plano e o checkout. |

