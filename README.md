# HireForge AI

HireForge is a private job-search workspace for tracking applications and preparing for interviews. It combines a React dashboard with a JWT-protected Spring Boot API and Groq-powered career tools.

**Live demo:** [hireforge-ai.pages.dev](https://hireforge-ai.pages.dev)

## Features

- Email/password registration and sign-in with BCrypt password hashing and signed JWTs.
- User-scoped application CRUD, status updates, filtering, and company/role search.
- Dashboard totals, pipeline distribution, response rate, interview rate, and offer rate.
- PDF resume analysis with role-aware strengths, weaknesses, skills not evidenced, ATS suggestions, and actionable edits.
- Resume/job-description matching with evidence-backed overlaps and missing keywords.
- Interview question generation and answer evaluation with constructive feedback and a sample answer.
- Responsive dashboard and career-tool screens, accessible form states, and clear API errors.

Career-tool requests are sent to Groq for processing. HireForge does not store resume text, job descriptions, or interview answers. The Groq key remains on the backend.

## Application Screenshots

**Dashboard** — Track application totals and review pipeline progress at a glance.

![HireForge AI dashboard and application pipeline](01-dashboard.png)

**Application tracker** — Search, review, and update roles across application stages.

![HireForge AI application tracker](02-application-tracker.png)

**Resume review** — Get role-aware feedback on resume strengths and areas to improve.

![HireForge AI resume review](03-resume-review.png)

**Job match** — Compare resume evidence with a job description to identify role alignment.

![HireForge AI job match](04-job-match.png)

**Interview practice** — Generate role-specific questions and practice interview preparation.

![HireForge AI interview practice](05-interview-practice.png)

## Architecture

```mermaid
flowchart LR
  Browser[React 19 and Vite] -->|Bearer JWT and JSON| API[Spring Boot REST API]
  API -->|JPA and user-scoped queries| DB[(MySQL 8)]
  API -->|Server-side HTTPS and JSON schema| Groq[Groq Chat Completions]
  API -. JWT verification .-> Auth[Spring Security]
```

The frontend never receives the Groq API key. AI tools do not write their submitted text or results into the application database. Existing application records remain linked to their owning user.

## Tech stack

| Area | Technology |
| --- | --- |
| Frontend | React 19, Vite, Lucide icons, responsive CSS |
| Backend | Java 21, Spring Boot 3.5, Spring MVC, Bean Validation |
| Authentication | Spring Security, JWT, BCrypt |
| Persistence | Spring Data JPA, MySQL 8, H2 for tests |
| Resume parsing | Apache PDFBox |
| AI | Groq OpenAI-compatible Chat Completions, default model `openai/gpt-oss-120b` |
| Deployment | Docker, Docker Compose, Nginx |

The configured Groq model is `openai/gpt-oss-120b`. Check Groq's current model and structured-output documentation before changing the configured model.

## Requirements

- Java 21
- MySQL 8 or compatible for local development
- Node.js 22.12+ (or 20.19+) and npm
- Docker Engine with the Docker Compose plugin for the containerized demo

## Local development

1. Create a MySQL database named `hireforge` (or configure `DB_URL`). Local mode uses `JPA_DDL_AUTO=update` to keep the existing local schema usable. Back up any database before changing schemas.
2. Set backend variables in the shell where the backend will run. Do not commit an actual `.env`, password, JWT secret, or Groq key. The backend does not automatically load `.env` files.

   PowerShell example:

   ```powershell
   $env:DB_URL = 'jdbc:mysql://localhost:3306/hireforge?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
   $env:DB_USERNAME = 'your_mysql_user'
   $env:DB_PASSWORD = 'your_mysql_password'
   $env:JWT_SECRET = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
   $env:CORS_ALLOWED_ORIGINS = 'http://localhost:5173'
   $env:GROQ_API_KEY = 'set-this-locally-from-your-Groq-console'
   $env:GROQ_MODEL = 'openai/gpt-oss-120b'
   ```

   Keep the shell open while running the backend. To run without a Groq key, omit `GROQ_API_KEY`; protected AI endpoints then return `503` with a configuration message, while account and application features remain available.

3. Start the backend:

   ```powershell
   cd backend
   .\mvnw.cmd spring-boot:run
   ```

4. In a second terminal, start the frontend:

   ```powershell
   cd frontend
   npm ci
   npm run dev
   ```

   Vite uses `http://localhost:8080/api` by default. To point it elsewhere, create `frontend/.env` from `frontend/.env.example` and set `VITE_API_URL`. This value is public frontend configuration; never place API keys in a `VITE_` variable.

## Environment variables

Copy the relevant placeholder file for reference: root `.env.example` for Docker Compose, `backend/.env.example` for the API, or `frontend/.env.example` for Vite. These files contain placeholders only and are committed intentionally. Replace placeholders in your local environment or hosting dashboard, never in source files.

| Variable | Used by | Purpose |
| --- | --- | --- |
| `DB_URL` | Backend | JDBC URL for MySQL |
| `DB_USERNAME`, `DB_PASSWORD` | Backend | MySQL credentials |
| `PORT` | Backend on Render | Render-provided HTTP listening port; defaults to `10000` |
| `JPA_DDL_AUTO` | Backend | Local default is `update`; production profile uses `validate` |
| `JWT_SECRET` | Backend | Base64-encoded HMAC secret; generate at least 32 random bytes |
| `JWT_EXPIRATION_MS` | Backend | Optional JWT lifetime; defaults to 24 hours |
| `CORS_ALLOWED_ORIGINS` | Backend | Comma-separated exact frontend origins; no wildcard |
| `GROQ_API_KEY` | Backend only | Groq secret; never expose it to Vite or the browser |
| `GROQ_MODEL` | Backend | Defaults to `openai/gpt-oss-120b` |
| `GROQ_API_BASE_URL` | Backend | Defaults to `https://api.groq.com/openai/v1` |
| `GROQ_CONNECT_TIMEOUT_MS`, `GROQ_READ_TIMEOUT_MS` | Backend | Groq HTTP timeouts; defaults are 3000 and 45000 ms |
| `VITE_API_URL` | Frontend build | API base URL. Public, not a secret. |

## API endpoints

All routes except registration and login require `Authorization: Bearer <token>`.

| Method | Path | Request | Purpose |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | `{name,email,password}` | Create account and return JWT |
| `POST` | `/api/auth/login` | `{email,password}` | Authenticate and return JWT |
| `GET` | `/api/applications` | — | List current user’s applications |
| `GET` | `/api/applications/{id}` | — | Read an owned application |
| `POST` | `/api/applications` | Company, role, status, optional date/notes/description | Create an application |
| `PUT` | `/api/applications/{id}` | Same application fields | Update an owned application |
| `DELETE` | `/api/applications/{id}` | — | Delete an owned application |
| `POST` | `/api/career/resume-analysis` | Multipart `file` PDF and `role` | Parse and analyze a resume |
| `POST` | `/api/career/job-match` | `{resumeText,jobDescription}` | Compare resume evidence to a role |
| `POST` | `/api/career/interview-prep` | `{role,company?,jobDescription?}` | Generate six practice questions |
| `POST` | `/api/career/interview-evaluate` | `{role,question,answer,company?}` | Evaluate an answer and suggest a truthful revision |

Career inputs are bounded by the API, PDF uploads are limited to 5 MB, and Groq rate limits are returned as HTTP `429` without exposing provider response bodies. AI scores are estimates and not hiring predictions.

## Database schema and data preservation

`backend/src/main/resources/db/migration/V1__initial_schema.sql` defines the initial schema. In the `prod` profile, Spring SQL initialization executes this script on startup before Hibernate validation. Its `CREATE TABLE IF NOT EXISTS` statements create missing tables and leave existing tables and rows untouched. Hibernate then runs with `validate`; if an existing table does not match the entities, startup fails without trying to alter it. Docker Compose also mounts this same file for MySQL's first initialization; the application initializer is idempotent against those already-created tables.

This is an initial-schema initializer, not a versioned migration framework. For future schema changes, write and review a separate additive SQL migration, test it against a staging copy, and preserve `validate` in production. Never assign unowned application rows to a user automatically.

## Docker Compose demo

Docker Compose starts MySQL, Spring Boot, and Nginx with a persistent database volume. It exposes the web app at `http://localhost:8081` and proxies `/api` to the backend on the private Compose network.

```powershell
Copy-Item .env.example .env
# Edit .env locally. Generate JWT_SECRET rather than using the placeholder.
docker compose up --build
```

The Compose MySQL service initializes the schema only for a new empty volume. It does not delete or migrate an existing external database. Keep `.env` private. To stop the demo without deleting database data, use `docker compose down`; do not add `--volumes` unless you intentionally want to remove the Compose database volume.

## Deployment status

### Render Web Service

Create a Docker Web Service from this repository with branch `main`, repository root as the build context, and `backend/Dockerfile` as the Dockerfile path. Leave the Docker start command blank to use the Dockerfile entrypoint. Render supplies `PORT`; the API binds to `0.0.0.0` and uses that port. Set `SPRING_PROFILES_ACTIVE=prod` and the environment variables below in Render's environment settings. Set `CORS_ALLOWED_ORIGINS` to the exact deployed frontend origin, including `https://` and without a trailing slash.

| Render variable | Purpose |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | Set to `prod` to enable production database validation, TLS connection settings, and production error handling. |
| `DB_URL` | TiDB JDBC connection URL shown below; contains no credentials. |
| `DB_USERNAME`, `DB_PASSWORD` | TiDB credentials from the TiDB Cloud connection panel. |
| `JWT_SECRET` | Base64 HMAC signing key generated from at least 32 random bytes. |
| `GROQ_API_KEY` | Backend-only Groq key; optional if AI career tools are not needed. |
| `CORS_ALLOWED_ORIGINS` | Exact public frontend origin, for example `https://hireforge-ai.pages.dev`. |
| `PORT` | Set automatically by Render; no manual value is needed. |

The service region `Singapore` is closest to the supplied TiDB endpoint region. Enable TiDB's public endpoint and ensure its firewall permits the Render service's outbound IP ranges. Render shows these ranges in the service's **Connect → Outbound** panel; if you restrict TiDB's firewall, allow the applicable ranges rather than only a developer workstation address.

For TiDB Cloud Starter, set `DB_URL` to this JDBC URL (no username or password in the URL):

```text
jdbc:mysql://gateway01.ap-southeast-1.prod.aws.tidbcloud.com:4000/hireforge?sslMode=VERIFY_IDENTITY&tlsVersions=TLSv1.2,TLSv1.3&serverTimezone=UTC
```

Use the exact database username shown by TiDB Cloud in `DB_USERNAME` and its password in `DB_PASSWORD`. TiDB Starter's public endpoint requires TLS. `VERIFY_IDENTITY` requires encryption, validates the certificate chain using the Java trust store, and checks the certificate hostname. The Render Free instance has 512 MB RAM; the Docker entrypoint caps JVM heap and other major memory regions and limits request threads, but sustained PDF processing or concurrent AI requests may still need a larger instance.

Keep all credentials in Render's secret environment settings. `JWT_SECRET` must be a base64-encoded value generated from at least 32 random bytes. `GROQ_API_KEY` is only needed for AI career tools; without it, those endpoints remain unavailable while account and application features work.

## Tests and builds

From the repository root:

```powershell
cd frontend
npm ci
npm run lint
npm run build

cd ..\backend
.\mvnw.cmd clean package
```

Backend tests use H2 and mocked Groq HTTP responses; they do not require an API key. `clean package` includes test compilation and execution.

The commands above are instructions, not a report of successful runs. The backend Dockerfile builds with `-DskipTests`, so its image build does not run the test suite.
