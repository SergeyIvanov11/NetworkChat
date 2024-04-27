package org.example;

import java.io.*;
import java.net.Socket;

import static org.example.Server.serverLogger;

public class Connection extends Thread {
    private Socket socket;
    private BufferedReader in; // поток чтения из сокета
    private BufferedWriter out; // поток записи в сокет

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        start();
    }

    @Override
    public void run() {
        String word;
        try {
            // первое сообщение отправленное сюда - это никнейм
            word = in.readLine();
            try {
                out.write(word + "\n");
                serverLogger.info(word);
                out.flush();
            } catch (IOException ignored) {
            }
            try {
                while (true) {
                    word = in.readLine();
                    serverLogger.info(word);
                    if (word.equals("exit")) {
                        this.downService();
                        break;
                    }

                    for (Connection vr : Server.serverList) {
                        vr.send(word);
                    }
                }
            } catch (NullPointerException ignored) {
            }

        } catch (IOException e) {
            this.downService();
        }
    }

    private void send(String msg) {
        try {
            out.write(msg + "\n");
            out.flush();
        } catch (IOException ignored) {
        }

    }

    private void downService() {
        try {
            if (!socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
                for (Connection vr : Server.serverList) {
                    if (vr.equals(this)) vr.interrupt();
                    Server.serverList.remove(this);
                }
            }
        } catch (IOException ignored) {
        }
    }
}
