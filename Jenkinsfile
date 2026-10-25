pipeline {
    agent any

    parameters {
        choice(name: 'APP_VERSION', choices: ['v3', 'v2', 'v1'], description: 'Select Goal Tracker version to build, test, publish, and deploy')
    }

    environment {
        VERSION = "${params.APP_VERSION ?: (env.BRANCH_NAME ?: 'v3')}"
        DOCKERHUB_USER = 'vaishalibalaji'
        DOCKER_IMAGE = "${DOCKERHUB_USER}/goal-tracker:${VERSION}"
        DOCKERHUB_CREDENTIALS_ID = 'dockerhub-credentials'
    }

    tools {
        maven 'Maven3'
        jdk 'JDK21'
    }

    stages {
        stage('Checkout') {
            steps {
                echo "Stage 1: Checking out source code for Version ${env.VERSION}..."
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
                echo "Stage 4: Building Docker image ${env.DOCKER_IMAGE}..."
                bat "docker build -t ${env.DOCKER_IMAGE} ."
            }
        }

        stage('Docker Hub Push') {
            steps {
                echo "Stage 5: Publishing container image to Docker Hub repository..."
                withCredentials([usernamePassword(credentialsId: env.DOCKERHUB_CREDENTIALS_ID, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    bat 'docker login -u %DOCKER_USER% -p %DOCKER_PASS%'
                    bat "docker push ${env.DOCKER_IMAGE}"
                }
            }
        }

        stage('Kubernetes Deploy') {
            steps {
                echo "Stage 6: Deploying app-${env.VERSION}-deployment to Kubernetes..."
                bat "kubectl apply -f k8s/${env.VERSION}/"
                bat "kubectl rollout status deployment/app-${env.VERSION}-deployment --timeout=60s"
            }
        }

        stage('Verify Health') {
            steps {
                echo "Stage 7: Verifying application health probe..."
                script {
                    def targetPort = (env.VERSION == 'v1') ? '30081' : ((env.VERSION == 'v2') ? '30082' : '30083')
                    echo "Probing http://localhost:${targetPort}/health..."
                    bat "curl.exe -s http://localhost:${targetPort}/health"
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline completed successfully for Version ${env.VERSION}!"
        }
        failure {
            echo "Pipeline execution failed. Review build logs and test results."
        }
    }
}
