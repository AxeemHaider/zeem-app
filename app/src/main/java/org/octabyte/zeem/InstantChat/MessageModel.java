package org.octabyte.zeem.InstantChat;

public class MessageModel {

    private Long messageId = System.currentTimeMillis();
    private String message;
    private String senderToken;
    private boolean sender = false;
    private boolean send = false;
    private boolean received = false;

    public MessageModel() {
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getSenderToken() {
        return senderToken;
    }

    public void setSenderToken(String senderToken) {
        this.senderToken = senderToken;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSender() {
        return sender;
    }

    public void setSender(boolean sender) {
        this.sender = sender;
    }

    public boolean isSend() {
        return send;
    }

    public void setSend(boolean send) {
        this.send = send;
    }

    public boolean isReceived() {
        return received;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }
}
