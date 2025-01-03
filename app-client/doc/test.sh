wrk -t120 -c3000 -d40s http://localhost:8080/transfer

ab -n 200000 -c 2000 -p body.txt -T application/json http://localhost:8900/api/v1/balance/command/deposit

ghz --insecure --skipFirst 1 --proto CepService.proto --call com.namnv.proto.BalanceCommandService/sendCommand -d "{\"depositCommand\": {\"id\": 1, \"amount\": 1, \"correlationId\": \"random-uuid\" }}" -c 200 -n 600000 localhost:9091
ghz --insecure --skipFirst 1 --proto GreeterGrpc.proto --call com.namnv.Greeter/SayHello  -c 200 -n 400000 localhost:8080


ghz --insecure --proto CepService.proto --call com.namnv.proto.BalanceCommandService/sendCommand -d "{\"depositCommand\": {\"id\": 1, \"amount\": 1, \"correlationId\": \"random-uuid\" }}" -c 10 -n 10000000 localhost:9091



curl --location --request POST http://localhost:8900/api/balance-benchmark/benchmark/1000000/64
