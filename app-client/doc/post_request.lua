wrk.method = "POST"
wrk.headers["Authorization"] = "Bearer <token>"
wrk.headers["content-type"] = "application/json"
wrk.body = '{"id": 1, "amount": 1 }'
