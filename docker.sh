# Script to run Docker image
# Assuming that
#   you have running Cassandra image with name "cassandra"
#   you have running RabbitMQ image with name "rabbit"

# This script will copy your curreent directory with source code
# to /src/ folder inside of Docker with Java 8 and Maven 3

# Code can be compiled and run via rerun.sh script inside of Docker container

docker run --rm -it -v $(pwd):/src/ \
           --link cassandra:cassandra --link rabbit:rabbit \
           --link hazelcast:hazelcast \
           -it maven:3 sh -c "cd /src/ && bash"

