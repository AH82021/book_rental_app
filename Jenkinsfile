// ============================================================
// book-nest — Jenkins CI/CD Pipeline (Declarative)
// ============================================================
// Branch strategy:
//   feature/*  → build + test only
//   develop    → build + test + deploy to staging
//   main       → build + test + deploy to production (with approval)
// ============================================================

pipeline {

    // ─── Agent ──────────────────────────────────────────────
    agent {
        docker {
            image 'bookstore/jenkins-agent:latest'
            args  '--group-add docker -v /var/run/docker.sock:/var/run/docker.sock -v $HOME/.m2:/root/.m2'
            reuseNode true
        }
    }

    // ─── Parameters (can be overridden at runtime) ───────────
    parameters {
        choice(
            name: 'DEPLOY_ENV',
            choices: ['auto', 'staging', 'production', 'none'],
            description: 'Override deployment target. "auto" follows branch strategy.'
        )
        booleanParam(
            name: 'SKIP_TESTS',
            defaultValue: false,
            description: 'Skip unit & integration tests.'
        )
        booleanParam(
            name: 'SKIP_SECURITY_SCAN',
            defaultValue: false,
            description: 'Skip OWASP Dependency-Check and Trivy image scan.'
        )
        string(
            name: 'IMAGE_TAG_OVERRIDE',
            defaultValue: '',
            description: 'Override the Docker image tag. Leave blank to use auto-generated tag.'
        )
    }

    // ─── Environment Variables ───────────────────────────────
    environment {
        // Java / Maven
        JAVA_VERSION      = '17'
        MAVEN_OPTS        = '-Xmx1024m -XX:+UseG1GC'
        MAVEN_CLI_OPTS    = '--batch-mode --errors --fail-at-end --show-version -Dmaven.repo.local=/root/.m2/repository'

        // Docker
        DOCKER_REGISTRY   = 'docker.io'
        DOCKER_ORG        = 'bookstore'               // ← change to your Docker Hub org
        IMAGE_TAG         = "${params.IMAGE_TAG_OVERRIDE ?: "${env.BRANCH_NAME}-${env.GIT_COMMIT?.take(8)}"}"

        // Credentials (set in Jenkins → Manage Credentials)
        DOCKER_CREDS      = credentials('docker-registry-credentials')
        SONAR_TOKEN       = credentials('sonarqube-token')
        SLACK_WEBHOOK     = credentials('slack-webhook-url')
        DB_PASSWORD       = credentials('bookstore-db-password')

        // Service list (Space separated. Add new services here as they are developed)
        // e.g. 'auth-service user-service book-service report-service inventory-service'
        SERVICES          = 'auth-service user-service book-service'

        // SonarQube
        SONAR_HOST_URL    = 'http://sonarqube:9000'   // ← update to your SonarQube URL

        // Deployment
        STAGING_HOST      = credentials('staging-server-host')
        PRODUCTION_HOST   = credentials('production-server-host')
    }

    // ─── Options ─────────────────────────────────────────────
    options {
        buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '5'))
        timestamps()
        timeout(time: 60, unit: 'MINUTES')
        disableConcurrentBuilds(abortPrevious: true)
        ansiColor('xterm')
    }

    // ─── Triggers ────────────────────────────────────────────
    triggers {
        // Poll SCM every 5 minutes as fallback (prefer GitHub webhook)
        pollSCM('H/5 * * * *')
    }

    // ════════════════════════════════════════════════════════
    // S T A G E S
    // ════════════════════════════════════════════════════════
    stages {

        // ────────────────────────────────────────────────────
        // Stage 1: Checkout & Initialise
        // ────────────────────────────────────────────────────
        stage('🔍 Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_COMMIT_SHORT = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    env.GIT_AUTHOR      = sh(script: 'git log -1 --format="%an"', returnStdout: true).trim()
                    env.GIT_MESSAGE     = sh(script: 'git log -1 --format="%s"', returnStdout: true).trim()
                    env.BUILD_DISPLAY   = "#${BUILD_NUMBER} | ${env.BRANCH_NAME} | ${env.GIT_COMMIT_SHORT}"

                    currentBuild.displayName = env.BUILD_DISPLAY
                    currentBuild.description = "Author: ${env.GIT_AUTHOR} | ${env.GIT_MESSAGE}"

                    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                    echo "  Branch  : ${env.BRANCH_NAME}"
                    echo "  Commit  : ${env.GIT_COMMIT_SHORT}"
                    echo "  Author  : ${env.GIT_AUTHOR}"
                    echo "  Tag     : ${env.IMAGE_TAG}"
                    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                }
                // Make scripts executable
                sh 'chmod +x jenkins/scripts/*.sh mvnw'
            }
        }

        // ────────────────────────────────────────────────────
        // Stage 2: Compile
        // ────────────────────────────────────────────────────
        stage('🔨 Compile') {
            steps {
                sh './mvnw clean compile ${MAVEN_CLI_OPTS}'
            }
        }

        // ────────────────────────────────────────────────────
        // Stage 3: Test (Unit + Integration)
        // ────────────────────────────────────────────────────
        stage('🧪 Test') {
            when {
                not { expression { params.SKIP_TESTS } }
            }
            parallel {
                stage('Unit Tests') {
                    steps {
                        sh './mvnw test ${MAVEN_CLI_OPTS} -Dsurefire.failIfNoSpecifiedTests=false'
                    }
                    post {
                        always {
                            junit(
                                testResults: '**/target/surefire-reports/*.xml',
                                allowEmptyResults: true
                            )
                        }
                    }
                }
                stage('Integration Tests') {
                    steps {
                        sh '''
                            ./mvnw verify -P integration-tests ${MAVEN_CLI_OPTS} \
                              -Dspring.datasource.url=jdbc:postgresql://localhost:5432/testdb \
                              -Dspring.datasource.username=bookstore_user \
                              -Dspring.datasource.password=${DB_PASSWORD} \
                              -Dspring.redis.host=localhost \
                              -Dspring.redis.port=6379 \
                              -Dsurefire.failIfNoSpecifiedTests=false
                        '''
                    }
                    post {
                        always {
                            junit(
                                testResults: '**/target/failsafe-reports/*.xml',
                                allowEmptyResults: true
                            )
                        }
                    }
                }
            }
        }

        // ────────────────────────────────────────────────────
        // Stage 4: Code Quality — SonarQube
        // ────────────────────────────────────────────────────
        stage('📊 Code Quality') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        ./mvnw sonar:sonar ${MAVEN_CLI_OPTS} \
                          -Dsonar.projectKey=book-nest \
                          -Dsonar.projectName="Book Nest Services" \
                          -Dsonar.host.url=${SONAR_HOST_URL} \
                          -Dsonar.login=${SONAR_TOKEN} \
                          -Dsonar.java.coveragePlugin=jacoco \
                          -Dsonar.coverage.jacoco.xmlReportPaths=**/target/site/jacoco/jacoco.xml \
                          -Dsonar.exclusions="**/generated/**,**/target/**"
                    '''
                }
            }
            post {
                always {
                    catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                        timeout(time: 5, unit: 'MINUTES') {
                            waitForQualityGate abortPipeline: false
                        }
                    }
                }
            }
        }

        // ────────────────────────────────────────────────────
        // Stage 5: Security Scan — OWASP Dependency Check
        // ────────────────────────────────────────────────────
        stage('🔒 Security Scan') {
            when {
                not { expression { params.SKIP_SECURITY_SCAN } }
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                sh '''
                    ./mvnw ${MAVEN_CLI_OPTS} \
                      org.owasp:dependency-check-maven:check \
                      -DfailBuildOnCVSS=8 \
                      -DskipTestScope=true \
                      -Dformat=ALL \
                      || echo "⚠️  OWASP check completed with findings — review report"
                '''
            }
            post {
                always {
                    dependencyCheckPublisher(
                        pattern: '**/dependency-check-report.xml',
                        failedTotalCritical: 0,
                        unstableTotalHigh: 5
                    )
                }
            }
        }

        // ────────────────────────────────────────────────────
        // Stage 6: Package (build JARs)
        // ────────────────────────────────────────────────────
        stage('📦 Package') {
            steps {
                sh './mvnw package -DskipTests ${MAVEN_CLI_OPTS}'
            }
            post {
                success {
                    archiveArtifacts(
                        artifacts: '*/target/*.jar, !*/target/*-sources.jar, !*/target/*-javadoc.jar',
                        allowEmptyArchive: false
                    )
                }
            }
        }

        // ────────────────────────────────────────────────────
        // Stage 7: Docker Build (dynamic parallel builds)
        // ────────────────────────────────────────────────────
        stage('🐳 Docker Build') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    expression { params.DEPLOY_ENV != 'none' }
                }
            }
            steps {
                script {
                    sh "echo '${DOCKER_CREDS_PSW}' | docker login ${DOCKER_REGISTRY} -u '${DOCKER_CREDS_USR}' --password-stdin"

                    def serviceList = env.SERVICES.split(' ')
                    def buildStages = [:]

                    serviceList.each { service ->
                        def svc = service
                        buildStages["Build ${svc}"] = {
                            stage("Build ${svc}") {
                                def imageFullName = "${DOCKER_REGISTRY}/${DOCKER_ORG}/${svc}:${IMAGE_TAG}"
                                def imageLatest   = "${DOCKER_REGISTRY}/${DOCKER_ORG}/${svc}:latest"

                                sh """
                                    docker build \
                                      --build-arg VERSION=${IMAGE_TAG} \
                                      --cache-from ${imageLatest} \
                                      -f ${svc}/Dockerfile \
                                      -t ${imageFullName} \
                                      -t ${imageLatest} \
                                      .
                                """
                            }
                        }
                    }
                    parallel buildStages
                }
            }
        }

        // ────────────────────────────────────────────────────
        // Stage 8: Image Security Scan — Trivy
        // ────────────────────────────────────────────────────
        stage('🛡️ Image Scan') {
            when {
                not { expression { params.SKIP_SECURITY_SCAN } }
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                script {
                    def serviceList = env.SERVICES.split(' ')
                    def scanStages  = [:]

                    serviceList.each { service ->
                        def svc = service
                        scanStages["Scan ${svc}"] = {
                            sh """
                                trivy image \
                                  --exit-code 0 \
                                  --severity HIGH,CRITICAL \
                                  --no-progress \
                                  --format table \
                                  --output trivy-report-${svc}.txt \
                                  ${DOCKER_REGISTRY}/${DOCKER_ORG}/${svc}:${IMAGE_TAG} \
                                || true
                            """
                        }
                    }
                    parallel scanStages
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'trivy-report-*.txt', allowEmptyArchive: true
                }
            }
        }

        // ────────────────────────────────────────────────────
        // Stage 9: Push Docker Images
        // ────────────────────────────────────────────────────
        stage('⬆️ Push Images') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    expression { params.DEPLOY_ENV != 'none' }
                }
            }
            steps {
                script {
                    def serviceList = env.SERVICES.split(' ')
                    def pushStages  = [:]

                    serviceList.each { service ->
                        def svc = service
                        pushStages["Push ${svc}"] = {
                            sh """
                                docker push ${DOCKER_REGISTRY}/${DOCKER_ORG}/${svc}:${IMAGE_TAG}
                                docker push ${DOCKER_REGISTRY}/${DOCKER_ORG}/${svc}:latest
                            """
                        }
                    }
                    parallel pushStages
                }
            }
        }

        // ────────────────────────────────────────────────────
        // Stage 10: Deploy to Staging
        // ────────────────────────────────────────────────────
        stage('🚀 Deploy → Staging') {
            when {
                anyOf {
                    branch 'develop'
                    expression { params.DEPLOY_ENV == 'staging' }
                }
            }
            steps {
                withCredentials([sshUserPrivateKey(
                    credentialsId: 'staging-server-ssh',
                    keyFileVariable: 'SSH_KEY',
                    usernameVariable: 'SSH_USER'
                )]) {
                    sh 'jenkins/scripts/deploy.sh staging ${STAGING_HOST} ${SSH_KEY} ${SSH_USER} ${IMAGE_TAG}'
                }
            }
        }

        // ────────────────────────────────────────────────────
        // Stage 11: Smoke Tests (Staging)
        // ────────────────────────────────────────────────────
        stage('💨 Smoke Tests') {
            when {
                anyOf {
                    branch 'develop'
                    expression { params.DEPLOY_ENV == 'staging' }
                }
            }
            steps {
                sh 'jenkins/scripts/smoke-test.sh staging ${STAGING_HOST}'
            }
            post {
                failure {
                    script {
                        withCredentials([sshUserPrivateKey(credentialsId: 'staging-server-ssh', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER')]) {
                            sh 'jenkins/scripts/rollback.sh staging ${STAGING_HOST} ${SSH_KEY} ${SSH_USER}'
                        }
                    }
                }
            }
        }

        // ────────────────────────────────────────────────────
        // Stage 12: Manual Approval Gate (Production)
        // ────────────────────────────────────────────────────
        stage('⏸️ Approval Gate') {
            when {
                anyOf {
                    branch 'main'
                    expression { params.DEPLOY_ENV == 'production' }
                }
            }
            steps {
                script {
                    slackSend(
                        color: '#FFA500',
                        message: ":hourglass_flowing_sand: *Production Deploy Awaiting Approval*\nBranch: `${env.BRANCH_NAME}` | Tag: `${env.IMAGE_TAG}`\n<${env.BUILD_URL}input|Approve here>",
                        webhookUrl: env.SLACK_WEBHOOK
                    )
                    timeout(time: 30, unit: 'MINUTES') {
                        input id: 'ProdApproval', message: 'Deploy to PRODUCTION?', ok: 'Deploy'
                    }
                }
            }
        }

        // ────────────────────────────────────────────────────
        // Stage 13: Deploy to Production
        // ────────────────────────────────────────────────────
        stage('🚀 Deploy → Production') {
            when {
                anyOf {
                    branch 'main'
                    expression { params.DEPLOY_ENV == 'production' }
                }
            }
            steps {
                withCredentials([sshUserPrivateKey(
                    credentialsId: 'production-server-ssh',
                    keyFileVariable: 'SSH_KEY',
                    usernameVariable: 'SSH_USER'
                )]) {
                    sh 'jenkins/scripts/deploy.sh production ${PRODUCTION_HOST} ${SSH_KEY} ${SSH_USER} ${IMAGE_TAG}'
                }
            }
        }

        // ────────────────────────────────────────────────────
        // Stage 14: Health Check (Production)
        // ────────────────────────────────────────────────────
        stage('❤️ Health Check') {
            when {
                anyOf {
                    branch 'main'
                    expression { params.DEPLOY_ENV == 'production' }
                }
            }
            steps {
                sh 'jenkins/scripts/smoke-test.sh production ${PRODUCTION_HOST}'
            }
            post {
                failure {
                    script {
                        withCredentials([sshUserPrivateKey(credentialsId: 'production-server-ssh', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER')]) {
                            sh 'jenkins/scripts/rollback.sh production ${PRODUCTION_HOST} ${SSH_KEY} ${SSH_USER}'
                        }
                    }
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════
    // P O S T   A C T I O N S
    // ════════════════════════════════════════════════════════
    post {
        always {
            sh 'docker logout ${DOCKER_REGISTRY} || true'
            cleanWs(deleteDirs: true, notFailBuild: true)
        }
        success {
            slackSend(color: '#36A64F', message: ":white_check_mark: *Pipeline SUCCESS* — ${env.BUILD_DISPLAY}\n<${env.BUILD_URL}|View Build>", webhookUrl: env.SLACK_WEBHOOK)
        }
        failure {
            slackSend(color: '#FF0000', message: ":x: *Pipeline FAILED* — ${env.BUILD_DISPLAY} at stage `${env.STAGE_NAME}`\n<${env.BUILD_URL}console|View Log>", webhookUrl: env.SLACK_WEBHOOK)
        }
    }
}
