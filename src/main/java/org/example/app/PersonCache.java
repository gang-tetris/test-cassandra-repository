package org.example.app;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.util.Map;
import java.util.UUID;

public class PersonCache {
    private IMap<String, UUID> map;
    private HazelcastInstance instance;

    public PersonCache () {
        ClientConfig cfg = new ClientConfig();
        cfg.addAddress("hazelcast:5701");
        cfg.getGroupConfig().setName( "app1" ).setPassword( "app1-pass" );
        this.instance = HazelcastClient.newHazelcastClient(cfg);
        this.map = instance.getMap("person");
        this.map.put("Anonymous", new UUID(0, 0));
    }

    public boolean cachePerson (String name, UUID id) {
        System.out.println("Put " + name + ":" + id.toString() + "to cache");
        return this.map.putIfAbsent(name, id) == null;
    }

    public void forgetPerson (String name) {
        System.out.println("Removing " + name + " from cache");
        this.map.removeAsync(name);
    }
}

