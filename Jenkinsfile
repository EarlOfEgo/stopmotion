pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh './gradlew assembleDebug --no-daemon'
            }
        }
        stage('Test') {
            steps {
                sh './gradlew test --no-daemon'
            }
        }
    }
}