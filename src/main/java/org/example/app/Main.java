/**
 * Created by pti08 on 3/17/16.
 */
package org.example.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_HOSTNAME = "Repository";

    public static void main(String[] args) {
        Repository repository;
        RPCServer server;
        try {
            if (args.length == 0) {
                repository = new Repository(LOG);
            } else {
                repository = new Repository(LOG, args.length > 0 ? args[0] :
                        DEFAULT_HOST);
            }
        } catch (UnknownHostException e) {
            LOG.info("Failed");
            e.printStackTrace();
            return;
        }
        try {
            server = new RPCServer(
                         args.length > 1 ? args[1] : DEFAULT_HOST,
                         args.length > 2 ? args[2] : DEFAULT_HOSTNAME);
        } catch (IOException e) {
            LOG.info("Failed");
            e.printStackTrace();
            return;
        } catch (TimeoutException e) {
            LOG.info("Failed");
            e.printStackTrace();
            return;
        }
        server.run(repository);
        repository.close();
        LOG.info("Finished");
    }
}
