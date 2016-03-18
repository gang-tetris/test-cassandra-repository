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

    public static void main(String[] args) {
        Repository repository;
        try {
            if (args.length == 0) {
                repository = new Repository(LOG);
            } else {
                repository = new Repository(LOG, args[0]);
            }
        } catch (UnknownHostException e) {
            LOG.info("Failed");
            e.printStackTrace();
            return;
        }
        repository.insert(new Person("0", "Natalya", 20));
        repository.insert(new Person("1", "Alex", 24));
        repository.select("0");
        repository.truncate();
        repository.close();
        LOG.info("Finished");
    }
}
