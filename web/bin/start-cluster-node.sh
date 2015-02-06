#!/bin/bash
set -e

# Note this get ip address code only works for Centos
cluster_ip=$(ifconfig eth0 | grep 'inet ' | cut -d: -f2 | awk '{ print $2}')
echo "Got cluster_ip of $cluster_ip"

echo "Updating environment variables for application execution"
export CLUSTER_IP=$cluster_ip

echo 'start-cluster-node.sh entrypoint wrapper script'
echo 'node has following env:'
env

execCmd="java -Dspring.profiles.active=$SPRING_PROFILE -jar web-0.0.1.jar"
echo "executing: $execCmd"
exec $execCmd "$@"
