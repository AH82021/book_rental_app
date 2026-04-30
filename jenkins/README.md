# Jenkins CI/CD Setup Guide — book-nest

> A step-by-step guide to get the full CI/CD pipeline running for the book-nest microservices.

---

## 1. Prerequisites

- Jenkins 2.440+
- Docker & Docker Compose
- Java 17 & Maven 3.9+
- Git

## 2. Infrastructure Setup (Local/Base)

Before running the services, you must have the required infrastructure (PostgreSQL, Redis, MongoDB).
We provide a base `docker-compose.yml` in the root directory.

```bash
# Start infrastructure and services locally
docker compose up -d
```
*Note: This will also automatically run `init-dbs.sql` to create `authdb`, `userdb`, and `bookdb`.*

## 3. Jenkins Plugins Required

Install the following via Manage Jenkins → Plugins:
- Pipeline
- Docker Pipeline
- Git
- Credentials Binding
- Timestamper
- Workspace Cleanup
- AnsiColor
- JUnit
- OWASP Dependency-Check
- HTML Publisher
- Email Extension
- SonarQube Scanner
- Slack Notification
- SSH Agent

## 4. Credentials Required

Add these in Manage Jenkins → Credentials:

| Credential ID | Kind | Description |
|---|---|---|
| `docker-registry-credentials` | Username/Password | Docker Hub/Registry login |
| `sonarqube-token` | Secret text | SonarQube API token |
| `slack-webhook-url` | Secret text | Slack incoming webhook |
| `staging-server-host` | Secret text | Staging server IP/hostname |
| `staging-server-ssh` | SSH Username with private key | SSH key for staging server |
| `production-server-host` | Secret text | Production server IP/hostname |
| `production-server-ssh` | SSH Username with private key | SSH key for production server |
| `bookstore-db-password` | Secret text | Database password (`password` by default) |

## 5. Build Jenkins Agent

From the project root:
```bash
docker build -t bookstore/jenkins-agent:latest -f jenkins/agents/Dockerfile .
```

## 6. Adding New Services in the Future

When you add new services (e.g. `report-service`), you need to update a few places:
1. **`Jenkinsfile`**: Add it to the `SERVICES` environment variable list.
2. **`docker-compose.yml` (root)**: Add the new service definition.
3. **`jenkins/docker-compose.staging.yml` & `production.yml`**: Add the new service overrides.
4. **`jenkins/scripts/smoke-test.sh`**: Add the new service and its port to the `SERVICES` array.
5. **`jenkins/sonar-project.properties`**: Add the new module definition.

*Note: The `deploy.sh` and `rollback.sh` scripts are dynamic and will automatically pick up any new services defined in the docker-compose files!*

## 7. GitHub Actions

We also include a Pull Request validation workflow in `.github/workflows/pr-validation.yml`. This automatically runs compilation and unit/integration tests against ephemeral infrastructure whenever a PR is created against `main` or `develop`.
