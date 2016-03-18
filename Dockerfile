FROM maven:3.2-jdk-7-onbuild

WORKDIR /usr/src/app

RUN apt-get update
RUN apt-get install vim -y

CMD mvn exec:java -Dexec.mainClass='org.example.app.Main' -Dexec.args="cassandra"

