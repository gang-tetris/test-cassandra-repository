#!/bin/bash

until docker exec -it rabbit rabbitctl status
do
    sleep 1
done

HAZELCAST_URL="http://0.0.0.0:5701/hazelcast/rest/management/cluster/state"
until curl --data "app1&app1-pass" "${HAZELCAST_URL}"
do
    sleep 1
done

until docker exec cassandra cqlsh -e quit
do
    sleep 1
done

mvn exec:java -Dexec.mainClass='org.example.app.Main' -Dexec.args="cassandra rabbit"

