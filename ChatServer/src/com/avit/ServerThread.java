package com.avit;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;

public class ServerThread extends Thread {

    private final Socket clientSocket;
    private final Server server;
    private String name = null;
    private OutputStream outputStream;
    private HashSet<String> groupSet = new HashSet<>();

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
                } else if ("msg".equalsIgnoreCase(cmd)) {
                    handleMessage(tokens);
                } else if ("join".equalsIgnoreCase(cmd)) {
                    handleJoin(tokens);
                }
                else {
                    String msg = "Unknown command: " + cmd + "\n";
                    outputStream.write(msg.getBytes());
                }
            }

        }

        clientSocket.close();
    }

    private void handleJoin(String[] tokens) {
        if (tokens.length > 1) {
            String groupName = tokens[1];
            groupSet.add(groupName);
        }
    }

    private boolean isMemberOf(String group) {
        return groupSet.contains(group);
    }

    private void handleMessage(String[] tokens) throws IOException {
        String targetUser = tokens[1];

        boolean isGroupMsg = targetUser.charAt(0) == '#';

        for (ServerThread client : server.getClientList()) {
            if (isGroupMsg) {
                if (client.isMemberOf(targetUser)) {
                    StringBuilder msg = new StringBuilder(" ");
                    for (int i = 2; i < tokens.length; i++) {
                        msg.append(" ").append(tokens[i]);
                    }
                    String out = "(" + tokens[1] + ") " + this.getClientName() + " >" + msg + "\n";
                    if (!client.getClientName().equals(this.getClientName()) && client.getClientName() != null) client.send(out);
                    out = "(" + tokens[1] + ") You >" + msg + "\n";
                    this.send(out);
                }
            }
            else {
                if (client.getClientName().equalsIgnoreCase(targetUser)) {
                    StringBuilder msg = new StringBuilder(" ");
                    for (int i = 2; i < tokens.length; i++) {
                        msg.append(" ").append(tokens[i]);
                    }
                    String out = this.getClientName() + ">" + msg + "\n";
                    client.send(out);
                }
            }

        }
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
