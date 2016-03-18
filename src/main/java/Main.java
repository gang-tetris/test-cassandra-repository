/**
 * Created by pti08 on 3/17/16.
 */
package org.example.app;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private static Cluster cluster;
    private static Session session;

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                cluster = Cluster.builder().addContactPoints(InetAddress.getLocalHost()).build();
                //cluster = Cluster.builder().addContactPoints("::ffff:0.0.0.0").build();
            } else {
                cluster = Cluster.builder().addContactPoints(args[0]).build();
            }
            session = cluster.connect("mykeyspace");
            CassandraOperations cassandraOps = new CassandraTemplate(session);
            cassandraOps.insert(new Person("1234567890", "David", 40));
            Select s = QueryBuilder.select().from("person");
            s.where(QueryBuilder.eq("id", "1234567890"));
            LOG.info(cassandraOps.selectOne(s, Person.class).getId());
            cassandraOps.truncate("person");
            LOG.info("Truncated");
            session.close();
            cluster.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOG.info("Finished");
    }
}
