#!/bin/bash

until netcat -z -w 2 rabbit 5672; do sleep 1; done

HAZELCAST_URL="http://hazelcast:5701/hazelcast/rest/management/cluster/state"
until curl --data "app1&app1-pass" "${HAZELCAST_URL}"
do
    sleep 1
done

until wget --spider "cassandra:9042"; do sleep 1; done

java -cp target/maven-example-1.0-SNAPSHOT.jar org.example.app.Main \
         cassandra rabbit $(hostname)

