subvocal
========

# Build and run

```
mvn clean package && java -jar target/web-0.0.1.jar
```

# Spring integration

- Manage the app, and the spring mvn REST API
- Allow injection of spring managed services into key actors.  In particular the worker actors

# Todo
- Tidy up akka configs, and injection of work executors
- Tidy up response to create Sentiment (Ack & tell not ask for result)
- Get sentiment summary as a worker task
- Frontend app to use the API
- seed nodes and remove manual cluster joining
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
