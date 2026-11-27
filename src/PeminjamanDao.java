import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class PeminjamanDao {
    public ArrayList<Peminjaman> semua() throws SQLException {
        ArrayList<Peminjaman> hasil = new ArrayList<>();
        String sql = "SELECT p.id, p.nama_peminjam, p.nim, d.nama AS nama_perangkat, p.jumlah, p.sudah_kembali "
                + "FROM peminjaman p JOIN perangkat d ON d.kode = p.kode_perangkat ORDER BY p.id";
        try (Connection c = KoneksiDb.ambil();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                hasil.add(new Peminjaman(rs.getInt("id"), rs.getString("nama_peminjam"), rs.getString("nim"),
                        rs.getString("nama_perangkat"), rs.getInt("jumlah"), rs.getBoolean("sudah_kembali")));
            }
        }
        return hasil;
    }

    public int pinjam(String nama, String nim, String kodePerangkat, int jumlah) throws SQLException {
        try (Connection c = KoneksiDb.ambil()) {
            c.setAutoCommit(false);
            try {
                PreparedStatement ps1 = c.prepareStatement(
                        "UPDATE perangkat SET stok_tersedia = stok_tersedia - ? WHERE kode = ? AND stok_tersedia >= ?");
                ps1.setInt(1, jumlah);
                ps1.setString(2, kodePerangkat);
                ps1.setInt(3, jumlah);
                if (ps1.executeUpdate() == 0) {
                    throw new IllegalStateException("Stok " + kodePerangkat + " tidak cukup untuk " + jumlah + " unit.");
                }
                ps1.close();

                PreparedStatement ps2 = c.prepareStatement(
                        "INSERT INTO peminjaman (nama_peminjam, nim, kode_perangkat, jumlah, sudah_kembali, waktu_pinjam) "
                                + "VALUES (?, ?, ?, ?, 0, NOW())", Statement.RETURN_GENERATED_KEYS);
                ps2.setString(1, nama);
                ps2.setString(2, nim);
                ps2.setString(3, kodePerangkat);
                ps2.setInt(4, jumlah);
                ps2.executeUpdate();
                ResultSet kunci = ps2.getGeneratedKeys();
                kunci.next();
                int idBaru = kunci.getInt(1);
                kunci.close();
                ps2.close();

                c.commit();
                return idBaru;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public void kembalikan(int id) throws SQLException {
        try (Connection c = KoneksiDb.ambil()) {
            c.setAutoCommit(false);
            try {
                PreparedStatement ps1 = c.prepareStatement(
                        "SELECT kode_perangkat, jumlah, sudah_kembali FROM peminjaman WHERE id = ? FOR UPDATE");
                ps1.setInt(1, id);
                ResultSet rs = ps1.executeQuery();
                if (!rs.next()) {
                    throw new IllegalStateException("Transaksi tidak ditemukan.");
                }
                if (rs.getBoolean("sudah_kembali")) {
                    throw new IllegalStateException("Transaksi ini sudah dikembalikan sebelumnya.");
                }
                String kode = rs.getString("kode_perangkat");
                int jumlah = rs.getInt("jumlah");
                rs.close();
                ps1.close();

                PreparedStatement ps2 = c.prepareStatement(
                        "UPDATE peminjaman SET sudah_kembali = 1, waktu_kembali = NOW() WHERE id = ?");
                ps2.setInt(1, id);
                ps2.executeUpdate();
                ps2.close();

                PreparedStatement ps3 = c.prepareStatement(
                        "UPDATE perangkat SET stok_tersedia = stok_tersedia + ? WHERE kode = ?");
                ps3.setInt(1, jumlah);
                ps3.setString(2, kode);
                ps3.executeUpdate();
                ps3.close();

                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public void hapus(int id) throws SQLException {
        String sql = "DELETE FROM peminjaman WHERE id = ? AND sudah_kembali = 1";
        try (Connection c = KoneksiDb.ambil();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new IllegalStateException("Hanya riwayat yang sudah dikembalikan yang bisa dihapus.");
            }
        }
    }
}