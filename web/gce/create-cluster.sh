#!/bin/sh
set -e

# Setup some environment variables
gcloud config set compute/zone europe-west1-b
gcloud config set container/cluster subvocal

# Dependent on the docker image existing in the google repository

# Setup a low spec cluster with a 6 nodes running.  Initially running etcd, kube2sky, skydns containers
# Pods created are allocated to a suitable node in the cluster, new nodes are not added
gcloud preview container clusters create subvocal --machine-type f1-micro --num-nodes 6

# Create services for each master/seed. Only one pod for each service, this for networking rather than load balancing
gcloud preview container services create --config-file config/master-seed1-service.json
gcloud preview container services create --config-file config/master-seed2-service.json

# Create two master/seed nodes
gcloud preview container pods create --config-file config/master-seed1-pod.json
gcloud preview container pods create --config-file config/master-seed2-pod.json

# Roll out the worker pods (microservices)
gcloud preview container pods create --config-file config/sentiment-pod.json

# Roll out the api frontend
# api - create a replication strategy of 2 api pods
gcloud preview container replicationcontrollers create --config-file config/api-replication-controller.json
# Load balancer the api pods using this service
gcloud preview container services create --config-file config/api-service.json

# Open the firewall for all our nodes on port 80 to allow incoming api connections
gcloud compute firewall-rules create subvocal-80 --allow=tcp:80 --target-tags k8s-subvocal-node

# Useful commands
# gcloud preview container pods list
# gcloud compute ssh VM_NAME
# gcloud preview container services list
# gcloud compute forwarding-rules list