#!/bin/bash

# Script to run Cassandra migrations for docker container with Cassandra

# Receives optional positional argument with Cassandra container name,
# "cassandra" by default

CASSANDRA_CONTAINER="${1:-cassandra}"
SCRIPT="cqlsh -f /tmp/migrations.cql || rm /tmp/migrations.cql"
docker cp migrations.cql ${CASSANDRA_CONTAINER}:/tmp/
docker exec -it ${CASSANDRA_CONTAINER} sh -c "${SCRIPT}"

