from carboni.io/java-component

# Consul
WORKDIR /etc/consul.d
RUN echo '{"service": {"name": "brian", "tags": ["blue"], "port": 8080, "check": {"script": "curl http://localhost:8080 >/dev/null 2>&1", "interval": "10s"}}}' > brian.json

# Add the built artifact
WORKDIR /usr/src/target
ADD ./target/*-jar-with-dependencies.jar /usr/src/target/

# Build jar-with-dependencies
#RUN mvn install -DskipTests

# Update the entry point script
RUN mv /usr/entrypoint/container.sh /usr/src/target/
ENV PACKAGE_PREFIX=com.github.onsdigital.api
RUN echo "java -Xmx4094m \
          -Drestolino.packageprefix=$PACKAGE_PREFIX \
          -jar *-jar-with-dependencies.jar" >> container.sh
