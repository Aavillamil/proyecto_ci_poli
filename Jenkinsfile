pipeline {
    agent any

    tools {
        maven 'Maven-3.9.6'
        jdk   'JDK-17'
    }

    environment {
        APP_NAME    = 'ci-project'
        APP_VERSION = '1.0.0'
        IMAGE_NAME  = "ci-project:${APP_VERSION}"
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {

        stage('Checkout') {
            steps {
                cleanWs()
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests -B'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test -B'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh "docker build -t ${IMAGE_NAME} ."
            }
        }

        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                sh 'docker compose down --remove-orphans || true'
                sh 'docker compose up --build -d'
                sh 'docker compose ps'
            }
        }
    }

    post {
        failure {
            mail to: 'aanvillamilo@poligran.edu.co',
                 subject: "Build fallido: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "Revisar la salida del pipeline en: ${env.BUILD_URL}"
        }
        always {
            cleanWs()
        }
    }
}
