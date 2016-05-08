/**
 * Created by pti08 on 3/18/16.
 */
package org.example.app;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

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
                JSONObject obj = (JSONObject) (new JSONParser()).parse(message);
                System.out.println(" [.] Got message " + obj.toJSONString());
                response = "Inserted " + message;

                repository.insert(new Person(obj.get("name").toString(), Integer.parseInt(obj.get("age").toString())));
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
