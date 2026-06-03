pipeline {

    agent any

    tools {
        maven 'Maven'
        jdk   'JDK'
    }

    parameters {
        choice(name: 'BROWSER', choices: ['chrome', 'firefox'], description: 'Browser')
        booleanParam(name: 'HEADLESS', defaultValue: true, description: 'Run headless?')
        string(name: 'THREAD_COUNT', defaultValue: '3', description: 'Parallel threads')
    }

    environment {
        TEST_EMAIL         = credentials('notes-app-test-email')
        TEST_PASSWORD      = credentials('notes-app-test-password')
        ALLURE_RESULTS_DIR = 'target\\allure-results'
        SUREFIRE_DIR       = 'target\\surefire-reports'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        disableConcurrentBuilds()
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean compile test-compile -q'
            }
        }

        stage('Run Tests') {
            steps {
                bat """
                    mvn test ^
                        -Dbrowser=${params.BROWSER} ^
                        -Dheadless=${params.HEADLESS} ^
                        -DthreadCount=${params.THREAD_COUNT} ^
                        -Dtest.email=%TEST_EMAIL% ^
                        -Dtest.password=%TEST_PASSWORD% ^
                        -Dsurefire.suiteXmlFiles=src/test/resources/testng.xml ^
                        -Dallure.results.directory=%ALLURE_RESULTS_DIR% ^
                        -Dmaven.test.failure.ignore=true
                    exit 0
                """
            }

            post {
                always {
                    script {
                        def totalTests    = 0
                        def totalFailures = 0
                        def totalErrors   = 0
                        def totalSkipped  = 0

                        try {
                            findFiles(glob: "${env.SUREFIRE_DIR}/**/*.xml").each { f ->
                                try {
                                    def xml      = readFile(f.path)
                                    def tests    = (xml =~ /tests="(\d+)"/)
                                    def failures = (xml =~ /failures="(\d+)"/)
                                    def errors   = (xml =~ /errors="(\d+)"/)
                                    def skipped  = (xml =~ /skipped="(\d+)"/)
                                    if (tests.find())    totalTests    += tests[0][1].toInteger()
                                    if (failures.find()) totalFailures += failures[0][1].toInteger()
                                    if (errors.find())   totalErrors   += errors[0][1].toInteger()
                                    if (skipped.find())  totalSkipped  += skipped[0][1].toInteger()
                                } catch (ex) { echo "Could not parse ${f.path}" }
                            }
                        } catch (ex) { echo "Could not read surefire directory" }

                        def passed = totalTests - totalFailures - totalErrors - totalSkipped

                        echo "===== TEST SUMMARY ====="
                        echo "Total   : ${totalTests}"
                        echo "Passed  : ${passed}"
                        echo "Failed  : ${totalFailures}"
                        echo "Errors  : ${totalErrors}"
                        echo "Skipped : ${totalSkipped}"
                        echo "========================"

                        if (totalFailures > 0 || totalErrors > 0) {
                            currentBuild.result = 'UNSTABLE'
                        } else if (totalTests == 0) {
                            currentBuild.result = 'UNSTABLE'
                        }

                        currentBuild.description = "Passed:${passed} Failed:${totalFailures} Total:${totalTests}"
                    }

                    junit testResults: "${env.SUREFIRE_DIR}/**/*.xml", allowEmptyResults: true
                }
            }
        }

        stage('Collect Artefacts') {
            steps {
                catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                    archiveArtifacts artifacts: 'target\\screenshots\\**\\*.png', allowEmptyArchive: true
                }
                catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                    archiveArtifacts artifacts: 'target\\logs\\**', allowEmptyArchive: true
                }
            }
        }

        stage('Publish Allure Report') {
            steps {
                catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                    allure([
                        results          : [[path: "${env.ALLURE_RESULTS_DIR}"]],
                        report           : 'allure-report',
                        reportBuildPolicy: 'ALWAYS'
                    ])
                }
            }
        }

        stage('Publish HTML Report') {
            steps {
                catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                    publishHTML(target: [
                        allowMissing         : true,
                        alwaysLinkToLastBuild: true,
                        keepAll              : true,
                        reportDir            : "${env.SUREFIRE_DIR}",
                        reportFiles          : 'index.html',
                        reportName           : 'TestNG Report'
                    ])
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success  { echo "BUILD STABLE — All tests passed." }
        unstable { echo "BUILD UNSTABLE — Some tests failed. Check Allure report." }
        failure  { echo "BUILD FAILED — Pipeline error. Check console log." }
    }
}
