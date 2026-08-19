package org.example.stackOverFlow.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
public class Answer {
    public String id;
    public String content;
    public User user;

    public Answer(String content, User user){
        this.id = UUID.randomUUID().toString();
        this.content = content;
        this.user = user;
    }
}
