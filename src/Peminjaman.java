public class Peminjaman {
    private int id;
    private String namaPeminjam;
    private String nim;
    private String namaPerangkat;
    private int jumlah;
    private boolean sudahKembali;

    public Peminjaman(int id, String namaPeminjam, String nim, String namaPerangkat, int jumlah, boolean sudahKembali) {
        this.id = id;
        this.namaPeminjam = namaPeminjam;
        this.nim = nim;
        this.namaPerangkat = namaPerangkat;
        this.jumlah = jumlah;
        this.sudahKembali = sudahKembali;
    }

    public int getId() {
        return id;
    }

    public String getKode() {
        return String.format("PJM-%03d", id);
    }

    public String getNamaPeminjam() {
        return namaPeminjam;
    }

    public String getNim() {
        return nim;
    }

    public String getNamaPerangkat() {
        return namaPerangkat;
    }

    public int getJumlah() {
        return jumlah;
    }

    public boolean isSudahKembali() {
        return sudahKembali;
    }

    @Override
    public String toString() {
        return getKode() + " - " + namaPerangkat + " (" + namaPeminjam + ")";
    }
}