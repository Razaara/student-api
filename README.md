# Student CRUD REST API — SOC Practical Exam

Spring Boot 3 Student CRUD API with MySQL, Docker, Docker Compose, and Jenkins.

**GitHub:** https://github.com/Razaara/student-api  
**Docker Hub:** https://hub.docker.com/r/razzara/student-api  
**Local path:** `Desktop/SOC`

## Stack

- Java 21 · Spring Boot 3.4.5 · Spring Web · Spring Data JPA · MySQL 8 · Lombok
- Port: `8500`
- Base URL: `http://localhost:8500/api/students`

## Project structure

```
src/main/java/com/example/studentapi
├── StudentApiApplication.java
├── entity/Student.java
├── repository/StudentRepository.java
├── service/StudentService.java
├── service/StudentServiceImpl.java
└── controller/StudentController.java
```

## Quick start (local)

```bash
# 1) Start MySQL (Docker)
docker compose up -d mysql

# 2) Run the API
mvn spring-boot:run

# 3) Test
# POST/GET/PUT/DELETE http://localhost:8500/api/students
```

Default DB credentials (Docker MySQL):

- URL: `jdbc:mysql://localhost:3306/students?createDatabaseIfNotExist=true`
- Username: `root`
- Password: `root`

## Docker Compose (full stack)

```bash
docker compose up -d --build
```

This starts MySQL + Spring Boot. App: `http://localhost:8500/api/students`

## Docker Hub

```bash
docker login
docker tag soc-springboot:latest razzara/student-api:latest
docker push razzara/student-api:latest
```

## Endpoints

| Method | Path | Success |
|--------|------|---------|
| POST | `/api/students` | 201 |
| GET | `/api/students` | 200 |
| GET | `/api/students/{id}` | 200 |
| PUT | `/api/students/{id}` | 200 |
| DELETE | `/api/students/{id}` | 204 |

## Jenkins

Job name in Jenkins: **`student-api`**  
URL: http://localhost:8080/job/student-api/

Declarative `Jenkinsfile` stages:

1. Checkout — `https://github.com/Razaara/student-api.git`
2. Build with Maven — `mvn clean package -DskipTests`
3. **SonarQube Analysis** — `withSonarQubeEnv('sonar')` + Maven sonar plugin
4. **Quality Gate** — `waitForQualityGate abortPipeline: false`
5. Build Docker Image — `docker build -t student-api .`
6. Stop / Remove old container (`student-api-container`)
7. Run new container on `8500:8500` with MySQL env (`host.docker.internal`)
8. Verify — retries `GET /api/students` until healthy

**Jenkins + SonarQube setup**
- Tool name **Maven** (Manage Jenkins → Tools)
- SonarQube server name **`sonar`** (Manage Jenkins → System) → URL `http://localhost:9000` + token
- Docker available to the Jenkins agent
- MySQL running on host port `3306` (e.g. `docker compose up -d mysql`)
- Pipeline job → **Pipeline script from SCM** → Git → this repo → script path `Jenkinsfile`
- See `sonar-project.properties` and exam guidance PDF for full Sonar setup
