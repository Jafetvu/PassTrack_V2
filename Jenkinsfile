pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Quality & Security Validation') {
            when {
                anyOf {
                    branch 'test'
                    branch 'prod'
                }
            }
            steps {
                sh 'chmod +x scripts/check.sh PassTrack_backend/mvnw'
                sh './scripts/check.sh'
            }
        }

        stage('Build Docker Images') {
            when {
                anyOf {
                    branch 'test'
                    branch 'prod'
                }
            }
            steps {
                sh 'docker compose build'
            }
        }

        stage('Deploy Production') {
            when {
                branch 'prod'
            }
            steps {
                sh 'docker compose up -d --remove-orphans'
            }
        }
    }

    post {
        failure {
            echo 'Pipeline detenido por fallo en validaciones o despliegue.'
        }
        success {
            echo "Pipeline completado exitosamente en rama ${env.BRANCH_NAME}."
        }
    }
}
