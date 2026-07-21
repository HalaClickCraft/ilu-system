#!/bin/bash
# Formation Tracking System - Integration Test Script
# This script tests all major endpoints after deployment

API_BASE_URL="http://localhost:8080/api"
JWT_TOKEN="${1:-}"  # JWT token should be passed as first argument

if [ -z "$JWT_TOKEN" ]; then
    echo "Usage: $0 <JWT_TOKEN>"
    echo "Example: $0 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...'"
    exit 1
fi

HEADERS="-H 'Authorization: Bearer $JWT_TOKEN' -H 'Content-Type: application/json'"

echo "🧪 Formation Tracking System - Integration Tests"
echo "=================================================="
echo ""

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

passed=0
failed=0

# Test function
run_test() {
    local test_name="$1"
    local method="$2"
    local endpoint="$3"
    local data="$4"
    local expected_code="$5"
    
    echo -n "Testing: $test_name... "
    
    if [ "$method" = "POST" ] || [ "$method" = "PUT" ]; then
        response=$(curl -s -w "\n%{http_code}" -X $method \
            -H "Authorization: Bearer $JWT_TOKEN" \
            -H "Content-Type: application/json" \
            -d "$data" \
            "$API_BASE_URL$endpoint")
    else
        response=$(curl -s -w "\n%{http_code}" -X $method \
            -H "Authorization: Bearer $JWT_TOKEN" \
            "$API_BASE_URL$endpoint")
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n-1)
    
    if [ "$http_code" = "$expected_code" ]; then
        echo -e "${GREEN}✓ PASS${NC} (HTTP $http_code)"
        ((passed++))
    else
        echo -e "${RED}✗ FAIL${NC} (Expected HTTP $expected_code, got $http_code)"
        echo "Response: $body"
        ((failed++))
    fi
}

echo "--- Formation Template Tests ---"
run_test "Create Formation Template" "POST" "/formations/templates" \
    '{"posteId": 1, "cadenceObjectif": 100, "qualiteObjectifTexte": "< 7 defects in 12 days"}' \
    "200"

run_test "Get Template by Poste ID" "GET" "/formations/templates/1" \
    "" \
    "200"

run_test "Get All Templates" "GET" "/formations/templates" \
    "" \
    "200"

echo ""
echo "--- Formation Tracking Tests ---"
run_test "Get Formation Details" "GET" "/formations/1/details" \
    "" \
    "200"

run_test "Get Chart Data" "GET" "/formations/1/chart-data" \
    "" \
    "200"

run_test "Record Daily Tracking - Day 1" "PUT" "/formations/1/daily/1" \
    '{"cadenceRealisee": 90, "nbDefauts": 2, "remarques": "Test entry"}' \
    "200"

run_test "Record Daily Tracking - Day 2" "PUT" "/formations/1/daily/2" \
    '{"cadenceRealisee": 95, "nbDefauts": 1, "remarques": "Improving"}' \
    "200"

echo ""
echo "--- Authorization Tests ---"
run_test "Get Team Trainings (with auth)" "GET" "/formations/mon-equipe" \
    "" \
    "200"

run_test "Get Training Statistics" "GET" "/formations/stats" \
    "" \
    "200"

echo ""
echo "--- Error Handling Tests ---"
run_test "Get Non-existent Template" "GET" "/formations/templates/99999" \
    "" \
    "404"

run_test "Get Non-existent Formation" "GET" "/formations/99999/details" \
    "" \
    "404"

run_test "Invalid Daily Entry (bad data)" "PUT" "/formations/1/daily/999" \
    '{"cadenceRealisee": "invalid", "nbDefauts": 0}' \
    "400"

echo ""
echo "=================================================="
echo -e "Test Results: ${GREEN}Passed: $passed${NC}, ${RED}Failed: $failed${NC}"
echo "=================================================="

if [ $failed -eq 0 ]; then
    echo -e "${GREEN}✓ All tests passed!${NC}"
    exit 0
else
    echo -e "${RED}✗ Some tests failed${NC}"
    exit 1
fi
