CREATE KEYSPACE IF NOT EXISTS mykeyspace WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 40};
CREATE TABLE mykeyspace.person (id uuid , name varchar, age int,
                                PRIMARY KEY (id, name));

