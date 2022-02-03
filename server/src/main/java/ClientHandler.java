import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;

public class ClientHandler {
    private  Socket socket;
    private Server server;
    //private ClientsDate clientsDateBase;
    private  DataInputStream in;
    private  DataOutputStream out;
    private boolean authenticated;
    private String login;

    public String getNickname() {
        return nickname;
    }

    public String nickname;
    private List<ClientHandler> clients;

    public ClientHandler(Server server, Socket socket) {
        this.socket = socket;
        this.server = server;
        try{
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //цикл аутентификации
                    while (true) {

                        String str = in.readUTF();
                        if (str.equals("/end")) {
                            sendMessage("Server down");
                            break;
                        }
                        if(str.startsWith("/auth")){
                            String[] token = str.split(" ",3);
                            if(token.length<3){
                                continue;
                            }
                            String newNick = server.getClientsDate().getNicknameDate(token[1], token[2]);
                            if (newNick != null){
                                login = token[1];
                                if(!server.checkAuth(login)){
                                authenticated = true;
                                nickname = newNick;
                                sendMessage("/authok " + nickname);
                                server.subscribe(this);
                                System.out.println("Client " + nickname +" authenticated");
                                break;
                                }else sendMessage("Клиент уже подключен");
                            }else{
                                sendMessage("Неверный логин/пароль");
                            }
                        }
                        if(str.startsWith("/reg")){
                            String[] token = str.split(" ",4);
                            if(token.length<4){
                                continue;
                            }if(//server.getAuthService().
                                    server.getClientsDate().rsRegistration(token[1],token[2],token[3])){
                                    //checkRegistration()){
                                sendMessage("/reg_ok");
                            }else{
                                sendMessage("/reg_no");
                            }

                        }
                    }
                    //цикл работы
                    while (authenticated) {
                        String str = in.readUTF();
                        System.out.println("Client: " + str);
                        if(str.startsWith("/")){
                            if (str.equals("/end")) {
                                sendMessage("Server down");
                                break;
                            }
                            if(str.startsWith("/m")){
                                String[] token = str.split(" ",3);
                                if(token.length<3){
                                    continue;
                                }
                                server.broadcastPrivateMessage(this, token[1],token[2]);

                            }
                        }else {
                            server.broadcastMessage(   this,str + "\n");
                        }


                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Client disconnected");
                    try {
                        socket.close();
                        server.unsubscribe(this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String msg){
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

} 