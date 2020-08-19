package net.zaurbeck;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;

public class Connection implements Closeable {
    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.out =  new ObjectOutputStream(socket.getOutputStream());
        this.in =  new ObjectInputStream(socket.getInputStream());
    }

    public void send(net.zaurbeck.Message message) throws IOException {
        synchronized (out) {
            out.writeObject(message);
            out.flush();
        }
    }

    public net.zaurbeck.Message receive() throws IOException, ClassNotFoundException {
        synchronized (in) {
            return (net.zaurbeck.Message) in.readObject();
        }
    }

    public SocketAddress getRemoteSocketAddress() {
        return socket.getRemoteSocketAddress();
    }

    public void close() throws IOException {
        if (in != null) {
            in.close();
        }
        if (out != null) {
            out.close();
        }
        if (socket != null) {
            socket.close();
        }
    }
}