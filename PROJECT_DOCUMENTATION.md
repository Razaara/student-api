# Student API — Full Project Documentation

**Project:** Student CRUD REST API (SOC Practical Exam)  
**Local folder:** `C:\Users\rasar\OneDrive\Desktop\SOC`  
**GitHub user:** [Razaara](https://github.com/Razaara)  
**GitHub repo:** https://github.com/Razaara/student-api  
**Docker Hub user:** `razzara`  
**Docker image:** `razzara/student-api:latest`  
**Date built & verified:** 10 August 2026

---

## 1. What was built

A complete A→Z Student CRUD REST API matching the exam handbook:

| Layer | File |
|---|---|
| Main | `StudentApiApplication.java` |
| Entity | `entity/Student.java` |
| Repository | `repository/StudentRepository.java` |
| Service | `service/StudentService.java` + `StudentServiceImpl.java` |
| Controller | `controller/StudentController.java` |
| Config | `application.properties` (port **8500**) |
| Docker | `Dockerfile` (multi-stage, Java 21) |
| Compose | `docker-compose.yml` (MySQL 8 + Spring Boot) |
| CI | `Jenkinsfile` (Declarative pipeline) |

### Endpoints

| Method | Path | Success code | Tested |
|---|---|---|---|
| POST | `/api/students` | 201 Created | Yes |
| GET | `/api/students` | 200 OK | Yes |
| GET | `/api/students/{id}` | 200 OK | Yes |
| PUT | `/api/students/{id}` | 200 OK | Yes |
| DELETE | `/api/students/{id}` | 204 No Content | Yes |

Missing id currently returns **500** (unhandled `RuntimeException`) — same as the handbook sample code.

---

## 2. Step-by-step work log (everything done)

### Step A — Workspace setup

1. Used existing folder `Desktop\SOC` (already had `.git`).
2. Moved Cursor agent root into `SOC`.
3. Confirmed tools: Java 21, Maven 3.9.16, Docker 29.x, Git.

### Step B — Scaffold Spring Boot project

1. Tried Spring Initializr download → **failed** (`400 Bad Request`).
2. **Fix recorded:** start.spring.io currently only offers Spring Boot **4.x**. Handbook targets Boot **3.x**, so the project was scaffolded manually with:

```xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>3.4.5</version>
</parent>
```

3. Created packages:
   - `com.example.studentapi`
   - `...entity` / `...repository` / `...service` / `...controller`
4. Added dependencies: Web, Data JPA, MySQL connector (`mysql-connector-j`), Lombok, Validation.
5. Wrote all source files from the corrected handbook.

### Step C — Database

1. Local Windows service `MySQL97` was **Stopped**.
2. **Fix recorded:** started MySQL via Docker Compose instead of the local service:

```bash
docker compose up -d mysql
```

3. Set credentials for Docker MySQL:
   - username: `root`
   - password: `root`
4. **Fix recorded vs handbook empty password:** handbook showed `spring.datasource.password=` (empty). Empty password fails against Docker MySQL (`MYSQL_ROOT_PASSWORD=root`). Updated `application.properties` to `password=root` and documented it.

### Step D — Local Maven build & run

```bash
mvn clean package -DskipTests   # SUCCESS
mvn spring-boot:run             # Tomcat on 8500
```

### Step E — Local CRUD testing (verified)

| Test | Result |
|---|---|
| POST `AS2023000` John Doe | 201 + JSON body |
| POST `AS2023001` Jane Smith | 201 + JSON body |
| GET all | 200 with both students |
| GET by id `AS2023000` | 200 |
| PUT update John → John Updated | 200, id stayed `AS2023000` |
| DELETE `AS2023001` | 204 |
| GET missing id | 500 (expected for sample code) |

**Fix recorded (test tooling):** PowerShell `curl.exe -d "{\"id\":...}"` mangled JSON and produced HTTP 400. Retested successfully with `Invoke-RestMethod` and proper JSON strings.

### Step F — Docker image build

1. First `docker compose build` **failed** on:

```
mvn dependency:go-offline
Premature end of Content-Length delimited message body
```

2. **Fix recorded:** simplified `Dockerfile` — removed fragile `dependency:go-offline` step; copy sources and run a single `mvn -B clean package -DskipTests`.
3. Rebuild succeeded → image `soc-springboot:latest`.

### Step G — Docker Compose full stack testing

```bash
docker compose up -d --build
```

Containers:

| Name | Image | Ports | Status |
|---|---|---|---|
| `student-mysql` | `mysql:8.0` | `3306:3306` | healthy |
| `student-api` | `soc-springboot` | `8500:8500` | running |

Docker CRUD retested:

| Test | Result |
|---|---|
| POST `DCK001` | 201 |
| GET all | 200 (includes prior local data via MySQL volume) |
| PUT `DCK001` | 200 |
| DELETE `DCK001` | 204 |

### Step H — GitHub repository

1. Created public repo via GitHub API as user **Razaara**.
2. Repo URL: https://github.com/Razaara/student-api
3. Cleaned a stale gitlink entry named `studentapi` left from an earlier incomplete init (**fix recorded**).
4. Committed and pushed `main`:

```
Add complete Student CRUD Spring Boot project for SOC practical exam.
```

### Step I — Docker Hub push

```bash
docker tag soc-springboot:latest razzara/student-api:latest
docker push razzara/student-api:latest
```

Image: `docker.io/razzara/student-api:latest`

### Step J — Documentation

- `README.md` in the repo (quick start).
- This `PROJECT_DOCUMENTATION.md` (full A→Z log + fixes).
- PDF generated from this file for printing / exam reference.

---

## 3. How to run (exam-ready)

### Option 1 — Local app + Docker MySQL

```bash
cd C:\Users\rasar\OneDrive\Desktop\SOC
docker compose up -d mysql
mvn spring-boot:run
```

Open Postman: `http://localhost:8500/api/students`

### Option 2 — Full Docker Compose

```bash
cd C:\Users\rasar\OneDrive\Desktop\SOC
docker compose up -d --build
```

### Option 3 — Pull from Docker Hub

```bash
docker pull razzara/student-api:latest
# still need a MySQL instance; prefer Option 2 for exam
```

### Sample Postman body (POST)

```json
{
  "id": "AS2023000",
  "name": "John Doe",
  "email": "john@example.com",
  "age": 21
}
```

---

## 4. application.properties (final working)

```properties
spring.application.name=studentapi
spring.datasource.url=jdbc:mysql://localhost:3306/students?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=update
server.port=8500
```

Inside Compose, Spring overrides datasource via env vars to use host `mysql` (service name), not `localhost`.

---

## 5. Dockerfile (final working)

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8500
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 6. docker-compose.yml (final working)

- Service `mysql` with healthcheck
- Service `springboot` depends on healthy MySQL
- Port map `8500:8500`
- Volume `mysql_data` for persistence
- Datasource URL uses `jdbc:mysql://mysql:3306/students?...`

---

## 7. Jenkinsfile notes

Declarative pipeline stages:

1. Checkout (`https://github.com/Razaara/student-api.git`)
2. Maven package
3. Docker build
4. Stop/remove old container (`|| exit 0` for Windows)
5. Run new container on `8500:8500`

Requires Jenkins tools named `maven3` and `jdk21`.

---

## 8. Fixes summary (must remember for exam)

| # | Issue | Fix applied |
|---|---|---|
| 1 | Spring Initializr only offers Boot 4 | Manual Boot **3.4.5** project |
| 2 | Local MySQL service stopped | Use Docker MySQL (`docker compose up -d mysql`) |
| 3 | Empty DB password in handbook | Set password to `root` for Docker MySQL |
| 4 | Port confusion 8080 vs 8500 | Standardized on **8500** everywhere |
| 5 | Dockerfile `go-offline` network flake | Single `mvn package` step |
| 6 | Compose YAML incomplete in old handbook | Full MySQL + Spring services with healthcheck |
| 7 | PowerShell JSON escaping broke POST tests | Use `Invoke-RestMethod` / correct JSON strings |
| 8 | Stale gitlink `studentapi` in repo | Removed with `git rm --cached` before commit |
| 9 | Docker Hub username | Used **`razzara`** (not the GitHub username) |
| 10 | GitHub username | Used **`Razaara`** for the new repo |

---

## 9. Verification checklist

- [x] Maven package succeeds
- [x] App starts on port 8500
- [x] POST / GET / PUT / DELETE work locally
- [x] Docker image builds
- [x] Docker Compose stack healthy
- [x] CRUD works against Compose stack
- [x] GitHub repo created: https://github.com/Razaara/student-api
- [x] Code pushed to `main`
- [x] Image tagged/pushed: `razzara/student-api:latest`
- [x] Documentation + PDF produced

---

## 10. Useful commands cheat sheet

```bash
# Git
git add .
git commit -m "message"
git push -u origin main

# Maven
mvn spring-boot:run
mvn clean package -DskipTests

# Docker
docker compose up -d --build
docker compose ps
docker compose logs -f springboot
docker compose down

docker tag soc-springboot:latest razzara/student-api:latest
docker push razzara/student-api:latest
docker pull razzara/student-api:latest
```

---

## 11. Links

- GitHub: https://github.com/Razaara/student-api
- Docker Hub: https://hub.docker.com/r/razzara/student-api
- Local project: `C:\Users\rasar\OneDrive\Desktop\SOC`
