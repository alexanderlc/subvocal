# The docker image produced by the file is designed to be run configured by environment variables only.

FROM centos:latest

RUN yum -y update

# For ifconfig
RUN yum -y install net-tools

# Add/copy our scripts
COPY bin/start-cluster-node.sh /root/bin/start-cluster-node.sh
RUN chmod +x /root/bin/start-cluster-node.sh

# Java 7 SDK, -y to always answer yes to yum install
RUN yum -y install java-1.7.0-openjdk.x86_64

# Copy in latest deployable artefact (should/could come from build server)
ADD target/web-0.0.1.jar /usr/share/subvocal/web/web-0.0.1.jar

# Set the working directory for following RUN command
WORKDIR /usr/share/subvocal/web

# make the jar executable
RUN chmod +x web-0.0.1.jar

# Tell docker which ports the container will listen on (standard Tomcat)
EXPOSE 8009 8080

# Wrapper script to allow environment setup
ENTRYPOINT ["/root/bin/start-cluster-node.sh"]