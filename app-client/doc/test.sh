wrk -t120 -c3000 -d40s -s post_request.lua http://localhost:8900/api/v1/balance/command/deposit

#wrk -t6 -c120 -d30s -s post_request.lua http://localhost:8900/api/v1/balance/command/deposit
