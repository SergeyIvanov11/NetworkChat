package org.example;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Client implements Runnable {
    private BufferedReader in; // поток чтения из сокета
    private BufferedWriter out; // поток чтения в сокет
    private BufferedReader inputUser; // поток чтения с консоли
    private String nickname; // имя клиента
    private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private Socket socket;
    private String addr;
    private int port; // ip адрес клиента
    private File servers = new File("./servers.json");
    private Map<Integer, String> serverAddresses = new HashMap<>();
    private Map<Integer, Integer> serverPorts = new HashMap<>();

    @Override
    public void run() {
        try {
            inputUser = new BufferedReader(new InputStreamReader(System.in));
            this.pressNickname(); // перед началом необходимо спросить имя
            this.refreshServers(); //обновить и запомнить список доступных серверов
            this.showServers(); //показать список доступных серверов
            this.choose();   //выбрать сервер

            socket = new Socket(addr, port);
            System.out.println("Вы присоединяетесь к чату с адресом: " + addr + ", портом: " + port);
            System.out.println("Для выхода из чата введите exit");
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            new ReadMsg().start(); // нить читающая сообщения из сокета в бесконечном цикле
            new WriteMsg().start(); // нить пишущая сообщения в сокет приходящие с консоли в бесконечном цикле
            out.write(nickname + " присоединяется к чату\n");

        } catch (IOException | ParseException e) {
            try {
                out.write(sdf.format(new Date()) + " В приложении клиента " + nickname + " возникла ошибка");
            } catch (IOException ex) {
            }
            Client.this.downService();
        }
    }

    private void pressNickname() throws IOException {
        System.out.println("Здравствуйте! Введите ваше имя: ");
        try {
            nickname = inputUser.readLine();
        } catch (IOException e) {
            out.write(sdf.format(new Date()) + " В приложении клиента " + nickname + " возникла ошибка");
        }
    }

    private void refreshServers() throws IOException, ParseException {
        if (!servers.exists()) {
            servers.createNewFile();
        }
        URL pathToFileOfServer = new URL("https://raw.githubusercontent.com/SergeyIvanov11/NetworkChat/main/servers.json"); //оригинальный servers.json со списком серверов
        try (InputStream in = pathToFileOfServer.openStream()) {
            Files.copy(in, servers.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        String str;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(servers))) {
            str = bufferedReader.readLine();
        }

        JSONParser parser = new JSONParser();
        JSONArray array = (JSONArray) parser.parse(str);
        Iterator i = array.iterator();
        while (i.hasNext()) {
            JSONObject innerObj = (JSONObject) i.next();
            serverAddresses.put(Integer.valueOf(((Long) innerObj.get("id")).intValue()), (String) innerObj.get("addr"));
            serverPorts.put(Integer.valueOf(((Long) innerObj.get("id")).intValue()), Integer.valueOf(((Long) innerObj.get("port")).intValue()));
        }
    }

    private void showServers() {
        System.out.println("Список доступных серверов для общения в чате: ");
        for (int i : serverPorts.keySet()) {
            System.out.println(i + ". Адрес: " + serverAddresses.get(i) + " Порт: " + serverPorts.get(i));
        }
        System.out.println();
    }

    private void choose() throws IOException {
        System.out.print("Выберите номер сервера и введите на клавиатуре: ");
        try {
            int choice = Integer.parseInt(inputUser.readLine());
            this.addr = serverAddresses.get(choice);
            this.port = serverPorts.get(choice);
        } catch (IOException e) {
            out.write(sdf.format(new Date()) + " В приложении клиента " + nickname + " возникла ошибка");
        }
    }

    private void downService() {
        try {
            if (!socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
            }
        } catch (IOException ignored) {
        }
    }

    // нить чтения сообщений с сервера
    private class ReadMsg extends Thread {
        @Override
        public void run() {
            String str;
            try {
                while (true) {
                    str = in.readLine(); // ждем сообщения с сервера
                    if (str.equals("exit")) {
                        Client.this.downService();
                        break;
                    }
                    System.out.println(str); // пишем сообщение с сервера на консоль
                }
            } catch (IOException e) {
                Client.this.downService();
            }
        }
    }

    // нить отправляющая сообщения приходящие с консоли на сервер
    public class WriteMsg extends Thread {
        @Override
        public void run() {
            while (true) {
                String userWord;
                try {
                    userWord = inputUser.readLine(); // сообщения с консоли
                    if (userWord.equals("exit")) {
                        out.write(sdf.format(new Date()) + " " + nickname + " прощается с нами" + "\n");
                        Client.this.downService();
                        break;
                    } else {
                        out.write(sdf.format(new Date()) + " " + nickname + " пишет: " + userWord + "\n"); // отправляем на сервер
                    }
                    out.flush();
                } catch (IOException e) {
                    Client.this.downService();
                }
            }
        }
    }
}
