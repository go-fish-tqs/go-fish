#!/bin/bash
# Unified deployment script for Go-Fish
# Usage: ./deploy.sh [dev|prod] [options]

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Default values
ENV=""
PROFILE=""
ACTION="up"
DETACHED="-d"
BUILD="--build"

# Help message
show_help() {
    cat << EOF
Usage: ./deploy.sh [ENVIRONMENT] [OPTIONS]

ENVIRONMENT:
    dev         Start development environment
    prod        Start production environment

OPTIONS:
    --tools     Start with additional tools (PgAdmin for dev only)
    --down      Stop and remove containers
    --logs      View logs
    --rebuild   Force rebuild images
    --help      Show this help message

EXAMPLES:
    ./deploy.sh dev                 # Start dev environment
    ./deploy.sh dev --tools         # Start dev with PgAdmin
    ./deploy.sh prod                # Start prod environment
    ./deploy.sh dev --down          # Stop dev environment
    ./deploy.sh prod --logs         # View prod logs

EOF
    exit 0
}

# Parse arguments
if [ $# -eq 0 ]; then
    echo "Error: Environment not specified"
    echo "Usage: ./deploy.sh [dev|prod] [options]"
    echo "Run './deploy.sh --help' for more information"
    exit 1
fi

# Parse environment
case "$1" in
    dev)
        ENV="dev"
        COMPOSE_FILE="compose.dev.yaml"
        ENV_FILE=".env.dev"
        shift
        ;;
    prod)
        ENV="prod"
        COMPOSE_FILE="compose.prod.yaml"
        ENV_FILE=".env.prod"
        shift
        ;;
    --help|-h)
        show_help
        ;;
    *)
        echo "Error: Invalid environment '$1'"
        echo "Valid options: dev, prod"
        exit 1
        ;;
esac

# Parse options
while [[ $# -gt 0 ]]; do
    case $1 in
        --tools)
            if [ "$ENV" = "dev" ]; then
                PROFILE="--profile tools"
            else
                echo -e "${YELLOW}Warning: --tools flag is only available for dev environment${NC}"
            fi
            shift
            ;;
        --down)
            ACTION="down"
            shift
            ;;
        --logs)
            ACTION="logs"
            shift
            ;;
        --rebuild)
            BUILD="--build"
            shift
            ;;
        --help|-h)
            show_help
            ;;
        *)
            echo "Unknown option: $1"
            echo "Run './deploy.sh --help' for usage information"
            exit 1
            ;;
    esac
done

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}Error: Docker is not running. Please start Docker first.${NC}"
    exit 1
fi

# Check environment file for production
if [ "$ENV" = "prod" ] && [ "$ACTION" = "up" ]; then
    if [ ! -f "$ENV_FILE" ]; then
        echo -e "${RED}Error: $ENV_FILE file not found!${NC}"
        echo "Create it from the template:"
        echo "  cp .env.prod.example .env.prod"
        echo "  # Edit .env.prod and set secure passwords"
        exit 1
    fi
fi

# Execute action
case "$ACTION" in
    up)
        echo -e "${GREEN}Starting $ENV environment...${NC}"
        
        # Load environment file if it exists
        if [ -f "$ENV_FILE" ]; then
            export $(grep -v '^#' "$ENV_FILE" | xargs)
        fi
        
        docker compose -f "$COMPOSE_FILE" $PROFILE up $DETACHED $BUILD
        
        echo ""
        echo -e "${GREEN}$ENV environment started!${NC}"
        echo ""
        
        if [ "$ENV" = "dev" ]; then
            echo "Service URLs:"
            echo "  Frontend:  http://localhost:3000"
            echo "  Backend:   http://localhost:8080"
            echo "  Database:  postgresql://localhost:5432/gofish_dev"
            if [ -n "$PROFILE" ]; then
                echo "  PgAdmin:   http://localhost:5050"
            fi
        else
            echo "Service URLs:"
            echo "  Frontend:  http://localhost:3000"
            echo "  Backend:   http://localhost:8080"
        fi
        
        echo ""
        echo "View logs: ./deploy.sh $ENV --logs"
        echo "Stop:      ./deploy.sh $ENV --down"
        ;;
        
    down)
        echo -e "${YELLOW}Stopping $ENV environment...${NC}"
        docker compose -f "$COMPOSE_FILE" $PROFILE down
        echo -e "${GREEN}$ENV environment stopped${NC}"
        ;;
        
    logs)
        echo -e "${GREEN}Showing logs for $ENV environment (Ctrl+C to exit)${NC}"
        docker compose -f "$COMPOSE_FILE" logs -f
        ;;
esac
