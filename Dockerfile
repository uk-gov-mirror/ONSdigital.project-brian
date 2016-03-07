FROM onsdigital/java-component

# Add the build artifacts
WORKDIR /usr/src
ADD git_commit_id /usr/src/
ADD ./target/dependency/newrelic /usr/src/target/dependency/newrelic
ADD ./target/*-jar-with-dependencies.jar /usr/src/target/

# Set the entry point
ENTRYPOINT java -Xmx4094m \
          -Drestolino.packageprefix=com.github.onsdigital.brian.api \
          -jar target/*-jar-with-dependencies.jar
