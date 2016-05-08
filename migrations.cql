CREATE KEYSPACE IF NOT EXISTS mykeyspace \
       WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 3};
CREATE TABLE mykeyspace.person (id uuid PRIMARY KEY, name varchar, age int);
CREATE INDEX ON mykeyspace.person (name);

