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
    private static final String RPC_QUEUE_NAME = "java_queue";
    private String host;
    private Connection connection;
    private Channel channel;
    private QueueingConsumer consumer;
    private Repository repository;
    private PersonCache personCache;

    public RPCServer () throws IOException, TimeoutException {
        this.host = "localhost";
        this.connect();
        this.personCache = new PersonCache();
    }

    public RPCServer (String host) throws IOException, TimeoutException {
        this.host = host;
        this.connect();
        this.personCache = new PersonCache();
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
        this.repository = repository;
        try {
            mainLoop();
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

    private void mainLoop() throws InterruptedException, IOException {
        while (true) {
            this.processDelivery();
        }
    }

    private void processDelivery () throws InterruptedException, IOException {
        JSONParser json = new JSONParser();
        byte [] fail_b = "{success: \"false\"}".getBytes("UTF-8");
        String response = null;
        QueueingConsumer.Delivery delivery = this.consumer.nextDelivery();
        long deliveryTag = delivery.getEnvelope().getDeliveryTag();
        BasicProperties props = delivery.getProperties();
        String correlationId = props.getCorrelationId();
        String routingKey = props.getReplyTo();
        BasicProperties replyProps = new BasicProperties.Builder()
                                         .correlationId(correlationId)
                                         .build();

        try {
            String message = new String(delivery.getBody(), "UTF-8");
            JSONObject msg = (JSONObject)json.parse(message);
            System.out.println(" [.] Got message " + msg.toJSONString());
            response = this.handleQuery(msg);
        } catch (Exception e) {
            System.out.println(" [.] " + e.toString());
        } finally {
            byte [] response_b = response.getBytes("UTF-8");
            byte[] body = (response != null) ?  response_b : fail_b;

            this.channel.basicPublish("", routingKey, replyProps, body);
            this.channel.basicAck(deliveryTag, false);
        }
    }

    private String handleQuery(JSONObject msg) {
        JSONObject response = null;
        String operation = msg.get("op").toString();
        if (operation.equals("insert")) {
            response = this.handleInsert(msg);
        }
        else if (operation.equals("select")) {
            response = this.handleSelect(msg);
        }
        else {
            response = new JSONObject();
            response.put("success", false);
        }
        return response.toString();
    }

    private JSONObject handleSelect(JSONObject msg) {
        JSONObject response = new JSONObject();
        JSONObject po = new JSONObject();
        String personName = msg.get("name").toString();
        Person p = this.repository.find(personName);

        if (p == null) {
            JSONObject error = new JSONObject();
            error.put("msg", "Person not found");
            error.put("code", 404);

            response.put("success", false);
            response.put("error", error);
        }
        else {
            response.put("person", p.toJSON());
            response.put("success", true);
        }
        return response;
    }

    private JSONObject handleInsert(JSONObject msg) {
        JSONObject response = new JSONObject();
        String personName = msg.get("name").toString();
        int personAge = Integer.parseInt(msg.get("age").toString());
        Person p = new Person(personName, personAge);
        boolean personCached = !this.personCache.cachePerson(p.getName(), p.getId());

        if (personCached || this.repository.find(personName) != null) {
            System.out.println("Exists");
            if (!personCached) {
                this.personCache.forgetPerson(p.getName());
            }
            JSONObject error = new JSONObject();
            error.put("msg", "Person already exists");
            error.put("code", 402);

            response.put("success", false);
            response.put("error", error);
        }
        else {
            response.put("person", this.repository.insert(p).toJSON());
            response.put("success", true);
            this.personCache.forgetPerson(p.getName());
        }
        return response;
    }
}
