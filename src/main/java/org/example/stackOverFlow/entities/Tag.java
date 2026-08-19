package org.example.stackOverFlow.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Tag {
    public String id;
    public String name;

    public Tag(String name){
        this.id = UUID.randomUUID().toString();
        this.name = name;
    }
}
