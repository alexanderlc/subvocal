#!/bin/sh
set -e

mvn clean package -DskipTests

docker build -t subvocal/web:0.0.1 .

# tag in format for pushing to google cloud registry
docker tag -f subvocal/web:0.0.1 gcr.io/subvocal_01/subvocal-web

# todo make this pub to the google container repository optional
gcloud preview docker push gcr.io/subvocal_01/subvocal-web