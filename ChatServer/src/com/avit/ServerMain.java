package com.avit;

public class ServerMain {
    public static void main(String[] args) {
        int port = 2222;
        Server server = new Server(port);
        server.start();

    }

}
