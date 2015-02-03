#!/bin/sh

# todo, work out how to connect the workers to the master.  Also the public web service is not working

# Setup some environment variables
gcloud config set compute/zone <your-cluster-zone>
gcloud config set container/cluster <your-cluster-name>
GCP_PROJECT=subvocal-01
GCR_NAMESPACE=${GCP_PROJECT//-/_}

docker tag subvocal/web:0.0.1 gcr.io/subvocal_01/subvocal-web

# Push your image
gcloud preview docker push gcr.io/subvocal_01/subvocal-web

    Pushing tag for rev [9b0daa132b35] on {https://gcr.io/v1/repositories/subvocal_01/subvocal-web/tags/latest}

gcloud preview container clusters create subvocal

# gcloud preview container pods list
# gcloud compute ssh VM_NAME

gcloud preview container services create \
    --config-file config/akka-master-service.json

gcloud preview container pods create \
    --config-file config/akka-master-pod.json

gcloud preview container pods create \
    --config-file config/akka-worker-pod.json

# gcloud preview container services list

gcloud preview container replicationcontrollers create \
    --config-file config/akka-worker-controller.json

# gcloud preview container pods list

gcloud compute firewall-rules create frontend-node-8080 --allow=tcp:8080 \
    --target-tags k8s-frontend-node

# gcloud compute forwarding-rules list