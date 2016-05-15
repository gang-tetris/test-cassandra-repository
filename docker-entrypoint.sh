#!/bin/bash

until netcat -z -w 2 rabbit 5672; do sleep 1; done

HAZELCAST_URL="http://hazelcast:5701/hazelcast/rest/management/cluster/state"
until curl --data "app1&app1-pass" "${HAZELCAST_URL}"
do
    sleep 1
done

until wget --spider "cassandra:9042"; do sleep 1; done

mvn exec:java -Dexec.mainClass="org.example.app.Main" -Dexec.args="cassandra rabbit"

