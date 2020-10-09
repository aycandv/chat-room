package com.avit;

import java.io.*;
import java.net.Socket;

public class ServerThread extends Thread {

    private final Socket clientSocket;
    private final Server server;
    private String name = null;
    private OutputStream outputStream;

    public ServerThread(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            handleClient();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleClient() throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();
        outputStream.write("============================\n".getBytes());
        outputStream.write("| Welcome to the chat room |\n".getBytes());
        outputStream.write("============================\n\n".getBytes());

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while((line = reader.readLine()) != null) {
            String[] tokens = line.split(" ");
            if (tokens.length > 0) {
                String cmd = tokens[0];
                if ("logoff".equalsIgnoreCase(cmd) || "quit".equalsIgnoreCase(cmd)) {
                    handleLogoff();
                    break;
                } else if ("login".equalsIgnoreCase(cmd)) {
                    handleLogin(outputStream, tokens);
                }
                else {
                    String msg = "Unknown command: " + cmd + "\n";
                    outputStream.write(msg.getBytes());
                }
            }

        }

        clientSocket.close();
    }

    private void handleLogoff() throws IOException {

        server.removeClient(this);
        this.send("You are logged off\n");
        System.out.println(this.getClientName() + " logged off");
        String offlineMsg = this.getClientName() + " logged off\n";
        for (ServerThread client : server.getClientList()) {
            if (client.getClientName() != null) {
                if (client.getClientName().equals(this.name)) continue;
                client.send(offlineMsg);
            }
        }
        clientSocket.close();
    }

    public String getClientName() {
        return name;
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException{
        if (tokens.length == 3) {
            String name = tokens[1];
            String password = tokens[2];

            if ((name.equals("guest") && password.equals("guest")) || (name.equals("aycan") && password.equals("1234"))) {
                String msg = "You logged in\n";
                outputStream.write(msg.getBytes());
                this.name = name;
                System.out.println("User logged in successfully: " + name + "\n");

                if (server.getClientList().size() > 1) {
                    for (ServerThread client : server.getClientList()) {
                        if (client.getClientName() != null) {
                            if (client.getClientName().equals(this.name)) continue;
                            send(client.getClientName() + " is online\n");
                        }
                    }
                }

                String onlineMsg = name + " logged in\n";
                for (ServerThread client : server.getClientList()) {
                    if (client.getClientName() != null) {
                        if (client.getClientName().equals(this.name)) continue;
                        client.send(onlineMsg);
                    }

                }
            } else {
                String msg = "error login\n";
                outputStream.write(msg.getBytes());
            }
        }
    }

    private void send(String msg) throws IOException{
        if (server.getClientList() != null) {
            outputStream.write(msg.getBytes());
        }
    }
}
