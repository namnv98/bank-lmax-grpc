:: docker container prune -f && for /f "tokens=*" %i in ('docker volume ls -q') do docker volume rm %i

mvn clean install & docker-compose up --build
