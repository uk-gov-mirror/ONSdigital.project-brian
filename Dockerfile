from java:7


# Clean image repository metadata
# http://serverfault.com/questions/690639/api-get-error-reading-from-server-under-docker
RUN \
  apt-get clean && \
  apt-get update && \
  apt-get install -y git maven


# Consul agent - /usr/local/bin
ADD https://dl.bintray.com/mitchellh/consul/0.5.2_linux_amd64.zip /tmp/0.5.2_linux_amd64.zip
WORKDIR /usr/local/bin
RUN unzip /tmp/0.5.2_linux_amd64.zip
WORKDIR /etc/consul.d
RUN echo '{"service": {"name": "brian", "tags": ["blue"], "port": 8080, "check": {"script": "curl http://localhost:8080 >/dev/null 2>&1", "interval": "10s"}}}' > brian.json


# Check out from Github
WORKDIR /usr/src
RUN git clone https://github.com/thomasridd/project-brian.git
WORKDIR project-brian
RUN git checkout develop


# Build Jar and copy dependencyes
RUN mvn clean install -DskipTests


# Expose port
EXPOSE 8080


# Restolino configuration
ENV PACKAGE_PREFIX=com.github.onsdigital.api


# Entrypoint script
RUN echo "#!/bin/bash" >> container.sh
## Disabled for now: RUN echo "consul agent -data-dir /tmp/consul -config-dir /etc/consul.d -join=dockerhost &" > container.sh
RUN echo "java $JAVA_OPTS \
          -Drestolino.packageprefix=$PACKAGE_PREFIX \
          -jar target/*-jar-with-dependencies.jar" >> container.sh
RUN chmod u+x container.sh


ENTRYPOINT ["./container.sh"]
