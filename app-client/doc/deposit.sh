curl --location 'http://localhost:8900/api/v1/balance/command/deposit' \
--header 'Content-Type: application/json' \
--data '{
    "id": 1,
    "amount": 100
}'