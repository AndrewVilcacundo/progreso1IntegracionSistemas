pipeline {
    agent any

    tools {
        maven 'Maven-3.9.x' // Nombre del Maven tool configurado en Global Tool Configuration
        jdk 'JDK-17'        // Nombre del JDK tool configurado en Jenkins
    }

    // ===============================================================
    // ESTRATEGIAS DE OPTIMIZACIÓN GLOBALES
    // ===============================================================
    options {
        // Control de tiempo y logs
        timeout(time: 15, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        ansiColor('xterm')
        // Optimización: Deshabilitar ejecuciones concurrentes en la misma rama
        disableConcurrentBuilds()
    }

    environment {
        APP_VERSION = '1.0-SNAPSHOT'
        // Optimización Cache: Reutilizar repositorio Maven local del agente
        MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'
    }

    stages {
        // ===============================================================
        // ETAPA: VERIFICACIÓN Y CACHE
        // ===============================================================
        stage('Checkout & Cache Prep') {
            steps {
                echo 'Iniciando pipeline de Entrega Continua...'
                checkout scm
            }
        }

        // ===============================================================
        // ESTRATEGIA DE OPTIMIZACIÓN: EJECUCIÓN PARALELA
        // ===============================================================
        stage('Parallel Build & Quality Audit') {
            parallel {
                // Rama paralela 1: Compilación y Pruebas Unitarias
                stage('Build & Unit Tests') {
                    steps {
                        echo 'Compilando código y ejecutando pruebas unitarias...'
                        sh 'mvn test -Dmaven.repo.local=.m2/repository'
                        sh 'mvn package -DskipTests -Dmaven.repo.local=.m2/repository'
                    }
                    post {
                        always {
                            junit '**/target/surefire-reports/*.xml'
                            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                        }
                    }
                }

                // Rama paralela 2: Análisis de Calidad y Linter
                stage('Code Quality & Linting') {
                    steps {
                        echo 'Ejecutando Checkstyle Linter y SpotBugs Static Analysis...'
                        sh 'mvn checkstyle:checkstyle spotbugs:check -Dmaven.repo.local=.m2/repository'
                    }
                    post {
                        always {
                            archiveArtifacts artifacts: 'target/checkstyle-result.xml, target/spotbugsXml.xml', allowEmptyArchive: true
                        }
                    }
                }
            }
        }

        // ===============================================================
        // ESTRATEGIA DE OPTIMIZACIÓN: EJECUCIÓN SELECTIVA POR RAMA
        // ===============================================================
        stage('Continuous Delivery (CD)') {
            when {
                // Ejecución Selectiva: Solo entrega en rama main
                branch 'main'
            }
            steps {
                echo 'Desplegando paquete verificado en entorno de Entrega Continua (Staging/Production)...'
                sh 'echo "Artifact verificado: target/camel-csv-processor-${APP_VERSION}.jar"'
                sh 'echo "Entrega Continua completada con exito para la rama main."'
            }
        }
    }

    // ===============================================================
    // POST ACTIONS: ALWAYS, SUCCESS, FAIL + NOTIFICACIONES
    // ===============================================================
    post {
        always {
            echo "📊 [POST: ALWAYS] Limpieza y resumen de la ejecución del pipeline para el build ${currentBuild.fullDisplayName}"
            cleanWs deleteDirs: true, notFailBuild: true, patterns: [[pattern: 'data/output/**', type: 'INCLUDE']]
        }

        success {
            echo "🚀 [POST: SUCCESS] El pipeline se ejecutó satisfactoriamente. Todas las etapas pasaron."
            // Notificación a GitHub Status o Webhook
            // slackSend channel: '#ci-cd-alerts', color: 'good', message: "Pipeline exitoso: ${env.JOB_NAME} [${env.BUILD_NUMBER}]"
        }

        failure {
            echo "❌ [POST: FAIL] Error detectado en una de las etapas críticas del pipeline."
            // Notificación de falla
            // slackSend channel: '#ci-cd-alerts', color: 'danger', message: "Falla en Pipeline: ${env.JOB_NAME} [${env.BUILD_NUMBER}]"
        }
    }
}
