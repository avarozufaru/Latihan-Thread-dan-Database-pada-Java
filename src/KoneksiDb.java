import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class KoneksiDb {
    private static final String URL = "jdbc:mysql://localhost:3306/meja_pinjam_lab?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "";

    public static Connection ambil() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL tidak ditemukan. Pastikan jar di folder lib sudah masuk Referenced Libraries.", e);
        }
        return DriverManager.getConnection(URL, USER, PASS);
    }
}