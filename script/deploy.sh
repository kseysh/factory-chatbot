#!/bin/bash

set -e

# Configuration
APP_DIR="${HOME}/app"
BLUE_PORT=8080
HEALTH_CHECK_TIMEOUT=60
DOCKER_IMAGE="${DOCKER_IMAGE_URL:-}"
BEDROCK_API_KEY="${BEDROCK_API_KEY:-}"
CONTAINER_NAME="factory-chatbot-chat"

echo "Starting deployment..."

# Check Docker installation
if ! command -v docker &> /dev/null; then
    echo "ERROR: Docker is not installed"
    exit 1
fi

if [ ! -d "${APP_DIR}" ]; then
    echo "ERROR: App directory not found: ${APP_DIR}"
    exit 1
fi

# Stop existing container on port 8080
echo "Stopping existing container on port ${BLUE_PORT}..."
docker stop "${CONTAINER_NAME}" 2>/dev/null || true
docker rm "${CONTAINER_NAME}" 2>/dev/null || true

# Pull Docker image
echo "Pulling Docker image: ${DOCKER_IMAGE}..."
docker pull "${DOCKER_IMAGE}"

# Deploy new container
echo "Starting new container on port ${BLUE_PORT}..."

# Prepare environment variables
ENV_VARS=""
if [ ! -z "${BEDROCK_API_KEY}" ]; then
    ENV_VARS="-e SPRING_AI_BEDROCK_API_KEY=${BEDROCK_API_KEY}"
fi

# Start new container
docker run -d \
    --name "${CONTAINER_NAME}" \
    --restart always \
    -p "${BLUE_PORT}:8080" \
    -e SPRING_PROFILES_ACTIVE=prod \
    ${ENV_VARS} \
    "${DOCKER_IMAGE}"

echo "Container started. Waiting for health check..."

# Health check
RETRY_COUNT=0
MAX_RETRIES=30

while [ ${RETRY_COUNT} -lt ${MAX_RETRIES} ]; do
    if curl -sf "http://localhost:${BLUE_PORT}/health" > /dev/null 2>&1; then
        echo "Health check passed!"
        break
    fi

    RETRY_COUNT=$((RETRY_COUNT + 1))
    if [ ${RETRY_COUNT} -lt ${MAX_RETRIES} ]; then
        echo -n "."
        sleep 2
    fi
done

if [ ${RETRY_COUNT} -eq ${MAX_RETRIES} ]; then
    echo "ERROR: Health check failed"
    docker stop "${CONTAINER_NAME}" || true
    docker rm "${CONTAINER_NAME}" || true
    exit 1
fi

echo ""
echo "Deployment successful!"
echo "Container running on port ${BLUE_PORT}"
echo ""
echo "Running containers:"
docker ps
