package org.example;

public class Main {
    public static void main(String[] args) {
        Thread serverThread = new Thread(new Server());
        serverThread.start();
        Thread clientThread = new Thread(new Client());
        clientThread.start();
    }
}