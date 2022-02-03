import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private  ServerSocket server;
    private  Socket socket;
    private  final int PORT = 8189;
    private List<ClientHandler> clients;
    private AuthService authService;
    private ClientsDate clientsDate;

    public ClientsDate getClientsDate() {
        return clientsDate;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public Server() {
        authService = new SimpleAuthService();
        authService.start();
        clients = new CopyOnWriteArrayList<>();
        try {
            server = new ServerSocket(PORT);
            System.out.println("Server started!");

            while (true){
                socket = server.accept();
                System.out.println("Client connected:"+socket.getRemoteSocketAddress());
                new ClientHandler(this, socket);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            System.out.println("Server down");
            try{
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    public void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
        broadcastClientList();
    }

    public boolean checkAuth(String login){
        for (ClientHandler c : clients) {
            if(c.getNickname().equals(login)){
                return true;
            }
        }return false;
    }


    public void broadcastMessage(ClientHandler sender,String msg){
        String message = String.format("[ %s ]: %s", sender.getNickname(),msg);
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
    }
    public void broadcastPrivateMessage(ClientHandler sender,String receiver,String msg){
        String message = String.format("[ %s ] to [ %s ]: %s", sender.getNickname(),receiver, msg);
        for (ClientHandler client : clients) {
            if(client.getNickname().equals(receiver)){
            client.sendMessage(message);
                if(!sender.getNickname().equals(receiver)){
                    sender.sendMessage(message);
                }
                return;
            }
        }sender.sendMessage(String.format("Login %s not found!", receiver));

    }

    public void broadcastClientList() {
        StringBuilder stringBuilder = new StringBuilder();
        for (ClientHandler client : clients) {
            stringBuilder.append(" ").append(client.getNickname());
        }
        String msg = stringBuilder.toString();
        for (ClientHandler client : clients) {
            client.sendMessage("/clientList" + msg);
        }
    }
}