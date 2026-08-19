package org.example.stackOverFlow.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@ToString
public class Question {
    public String id;
    public String title;
    public String description;
    public List<Tag> tags;
    public List<Answer> answers;
    public User user;

    public Question(String title, String des, List<Tag> tags, User user){
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = des;
        this.tags = tags;
        this.user = user;
        this.answers = new ArrayList<>();
    }

    public void addAnswer(Answer answer){
        this.answers.add(answer);
    }
}
