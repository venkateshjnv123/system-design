package org.example.taskScheduler;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskService {
        Map<Integer, Task> taskMap = new HashMap<>();
        PriorityQueue<Task> pq = new PriorityQueue<>((a, b) -> b.getPriority() - a.getPriority());
        AtomicInteger taskCounter = new AtomicInteger(0);

        // Treeset can be used to optimise the time complexity of remove operation
//        TreeSet<Task> pq = new TreeSet<>(
//            Comparator.comparingInt(Task::getPriority).reversed()
//                .thenComparingInt(Task::getId) // tie-breaker to avoid equals collision
//        );

        Task createTaskHelper(int priority, String details, String name, Long ttl){
            Task.MetaData meta = new Task.MetaData(details);

            return new Task(
                taskCounter.incrementAndGet(),
                priority,
                meta,
                Task.TaskState.QUEUED,
                name,
                ttl
            );
        }

        synchronized Task addTask(String name, int pr, String de, Long ttl){
            Task task = createTaskHelper(pr, de, name, ttl);

            taskMap.put(task.getId(), task);
            pq.add(task);
            return task;
        }


        synchronized void modifyTask(int id, int pr){
            if(!taskMap.containsKey(id)) {
                throw new IllegalArgumentException("Task not found");
            }

            Task task = taskMap.get(id);

            if(!Task.TaskState.QUEUED.equals(task.getState())) {
                throw new RuntimeException("Modification not allowed");
            }

            pq.remove(task);
            task.setPriority(pr);
            pq.add(task);
        }

        synchronized void removeTask(int id){
            if(!taskMap.containsKey(id)) {
                throw new IllegalArgumentException("Task not found");
            }

            Task task = taskMap.get(id);

            if(Task.TaskState.EXECUTED.equals(task.getState())) {
                throw new RuntimeException("Executed tasks cannot be removed");
            }

            task.setState(Task.TaskState.REMOVED);
            // this is direct deletion
            pq.remove(task);
            System.out.println("Task removed sucessfully, task.name" + task.getName());
        }


        synchronized void executeTask(){

            boolean executed = false;
            if(pq.isEmpty()){
                System.out.println("No task to execute");
                return;
            }

//            if(System.currentTimeMillis() <=)
//            Task task = pq.poll();
//            System.out.println("Task executed successfully, taskId:" + task.getName());
//            task.setState(Task.TaskState.EXECUTED);

            // lazy deletion is recommended as execute task is async as well, if removing is not frequent
            while(!pq.isEmpty()){
                Task task = pq.poll();

                if(System.currentTimeMillis() > task.getCreatedAt() + task.getTtl()){
                    System.out.println("Task expired, taskId:" + task.getName());
                    task.setState(Task.TaskState.EXPIRED);
                }

                if(Task.TaskState.QUEUED.equals(task.getState())) {
                    System.out.println("Task executed successfully, taskId:" + task.getName());
                    task.setState(Task.TaskState.EXECUTED);
                    executed = true;
                    break;
                }
            }

            if(!executed){
                System.out.println("No task to execute");
            }
        }

}
