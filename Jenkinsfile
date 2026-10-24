
pipeline {
    agent any

    tools {
        maven 'Maven-3.9.x'
        jdk 'JDK-17'
    }

    options {
        timeout(time: 15, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        ansiColor('xterm')
        disableConcurrentBuilds()
    }

    environment {
        APP_VERSION = '1.0-SNAPSHOT'
        MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'
    }

    stages {
        stage('Checkout & Cache Prep') {
            steps {
                echo 'Iniciando pipeline de Entrega Continua...'
                checkout scm

                bat '''
                    if not exist .m2 mkdir .m2
                '''
            }
        }

        stage('Parallel Build & Quality Audit') {
            parallel {
                stage('Build & Unit Tests') {
                    steps {
                        echo 'Compilando código y ejecutando pruebas unitarias...'

                        bat 'mvn -B test -Dmaven.repo.local=.m2/repository'

                        bat 'mvn -B package -DskipTests -Dmaven.repo.local=.m2/repository'
                    }

                    post {
                        always {
                            junit testResults: '**/target/surefire-reports/*.xml',
                                  allowEmptyResults: true

                            archiveArtifacts artifacts: '**/target/*.jar',
                                             fingerprint: true,
                                             allowEmptyArchive: true
                        }
                    }
                }

                stage('Code Quality & Linting') {
                    steps {
                        echo 'Ejecutando Checkstyle y SpotBugs...'

                        bat 'mvn -B checkstyle:checkstyle spotbugs:check -Dmaven.repo.local=.m2/repository'
                    }

                    post {
                        always {
                            archiveArtifacts artifacts: '**/target/checkstyle-result.xml, **/target/spotbugsXml.xml',
                                             allowEmptyArchive: true
                        }
                    }
                }
            }
        }

        stage('Continuous Delivery (CD)') {
            when {
                branch 'main'
            }

            steps {
                echo 'Verificando el artefacto generado para la entrega...'

                bat '''
                    echo Rama principal: main
                    echo Version de la aplicacion: %APP_VERSION%
                    if exist target\\*.jar (
                        echo Se encontraron archivos JAR para la entrega.
                    ) else (
                        echo No se encontraron archivos JAR.
                        exit /b 1
                    )
                '''

                echo 'Verificacion de entrega continua completada.'
            }
        }
    }

    post {
        always {
            echo "Resumen del pipeline: ${currentBuild.fullDisplayName}"
            cleanWs deleteDirs: true, notFailBuild: true
        }

        success {
            echo 'El pipeline se ejecuto satisfactoriamente.'
        }

        failure {
            echo 'Error detectado en una de las etapas del pipeline.'
        }
    }
}
