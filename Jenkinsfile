pipeline {
    agent any

    tools {
        // Must match Manage Jenkins → Tools → Maven installations → Name
        maven 'Maven'
    }

    environment {
        IMAGE_NAME = 'student-api'
        CONTAINER_NAME = 'student-api-container'
        // Host MySQL from docker-compose service student-mysql on port 3306
        DB_URL = 'jdbc:mysql://host.docker.internal:3306/students?createDatabaseIfNotExist=true'
        DB_USER = 'root'
        DB_PASS = 'root'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/Razaara/student-api.git'
            }
        }

        stage('Build with Maven') {
            steps {
                bat 'mvn clean package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                bat 'docker build -t %IMAGE_NAME% .'
            }
        }

        stage('Stop Old Container') {
            steps {
                // Windows-safe: ignore error if container does not exist
                bat 'docker stop %CONTAINER_NAME% 2>NUL || exit /b 0'
            }
        }

        stage('Remove Old Container') {
            steps {
                bat 'docker rm %CONTAINER_NAME% 2>NUL || exit /b 0'
            }
        }

        stage('Run New Container') {
            steps {
                // Pass DB settings so the container can reach MySQL on the host/Docker Desktop
                bat '''
                    docker run -d ^
                      -p 8500:8500 ^
                      --name %CONTAINER_NAME% ^
                      -e SPRING_DATASOURCE_URL=%DB_URL% ^
                      -e SPRING_DATASOURCE_USERNAME=%DB_USER% ^
                      -e SPRING_DATASOURCE_PASSWORD=%DB_PASS% ^
                      %IMAGE_NAME%
                '''
            }
        }

        stage('Verify') {
            steps {
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
                      docker logs %CONTAINER_NAME%
                      exit /b 1
                    )
                    curl.exe -s http://localhost:8500/api/students
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
            echo 'Pipeline failed — check console output.'
            bat 'docker logs %CONTAINER_NAME% 2>NUL || exit /b 0'
        }
    }
}
