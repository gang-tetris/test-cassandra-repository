CREATE KEYSPACE mykeyspace WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 3};
CREATE TABLE mykeyspace.person(id varchar PRIMARY KEY , name varchar, age int);

