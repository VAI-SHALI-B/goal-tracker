pipeline {
    agent any

    environment {
        APP_VERSION = 'v1'
        DOCKER_IMAGE = "goal-tracker:${APP_VERSION}"
        DOCKERHUB_CREDENTIALS_ID = 'dockerhub-credentials'
    }

    tools {
        maven 'Maven3'
        jdk 'JDK21'
    }

    stages {
        stage('Checkout') {
            steps {
                echo "Stage 1: Checking out source code for Version ${env.APP_VERSION}..."
                checkout scm
            }
        }

        stage('Compile & Test') {
            steps {
                echo "Stage 2: Compiling Java 21 Spring Boot project and executing unit tests..."
                bat 'mvn clean test'
            }
        }

        stage('Package') {
            steps {
                echo "Stage 3: Packaging executable Spring Boot fat JAR..."
                bat 'mvn package -DskipTests'
            }
        }

        stage('Docker Build') {
            steps {
                echo "Stage 4: Building Docker container image for ${env.APP_VERSION}..."
                bat "docker build -t ${env.DOCKER_IMAGE} ."
            }
        }

        stage('Docker Hub Push') {
            steps {
                echo "Stage 5: Publishing container image to Docker Hub repository..."
                // When Docker Hub credentials are bound in Jenkins:
                // withCredentials([usernamePassword(credentialsId: env.DOCKERHUB_CREDENTIALS_ID, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                //     bat 'docker login -u %DOCKER_USER% -p %DOCKER_PASS%'
                //     bat "docker tag ${env.DOCKER_IMAGE} %DOCKER_USER%/goal-tracker:${env.APP_VERSION}"
                //     bat "docker push %DOCKER_USER%/goal-tracker:${env.APP_VERSION}"
                // }
                echo "Docker image ${env.DOCKER_IMAGE} built and ready for deployment."
            }
        }

        stage('Kubernetes Deploy') {
            steps {
                echo "Stage 6: Deploying ${env.APP_VERSION} to Kubernetes Cluster (NodePort 30081)..."
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
            echo "Pipeline execution failed. Review Maven test outputs and console logs."
        }
    }
}
