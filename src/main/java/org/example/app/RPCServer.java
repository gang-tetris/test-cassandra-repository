/**
 * Created by pti08 on 3/18/16.
 */
package org.example.app;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RPCServer {
    private static final String RPC_QUEUE_NAME = "rpc_queue";
    private String host;
    private Connection connection;
    private Channel channel;
    private QueueingConsumer consumer;

    public RPCServer () throws IOException, TimeoutException {
        this.host = "localhost";
        this.connect();
    }

    public RPCServer (String host) throws IOException, TimeoutException {
        this.host = host;
        this.connect();
    }

    private int fib(int n) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        return fib(n - 1) + fib(n - 2);
    }

    private void connect() throws IOException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(this.host);

        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
        this.consumer = new QueueingConsumer(channel);

        channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);

        channel.basicQos(1);

        channel.basicConsume(RPC_QUEUE_NAME, false, consumer);

        System.out.println(" [x] Awaiting RPC requests");
    }

    public void run(Repository repository) {
        try {
            mainLoop(repository);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (this.connection != null) {
                try {
                    this.connection.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    private void mainLoop(Repository repository) throws
            InterruptedException, IOException {
        int id = 0;
        while (true) {
            String response = null;

            QueueingConsumer.Delivery delivery = this.consumer.nextDelivery();

            BasicProperties props = delivery.getProperties();
            BasicProperties replyProps = new BasicProperties
                    .Builder()
                    .correlationId(props.getCorrelationId())
                    .build();

            try {
                String message = new String(delivery.getBody(), "UTF-8");
                int n = Integer.parseInt(message);

                System.out.println(" [.] fib(" + message + ")");
                response = "" + fib(n);
                repository.insert(new Person(String.valueOf(id), response, 0));
                id++;
            } catch (Exception e) {
                System.out.println(" [.] " + e.toString());
                response = "";
            } finally {
                this.channel.basicPublish("", props.getReplyTo(), replyProps,
                        response != null ? response.getBytes("UTF-8") :
                                "Fail".getBytes("UTF-8"));

                this.channel.basicAck(delivery.getEnvelope().getDeliveryTag(),
                        false);
            }
        }
    }
}
