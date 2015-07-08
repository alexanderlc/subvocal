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

Start neo4j locally:

```
docker run -i -t -d --name neo4j --cap-add=SYS_RESOURCE -p 7474:7474 tpires/neo4j
```

http://192.168.59.103:7474/

### Trouble shooting
DNS for docker daemon, to get yum to resolve mirrors during docker build process.
```
boot2docker ssh

Add the following to /etc/resolv.conf:

# Google DNS
```
nameserver 8.8.8.8
nameserver 8.8.4.4
```

# Spring integration

- Manage the app, and the spring mvn REST API
- Allow injection of spring managed services into key actors.  In particular the worker actors

# Todo
- Remove necessity for master service to know about the different types of messages.  It should be need a new build when
new functionality is added.
- Review how to operate as an event bus, rather than a producer-consumer queues (http://doc.akka.io/docs/akka/snapshot/scala/event-bus.html),
Perhaps the topic notification should be a secondary event?
- Setup DNS
- Setup SSL
- API & Service versions and zero downtime deployments
- Setup local development environment with Docker allowing IDE debugging
- Tidy up akka configs, and injection of work executors
- Tidy up response to create Sentiment (Ack & tell not ask for result)
- Get sentiment summary as a worker task
- Frontend app to use the API
- Monitor endpoints for services and versions
- Add test support
- Use validation annotations
- Call system actor shutdown and terminate when the spring context is shutting down

# Done
- Expose GCE environment to world
- Google Container Engine deployment
- Docker builds, services to be run in separate containers
- Select node functionality/service by spring profile
- seed nodes and remove manual cluster joining
- Add create sentiment as a worker task
- revert to a multiple actor system context
- workers should only register for work they can complete

# Acknowledgements

- Akka integration based on http://typesafe.com/activator/template/akka-java-spring
- Distributed workers based on https://github.com/typesafehub/activator-akka-distributed-workers-java
- https://cloud.google.com/container-engine/docs/clusters/?hl=en_US
- Example of Google Container Engine deployment https://cloud.google.com/container-engine/docs/guestbook
- http://docs.spring.io/autorepo/docs/spring-data-neo4j/3.3.0.M1/reference/html/
