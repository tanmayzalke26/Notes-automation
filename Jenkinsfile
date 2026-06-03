// ═══════════════════════════════════════════════════════════════
//  Notes App Capstone — Jenkins Declarative Pipeline
//  Stages: Checkout → Build → Test → Reports → Archive
// ═══════════════════════════════════════════════════════════════

pipeline {

    agent any

    // ── Tool aliases (configured in Jenkins → Manage Jenkins → Tools) ──
    tools {
        maven 'Maven-3.9'
        jdk   'JDK-11'
    }

    // ── Parameters for flexible CI runs ───────────────────────────────
    parameters {
        choice(name: 'BROWSER',   choices: ['chrome', 'firefox'], description: 'Browser to run tests on')
        booleanParam(name: 'HEADLESS', defaultValue: true,        description: 'Run browser in headless mode?')
        string(name: 'THREAD_COUNT', defaultValue: '3',           description: 'Parallel thread count')
    }

    // ── Environment variables (credentials stored in Jenkins Credential Store) ──
    environment {
        TEST_EMAIL    = credentials('notes-app-test-email')
        TEST_PASSWORD = credentials('notes-app-test-password')
        ALLURE_RESULTS_DIR = 'target/allure-results'
    }

    stages {

        // ─────────────────────────────────────────────
        stage('Checkout') {
        // ─────────────────────────────────────────────
            steps {
                echo "Checking out source code..."
                checkout scm
            }
        }

        // ─────────────────────────────────────────────
        stage('Build') {
        // ─────────────────────────────────────────────
            steps {
                echo "Compiling project..."
                sh 'mvn clean compile test-compile -q'
            }
        }

        // ─────────────────────────────────────────────
        stage('Run Tests') {
        // ─────────────────────────────────────────────
            steps {
                echo "Running TestNG suite with ${params.THREAD_COUNT} parallel threads..."
                sh """
                    mvn test \
                        -Dbrowser=${params.BROWSER} \
                        -Dheadless=${params.HEADLESS} \
                        -DthreadCount=${params.THREAD_COUNT} \
                        -Dtest.email=${env.TEST_EMAIL} \
                        -Dtest.password=${env.TEST_PASSWORD} \
                        -Dsurefire.suiteXmlFiles=src/test/resources/testng.xml \
                        -Dallure.results.directory=${env.ALLURE_RESULTS_DIR} \
                        || true
                """
                // '|| true' prevents pipeline failure at this stage; let reports decide
            }
        }

        // ─────────────────────────────────────────────
        stage('Collect Artefacts') {
        // ─────────────────────────────────────────────
            steps {
                echo "Collecting screenshots, logs and test artefacts..."
                // Archive screenshots captured on failure
                archiveArtifacts artifacts: 'target/screenshots/**/*.png',
                                 allowEmptyArchive: true

                // Archive full log file
                archiveArtifacts artifacts: 'target/logs/**',
                                 allowEmptyArchive: true

                // Archive TestNG HTML report
                archiveArtifacts artifacts: 'target/surefire-reports/**',
                                 allowEmptyArchive: true
            }
        }

        // ─────────────────────────────────────────────
        stage('Publish Allure Report') {
        // ─────────────────────────────────────────────
            steps {
                echo "Generating and publishing Allure report..."
                // Requires the Allure Jenkins Plugin
                allure([
                    includeProperties: true,
                    jdk              : '',
                    results          : [[path: "${env.ALLURE_RESULTS_DIR}"]],
                    report           : 'allure-report',
                    reportBuildPolicy: 'ALWAYS'
                ])
            }
        }

        // ─────────────────────────────────────────────
        stage('Publish HTML Report') {
        // ─────────────────────────────────────────────
            steps {
                echo "Publishing TestNG Surefire HTML report..."
                // Requires the HTML Publisher Plugin
                publishHTML(target: [
                    allowMissing         : true,
                    alwaysLinkToLastBuild: true,
                    keepAll              : true,
                    reportDir            : 'target/surefire-reports',
                    reportFiles          : 'index.html',
                    reportName           : 'TestNG HTML Report'
                ])
            }
        }
    }

    // ── Post actions ──────────────────────────────────────────────────
    post {

        always {
            echo "Pipeline finished. Archiving JUnit XML results for trend charts..."
            junit testResults: 'target/surefire-reports/*.xml',
                  allowEmptyResults: true

            // Clean up workspace after run to save disk space
            cleanWs(cleanWhenAborted: true,
                    cleanWhenFailure: true,
                    cleanWhenSuccess: true)
        }

        success {
            echo "✅ All tests passed. Build STABLE."
        }

        failure {
            echo "❌ One or more tests failed. Check Allure and screenshots above."
            // Optional: mail(to: 'qa-team@company.com', subject: "Build FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}", body: "Check: ${env.BUILD_URL}")
        }

        unstable {
            echo "⚠️ Some tests were skipped or flaky. Build UNSTABLE."
        }
    }
}
