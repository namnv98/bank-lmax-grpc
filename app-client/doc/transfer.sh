curl --location 'http://localhost:8900/api/v1/balance/command/transfer' \
--header 'Content-Type: application/json' \
--data '{
    "fromId": 1,
    "toId": 2,
    "amount": 1
}'