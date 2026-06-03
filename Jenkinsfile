pipeline {
    agent any
    tools {
        maven 'Maven'
        jdk   'JDK'
    }
    parameters {
        choice(
            name: 'BROWSER',
            choices: ['chrome', 'firefox'],
            description: 'Browser to execute UI tests on'
        )
        booleanParam(
            name: 'HEADLESS',
            defaultValue: true,
            description: 'Run browser headless? (true for CI, false for local debug)'
        )
        string(
            name: 'THREAD_COUNT',
            defaultValue: '3',
            description: 'Number of parallel test threads'
        )
        choice(
            name: 'TEST_SUITE',
            choices: ['src/test/resources/testng.xml',
                      'src/test/resources/testng-smoke.xml'],
            description: 'TestNG suite file to execute'
        )
    }
    environment {
        TEST_EMAIL         = credentials('notes-app-test-email')
        TEST_PASSWORD      = credentials('notes-app-test-password')
        ALLURE_RESULTS_DIR = 'target/allure-results'
        SUREFIRE_DIR       = 'target/surefire-reports'
        SCREENSHOT_DIR     = 'target/screenshots'
        LOG_DIR            = 'target/logs'
    }
    options {
        buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '10'))
        timestamps()
        disableConcurrentBuilds()
        ansiColor('xterm')
    }
    stages {
        stage('Checkout') {
            steps {
                echo "\033[34m📥 Checking out source code from SCM...\033[0m"
                checkout scm
                echo "\033[32m✔ Checkout complete. Branch: ${env.GIT_BRANCH ?: 'N/A'}\033[0m"
            }
        }
        stage('Build & Compile') {
            steps {
                echo "\033[34m🔨 Compiling main and test sources...\033[0m"
                sh 'mvn clean compile test-compile -q'
                echo "\033[32m✔ Compilation successful.\033[0m"
            }
        }
        stage('Run Tests') {
            steps {
                echo "\033[34m🧪 Executing TestNG suite: ${params.TEST_SUITE}\033[0m"
                echo "\033[34m   Browser: ${params.BROWSER} | Headless: ${params.HEADLESS} | Threads: ${params.THREAD_COUNT}\033[0m"
                sh """
                    mvn test \\
                        -Dbrowser=${params.BROWSER} \\
                        -Dheadless=${params.HEADLESS} \\
                        -DthreadCount=${params.THREAD_COUNT} \\
                        -Dtest.email=${env.TEST_EMAIL} \\
                        -Dtest.password=${env.TEST_PASSWORD} \\
                        -Dsurefire.suiteXmlFiles=${params.TEST_SUITE} \\
                        -Dallure.results.directory=${env.ALLURE_RESULTS_DIR} \\
                        -Dmaven.test.failure.ignore=true \\
                    || true
                """
                echo "\033[32m✔ Test execution finished. Checking results...\033[0m"
            }
            post {
                always {
                    script {
                        def surefireDir = "${env.WORKSPACE}/${env.SUREFIRE_DIR}"
                        def totalTests    = 0
                        def totalFailures = 0
                        def totalErrors   = 0
                        def totalSkipped  = 0
                        try {
                            def xmlFiles = findFiles(glob: "${env.SUREFIRE_DIR}/**/*.xml")
                            xmlFiles.each { f ->
                                try {
                                    def xml = readFile(f.path)
                                    def tests    = (xml =~ /tests="(\d+)"/)
                                    def failures = (xml =~ /failures="(\d+)"/)
                                    def errors   = (xml =~ /errors="(\d+)"/)
                                    def skipped  = (xml =~ /skipped="(\d+)"/)
                                    if (tests.find())    totalTests    += tests[0][1].toInteger()
                                    if (failures.find()) totalFailures += failures[0][1].toInteger()
                                    if (errors.find())   totalErrors   += errors[0][1].toInteger()
                                    if (skipped.find())  totalSkipped  += skipped[0][1].toInteger()
                                } catch (ex) {
                                    echo "Could not parse ${f.path}: ${ex.message}"
                                }
                            }
                        } catch (ex) {
                            echo "Could not read surefire directory: ${ex.message}"
                        }
                        def totalPassed = totalTests - totalFailures - totalErrors - totalSkipped
                        echo ""
                        echo "╔══════════════════════════════════════╗"
                        echo "║        TEST EXECUTION SUMMARY        ║"
                        echo "╠══════════════════════════════════════╣"
                        echo "║  Total Tests  : ${totalTests}"
                        echo "║  ✅ Passed    : ${totalPassed}"
                        echo "║  ❌ Failed    : ${totalFailures}"
                        echo "║  💥 Errors    : ${totalErrors}"
                        echo "║  ⏭  Skipped   : ${totalSkipped}"
                        echo "╚══════════════════════════════════════╝"
                        echo ""
                        if (totalFailures > 0 || totalErrors > 0) {
                            echo "\033[33m⚠ ${totalFailures + totalErrors} test(s) failed/errored. Marking build UNSTABLE.\033[0m"
                            currentBuild.result = 'UNSTABLE'
                        } else if (totalTests == 0) {
                            echo "\033[33m⚠ No test results found. Marking build UNSTABLE.\033[0m"
                            currentBuild.result = 'UNSTABLE'
                        } else {
                            echo "\033[32m✔ All ${totalTests} tests passed! Build remains STABLE.\033[0m"
                        }
                        currentBuild.description = "✅${totalPassed} ❌${totalFailures} ⏭${totalSkipped} / ${totalTests} tests"
                    }
                }
            }
        }
        stage('Collect Artefacts') {
            steps {
                echo "\033[34m📦 Archiving screenshots, logs and surefire reports...\033[0m"
                catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                    archiveArtifacts(
                        artifacts: "${env.SCREENSHOT_DIR}/**/*.png",
                        allowEmptyArchive: true,
                        fingerprint: false
                    )
                }
                catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                    archiveArtifacts(
                        artifacts: "${env.LOG_DIR}/**",
                        allowEmptyArchive: true
                    )
                }
                catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                    archiveArtifacts(
                        artifacts: "${env.SUREFIRE_DIR}/**",
                        allowEmptyArchive: true
                    )
                }
                echo "\033[32m✔ Artefact collection complete.\033[0m"
            }
        }
        stage('Publish Allure Report') {
            steps {
                echo "\033[34m📊 Generating Allure HTML report...\033[0m"
                catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                    allure([
                        includeProperties: true,
                        jdk              : '',
                        results          : [[path: "${env.ALLURE_RESULTS_DIR}"]],
                        report           : 'allure-report',
                        reportBuildPolicy: 'ALWAYS'
                    ])
                }
                echo "\033[32m✔ Allure report published.\033[0m"
            }
        }
        stage('Publish HTML Report') {
            steps {
                echo "\033[34m📋 Publishing TestNG HTML report...\033[0m"
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
                echo "\033[32m✔ TestNG HTML report published.\033[0m"
            }
        }
    }
    post {
        always {
            echo "\033[34m📁 Processing JUnit XML for trend charts...\033[0m"
            junit(
                testResults          : "${env.SUREFIRE_DIR}/**/*.xml",
                allowEmptyResults    : true,
                skipPublishingChecks : false
            )
            echo "Pipeline complete. Final status: ${currentBuild.result ?: 'SUCCESS'}"
        }
        success {
            echo "\033[32m✅ BUILD STABLE — All tests passed.\033[0m"
        }
        unstable {
            echo "\033[33m⚠ BUILD UNSTABLE — One or more tests failed. Check Allure report.\033[0m"
        }
        failure {
            echo "\033[31m❌ BUILD FAILED — Pipeline error (not test failure). Check console log.\033[0m"
        }
        cleanup {
            cleanWs(
                cleanWhenSuccess : true,
                cleanWhenUnstable: true,
                cleanWhenFailure : true,
                cleanWhenAborted : true,
                deleteDirs       : true
            )
        }
    }
}