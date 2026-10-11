# Hirely — Job Portal

Hirely is a public-ready job portal with a React/Vite client and Spring Boot REST API. It supports job seeker and recruiter accounts, company/job management, job discovery, saved jobs, applications, recruiter applicant review, profiles, and private resume uploads. The root `Dockerfile` builds and serves the frontend and API together for Render.

## Prerequisites

- Java 21 or newer (the project targets Java 21)
- Node.js 20+
- MongoDB 6+; Docker Compose is optional
- Maven 3.9+

## Start MongoDB

With Docker: `docker compose up -d mongodb`.

Without Docker, start your local MongoDB server. The application creates the `hirely` database and its indexes as it starts. You can also point `MONGODB_URI` to an authenticated MongoDB server or Atlas deployment.

## Configure and run

Set `MONGODB_URI` (default `mongodb://localhost:27017/hirely`), `MONGODB_DATABASE` (default `hirely`), `APP_CORS_ORIGIN` (default `http://localhost:5173`), and `APP_STORAGE_DIR` (default `./private-uploads`). `backend/.env.example` and `frontend/.env.example` show the settings. The backend loads a git-ignored `backend/.env` when launched from the `backend` directory. In production, use MongoDB authentication, set `APP_SESSION_COOKIE_SECURE=true`, and serve over HTTPS. Never commit secrets.

```powershell
cd backend
mvn spring-boot:run
```

To build a self-contained executable JAR instead, run `mvn -DskipTests package` from `backend`, then start it with `java -jar .\target\job-portal-1.0.0-exec.jar`. The executable artifact uses a separate filename so it can be rebuilt while the plain JAR is in use.

```powershell
cd frontend
npm install
npm run dev
```

The frontend runs at http://localhost:5173; the API runs at http://localhost:8080. `GET /api/health` reports API health.

## Deploy publicly on Render

The root `render.yaml` defines one Docker web service that serves the React site and Spring API from the same HTTPS origin. This avoids a separate frontend/API CORS setup. The service also attaches persistent storage at `/var/data` for uploaded resumes; Render persistent disks require a paid web service.

1. Push this repository to GitHub, then create a Blueprint in Render and connect the repository. Render reads `render.yaml` and builds the root `Dockerfile`.
2. Enter `MONGODB_URI`, `APP_ADMIN_EMAIL`, and `APP_ADMIN_PASSWORD` when Render prompts for secrets. Use a new Atlas database password, and grant the database user access only to the `hirely` database.
3. In Atlas Network Access, allow the outbound IP ranges shown for the Render service (Dashboard → service → Connect → Outbound). Keep the database private to those ranges where possible.
4. Wait for the deployment health check at `/api/health` to pass, then open the service's `onrender.com` URL.

The Blueprint selects a small paid instance because the resume uploads need persistent storage. Change the plan or disk size in `render.yaml` if your traffic or storage needs grow. Render's filesystem is otherwise ephemeral, so the disk is needed to preserve uploaded resumes across deploys and restarts.

`MONGODB_URI`, `APP_ADMIN_EMAIL`, and `APP_ADMIN_PASSWORD` are deliberately not stored in this repository. The admin account is created or updated at each startup while both admin environment variables are configured.

## Features and routes

- Public landing and searchable job listings with filters and detail pages; the API supports page and size parameters.
- Registration and login with server-managed sessions, BCrypt, role allowlisting, CSRF protection, and role-gated routes.
- Registration creates a one-time recovery code for self-service password reset; accounts can be deactivated from account settings.
- Seeker profile editing, education and experience history, profile completeness, resume upload/download/deletion, saved-role management, application tracking, and status notifications.
- Recruiter company profile and job management, including job editing and deletion, applicant lists, status updates, and authorized resume access.
- MongoDB-backed login throttling plus admin account activation controls and job moderation.
- MongoDB collections with unique indexes for emails, applications, saved jobs, companies, and resumes.

To provision the first administrator, set both `APP_ADMIN_EMAIL` and a strong `APP_ADMIN_PASSWORD` in the backend environment. The account is created or updated at startup and receives the `ADMIN` role; role selection during public registration cannot grant admin access.

## Tests and build

```powershell
cd backend
mvn test
cd ../frontend
npm test -- --run
npm run build
```

The backend password hashing unit test and four frontend tests pass. The frontend production build succeeds. Maven compilation and backend tests pass. MongoDB integration tests still require a reachable MongoDB server; Docker Compose is optional.

## Current scope limits

Password recovery uses the one-time recovery code shown after registration; save it because it is not emailed or shown again. A successful reset returns a replacement code. No public demo accounts or jobs are seeded. The Atlas connection was smoke-tested locally; configure the Render IP allowlist and environment secrets before deploying.

## Security and deployment notes

Resume files are stored outside the web root under a random identifier. Every resume read checks ownership or the recruiter’s relationship to an application. Uploads are size/extension/MIME checked; use malware scanning and private persistent object storage for production. Session cookies are HttpOnly and SameSite=Lax, with Secure configurable; CSRF tokens are required for unsafe requests. Login throttling is stored in MongoDB for cross-instance enforcement. Configure HTTPS, secure secrets, database backups, session expiry, monitoring, and persistent private file storage before production deployment.

## Project layout

- `backend/`: Spring Boot API, MongoDB documents, security, storage, tests
- `frontend/`: React/Vite application and API client
- `docker-compose.yml`: optional local MongoDB service

