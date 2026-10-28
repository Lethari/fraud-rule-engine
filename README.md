# Fraud Rule Engine

A Spring Boot REST service that evaluates categorized transaction events against configurable fraud rules, stores the result in PostgreSQL, and provides a paginated history API.

The bundled rule policy is **provisional demo configuration**, not approved fraud guidance. Have the responsible risk/business owners define and review production thresholds before relying on decisions.

## Requirements

- Docker Desktop with Docker Compose, for the recommended local setup
- Java 21 and Maven 3.9 or later, if running or testing outside Docker

## Run locally with Docker Compose

1. Copy the example environment file and edit it. Use private values for your local environment:

   ```powershell
   Copy-Item .env.example .env
   notepad .env
   ```

   `FRAUD_API_PASSWORD` must contain at least 12 characters. The example values are for local development only and must not be used in a deployed environment. Add any policy overrides described in [Rule policy](#rule-policy) to `.env`.

2. Build the application image and start the API and PostgreSQL:

   ```powershell
   docker compose up --build
   ```

   Compose waits for PostgreSQL to pass its health check before starting the API. On its first start, Flyway applies the database migrations. The runtime image runs as a non-root user and has a Docker health check.

3. Check that the service is responding:

   ```powershell
   Invoke-RestMethod http://localhost:8080/api/fraud/health
   ```

The API is available at `http://localhost:8080`. PostgreSQL is exposed only on the local machine at port `5432`.

Stop the containers with `Ctrl+C`, or run `docker compose down`. The named PostgreSQL volume keeps data across container restarts. **To permanently delete the local database and its data**, run `docker compose down --volumes`.

## Run with a local PostgreSQL server

Create a PostgreSQL database and user, then set the required variables in the PowerShell session that will run the app:

```powershell
$env:DATABASE_URL = "jdbc:postgresql://localhost:5432/fraud_engine"
$env:DATABASE_USERNAME = "fraud_engine"
$env:DATABASE_PASSWORD = "your-database-password"
$env:FRAUD_API_USERNAME = "your-api-username"
$env:FRAUD_API_PASSWORD = "your-api-password-at-least-12-characters"
$env:FRAUD_CORS_ALLOWED_ORIGINS = ""
mvn spring-boot:run
```

The database must be running and reachable before startup. The application uses Flyway for schema migrations and Hibernate schema validation; it does not create an in-memory or fallback database.

## API

Only the health endpoint is public. `/api/fraud/check` and `/api/fraud/history` require HTTP Basic authentication using `FRAUD_API_USERNAME` and `FRAUD_API_PASSWORD`. Use HTTPS in any environment where credentials or transaction data cross an untrusted network.

### Health

```http
GET /api/fraud/health
```

### Evaluate a transaction

Send a nonblank event category, positive amount, and an ISO-8601 timestamp that includes a UTC offset:

```powershell
$credentials = "your-api-username:your-api-password-at-least-12-characters"
$encoded = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes($credentials))
$headers = @{ Authorization = "Basic $encoded" }
$transaction = @{
    "id": "txn-001",
    "eventCategory": "PURCHASE",
    "amount": 1500.50,
    "country": "ZA",
    "timestamp": "2026-10-28T20:10:00+02:00"
} | ConvertTo-Json

Invoke-RestMethod `
    -Uri http://localhost:8080/api/fraud/check `
    -Method Post `
    -Headers $headers `
    -ContentType "application/json" `
    -Body $transaction
```

The endpoint stores one result for each transaction ID, including its event category and timestamp. Repeating a request with an existing ID returns its stored result rather than creating a duplicate. Event category is currently persisted as metadata; it does not affect scoring because category-specific rules were not provided.

#### Windows CMD / curl.exe alternative (avoids PowerShell alias issue)

On Windows, `curl` is an alias for PowerShell's Invoke-WebRequest. Use `curl.exe` instead:

```cmd
curl.exe -X POST http://localhost:8080/api/fraud/check -u admin:Admin@123 -H "Content-Type: application/json" -d @transaction.json
```

**Tested examples — different transaction types and countries:**

1. High-risk: `WIRE_TRANSFER` + `NG` (95 points - CRITICAL)
```json
{
  "transactionId": "TXN-WIRE-NG-001",
  "eventCategory": "WIRE_TRANSFER",
  "transactionTimestamp": "2026-10-28T07:30:00+02:00",
  "country": "NG",
  "amount": 15000.00,
  "userId": "user123"
}
```
Response: `{"fraud":true,"riskScore":95,"category":"CRITICAL","reasons":["High risk country: NG [Risk: +95]"]}`

2. Low-risk: `ATM_WITHDRAWAL` + `ZA` (0 points - LOW)
```json
{
  "transactionId": "TXN-ATM-ZA-001",
  "eventCategory": "ATM_WITHDRAWAL",
  "transactionTimestamp": "2026-10-28T07:30:00+02:00",
  "country": "ZA",
  "amount": 5000.00,
  "userId": "user123"
}
```
Response: `{"fraud":false,"riskScore":0,"category":"LOW","reasons":[]}`

Note: `transaction.json` must contain only ONE transaction object. `id` and `transactionId` are aliases; `timestamp` and `transactionTimestamp` are aliases.


### Retrieve result history

History is ordered newest first. The default page size is 20; `size` must be between 1 and 100, and `page` starts at 0:

```powershell
Invoke-RestMethod `
    -Uri "http://localhost:8080/api/fraud/history?page=0&size=20" `
    -Headers $headers
```

### Configure cross-origin requests

Cross-origin access is disabled by default. If a browser client needs it, set `FRAUD_CORS_ALLOWED_ORIGINS` to a comma-separated list of explicit origins, for example `https://app.example.com`. Wildcard origins are rejected.

## Rule policy

Rule values live in externalized Spring configuration rather than Java rule implementations. The `fraud.rules.*` and `fraud.scoring.*` properties in `application.properties` supply provisional local defaults; environment variables override them. For Docker Compose, put overrides in `.env`, which Compose passes to the application container. Values are read at startup, so a change requires an application restart. Set `FRAUD_POLICY_VERSION` to a new approved version for each policy release; each stored fraud result records the version used to evaluate it.

Examples:

```dotenv
FRAUD_RULES_AMOUNT_THRESHOLD=10000.00
FRAUD_RULES_AMOUNT_HIGH_THRESHOLD=20000.00
FRAUD_RULES_AMOUNT_CRITICAL_THRESHOLD=50000.00
FRAUD_RULES_AMOUNT_SCORE=30
FRAUD_RULES_AMOUNT_HIGH_SCORE=50
FRAUD_RULES_AMOUNT_CRITICAL_SCORE=80
FRAUD_SCORING_MEDIUM_THRESHOLD=20
FRAUD_SCORING_FRAUD_THRESHOLD=50
FRAUD_SCORING_CRITICAL_THRESHOLD=80
FRAUD_RULES_COUNTRY_NG_SCORE=95
FRAUD_POLICY_VERSION=risk-policy-2026-01
```

Amount thresholds must be positive and strictly increasing. Night-time hour ranges must be valid and ordered, scoring cutoffs must be nondecreasing, and country scores must be between 0 and 100. Country overrides use `FRAUD_RULES_COUNTRY_<COUNTRY_KEY>_SCORE`; remove or add supported keys in `application.properties` when changing the configured country set.

Before production, establish a controlled policy lifecycle: obtain business/risk approval, record the policy version and effective date, validate changes in tests, and deploy reviewed configuration through a secrets/configuration manager. Do not treat the sample country list or score defaults as factual risk advice.

## Build and test

Run the automated unit and API integration tests:

```powershell
mvn test
```

If Maven Central is temporarily unreachable and all dependencies are already in your local Maven cache, use offline mode:

```powershell
mvn -o test
```

`mvn -o` cannot retrieve uncached dependencies. Certificate validation errors when resolving artifacts are environmental Maven/Java trust configuration issues; do not disable TLS certificate validation to work around them.

The Docker image can be built with `docker compose build app`; run the complete local stack with the Compose instructions above. Docker image builds require Docker Desktop and access to the configured container registries and Maven repositories.

## Configuration reference

| Variable | Required | Description |
|---|---:|---|
| `DATABASE_URL` | No | JDBC URL; defaults to `jdbc:postgresql://localhost:5432/fraud_engine` |
| `DATABASE_USERNAME` | Yes | PostgreSQL account used by the application |
| `DATABASE_PASSWORD` | Yes | PostgreSQL account password |
| `DATABASE_NAME` | No | Database name used by Docker Compose; defaults to `fraud_engine` |
| `FRAUD_API_USERNAME` | Yes | HTTP Basic API username |
| `FRAUD_API_PASSWORD` | Yes | HTTP Basic password; at least 12 characters |
| `FRAUD_CORS_ALLOWED_ORIGINS` | No | Comma-separated explicit browser origins; empty disables cross-origin access |
| `FRAUD_POLICY_VERSION` | No | Policy identifier stored with each decision; default is `demo-provisional-v1` |
| `FRAUD_RULES_*` | No | Overrides for the provisional amount, country, and time rule settings |
| `FRAUD_SCORING_*` | No | Overrides for risk-band and fraud-decision thresholds |

Never commit a real `.env` file or use the development credentials in production.