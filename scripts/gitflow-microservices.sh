#!/bin/bash

# Git Flow for Microservices - iFood Clone
# Manages feature branches with core services dependencies

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

# Core services that other services depend on
CORE_SERVICES=("config-server" "service-discovery" "api-gateway")

# Business services that can be developed in parallel
BUSINESS_SERVICES=("restaurant-service" "menu-service" "order-service" "payment-service" "delivery-service" "notification-service" "review-service" "user-service")

# Function to display help
show_help() {
    echo -e "${BLUE}🌊 Git Flow for Microservices${NC}"
    echo
    echo "Usage: $0 <command> [service] [options]"
    echo
    echo -e "${YELLOW}Commands:${NC}"
    echo "  init                     Initialize Git Flow (create develop branch)"
    echo "  feature start <service>  Start feature branch for a service"
    echo "  feature finish <service> Finish feature branch (merge to develop)"
    echo "  feature list            List all feature branches"
    echo "  release start <version> Start release branch"
    echo "  release finish <version> Finish release (merge to main)"
    echo "  hotfix start <name>     Start hotfix branch"
    echo "  hotfix finish <name>    Finish hotfix"
    echo "  status                  Show Git Flow status"
    echo "  core-sync               Sync core services to all feature branches"
    echo
    echo -e "${YELLOW}Business Services:${NC}"
    printf "  %s\n" "${BUSINESS_SERVICES[@]}"
    echo
    echo -e "${YELLOW}Core Services (auto-included):${NC}"
    printf "  %s\n" "${CORE_SERVICES[@]}"
    echo
    echo -e "${YELLOW}Examples:${NC}"
    echo "  $0 init"
    echo "  $0 feature start restaurant-service"
    echo "  $0 feature finish restaurant-service"
    echo "  $0 core-sync"
    echo "  $0 release start v1.0.0"
}

# Initialize Git Flow
init_gitflow() {
    echo -e "${BLUE}🚀 Initializing Git Flow for Microservices...${NC}"
    
    # Create develop branch if it doesn't exist
    if ! git show-ref --verify --quiet refs/heads/develop; then
        echo -e "${YELLOW}Creating develop branch...${NC}"
        git checkout -b develop
        git push -u origin develop
        echo -e "${GREEN}✅ Develop branch created${NC}"
    else
        echo -e "${GREEN}✅ Develop branch already exists${NC}"
    fi
    
    # Ensure we're on develop
    git checkout develop
    
    echo -e "${GREEN}🎉 Git Flow initialized successfully!${NC}"
}

# Start feature branch
start_feature() {
    local service=$1
    
    if [ -z "$service" ]; then
        echo -e "${RED}❌ Error: Service name required${NC}"
        echo "Usage: $0 feature start <service>"
        exit 1
    fi
    
    # Validate service name
    if [[ ! " ${BUSINESS_SERVICES[@]} " =~ " ${service} " ]]; then
        echo -e "${RED}❌ Error: Invalid service '${service}'${NC}"
        echo -e "${YELLOW}Valid services: ${BUSINESS_SERVICES[*]}${NC}"
        exit 1
    fi
    
    local feature_branch="feature/${service}"
    
    # Check if branch already exists
    if git show-ref --verify --quiet refs/heads/${feature_branch}; then
        echo -e "${YELLOW}⚠️  Feature branch '${feature_branch}' already exists${NC}"
        git checkout ${feature_branch}
        return
    fi
    
    echo -e "${BLUE}🌱 Starting feature branch: ${feature_branch}${NC}"
    
    # Ensure we're on latest develop
    git checkout develop
    git pull origin develop
    
    # Create feature branch
    git checkout -b ${feature_branch}
    
    echo -e "${GREEN}✅ Feature branch '${feature_branch}' created${NC}"
    echo -e "${CYAN}📝 You can now work on ${service} independently${NC}"
    echo -e "${YELLOW}💡 Core services are automatically available${NC}"
    
    # Push feature branch
    git push -u origin ${feature_branch}
}

# Finish feature branch
finish_feature() {
    local service=$1
    
    if [ -z "$service" ]; then
        echo -e "${RED}❌ Error: Service name required${NC}"
        echo "Usage: $0 feature finish <service>"
        exit 1
    fi
    
    local feature_branch="feature/${service}"
    
    # Check if branch exists
    if ! git show-ref --verify --quiet refs/heads/${feature_branch}; then
        echo -e "${RED}❌ Error: Feature branch '${feature_branch}' does not exist${NC}"
        exit 1
    fi
    
    echo -e "${BLUE}🔄 Finishing feature branch: ${feature_branch}${NC}"
    
    # Switch to feature branch and ensure it's up to date
    git checkout ${feature_branch}
    git pull origin ${feature_branch}
    
    # Switch to develop and update
    git checkout develop
    git pull origin develop
    
    # Merge feature branch
    echo -e "${YELLOW}🔀 Merging ${feature_branch} into develop...${NC}"
    git merge --no-ff ${feature_branch} -m "feat(${service}): merge feature branch ${feature_branch}"
    
    # Push develop
    git push origin develop
    
    # Delete feature branch
    echo -e "${YELLOW}🗑️  Cleaning up feature branch...${NC}"
    git branch -d ${feature_branch}
    git push origin --delete ${feature_branch}
    
    echo -e "${GREEN}✅ Feature '${service}' merged successfully!${NC}"
    echo -e "${CYAN}📋 Feature is now integrated in develop branch${NC}"
}

# List feature branches
list_features() {
    echo -e "${BLUE}🌿 Active Feature Branches:${NC}"
    git branch -r | grep "origin/feature/" | sed 's/origin\///' | while read branch; do
        service=$(echo $branch | cut -d'/' -f2)
        echo -e "  ${GREEN}${branch}${NC} (${service})"
    done
}

# Sync core services to all feature branches
sync_core_services() {
    echo -e "${BLUE}🔄 Syncing core services to all feature branches...${NC}"
    
    # Get current branch
    local current_branch=$(git branch --show-current)
    
    # Get all feature branches
    local feature_branches=$(git branch -r | grep "origin/feature/" | sed 's/origin\///' | tr -d ' ')
    
    if [ -z "$feature_branches" ]; then
        echo -e "${YELLOW}⚠️  No feature branches found${NC}"
        return
    fi
    
    # Switch to develop to get latest core services
    git checkout develop
    git pull origin develop
    
    echo "$feature_branches" | while read branch; do
        if [ -n "$branch" ]; then
            echo -e "${CYAN}📦 Syncing core services to ${branch}...${NC}"
            git checkout $branch
            git pull origin $branch
            
            # Merge develop (contains core services updates)
            git merge develop --no-ff -m "chore: sync core services from develop"
            
            # Push updated feature branch
            git push origin $branch
        fi
    done
    
    # Return to original branch
    git checkout $current_branch
    echo -e "${GREEN}✅ Core services synced to all feature branches${NC}"
}

# Start release
start_release() {
    local version=$1
    
    if [ -z "$version" ]; then
        echo -e "${RED}❌ Error: Version required${NC}"
        echo "Usage: $0 release start <version>"
        exit 1
    fi
    
    local release_branch="release/${version}"
    
    echo -e "${BLUE}🚢 Starting release: ${release_branch}${NC}"
    
    # Ensure develop is up to date
    git checkout develop
    git pull origin develop
    
    # Create release branch
    git checkout -b ${release_branch}
    git push -u origin ${release_branch}
    
    echo -e "${GREEN}✅ Release branch '${release_branch}' created${NC}"
    echo -e "${CYAN}📋 Ready for release testing and final preparations${NC}"
}

# Finish release
finish_release() {
    local version=$1
    
    if [ -z "$version" ]; then
        echo -e "${RED}❌ Error: Version required${NC}"
        echo "Usage: $0 release finish <version>"
        exit 1
    fi
    
    local release_branch="release/${version}"
    
    echo -e "${BLUE}🎉 Finishing release: ${release_branch}${NC}"
    
    # Merge to main
    git checkout main
    git pull origin main
    git merge --no-ff ${release_branch} -m "release: ${version}"
    git tag -a "v${version}" -m "Release version ${version}"
    git push origin main --tags
    
    # Merge back to develop
    git checkout develop
    git merge --no-ff ${release_branch} -m "chore: merge release ${version} back to develop"
    git push origin develop
    
    # Clean up release branch
    git branch -d ${release_branch}
    git push origin --delete ${release_branch}
    
    echo -e "${GREEN}✅ Release ${version} finished successfully!${NC}"
}

# Show Git Flow status
show_status() {
    echo -e "${BLUE}📊 Git Flow Status${NC}"
    echo
    
    # Current branch
    local current_branch=$(git branch --show-current)
    echo -e "${YELLOW}Current branch:${NC} ${GREEN}${current_branch}${NC}"
    echo
    
    # Feature branches
    echo -e "${YELLOW}Feature branches:${NC}"
    git branch -r | grep "origin/feature/" | sed 's/origin\///' | while read branch; do
        echo -e "  ${GREEN}${branch}${NC}"
    done
    echo
    
    # Release branches
    echo -e "${YELLOW}Release branches:${NC}"
    git branch -r | grep "origin/release/" | sed 's/origin\///' | while read branch; do
        echo -e "  ${PURPLE}${branch}${NC}"
    done
    echo
    
    # Recent commits
    echo -e "${YELLOW}Recent commits:${NC}"
    git log --oneline -5
}

# Main script logic
case "$1" in
    "init")
        init_gitflow
        ;;
    "feature")
        case "$2" in
            "start")
                start_feature "$3"
                ;;
            "finish")
                finish_feature "$3"
                ;;
            "list")
                list_features
                ;;
            *)
                echo -e "${RED}❌ Error: Unknown feature command '$2'${NC}"
                show_help
                exit 1
                ;;
        esac
        ;;
    "release")
        case "$2" in
            "start")
                start_release "$3"
                ;;
            "finish")
                finish_release "$3"
                ;;
            *)
                echo -e "${RED}❌ Error: Unknown release command '$2'${NC}"
                show_help
                exit 1
                ;;
        esac
        ;;
    "hotfix")
        case "$2" in
            "start")
                echo -e "${YELLOW}🚧 Hotfix functionality coming soon${NC}"
                ;;
            "finish")
                echo -e "${YELLOW}🚧 Hotfix functionality coming soon${NC}"
                ;;
            *)
                echo -e "${RED}❌ Error: Unknown hotfix command '$2'${NC}"
                show_help
                exit 1
                ;;
        esac
        ;;
    "core-sync")
        sync_core_services
        ;;
    "status")
        show_status
        ;;
    *)
        show_help
        exit 1
        ;;
esac