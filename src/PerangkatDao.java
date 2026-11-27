import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class PerangkatDao {
    public ArrayList<Perangkat> semua() throws SQLException {
        ArrayList<Perangkat> hasil = new ArrayList<>();
        String sql = "SELECT kode, nama, kategori, stok_total, stok_tersedia FROM perangkat ORDER BY kode";
        try (Connection c = KoneksiDb.ambil();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                hasil.add(new Perangkat(rs.getString("kode"), rs.getString("nama"), rs.getString("kategori"),
                        rs.getInt("stok_total"), rs.getInt("stok_tersedia")));
            }
        }
        return hasil;
    }
}