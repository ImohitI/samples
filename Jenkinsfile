// Runs on the custom Jenkins image from sast-platform/jenkins, which has os-sast, opengrep and bandit installed.
// INGEST_URL and OLLAMA_URL come from the container environment (docker-compose.yml).
pipeline {
    agent any

    triggers {
        pollSCM('H/2 * * * *')
    }

    parameters {
        booleanParam(name: 'AI_TRIAGE', defaultValue: false, description: 'Ask the local LLM (Ollama) to triage each finding')
    }

    options {
        timeout(time: 15, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    stages {
        stage('Scan') {
            steps {
                sh '''
                    os-sast scan . \
                        --repo samples \
                        --build "$BUILD_NUMBER" \
                        --commit "$GIT_COMMIT" \
                        $( [ "$AI_TRIAGE" = "true" ] && echo --ai-triage ) \
                        --out result.json
                '''
            }
        }

        stage('Publish') {
            steps {
                // curl retries 5xx (e.g. 503 when Kafka is unreachable). A retry can deliver the same build twice;
                // that is safe because the consumer is idempotent on (repoId, buildId).
                sh '''
                    code=$(curl -sS -o response.json -w '%{http_code}' \
                        --retry 5 --retry-delay 3 \
                        -X POST -H 'Content-Type: application/json' \
                        --data-binary @result.json \
                        "$INGEST_URL/api/scan-results")
                    echo "ingest-api answered HTTP $code: $(cat response.json)"
                    [ "$code" = "202" ]
                '''
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'result.json', allowEmptyArchive: true
        }
    }
}
