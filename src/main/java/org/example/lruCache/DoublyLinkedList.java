package org.example.lruCache;

public class DoublyLinkedList {
    private Node head = null;
    private Node tail = null;

    public Node append(String data){
        Node temp = new Node(data);

        if(head == null) {
            head = temp;
            tail = temp;
            return temp;
        }

        tail.next = temp;
        temp.prev = tail;
        tail = tail.next;
        return temp;
    }

    public void removeNode(Node node){
        if (node == head) head = head.next;
        if (node == tail) tail = tail.prev;

        Node prevnode = node.prev;
        Node nextnode = node.next;

        if (prevnode != null) prevnode.next = nextnode;
        if (nextnode != null) nextnode.prev = prevnode;
        node.prev = null;
        node.next = null;
    }

    public Node removeFirst(){
        if(head == null) return null;
        Node firstNode = head;
        head = head.next;
        return firstNode;
    }
}
