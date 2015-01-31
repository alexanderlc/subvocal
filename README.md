subvocal
========

# Build locally and run

```
mvn clean package && java -jar target/web-0.0.1.jar
```

## Using Docker

To build the docker image:

```
docker build -t subvocal/web:0.0.1 .
```

Confirm image was added to local repository:

```
docker images
```

To run this container in the background:

```
docker run -t -i -d -p 8080:8080 --name backend subvocal/web:0.0.1 java -Dspring.profiles.active=backend -jar web-0.0.1.jar
docker run -t -i -d -p 8081:8081 --name worker subvocal/web:0.0.1 java -Dspring.profiles.active=worker -Dserver.port=8081 -jar web-0.0.1.jar
```

Confirm the container is running:

```
docker ps
```

Review the container logs to confirm correct application startup:

```
docker logs -f --tail 100 <container-id>
```

Review full details of the container:

```
docker inspect <container-id>
```

The application should be now running at

http://192.168.59.103:8080/

# Spring integration

- Manage the app, and the spring mvn REST API
- Allow injection of spring managed services into key actors.  In particular the worker actors

# Todo
- seed nodes and remove manual cluster joining
- Join regular nodel to seeds via Docker container linking and using jvm params to pass in seed node details
- Setup local development environment with Docker allowing IDE debugging
- Split components into separate containers and link then together to simplify networking config https://docs.docker.com/userguide/dockerlinks/
- Tidy up akka configs, and injection of work executors
- Tidy up response to create Sentiment (Ack & tell not ask for result)
- Get sentiment summary as a worker task
- Frontend app to use the API
- Docker install for API instance, Worker system
- Add test support
- Use validation annotations
- Call system actor shutdown and terminate when the spring context is shutting down

# Done
- Add create sentiment as a worker task
- revert to a multiple actor system context
- workers should only register for work they can complete

# Acknowledgements

- Akka integration based on http://typesafe.com/activator/template/akka-java-spring
- Distributed workers based on https://github.com/typesafehub/activator-akka-distributed-workers-java
