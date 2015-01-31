#!/bin/sh

mvn clean package -DskipTests

docker build -t subvocal/web:0.0.1 .