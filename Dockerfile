FROM maven:3

WORKDIR /usr/src/app

RUN apt-get update
RUN apt-get install vim curl wget -y

COPY ./docker-entrypoint.sh /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]

