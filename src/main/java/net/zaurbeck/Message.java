package net.zaurbeck;

import java.io.Serializable;

public class Message implements Serializable {
    private final net.zaurbeck.MessageType type;
    private final String data;

    public Message(net.zaurbeck.MessageType type) {
        this.type = type;
        this.data = null;
    }

    public Message(net.zaurbeck.MessageType type, String data) {
        this.type = type;
        this.data = data;
    }



    public net.zaurbeck.MessageType getType() {
        return type;
    }

    public String getData() {
        return data;
    }
}