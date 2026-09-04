#!/bin/bash

# iFood Clone - Microservices Commit Helper
# Usage: ./commit.sh <type> <scope> <description> [body]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Valid commit types
VALID_TYPES=("feat" "fix" "docs" "style" "refactor" "test" "chore" "perf" "ci" "build" "security")

# Valid scopes (microservices)
VALID_SCOPES=(
    "config-server" 
    "service-discovery" 
    "api-gateway" 
    "auth-service" 
    "user-service"
    "restaurant-service" 
    "menu-service" 
    "order-service" 
    "payment-service"
    "delivery-service" 
    "notification-service" 
    "review-service"
    "database"
    "docker" 
    "monitoring"
    "security"
    "docs"
    "ci"
)

# Function to display usage
usage() {
    echo -e "${BLUE}iFood Clone - Microservices Commit Helper${NC}"
    echo -e "${YELLOW}Usage:${NC} ./commit.sh <type> <scope> <description> [body]"
    echo ""
    echo -e "${YELLOW}Valid Types:${NC}"
    printf "  %s\n" "${VALID_TYPES[@]}"
    echo ""
    echo -e "${YELLOW}Valid Scopes:${NC}"
    printf "  %s\n" "${VALID_SCOPES[@]}"
    echo ""
    echo -e "${YELLOW}Examples:${NC}"
    echo "  ./commit.sh feat auth-service 'add JWT token generation'"
    echo "  ./commit.sh fix api-gateway 'resolve Redis connection timeout' 'Updated configuration to use container networking'"
    echo "  ./commit.sh docs restaurant-service 'add API documentation for search endpoints'"
    exit 1
}

# Function to validate type
validate_type() {
    local type=$1
    for valid_type in "${VALID_TYPES[@]}"; do
        if [[ "$type" == "$valid_type" ]]; then
            return 0
        fi
    done
    return 1
}

# Function to validate scope
validate_scope() {
    local scope=$1
    for valid_scope in "${VALID_SCOPES[@]}"; do
        if [[ "$scope" == "$valid_scope" ]]; then
            return 0
        fi
    done
    return 1
}

# Function to check if there are staged changes
check_staged_changes() {
    if ! git diff --cached --quiet; then
        return 0  # There are staged changes
    else
        return 1  # No staged changes
    fi
}

# Function to show current status
show_status() {
    echo -e "${BLUE}Current Git Status:${NC}"
    git status --short
    echo ""
}

# Main script logic
main() {
    # Check if we're in a git repository
    if ! git rev-parse --git-dir > /dev/null 2>&1; then
        echo -e "${RED}Error: Not in a git repository${NC}"
        exit 1
    fi

    # Parse arguments
    if [[ $# -lt 3 ]]; then
        echo -e "${RED}Error: Missing required arguments${NC}"
        usage
    fi

    local type=$1
    local scope=$2
    local description=$3
    local body=${4:-""}

    # Validate type
    if ! validate_type "$type"; then
        echo -e "${RED}Error: Invalid type '$type'${NC}"
        echo -e "${YELLOW}Valid types:${NC} ${VALID_TYPES[*]}"
        exit 1
    fi

    # Validate scope
    if ! validate_scope "$scope"; then
        echo -e "${RED}Error: Invalid scope '$scope'${NC}"
        echo -e "${YELLOW}Valid scopes:${NC} ${VALID_SCOPES[*]}"
        exit 1
    fi

    # Build commit message
    local commit_message="$type($scope): $description"
    
    # Add body if provided
    if [[ -n "$body" ]]; then
        commit_message="$commit_message

$body"
    fi

    # Show what we're about to do
    echo -e "${BLUE}Preparing to commit:${NC}"
    echo -e "${YELLOW}Type:${NC} $type"
    echo -e "${YELLOW}Scope:${NC} $scope"
    echo -e "${YELLOW}Description:${NC} $description"
    if [[ -n "$body" ]]; then
        echo -e "${YELLOW}Body:${NC} yes"
    fi
    echo -e "${YELLOW}Message:${NC} $commit_message"
    echo ""

    # Check if there are any changes
    if git diff --quiet && git diff --cached --quiet; then
        echo -e "${YELLOW}Warning: No changes detected${NC}"
        exit 0
    fi

    # Show current status
    show_status

    # Ask for confirmation if there are unstaged changes
    if ! git diff --quiet; then
        echo -e "${YELLOW}Warning: You have unstaged changes.${NC}"
        echo -e "${YELLOW}Do you want to stage all changes? (y/n):${NC}"
        read -r stage_all
        
        if [[ "$stage_all" =~ ^[Yy]$ ]]; then
            git add .
            echo -e "${GREEN}All changes staged${NC}"
        else
            echo -e "${YELLOW}Only staged changes will be committed${NC}"
        fi
    fi

    # Check if there are staged changes
    if git diff --cached --quiet; then
        echo -e "${RED}Error: No staged changes to commit${NC}"
        exit 1
    fi

    # Make the commit
    echo -e "${BLUE}Creating commit...${NC}"
    git commit -m "$commit_message"
    
    echo -e "${GREEN}✅ Commit created successfully!${NC}"
    
    # Show the commit
    echo -e "${BLUE}Latest commit:${NC}"
    git log --oneline -1
}

# Handle help argument
if [[ $# -eq 0 ]] || [[ "$1" == "help" ]] || [[ "$1" == "-h" ]] || [[ "$1" == "--help" ]]; then
    usage
fi

# Run main function with all arguments
main "$@"
