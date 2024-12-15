curl --location 'http://localhost:8900/api/v1/balance/command/withdraw' \
--header 'Content-Type: application/json' \
--data '{
    "id": 1,
    "amount": 209184
}'