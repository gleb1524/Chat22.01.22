import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class Registration {


    @FXML
    public TextField textFieldLogin;
    @FXML
    public PasswordField textFieldPassword;
    @FXML
    public TextField textFieldNick;
    @FXML
    public TextArea textArea;
    @FXML
    private Client client;
    @FXML
    public void setClient(Client client) {
        this.client = client;
    }

    @FXML
    public void regBtn(ActionEvent actionEvent) {
        String login = textFieldLogin.getText().trim();
        String password = textFieldPassword.getText().trim();
        String nickname = textFieldNick.getText().trim();
        client.setRegMsg(login, password, nickname);
    }

    public void registrationResult(String result){
        if(result.equals("/reg_ok")){
            textArea.appendText("Registration satisfied!\n");
        }else{
            textArea.appendText("Registration failed!\n");
        }
    }
}
