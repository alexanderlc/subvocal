FROM centos:latest

# todo revert RUN yum update

# Java 7 SDK, -y to always answer yes to yum install
RUN yum -y install java-1.7.0-openjdk.x86_64

# Copy in latest deployable artefact (should/could come from build server)
ADD target/web-0.0.1.jar /usr/share/subvocal/web/web-0.0.1.jar

# Set the working directory for following RUN command
WORKDIR /usr/share/subvocal/web

# make the jar executable
RUN chmod a+x web-0.0.1.jar

# Tell docker which ports the container will listen on (standard Tomcat)
EXPOSE 8009 8080

# ENTRYPOINT java -jar web-0.0.1.jar