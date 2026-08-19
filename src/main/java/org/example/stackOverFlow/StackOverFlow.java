package org.example.stackOverFlow;

import org.example.stackOverFlow.entities.Answer;
import org.example.stackOverFlow.entities.Question;
import org.example.stackOverFlow.entities.Tag;
import org.example.stackOverFlow.entities.User;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StackOverFlow {
    public static void main(String[] args) {
        StackOverFlowService stackOverFlowService = new StackOverFlowService();
        User user1 = stackOverFlowService.createUser("venky");
        System.out.println(user1);
        User user2 = stackOverFlowService.createUser("syam");
        System.out.println(user2);
        User user3 = stackOverFlowService.createUser("priya");
        System.out.println(user2);

        Tag tag = new Tag("water");
        Tag tag2 = new Tag("blue");

        Question question = stackOverFlowService.createQuestion("What is water?", "Lets discuss is water",user1.getId(), List.of(tag));
        System.out.println(question);

        Answer answer = stackOverFlowService.createAnswer("water is blue", user2.getId(), question.getId());
        System.out.println(answer);
        System.out.println(question);
    }
}
