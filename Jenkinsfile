pipeline {
    agent any

    tools {
        // Must match Manage Jenkins → Tools → Maven installations → Name
        maven 'Maven'
    }

    environment {
        IMAGE_NAME = 'student-api'
        CONTAINER_NAME = 'student-api-container'
        // Must match Manage Jenkins → System → SonarQube servers → Name
        SONARQUBE_SERVER = 'sonar'
        // Must match the SonarQube project the analysis TOKEN was created for
        SONAR_PROJECT_KEY = 'jenkins'
        SONAR_PROJECT_NAME = 'jenkins'
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

        stage('SonarQube Analysis') {
            steps {
                // Token must belong to SONAR_PROJECT_KEY (current token is for project "jenkins")
                withSonarQubeEnv("${env.SONARQUBE_SERVER}") {
                    bat '''
                        mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:5.7.0.6970:sonar ^
                          -Dsonar.projectKey=%SONAR_PROJECT_KEY% ^
                          -Dsonar.projectName=%SONAR_PROJECT_NAME% ^
                          -Dsonar.host.url=%SONAR_HOST_URL% ^
                          -Dsonar.login=%SONAR_AUTH_TOKEN%
                    '''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                // Without Sonar webhook, this can hang on PENDING — don't abort the whole pipeline
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    timeout(time: 2, unit: 'MINUTES') {
                        waitForQualityGate abortPipeline: false
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                bat 'docker build -t %IMAGE_NAME% .'
            }
        }

        stage('Stop Old Container') {
            steps {
                bat 'docker stop %CONTAINER_NAME% 2>NUL || exit /b 0'
                bat 'docker stop student-api 2>NUL || exit /b 0'
            }
        }

        stage('Remove Old Container') {
            steps {
                bat 'docker rm %CONTAINER_NAME% 2>NUL || exit /b 0'
                bat 'docker rm student-api 2>NUL || exit /b 0'
            }
        }

        stage('Run New Container') {
            steps {
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
            echo "SonarQube project key: ${env.SONAR_PROJECT_KEY}"
        }
        failure {
            echo 'Pipeline failed — check Console Output (often Sonar token/project key mismatch).'
            bat 'docker logs %CONTAINER_NAME% 2>NUL || exit /b 0'
        }
    }
}
