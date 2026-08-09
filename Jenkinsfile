pipeline {
    agent any

    tools {
        maven 'maven3'
        jdk 'jdk21'
    }

    environment {
        IMAGE_NAME = 'student-api'
        // Matches docker-compose.yml service / container
        COMPOSE_PROJECT_NAME = 'soc'
    }

    stages {
        stage('Checkout') {
            steps {
                // CHANGE only if your fork/URL differs
                git branch: 'main', url: 'https://github.com/Razaara/student-api.git'
            }
        }

        stage('Build with Maven') {
            steps {
                // Compile/package on the Jenkins agent (exam-visible Maven stage)
                bat 'mvn clean package -DskipTests'
            }
        }

        stage('Deploy with Docker Compose') {
            steps {
                // Full stack: MySQL (healthy) + Spring Boot on port 8500
                // Uses service hostname "mysql" inside the compose network
                bat 'docker compose down'
                bat 'docker compose up -d --build'
            }
        }

        stage('Verify Deployment') {
            steps {
                bat 'docker compose ps'
                bat 'docker compose logs --tail 40 springboot'
                // Retry until Spring Boot answers on :8500 (up to ~90s)
                bat '''
                    setlocal EnableDelayedExpansion
                    set OK=0
                    for /L %%i in (1,1,18) do (
                      curl.exe -f -s -o NUL http://localhost:8500/api/students
                      if !ERRORLEVEL! EQU 0 (
                        echo API is UP
                        set OK=1
                        goto :done
                      )
                      echo Waiting for API... attempt %%i
                      ping -n 6 127.0.0.1 >NUL
                    )
                    :done
                    if !OK! NEQ 1 (
                      echo API did not become ready
                      docker compose logs --tail 120 springboot
                      exit /b 1
                    )
                    curl.exe -s -w "HTTP %%{http_code}" http://localhost:8500/api/students
                    echo.
                '''
            }
        }
    }

    post {
        success {
            echo 'Deployment successful. API: http://localhost:8500/api/students'
        }
        failure {
            echo 'Pipeline failed — check console output / Docker logs.'
            bat 'docker compose ps || exit /b 0'
            bat 'docker compose logs --tail 120 springboot || exit /b 0'
            bat 'docker compose logs --tail 80 mysql || exit /b 0'
        }
    }
}
