pipeline {
    agent any
    
    environment {
        LIMIT = "test"
        PROJECT = "bike-chargers"
        PROJECT_FOLDER = "data-collectors/${PROJECT}"
        ARTIFACT_NAME = "dc-${PROJECT}"
        DOCKER_IMAGE = "755952719952.dkr.ecr.eu-west-1.amazonaws.com/${PROJECT}"
        DOCKER_TAG = "$LIMIT-$BUILD_NUMBER"
        DATACOLLECTORS_CLIENT_SECRET = credentials("keycloak-datacollectors-secret-${LIMIT}")
        KEYCLOAK_URL = "https://auth.opendatahub.testingmachine.eu"
        WRITER_URL = "https://mobility.share.opendatahub.testingmachine.eu"
        LOG_LEVEL = "info"
        CONNECTOR_URL = "https://maas.ecospazio.it/api"
        CONNECTOR_API_KEY = credentials('dc-bike-chargers-api-key')
    }

    stages {
        stage('Configure') {
            steps {
                sh """
                    cd ${PROJECT_FOLDER}
                    echo 'COMPOSE_PROJECT_NAME=${PROJECT}' > .env
                    echo 'DOCKER_IMAGE=${DOCKER_IMAGE}' >> .env
                    echo 'DOCKER_TAG=${DOCKER_TAG}' >> .env
                    echo 'LOG_LEVEL=${LOG_LEVEL}' >> .env
                    echo 'ARTIFACT_NAME=${ARTIFACT_NAME}' >> .env
                    echo 'authorizationUri=${KEYCLOAK_URL}/auth' >> .env
                    echo 'tokenUri=${KEYCLOAK_URL}/auth/realms/noi/protocol/openid-connect/token' >> .env 
                    echo 'clientId=odh-mobility-datacollector' >> .env
                    echo 'clientName=odh-mobility-datacollector' >> .env
                    echo 'clientSecret=${DATACOLLECTORS_CLIENT_SECRET}' >> .env
                    echo 'scope=openid' >> .env 
                    echo -n 'provenance_version=' >> .env
                    xmlstarlet sel -N pom=http://maven.apache.org/POM/4.0.0 -t -v '/pom:project/pom:version' pom.xml >> .env
                    echo '' >> .env
                    echo -n 'provenance_name=' >> .env 
                    xmlstarlet sel -N pom=http://maven.apache.org/POM/4.0.0 -t -v '/pom:project/pom:artifactId' pom.xml >> .env
                    echo '' >> .env
                    echo 'BASE_URI=${WRITER_URL}/json' >> .env
                    echo 'CONNECTOR_URL=${CONNECTOR_URL}' >> .env
                    echo 'CONNECTOR_API_KEY=${CONNECTOR_API_KEY}' >> .env
                """
            }
        }
        stage('Test & Build') {
            steps {
                sh """
                    cd ${PROJECT_FOLDER}
                    aws ecr get-login --region eu-west-1 --no-include-email | bash
                    docker-compose --no-ansi -f infrastructure/docker-compose.build.yml build --pull
                    docker-compose --no-ansi -f infrastructure/docker-compose.build.yml push
                """
            }
        }
        stage('Deploy') {
            steps {
               sshagent(['jenkins-ssh-key']) {
                    sh """
                        (cd ${PROJECT_FOLDER}/infrastructure/ansible && ansible-galaxy install -f -r requirements.yml)
                        (cd ${PROJECT_FOLDER}/infrastructure/ansible && ansible-playbook --limit=${LIMIT} deploy.yml --extra-vars "release_name=${BUILD_NUMBER} project_name=${PROJECT}")
                    """
                }
            }
        }
    }
}
