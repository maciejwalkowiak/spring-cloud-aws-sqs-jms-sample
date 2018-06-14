package com.example.demo;

class SampleMessage {
    private String content;

    public SampleMessage() {
    }

    public SampleMessage(String content) {
        this.content = content;
    }

    String getContent() {
        return content;
    }

    void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "SampleMessage{" +
               "content='" + content + '\'' +
               '}';
    }
}
