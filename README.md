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

- seed nodes and remove manual cluster joining
- Use validation annotations
- Call system actor shutdown and terminate when the spring context is shutting down

# Acknowledgements

- Akka integration based on http://typesafe.com/activator/template/akka-java-spring
- Distributed workers based on https://github.com/typesafehub/activator-akka-distributed-workers-java
