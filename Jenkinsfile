pipeline {
    agent any

    environment {
        APP_VERSION = 'v1'
        DOCKER_IMAGE = "goal-tracker:${APP_VERSION}"
        DOCKERHUB_CREDENTIALS_ID = 'dockerhub-credentials'
    }

    stages {
        stage('Checkout') {
            steps {
                echo "Stage 1: Checking out code for Version ${env.APP_VERSION}..."
                checkout scm
            }
        }

        stage('Test') {
            steps {
                echo "Stage 2: Running automated tests with pytest..."
                // Runs on Windows with bat or Linux with sh
                bat '''
                    python -m pip install --upgrade pip
                    pip install -r requirements.txt
                    python -m pytest -v tests/
                '''
            }
        }

        stage('Docker Build') {
            steps {
                echo "Stage 3: Building Docker container image for ${env.APP_VERSION}..."
                bat "docker build -t ${env.DOCKER_IMAGE} ."
            }
        }

        stage('Docker Hub Push') {
            steps {
                echo "Stage 4: Pushing Docker image to Docker Hub repository..."
                // If Docker Hub credentials are configured in Jenkins:
                // withCredentials([usernamePassword(credentialsId: env.DOCKERHUB_CREDENTIALS_ID, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                //     bat 'docker login -u %DOCKER_USER% -p %DOCKER_PASS%'
                //     bat "docker tag ${env.DOCKER_IMAGE} %DOCKER_USER%/goal-tracker:${env.APP_VERSION}"
                //     bat "docker push %DOCKER_USER%/goal-tracker:${env.APP_VERSION}"
                // }
                echo "Docker image ${env.DOCKER_IMAGE} tagged and ready for push."
            }
        }

        stage('Kubernetes Deploy') {
            steps {
                echo "Stage 5: Deploying ${env.APP_VERSION} to Kubernetes Cluster..."
                bat "kubectl apply -f k8s/${env.APP_VERSION}/"
                bat "kubectl rollout status deployment/goal-tracker-${env.APP_VERSION}"
            }
        }
    }

    post {
        success {
            echo "Pipeline completed successfully for Version ${env.APP_VERSION}!"
        }
        failure {
            echo "Pipeline failed. Check test reports and build console logs."
        }
    }
}
