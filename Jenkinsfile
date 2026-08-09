pipeline {
    agent any
    tools {
        maven 'maven3'
        jdk 'jdk21'
    }
    environment {
        IMAGE_NAME = "student-api"
        CONTAINER_NAME = "student-api-container"
    }
    stages {
        stage('Checkout') {
            steps {
                // 1. CHANGE THIS to your actual repository URL
                git branch: 'main', url: 'https://github.com/Razaara/student-api.git'
            }
        }
        stage('Build with Maven') {
            steps {
                // Compile and package the Spring Boot app
                bat 'mvn clean package -DskipTests'
            }
        }
        stage('Build Docker Image') {
            steps {
                // Build the image from the Dockerfile
                bat 'docker build -t %IMAGE_NAME% .'
            }
        }
        stage('Stop Old Container') {
            steps {
                // Ignore failure if no container is currently running
                bat 'docker stop %CONTAINER_NAME% || exit 0'
            }
        }
        stage('Remove Old Container') {
            steps {
                bat 'docker rm %CONTAINER_NAME% || exit 0'
            }
        }
        stage('Run New Container') {
            steps {
                // 2. Port changed from 8080:8080 to 8500:8500 to match the app
                bat 'docker run -d -p 8500:8500 --name %CONTAINER_NAME% %IMAGE_NAME%'
            }
        }
    }
    post {
        success {
            echo 'Deployment successful.'
        }
        failure {
            echo 'Pipeline failed — check console output.'
        }
    }
}

pipeline {
    agent any
    tools {
        maven 'maven3'
        jdk 'jdk21'
    }
    environment {
        SONARQUBE_SERVER = 'sonar'
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
                withSonarQubeEnv("${env.SONARQUBE_SERVER}") {
                    bat 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar'
                }
            }
        }
        stage('Deploy with Docker Compose') {
            steps {
                // Automatically builds, stops old container, and starts the new one
                bat 'docker compose up -d --build'
            }
        }
    }
    post {
        success {
            echo 'Deployment successful.'
        }
        failure {
            echo 'Pipeline failed — check console output.'
        }
    }
}
