# Script to compile and run the project

# Assuming that
#   Cassandra host named "cassandra"
#   and RabbitMQ host called "rabbit"

# Works fine inside of Docker container executed by docker.sh

mvn package && \
mvn exec:java \
    -Dexec.mainClass="org.example.app.Main" -Dexec.args="cassandra rabbit"

