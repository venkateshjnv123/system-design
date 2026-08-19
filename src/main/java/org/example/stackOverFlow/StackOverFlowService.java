package org.example.stackOverFlow;

import org.example.stackOverFlow.entities.Answer;
import org.example.stackOverFlow.entities.Question;
import org.example.stackOverFlow.entities.Tag;
import org.example.stackOverFlow.entities.User;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StackOverFlowService {
    final Map<String, User> currentUsers = new ConcurrentHashMap<>();
    final Map<String, Question> questionMap = new ConcurrentHashMap<>();
    final Map<String, Answer> answerMap = new ConcurrentHashMap<>();

    public User createUser(String name){
        User user = new User(name);
        currentUsers.put(user.getId(), user);
        return user;
    }

    public Question createQuestion(String title, String des, String userId, List<Tag> tags){
        if(title.isBlank() || des.isBlank()) {
            throw new RuntimeException("Title and des should not be blank");
        }
        User user = currentUsers.get(userId);
        Question question = new Question(title,des,tags,user);
        questionMap.put(question.getId(), question);
        return question;
    }

    public Answer createAnswer(String content, String userId, String questionId){
        if(content.isBlank() || userId.isBlank() || questionId.isBlank()) {
            throw new RuntimeException("content and userId and questionId should not be blank");
        }
        User user = currentUsers.get(userId);
        Question question = questionMap.get(questionId);
        Answer answer = new Answer(content, user);
        question.addAnswer(answer);
        return answer;
    }
}
