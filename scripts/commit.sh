#!/bin/bash#!/bin/bash#!/bin/bash



# Commit script for iFood Clone project

# Enforces conventional commit format with service context

# Commit script for iFood Clone project# iFood Clone - Microservices Commit Helper

set -e

# Enforces conventional commit format with service context# Usage: ./commit.sh <type> <scope> <description> [body]

# Colors

RED='\033[0;31m'

GREEN='\033[0;32m'

YELLOW='\033[1;33m'set -eset -e

BLUE='\033[0;34m'

CYAN='\033[0;36m'

NC='\033[0m'

# Colors# Colors for output

# Valid commit types

VALID_TYPES=("feat" "fix" "docs" "style" "refactor" "test" "chore" "perf" "ci" "build" "revert")RED='\033[0;31m'RED='\033[0;31m'



# Services in the projectGREEN='\033[0;32m'GREEN='\033[0;32m'

SERVICES=("auth-service" "user-service" "restaurant-service" "menu-service" "order-service" "payment-service" "delivery-service" "notification-service" "review-service" "config-server" "service-discovery" "api-gateway")

YELLOW='\033[1;33m'YELLOW='\033[1;33m'

# Function to display help

show_help() {BLUE='\033[0;34m'BLUE='\033[0;34m'

    echo -e "${BLUE}📝 iFood Clone Commit Script${NC}"

    echoNC='\033[0m'NC='\033[0m' # No Color

    echo "Usage: $0 <type> <service> <message>"

    echo

    echo -e "${YELLOW}Commit Types:${NC}"

    printf "  %s\n" "${VALID_TYPES[@]}"# Valid commit types# Valid commit types

    echo

    echo -e "${YELLOW}Services:${NC}"VALID_TYPES=("feat" "fix" "docs" "style" "refactor" "test" "chore" "perf" "ci" "build" "revert")VALID_TYPES=("feat" "fix" "docs" "style" "refactor" "test" "chore" "perf" "ci" "build" "security")

    printf "  %s\n" "${SERVICES[@]}"

    echo

    echo -e "${YELLOW}Examples:${NC}"

    echo "  $0 feat auth-service \"add user registration API\""# Services in the project# Valid scopes (microservices)

    echo "  $0 fix config-server \"resolve configuration loading issue\""

    echo "  $0 docs api-gateway \"update API documentation\""SERVICES=("auth-service" "user-service" "restaurant-service" "menu-service" "order-service" "payment-service" "delivery-service" "notification-service" "review-service" "config-server" "service-discovery" "api-gateway")VALID_SCOPES=(

    echo "  $0 test restaurant-service \"add unit tests for restaurant validation\""

    echo    "config-server" 

    echo -e "${YELLOW}Format:${NC}"

    echo "  <type>(<service>): <message>"# Function to display help    "service-discovery" 

}

show_help() {    "api-gateway" 

# Validate commit type

validate_type() {    echo -e "${BLUE}📝 iFood Clone Commit Script${NC}"    "auth-service" 

    local type=$1

    if [[ ! " ${VALID_TYPES[@]} " =~ " ${type} " ]]; then    echo    "user-service"

        echo -e "${RED}❌ Error: Invalid commit type '${type}'${NC}"

        echo -e "${YELLOW}Valid types: ${VALID_TYPES[*]}${NC}"    echo "Usage: $0 <type> <service> <message>"    "restaurant-service" 

        exit 1

    fi    echo    "menu-service" 

}

    echo -e "${YELLOW}Commit Types:${NC}"    "order-service" 

# Validate service

validate_service() {    printf "  %s\n" "${VALID_TYPES[@]}"    "payment-service"

    local service=$1

    if [[ ! " ${SERVICES[@]} " =~ " ${service} " ]]; then    echo    "delivery-service" 

        echo -e "${RED}❌ Error: Invalid service '${service}'${NC}"

        echo -e "${YELLOW}Valid services: ${SERVICES[*]}${NC}"    echo -e "${YELLOW}Services:${NC}"    "notification-service" 

        exit 1

    fi    printf "  %s\n" "${SERVICES[@]}"    "review-service"

}

    echo    "database"

# Main commit function

make_commit() {    echo -e "${YELLOW}Examples:${NC}"    "docker" 

    local type=$1

    local service=$2    echo "  $0 feat auth-service \"add user registration API\""    "monitoring"

    local message=$3

        echo "  $0 fix config-server \"resolve configuration loading issue\""    "security"

    # Validate inputs

    if [ -z "$type" ] || [ -z "$service" ] || [ -z "$message" ]; then    echo "  $0 docs api-gateway \"update API documentation\""    "docs"

        echo -e "${RED}❌ Error: All parameters required${NC}"

        show_help    echo "  $0 test restaurant-service \"add unit tests for restaurant validation\""    "ci"

        exit 1

    fi    echo)

    

    validate_type "$type"    echo -e "${YELLOW}Format:${NC}"

    validate_service "$service"

        echo "  <type>(<service>): <message>"# Function to display usage

    # Format commit message

    local commit_msg="${type}(${service}): ${message}"    echousage() {

    

    echo -e "${BLUE}📝 Making commit...${NC}"    echo -e "${YELLOW}Breaking Changes:${NC}"    echo -e "${BLUE}iFood Clone - Microservices Commit Helper${NC}"

    echo -e "${YELLOW}Type:${NC} ${type}"

    echo -e "${YELLOW}Service:${NC} ${service}"    echo "  Add 'BREAKING CHANGE:' in commit body for breaking changes"    echo -e "${YELLOW}Usage:${NC} ./commit.sh <type> <scope> <description> [body]"

    echo -e "${YELLOW}Message:${NC} ${message}"

    echo -e "${YELLOW}Full commit:${NC} ${commit_msg}"}    echo ""

    echo

        echo -e "${YELLOW}Valid Types:${NC}"

    # Add all files

    git add .# Validate commit type    printf "  %s\n" "${VALID_TYPES[@]}"

    

    # Make the commitvalidate_type() {    echo ""

    git commit -m "$commit_msg"

        local type=$1    echo -e "${YELLOW}Valid Scopes:${NC}"

    echo -e "${GREEN}✅ Commit created successfully!${NC}"

        if [[ ! " ${VALID_TYPES[@]} " =~ " ${type} " ]]; then    printf "  %s\n" "${VALID_SCOPES[@]}"

    # Show the commit

    echo -e "${CYAN}Latest commit:${NC}"        echo -e "${RED}❌ Error: Invalid commit type '${type}'${NC}"    echo ""

    git log --oneline -1

}        echo -e "${YELLOW}Valid types: ${VALID_TYPES[*]}${NC}"    echo -e "${YELLOW}Examples:${NC}"



# Check if we're in a git repository        exit 1    echo "  ./commit.sh feat auth-service 'add JWT token generation'"

if ! git rev-parse --git-dir > /dev/null 2>&1; then

    echo -e "${RED}❌ Error: Not in a Git repository${NC}"    fi    echo "  ./commit.sh fix api-gateway 'resolve Redis connection timeout' 'Updated configuration to use container networking'"

    exit 1

fi}    echo "  ./commit.sh docs restaurant-service 'add API documentation for search endpoints'"



# Check for uncommitted changes    exit 1

if git diff-index --quiet HEAD --; then

    echo -e "${YELLOW}⚠️  No changes to commit${NC}"# Validate service}

    exit 0

fivalidate_service() {



# Main script logic    local service=$1# Function to validate type

case "$1" in

    "help"|"-h"|"--help"|"")    if [[ ! " ${SERVICES[@]} " =~ " ${service} " ]]; thenvalidate_type() {

        show_help

        ;;        echo -e "${RED}❌ Error: Invalid service '${service}'${NC}"    local type=$1

    *)

        make_commit "$1" "$2" "$3"        echo -e "${YELLOW}Valid services: ${SERVICES[*]}${NC}"    for valid_type in "${VALID_TYPES[@]}"; do

        ;;

esac        exit 1        if [[ "$type" == "$valid_type" ]]; then

    fi            return 0

}        fi

    done

# Main commit function    return 1

make_commit() {}

    local type=$1

    local service=$2# Function to validate scope

    local message=$3validate_scope() {

        local scope=$1

    # Validate inputs    for valid_scope in "${VALID_SCOPES[@]}"; do

    if [ -z "$type" ] || [ -z "$service" ] || [ -z "$message" ]; then        if [[ "$scope" == "$valid_scope" ]]; then

        echo -e "${RED}❌ Error: All parameters required${NC}"            return 0

        show_help        fi

        exit 1    done

    fi    return 1

    }

    validate_type "$type"

    validate_service "$service"# Function to check if there are staged changes

    check_staged_changes() {

    # Format commit message    if ! git diff --cached --quiet; then

    local commit_msg="${type}(${service}): ${message}"        return 0  # There are staged changes

        else

    echo -e "${BLUE}📝 Making commit...${NC}"        return 1  # No staged changes

    echo -e "${YELLOW}Type:${NC} ${type}"    fi

    echo -e "${YELLOW}Service:${NC} ${service}"}

    echo -e "${YELLOW}Message:${NC} ${message}"

    echo -e "${YELLOW}Full commit:${NC} ${commit_msg}"# Function to show current status

    echoshow_status() {

        echo -e "${BLUE}Current Git Status:${NC}"

    # Add all files    git status --short

    git add .    echo ""

    }

    # Make the commit

    git commit -m "$commit_msg"# Main script logic

    main() {

    echo -e "${GREEN}✅ Commit created successfully!${NC}"    # Check if we're in a git repository

        if ! git rev-parse --git-dir > /dev/null 2>&1; then

    # Show the commit        echo -e "${RED}Error: Not in a git repository${NC}"

    echo -e "${CYAN}Latest commit:${NC}"        exit 1

    git log --oneline -1    fi

}

    # Parse arguments

# Check if we're in a git repository    if [[ $# -lt 3 ]]; then

if ! git rev-parse --git-dir > /dev/null 2>&1; then        echo -e "${RED}Error: Missing required arguments${NC}"

    echo -e "${RED}❌ Error: Not in a Git repository${NC}"        usage

    exit 1    fi

fi

    local type=$1

# Check for uncommitted changes    local scope=$2

if git diff-index --quiet HEAD --; then    local description=$3

    echo -e "${YELLOW}⚠️  No changes to commit${NC}"    local body=${4:-""}

    exit 0

fi    # Validate type

    if ! validate_type "$type"; then

# Main script logic        echo -e "${RED}Error: Invalid type '$type'${NC}"

case "$1" in        echo -e "${YELLOW}Valid types:${NC} ${VALID_TYPES[*]}"

    "help"|"-h"|"--help"|"")        exit 1

        show_help    fi

        ;;

    *)    # Validate scope

        make_commit "$1" "$2" "$3"    if ! validate_scope "$scope"; then

        ;;        echo -e "${RED}Error: Invalid scope '$scope'${NC}"

esac        echo -e "${YELLOW}Valid scopes:${NC} ${VALID_SCOPES[*]}"
        exit 1
    fi

    # Check for staged changes
    if ! check_staged_changes; then
        echo -e "${RED}Error: No staged changes found${NC}"
        echo -e "${YELLOW}Please stage your changes first with:${NC} git add <files>"
        show_status
        exit 1
    fi

    # Construct commit message
    local commit_message="$type($scope): $description"
    
    if [[ -n "$body" ]]; then
        commit_message="$commit_message

$body"
    fi

    # Show what will be committed
    echo -e "${BLUE}Staged Changes:${NC}"
    git diff --cached --name-status
    echo ""
    
    echo -e "${BLUE}Commit Message:${NC}"
    echo -e "${GREEN}$commit_message${NC}"
    echo ""

    # Confirm commit
    read -p "Proceed with commit? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        git commit -m "$commit_message"
        echo -e "${GREEN}✅ Commit successful!${NC}"
        
        # Show recent commits
        echo ""
        echo -e "${BLUE}Recent commits:${NC}"
        git log --oneline -5
    else
        echo -e "${YELLOW}Commit cancelled${NC}"
    fi
}

# Script entry point
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi