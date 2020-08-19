package net.zaurbeck;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, net.zaurbeck.Connection> connectionMap = new ConcurrentHashMap<>();

    public static void sendBroadcastMessage(net.zaurbeck.Message message) {
        try {
            for (net.zaurbeck.Connection connection : connectionMap.values()) {
                connection.send(message);
            }

        } catch (IOException e) {
            e.printStackTrace();
            net.zaurbeck.ConsoleHelper.writeMessage("Сообщение не отправлено");
        }
    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            net.zaurbeck.ConsoleHelper.writeMessage("Установлено соединение с сервером " + socket.getRemoteSocketAddress());
            String userName = null;

            try (net.zaurbeck.Connection connection = new net.zaurbeck.Connection(socket)) {
                userName = serverHandshake(connection);
                sendBroadcastMessage(new net.zaurbeck.Message(net.zaurbeck.MessageType.USER_ADDED, userName));
                notifyUsers(connection, userName);
                serverMainLoop(connection, userName);

            } catch (IOException | ClassNotFoundException e) {
                net.zaurbeck.ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом.");
            }
            if (userName != null) {
                connectionMap.remove(userName);
                sendBroadcastMessage(new net.zaurbeck.Message(net.zaurbeck.MessageType.USER_REMOVED, userName));
            }
            net.zaurbeck.ConsoleHelper.writeMessage("Соединение с удаленным адресом закрыто.");
        }

        private String serverHandshake(net.zaurbeck.Connection connection) throws IOException, ClassNotFoundException {
            while (true) {
                connection.send(new net.zaurbeck.Message(net.zaurbeck.MessageType.NAME_REQUEST));
                net.zaurbeck.Message message = connection.receive();

                if (message.getType() == net.zaurbeck.MessageType.USER_NAME) {
                    if (!message.getData().isEmpty()) {
                        if (connectionMap.get(message.getData()) == null) {
                            connectionMap.put(message.getData(), connection);
                            connection.send(new net.zaurbeck.Message(net.zaurbeck.MessageType.NAME_ACCEPTED));
                            return message.getData();
                        }
                    }
                }
            }
        }

        private void notifyUsers(net.zaurbeck.Connection connection, String userName) throws IOException {
            for (Map.Entry entry : connectionMap.entrySet()) {
                String name = (String) entry.getKey();
                if (!name.equals(userName)) {
                    connection.send(new net.zaurbeck.Message(net.zaurbeck.MessageType.USER_ADDED, name));
                }
            }
        }

        private void serverMainLoop(net.zaurbeck.Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                net.zaurbeck.Message message = connection.receive();

                if (message.getType() == net.zaurbeck.MessageType.TEXT) {
                    String text = userName + ": " + message.getData();
                    sendBroadcastMessage(new net.zaurbeck.Message(net.zaurbeck.MessageType.TEXT, text));
                } else {
                    net.zaurbeck.ConsoleHelper.writeMessage("Ошибка!");
                }
            }
        }
    }

    public static void main(String[] args) {
        net.zaurbeck.ConsoleHelper.writeMessage("Введите порт сервера: ");
        int port = net.zaurbeck.ConsoleHelper.readInt();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            net.zaurbeck.ConsoleHelper.writeMessage("Сервер запущен");

            while (true) {
                Socket socket = serverSocket.accept();
                new Handler(socket).start();
            }
        } catch (Exception e) {
            e.getMessage();
        }

    }
}