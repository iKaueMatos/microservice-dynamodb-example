# Aplicação de Exemplo CRUD com DynamoDB

Esta é uma aplicação Spring Boot que demonstra como realizar operações CRUD usando o AWS DynamoDB. A aplicação é projetada para gerenciar perfis de jogadores e históricos de jogos, utilizando o AWS DynamoDB para armazenamento de dados e o LocalStack para emulação local dos serviços AWS.


## Funcionalidades

- **Gerenciamento de Perfis de Jogadores**:
  - Criar, recuperar, atualizar e excluir perfis de jogadores.
  - Desativar perfis de jogadores.
  - Prevenir duplicação de perfis com base no e-mail.

- **Gerenciamento de Histórico de Jogos**:
  - Criar, recuperar, atualizar e excluir históricos de jogos para jogadores.
  - Filtrar históricos de jogos por pontuação e intervalo de datas.
  - Recuperar as maiores pontuações de um jogador.
  - Limitar o número de jogos que um jogador pode jogar por dia.

- **Integração com AWS**:
  - Utiliza o AWS DynamoDB para armazenamento de dados.
  - O LocalStack é usado para emular os serviços AWS localmente.

## Pré-requisitos

- **Java**: Certifique-se de que o Java 17 ou superior está instalado.
- **Maven**: Certifique-se de que o Maven está instalado.
- **Docker**: Certifique-se de que o Docker está instalado e em execução.
- **LocalStack**: Usado para emulação local dos serviços AWS.

## Instruções de Configuração

### 1. Clone o Repositório

```bash
git clone https://github.com/your-repository-url.git
cd buildrun-java-aws-dynamodb-crud-exemplo-master
```

### 2. Inicie o LocalStack

Inicie o LocalStack usando o Docker Compose:

```bash
docker-compose up -d
```

Isso iniciará o LocalStack com os serviços necessários (DynamoDB e S3).

### 3. Crie as Tabelas do DynamoDB

Execute o script `up-localstack.sh` para criar as tabelas necessárias no DynamoDB:

```bash
bash infra/up-localstack.sh
```

Este script cria as seguintes tabelas:
- `player_history`: Armazena o histórico de jogos dos jogadores.
- `player_profile`: Armazena informações dos perfis dos jogadores.

### 4. Compile e Execute a Aplicação

Compile a aplicação usando o Maven:

```bash
./mvnw clean install
```

Execute a aplicação:

```bash
./mvnw spring-boot:run
```

A aplicação será iniciada em `http://localhost:8080`.

---

## Endpoints da API

### Gerenciamento de Perfis de Jogadores

#### Criar um Perfil de Jogador
**POST** `/v1/players/profiles`

Corpo da Requisição:
```json
{
  "playerId": "player123",
  "name": "João Silva",
  "email": "joao.silva@exemplo.com"
}
```

Resposta:
- `201 Created`: Perfil criado com sucesso.
- `409 Conflict`: Um perfil com o mesmo e-mail já existe.

---

#### Recuperar um Perfil de Jogador
**GET** `/v1/players/profiles/{playerId}`

Resposta:
- `200 OK`: Retorna o perfil do jogador.
- `404 Not Found`: Perfil não encontrado.

---

#### Atualizar um Perfil de Jogador
**PUT** `/v1/players/profiles/{playerId}`

Corpo da Requisição:
```json
{
  "name": "João Atualizado",
  "email": "joao.atualizado@exemplo.com"
}
```

Resposta:
- `204 No Content`: Perfil atualizado com sucesso.
- `404 Not Found`: Perfil não encontrado.

---

#### Excluir um Perfil de Jogador
**DELETE** `/v1/players/profiles/{playerId}`

Resposta:
- `204 No Content`: Perfil excluído com sucesso.
- `404 Not Found`: Perfil não encontrado.

---

#### Desativar um Perfil de Jogador
**PUT** `/v1/players/profiles/{playerId}/deactivate`

Resposta:
- `204 No Content`: Perfil desativado com sucesso.
- `404 Not Found`: Perfil não encontrado.

---

### Gerenciamento de Histórico de Jogos

#### Criar um Histórico de Jogo
**POST** `/v1/players/{playerId}/games`

Corpo da Requisição:
```json
{
  "score": 100.0
}
```

Resposta:
- `201 Created`: Histórico de jogo criado com sucesso.
- `429 Too Many Requests`: O jogador atingiu o limite diário de jogos.

---

#### Recuperar Todos os Jogos de um Jogador
**GET** `/v1/players/{playerId}/games`

Resposta:
- `200 OK`: Retorna uma lista de históricos de jogos.

---

#### Recuperar um Jogo Específico
**GET** `/v1/players/{playerId}/games/{gameId}`

Resposta:
- `200 OK`: Retorna o histórico do jogo.
- `404 Not Found`: Jogo não encontrado.

---

#### Atualizar um Histórico de Jogo
**PUT** `/v1/players/{playerId}/games/{gameId}`

Corpo da Requisição:
```json
{
  "score": 150.0
}
```

Resposta:
- `204 No Content`: Histórico de jogo atualizado com sucesso.
- `404 Not Found`: Jogo não encontrado.

---

#### Excluir um Histórico de Jogo
**DELETE** `/v1/players/{playerId}/games/{gameId}`

Resposta:
- `204 No Content`: Histórico de jogo excluído com sucesso.
- `404 Not Found`: Jogo não encontrado.

---

#### Filtrar Jogos por Pontuação e Data
**GET** `/v1/players/{playerId}/games/filter`

Parâmetros de Consulta:
- `minScore` (opcional): Pontuação mínima.
- `maxScore` (opcional): Pontuação máxima.
- `startDate` (opcional): Data de início (formato ISO 8601).
- `endDate` (opcional): Data de término (formato ISO 8601).

Resposta:
- `200 OK`: Retorna uma lista filtrada de históricos de jogos.

---

#### Recuperar Maiores Pontuações
**GET** `/v1/players/{playerId}/games/top-scores`

Resposta:
- `200 OK`: Retorna as 10 maiores pontuações do jogador.

---

## Esquema das Tabelas do DynamoDB

### `player_profile`
| Atributo           | Tipo   | Chave |
|---------------------|--------|-------|
| `player_id`         | String | HASH  |
| `name`              | String |       |
| `email`             | String |       |
| `registration_date` | String |       |
| `status`            | String |       |

---

### `player_history`
| Atributo    | Tipo   | Chave |
|-------------|--------|-------|
| `player_id` | String | HASH  |
| `game_id`   | String | RANGE |
| `score`     | Number |       |
| `created_at`| String |       |
| `game_mode` | String |       |

---

## Limpeza

Para parar e remover o LocalStack, execute:

```bash
docker-compose down
```

Para excluir as tabelas do DynamoDB, execute:

```bash
bash infra/down-localstack.sh
```

---

## Tecnologias Utilizadas

- **Spring Boot**: Framework backend.
- **AWS DynamoDB**: Banco de dados NoSQL.
- **LocalStack**: Emulador local de serviços AWS.
- **Docker**: Plataforma de containerização.

## Licença

Este projeto está licenciado sob a Licença MIT. Consulte o arquivo `LICENSE` para mais detalhes.
