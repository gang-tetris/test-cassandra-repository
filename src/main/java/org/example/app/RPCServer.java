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

    private static int fib(int n) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        return fib(n - 1) + fib(n - 2);
    }

    private static void connect(Connection connection, Channel channel,
                                QueueingConsumer consumer) throws IOException,
            TimeoutException {
        channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);

        channel.basicQos(1);

        channel.basicConsume(RPC_QUEUE_NAME, false, consumer);

        System.out.println(" [x] Awaiting RPC requests");
    }

    private static void operate(String host) {
        Connection connection = null;
        Channel channel;
        QueueingConsumer consumer;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);

        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            consumer = new QueueingConsumer(channel);
            connect(connection, channel, consumer);
            mainLoop(connection, channel, consumer);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    private static void mainLoop(Connection connection, Channel channel,
                                 QueueingConsumer consumer) throws
            InterruptedException, IOException {
        while (true) {
            String response = null;

            QueueingConsumer.Delivery delivery = consumer.nextDelivery();

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
            } catch (Exception e) {
                System.out.println(" [.] " + e.toString());
                response = "";
            } finally {
                channel.basicPublish("", props.getReplyTo(), replyProps,
                        response != null ? response.getBytes("UTF-8") :
                                "Fail".getBytes("UTF-8"));

                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        }
    }

    public static void main(String[] argv) {
        operate("localhost");
    }
}
