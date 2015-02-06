#!/bin/sh
set -eo pipefail
#IFS=$'\n\t'

name=backend

docker run -t -i -d -p 8080:8080 -p 2551:2551 -e "SPRING_PROFILE=backend" --name $name subvocal/web:0.0.1

# get the docker container id for this name
container_id=$(docker ps -a | grep $name | cut -d ' ' -f1)
echo "started container id $container_id as $name"

docker logs -f --tail 50 $container_id