#!/bin/sh
set -eo pipefail
#IFS=$'\n\t'

name=worker

# todo make the AKKA_MASTER_SERVICE_HOST value a parameter to this script
docker run -t -i -d -p 8081:8080 -e "SPRING_PROFILE=worker" -e "AKKA_MASTER_SERVICE_HOST=172.17.0.28" --name $name subvocal/web:0.0.1

# get the docker container id for this name
container_id=$(docker ps -a | grep $name | cut -d ' ' -f1)
echo "started container id $container_id as $name"

docker logs -f --tail 50 $container_id