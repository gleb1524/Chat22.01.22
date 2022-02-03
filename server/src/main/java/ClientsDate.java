import java.sql.*;

public class ClientsDate {

    private static Statement stmt;
    private static Connection connection;
    private static PreparedStatement pr;

//    public static void main(String[] args) {
//        try {
//            connect();
//            System.out.println("connect..");
//            System.out.println(rsRegistration("Login1", "Password", "Nickname1"));
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }finally {
//           disconnect();
//        }
//    }

    public ClientsDate() {
        try {
            connect();
            System.out.println("connect..");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }//finally {
//           disconnect();
//        }
    }

    public static void prInsert() throws SQLException {
        pr = connection.prepareStatement("INSERT INTO clients  (login, password, nickname) VALUES  (?, ?, ?);");
    }

    public static boolean rsRegistration(String login1, String password1, String nickname1) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT * FROM clients;");
        while (rs.next()){
            if(rs.getString(2).equals(login1)||rs.getString(4).equals(nickname1)){
                return false;
            }
        }
        prInsert();
        pr.setString(1, login1);
        pr.setString(2, password1);
        pr.setString(3, nickname1);
        pr.executeUpdate();
        rs.close();
        return true;
    }
    public static String getNicknameDate(String login, String password) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT * FROM clients;");
        while (rs.next()){
            if(rs.getString(2).equals(login)&&rs.getString(3).equals(password)){
                return rs.getString(4);
            }
        }
        return null;
    }

    public static void executeAdd() throws SQLException {
        stmt.executeUpdate("INSERT INTO clients  (nickname, password, login) VALUES  ('nickname', 'password', 'login');");
    }
    public static void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("JDBC:sqlite:clientsDate.db");
        stmt = connection.createStatement();

    }
    public static void disconnect(){
        try {
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
