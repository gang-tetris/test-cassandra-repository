/**
 * Created by pti08 on 3/17/16.
 */
package org.example.app;

import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;
import org.json.simple.JSONObject;
import java.util.UUID;

@Table
public class Person {

    @PrimaryKey
    private UUID id;

    private String name;
    private int age;

    public Person(String name, int age) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.age = age;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    @Override
    public String toString() {
        return "Person [id=" + id + ", name=" + name + ", age=" + age + "]";
    }

    public JSONObject toJSON () {
        JSONObject result = new JSONObject();
        result.put("id", this.id.toString());
        result.put("name", this.name);
        result.put("age", this.age);
        return result;
    }

}
