package org.example.lruCache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LRUCacheService {
    ConcurrentHashMap<String, Node> nodeMap = new ConcurrentHashMap<>();
    DoublyLinkedList doublyLinkedList = new DoublyLinkedList();
    int capacity = 0;

    LRUCacheService(int capacity){
        this.capacity = capacity;
    }

    public synchronized void putKey(String key, String value){
        if (nodeMap.containsKey(key)){
            Node updateNode = nodeMap.get(key);
            updateNode.data = value;
            doublyLinkedList.removeNode(updateNode);
            Node updatedNode = doublyLinkedList.append(key);
            nodeMap.put(key, updatedNode);
            return;
        }

        if(nodeMap.size() == capacity){
            Node node = doublyLinkedList.removeFirst();
            nodeMap.remove(node.data);
        }
        Node updatedNode = doublyLinkedList.append(key);
        nodeMap.put(key, updatedNode);
    }

    public synchronized String getKey(String key){
        if(!nodeMap.containsKey(key)){
            throw new IllegalArgumentException("Key not found");
        }
        org.example.lruCache.Node node = nodeMap.get(key);
        doublyLinkedList.removeNode(node);
        Node updatedNode = doublyLinkedList.append(key);
        nodeMap.put(key, updatedNode);
        return updatedNode.data;
    }

    public synchronized void delete(String key){
        if(!nodeMap.containsKey(key)){
            throw new IllegalArgumentException("Key not found");
        }

        Node node = nodeMap.get(key);
        doublyLinkedList.removeNode(node);
        nodeMap.remove(key);
    }

    public void printCacheMap(){
        if (nodeMap.isEmpty()) {
           System.out.println("Cache is empty");
        }

        for (Map.Entry<String, Node> entry: nodeMap.entrySet()){
            System.out.println("Key:" + entry.getKey() + "  value: "+ entry.getValue().data);
        }
    }

}
