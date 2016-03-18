package org.example.app;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import org.slf4j.Logger;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by pti08 on 3/18/16.
 */
public class Repository {
    private Logger log;
    private static Cluster cluster;
    private static Session session;
    private static CassandraOperations cassandraOps;

    public Repository (Logger log) throws UnknownHostException {
        cluster = Cluster.builder().addContactPoints(InetAddress.getLocalHost()).build();
        //cluster = Cluster.builder().addContactPoints("::ffff:0.0.0.0").build();
        this.init(log);
    }

    public Repository (Logger log, String host) {
        cluster = Cluster.builder().addContactPoints(host).build();
        this.init(log);
    }

    public void init(Logger log) {
        this.log = log;
        session = cluster.connect("mykeyspace");
        cassandraOps = new CassandraTemplate(session);
    }

    public void close() {
        session.close();
        cluster.close();
        log.info("Closed");
    }

    public void truncate() {
        cassandraOps.truncate("person");
        log.info("Truncated");
    }

    public void insert (Person p) {
        cassandraOps.insert(p);
        log.info("Inserted person " + p);
    }

    public Person select (String id) {
        Select s = QueryBuilder.select().from("person");
        s.where(QueryBuilder.eq("id", id));
        Person p = cassandraOps.selectOne(s, Person.class);
        log.info("Found person " + p);
        return p;
    }
}
