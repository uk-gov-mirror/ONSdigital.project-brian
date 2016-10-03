FROM onsdigital/java-component

# Add the build artifacts
WORKDIR /usr/src
ADD ./target/*-jar-with-dependencies.jar /usr/src/target/

# Set the entry point
ENTRYPOINT java -Xmx4094m \
          -Drestolino.packageprefix=com.github.onsdigital.brian.api \
          -jar target/*-jar-with-dependencies.jar
