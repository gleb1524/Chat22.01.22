import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.w3c.dom.ls.LSOutput;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.security.spec.RSAOtherPrimeInfo;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;


public class Client implements Initializable {


    private Socket socket;
    private final int PORT = 8189;
    private final String HOST = "localhost";
    private DataInputStream in;
    private DataOutputStream out;
    private boolean authenticated;
    private String nickname;
    private long timeAuth = 0;
    @FXML
    public HBox sendMessageBar;
    @FXML
    private Stage stage;
    @FXML
    private Stage regStage;
    @FXML
    private Registration registration;
    @FXML
    public javafx.scene.control.TextArea textArea;
    @FXML
    public javafx.scene.control.TextField textField;
    @FXML
    public TextField textFieldLogin;
    @FXML
    public PasswordField textFieldPassword;
    @FXML
    public Button sigIn;
    @FXML
    public HBox authBar;
    @FXML
    public ListView clientList;


    @FXML
    public void pressToClose(ActionEvent actionEvent) {
        if (socket != null && !socket.isClosed()) {
            try {
                out.writeUTF("/end");
                setAuthenticated(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            stage.close();
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        Platform.runLater(() -> {
            stage = (Stage) textField.getScene().getWindow();
            stage.setOnCloseRequest(windowEvent -> {
                if (socket != null && !socket.isClosed()) {
                    try {
                        out.writeUTF("/end");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        setAuthenticated(false);
    }

    @FXML
    protected void enterBtn(ActionEvent actionEvent) throws IOException {
        if (textField.getText().length() > 0) {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        }
    }

    @FXML
    public void pressToAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        //Отключение не аутентифицированных пользователей через 120 секунд
        checkAuthTime();
        try {
            String str = String.format("/auth %s %s",
                    textFieldLogin.getText().trim(), textFieldPassword.getText().trim());
            out.writeUTF(str);
            textFieldPassword.clear();
        } catch (IOException e) {
            System.out.println(e + "ошибка в отправке пары пароль/логин");
        }
    }

    public void connect() {
        try {
            socket = new Socket(HOST, PORT);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            /* Thread inputStream =*/
            new Thread(() -> {
                try {
                    while (!authenticated) {
                        //цикл аутентификации
                        String str = in.readUTF();
                        if(str.startsWith("/")) {
                            if (str.equals("/end")) {
                                break;
                            }
                            if (str.startsWith("/authok")) {
                                nickname = str.split(" ")[1];
                                setAuthenticated(true);
                                System.err.println("Authentication satisfied ");
                                textArea.clear();
                                break;
                            }
                            if(str.startsWith("/reg")){
                                registration.registrationResult(str);
                            }

                        }else {
                            textArea.appendText((str + "\n"));
                        }
                    }
                    //цикл работы
                    while (authenticated) {
                        String str = in.readUTF();
                        if (str.startsWith("/")) {

                            if (str.startsWith("/clientList")) {
                                String[] token = str.split(" ");
                                Platform.runLater(() -> {
                                    clientList.getItems().clear();
                                    for (int i = 1; i < token.length; i++) {
                                        clientList.getItems().add(token[i]);
                                    }
                                });
                            }
                        } else {
                            textArea.appendText((str + "\n"));
                            if (str.equals("Server down")) {
                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Socked closed, check connection ");
                } finally {
                    try {
                        setAuthenticated(false);
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authBar.setVisible(!authenticated);
        authBar.setManaged(!authenticated);
        sendMessageBar.setVisible(authenticated);
        sendMessageBar.setManaged(authenticated);
        clientList.setVisible(authenticated);
        clientList.setManaged(authenticated);

        if (!authenticated) {
            nickname = "";
        }
        setTitle(nickname);
    }

    private void setTitle(String nickname) {
        String title;
        if (nickname.equals("")) {
            title = "MyChat";
        } else {
            title = String.format("MyChat - %s", nickname);
        }
        Platform.runLater(() -> {
            stage.setTitle(title);
        });
    }

    private void checkAuthTime() {
        new Thread(() -> {
            timeAuth = 0;
            System.err.println("Timer authentication started");
            while (!authenticated) {
                try {
                    System.err.println("Socked closed after: " + (120 - timeAuth));
                    Thread.sleep(1000);
                    timeAuth++;
                    if (timeAuth > 120) {
                        textArea.appendText("Connection lost");
                        out.writeUTF("/end");
                        socket.close();
                        break;
                    }
                    if (socket.isClosed()) {
                        break;
                    }

                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void pressToPrivateMessage(MouseEvent mouseEvent) {
        String receiver = clientList.getSelectionModel().getSelectedItem().toString();
        textField.appendText("/m " + receiver + " ");
    }

    public void regBtn(ActionEvent actionEvent) {
        if(regStage==null){
            registrationWindow();
        }
        regStage.show();
    }

    public void registrationWindow() {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/reg.fxml"));

        try {
            Scene scene = new Scene(fxmlLoader.load(), 350, 350);
            regStage = new Stage();
            regStage.setTitle("Registration");
            regStage.setScene(scene);
            regStage.initModality(Modality.APPLICATION_MODAL);
            regStage.initStyle(StageStyle.UTILITY);

            registration = fxmlLoader.getController();
            registration.setClient(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void setRegMsg(String login, String password, String nickname){
        if (socket == null || socket.isClosed()) {
            connect();
        }
        if(login.equals("")||password.equals("")||nickname.equals("")){
            registration.registrationResult("reg_no");
            return;
        }
      String message = String.format("/reg %s %s %s", login, password, nickname);
        try {
            out.writeUTF( message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

