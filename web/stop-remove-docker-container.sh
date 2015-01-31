#!/bin/sh
set -eo pipefail
#IFS=$'\n\t'

function usage {
    echo "
          This script stops and removes a docker container. Specify the container name as a command line argument.
          "
    exit 1
}

if [ -z $1  ]
then
    usage
fi

name=$1

# get the docker container id for this name
container_id=$(docker ps -a | grep $name | cut -d ' ' -f1)
echo "found container id $container_id for $name"
echo "stopped container:" $(docker stop $container_id)
echo "removed container:" $(docker rm $container_id)