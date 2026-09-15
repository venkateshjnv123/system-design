package org.example.lruCache;

public class Node {
    Node prev;
    Node next;
    String data;


    public Node(String data){
        this.data = data;
        this.prev = null;
        this.next = null;
    }
}
