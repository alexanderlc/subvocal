#!/bin/sh
set -eo pipefail
#IFS=$'\n\t'

name=worker

docker run -t -i -d -p 8081:8081 --name $name subvocal/web:0.0.1 java -Dspring.profiles.active=worker -Dserver.port=8081 -jar web-0.0.1.jar

# get the docker container id for this name
container_id=$(docker ps -a | grep $name | cut -d ' ' -f1)
echo "started container id $container_id as $name"

docker logs -f --tail 50 $container_id