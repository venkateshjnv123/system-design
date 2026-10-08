package org.example.taskScheduler;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
public class Task {
    private int id;
    private int priority;
    private String name;
    private MetaData metaData;
    private Long ttl;
    private TaskState state;
    private Long createdAt;

    static class MetaData{
        private String details;
        // LocalDateTime time;

        MetaData(String details){
            this.details = details;
            // this.time = time;
        }
    }

    Task(int id, int priority, MetaData metaData, TaskState state, String name, Long ttl){
        this.id = id;
        this.priority = priority;
        this.metaData = metaData;
        this.state = state;
        this.name = name;
        this.ttl = ttl;
        this.createdAt = System.currentTimeMillis();
    }

    enum TaskState {
        QUEUED,
        REMOVED,
        EXECUTED,
        EXPIRED
    }
}
