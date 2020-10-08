package com.avit;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread{
    private final int serverPort;
    private List<Client> clientList = new ArrayList<>();

    public Server(int port) {
        this.serverPort = port;
    }

    public List<Client> getClientList() {
        return clientList;
    }

    @Override
    public void run() {
        handleServer();
    }

    private void handleServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            System.out.println("ChatRoom has started...");
            while (true) {
                System.out.println("Waiting for new clients...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket);
                Client client = new Client(this, clientSocket);
                clientList.add(client);
                client.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
