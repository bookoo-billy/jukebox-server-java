package com.bookoo;

public class AudioSink {

    private final String host;
    private final int port;

    public AudioSink(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }
}