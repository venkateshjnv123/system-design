package org.example.taskScheduler;

public class TaskMain {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Task scheduler");

        TaskService taskService = new TaskService();
        Task task1= taskService.addTask("Task1", 1, "de", 1000L);
        Task task2= taskService.addTask("Task2", 4, "de", 1000L);
        Task task3= taskService.addTask("Task3", 10, "de", 1000L);

        taskService.executeTask();
        taskService.removeTask(task2.getId());
        Thread.sleep(1000);
        taskService.executeTask();

        Task task4 = taskService.addTask("Task4", 11, "de", 1000L);
        taskService.modifyTask(task1.getId(), 15);

        taskService.executeTask();
        taskService.removeTask(task4.getId());
        taskService.executeTask();
    }
}
