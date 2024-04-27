package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class Server implements Runnable {
    public static final Logger serverLogger = LoggerFactory.getLogger(Server.class);
    public static final int PORT = 8080;
    public static final SimpleDateFormat dt1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    public static LinkedList<Connection> serverList = new LinkedList<>();

    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(PORT);
            serverLogger.info(dt1.format(new Date()) + " Чат был запущен");
            while (true) {
                Socket socket = server.accept();
                try {
                    serverList.add(new Connection(socket)); // добавить новое соединенние в список
                } catch (IOException e) {
                    serverLogger.error(dt1.format(new Date()) + " На сервере возникла ошибка");
                    socket.close();
                }
            }
        } catch (IOException e) {
            serverLogger.error(dt1.format(new Date()) + " На сервере возникла ошибка");
        }
    }
}
