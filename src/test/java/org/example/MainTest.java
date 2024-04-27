package org.example;

import org.junit.jupiter.api.Test;

class MainTest  {

    @Test
    void main() {
        Thread serverThread = new Thread(new Server());
        serverThread.start();
        Thread clientThread = new Thread(new Client());
        clientThread.start();
    }
}