pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh './gradlew shadowJar'

            }
        }
    }
    post {
           always {
               archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
               cleanWs()
           }
    }
}
