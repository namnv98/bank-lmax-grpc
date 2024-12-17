wrk -t120 -c3000 -d40s -s post_request.lua http://localhost:8900/api/v1/balance/command/deposit

ab -n 200000 -c 2000 -p body.txt -T application/json http://localhost:8900/api/v1/balance/command/deposit

ghz --insecure --skipFirst 1 --proto CepService.proto --call com.namnv.proto.BalanceCommandService/sendCommand -d "{\"depositCommand\": {\"id\": 1, \"amount\": 1, \"correlationId\": \"random-uuid\" }}" -c 200 -n 600000 localhost:9091
