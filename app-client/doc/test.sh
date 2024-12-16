wrk -t120 -c3000 -d40s -s post_request.lua http://localhost:8900/api/v1/balance/command/deposit

#wrk -t6 -c120 -d30s -s post_request.lua http://localhost:8900/api/v1/balance/command/deposit



ab -n 200000 -c 2000 -p body.txt -T application/json http://localhost:8900/api/v1/balance/command/deposit


ab -n 1000 -c 50 http://localhost:9091/your_grpc_endpoint

ghz --insecure --proto CepService.proto --call com.namnv.proto.BalanceCommandService/sendCommand -d "{\"depositCommand\": {\"id\": 1, \"amount\": 1 }}" -c 2000 -n 500000 localhost:9091
ghz --insecure --skipFirst 100000 --proto CepService.proto --call com.namnv.proto.BalanceCommandService/sendCommand -d "{\"depositCommand\": {\"id\": 1, \"amount\": 1, \"correlationId\": \"random-uuid\" }}" -c 200 -n 600000 localhost:9091
