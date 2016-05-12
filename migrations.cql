CREATE KEYSPACE IF NOT EXISTS mykeyspace
       WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 3};

USE mykeyspace;
CREATE TABLE person (id uuid PRIMARY KEY, name varchar, age int);
CREATE INDEX ON person (name);
INSERT INTO person(id, age, name) VALUES (00000000-0000-0000-0000-000000000000,
                                          0, 'Anonymous');

