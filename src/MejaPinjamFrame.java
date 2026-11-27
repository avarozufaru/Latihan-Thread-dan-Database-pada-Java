import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.ArrayList;

public class MejaPinjamFrame {
    private static final Color TINTA = new Color(31, 42, 48);
    private static final Color KERTAS = new Color(244, 241, 234);
    private static final Color AKSEN = new Color(194, 87, 26);
    private static final Color HIJAU = new Color(47, 122, 77);
    private static final Color MERAH = new Color(176, 58, 46);

    private static final int JEDA_SIMULASI = 1500;

    private JFrame frame;

    private JPanel panelHalaman;
    private CardLayout cardLayout;
    private JPanel panelKartu;

    private JButton btnRak, btnForm, btnBuku, btnKeluar;
    private JLabel labelRingkasan, labelStatus, labelStok;
    private JTextField fieldNama, fieldNim, fieldJumlah;
    private JComboBox<String> comboPerangkat;
    private JCheckBox cekSetuju;
    private JButton btnCatat, btnKembali, btnHapus;
    private JTextArea areaBuku;

    private ArrayList<Perangkat> daftarPerangkat = new ArrayList<>();
    private ArrayList<Peminjaman> daftarPinjam = new ArrayList<>();

    private PerangkatDao perangkatDao = new PerangkatDao();
    private PeminjamanDao peminjamanDao = new PeminjamanDao();

    private boolean sibuk = false;

    private TombolHandler tombolHandler = new TombolHandler();
    private KartuHandler kartuHandler = new KartuHandler();

    public MejaPinjamFrame() {
        frame = new JFrame("Meja Pinjam Lab - Thread dan Database");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new TutupHandler());
        frame.setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        panelHalaman = new JPanel(cardLayout);
        panelHalaman.add(buatHalamanRak(), "RAK");
        panelHalaman.add(buatHalamanForm(), "FORM");
        panelHalaman.add(buatHalamanBuku(), "BUKU");

        labelStatus = new JLabel("Memuat data dari database...");
        labelStatus.setOpaque(true);
        labelStatus.setBackground(new Color(230, 226, 216));
        labelStatus.setBorder(BorderFactory.createEmptyBorder(6, 15, 6, 15));

        frame.add(buatSidebar(), BorderLayout.WEST);
        frame.add(panelHalaman, BorderLayout.CENTER);
        frame.add(labelStatus, BorderLayout.SOUTH);

        perbaruiTampilan();
        bukaHalaman("RAK");

        frame.setSize(920, 560);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        muatData();
    }

    private JPanel buatSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(TINTA);
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JLabel judul = new JLabel("MEJA PINJAM");
        judul.setFont(new Font("SansSerif", Font.BOLD, 18));
        judul.setForeground(Color.WHITE);
        judul.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        JLabel subjudul = new JLabel("Lab Informatika Unram");
        subjudul.setForeground(Color.LIGHT_GRAY);
        subjudul.setBorder(BorderFactory.createEmptyBorder(2, 20, 0, 0));

        btnRak = buatTombolMenu("Rak Perangkat");
        btnForm = buatTombolMenu("Formulir Pinjam");
        btnBuku = buatTombolMenu("Buku Pinjaman");
        btnKeluar = buatTombolMenu("Keluar");

        labelRingkasan = new JLabel();
        labelRingkasan.setForeground(Color.WHITE);
        labelRingkasan.setFont(new Font("SansSerif", Font.BOLD, 14));
        labelRingkasan.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        judul.setAlignmentX(Component.LEFT_ALIGNMENT);
        subjudul.setAlignmentX(Component.LEFT_ALIGNMENT);
        labelRingkasan.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(judul);
        sidebar.add(subjudul);
        sidebar.add(Box.createVerticalStrut(25));
        sidebar.add(btnRak);
        sidebar.add(btnForm);
        sidebar.add(btnBuku);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(labelRingkasan);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(btnKeluar);
        return sidebar;
    }

    private JButton buatTombolMenu(String teks) {
        JButton tombol = new JButton(teks);
        tombol.setHorizontalAlignment(SwingConstants.LEFT);
        tombol.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tombol.setAlignmentX(Component.LEFT_ALIGNMENT);
        tombol.setForeground(Color.WHITE);
        tombol.setBackground(TINTA);
        tombol.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tombol.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        tombol.setFocusPainted(false);
        tombol.addActionListener(tombolHandler);
        return tombol;
    }

    private JPanel buatHalamanRak() {
        panelKartu = new JPanel(new GridLayout(0, 3, 12, 12));
        panelKartu.setBackground(KERTAS);

        JPanel pembungkus = new JPanel(new BorderLayout());
        pembungkus.setBackground(KERTAS);
        pembungkus.add(panelKartu, BorderLayout.NORTH);

        JPanel halaman = buatHalaman("Rak Perangkat");
        halaman.add(pembungkus, BorderLayout.CENTER);
        return halaman;
    }

    private JPanel buatKartu(Perangkat p) {
        JPanel kartu = new JPanel(new GridLayout(3, 1, 0, 6));
        kartu.setName(p.getKode());
        kartu.setBackground(Color.WHITE);
        kartu.setBorder(borderKartu(Color.LIGHT_GRAY));

        JLabel labelKode = new JLabel(p.getKode() + " - " + p.getKategori());
        labelKode.setForeground(Color.GRAY);
        JLabel labelNama = new JLabel(p.getNama());
        labelNama.setFont(new Font("SansSerif", Font.BOLD, 15));
        JLabel labelSisa = new JLabel("Tersedia " + p.getStokTersedia() + " dari " + p.getStokTotal());
        labelSisa.setForeground(p.getStokTersedia() > 0 ? HIJAU : MERAH);

        kartu.add(labelKode);
        kartu.add(labelNama);
        kartu.add(labelSisa);
        kartu.addMouseListener(kartuHandler);
        return kartu;
    }

    private javax.swing.border.Border borderKartu(Color warna) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(warna, 2),
                BorderFactory.createEmptyBorder(12, 15, 12, 15));
    }

    private JPanel buatHalamanForm() {
        fieldNama = new JTextField(20);
        fieldNim = new JTextField(20);
        fieldJumlah = new JTextField("1", 5);
        fieldJumlah.addKeyListener(new AngkaHandler());

        comboPerangkat = new JComboBox<>();
        labelStok = new JLabel();

        cekSetuju = new JCheckBox("Saya bertanggung jawab atas perangkat ini");
        cekSetuju.setBackground(KERTAS);

        PilihanHandler pilihanHandler = new PilihanHandler();
        comboPerangkat.addItemListener(pilihanHandler);
        cekSetuju.addItemListener(pilihanHandler);

        btnCatat = new JButton("Catat Peminjaman");
        btnCatat.setBackground(AKSEN);
        btnCatat.setForeground(Color.WHITE);
        btnCatat.setEnabled(false);
        btnCatat.addActionListener(tombolHandler);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(KERTAS);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        tambahBaris(form, gbc, 0, "Nama", fieldNama);
        tambahBaris(form, gbc, 1, "NIM", fieldNim);
        tambahBaris(form, gbc, 2, "Perangkat", comboPerangkat);
        tambahBaris(form, gbc, 3, "", labelStok);
        tambahBaris(form, gbc, 4, "Jumlah", fieldJumlah);
        tambahBaris(form, gbc, 5, "", cekSetuju);
        tambahBaris(form, gbc, 6, "", btnCatat);

        JPanel pembungkus = new JPanel(new BorderLayout());
        pembungkus.setBackground(KERTAS);
        pembungkus.add(form, BorderLayout.NORTH);

        JPanel halaman = buatHalaman("Formulir Pinjam");
        halaman.add(pembungkus, BorderLayout.CENTER);
        return halaman;
    }

    private void tambahBaris(JPanel form, GridBagConstraints gbc, int baris, String teks, JComponent komponen) {
        gbc.gridy = baris;
        gbc.gridx = 0;
        gbc.weightx = 0;
        form.add(new JLabel(teks), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(komponen, gbc);
    }

    private JPanel buatHalamanBuku() {
        areaBuku = new JTextArea();
        areaBuku.setEditable(false);
        areaBuku.setFont(new Font("Monospaced", Font.PLAIN, 13));
        areaBuku.setMargin(new Insets(8, 8, 8, 8));

        btnKembali = new JButton("Terima Pengembalian");
        btnKembali.addActionListener(tombolHandler);
        btnHapus = new JButton("Hapus Riwayat");
        btnHapus.addActionListener(tombolHandler);

        JPanel panelTombol = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTombol.setBackground(KERTAS);
        panelTombol.add(btnKembali);
        panelTombol.add(btnHapus);

        JPanel halaman = buatHalaman("Buku Pinjaman");
        halaman.add(new JScrollPane(areaBuku), BorderLayout.CENTER);
        halaman.add(panelTombol, BorderLayout.SOUTH);
        return halaman;
    }

    private JPanel buatHalaman(String judul) {
        JLabel labelJudul = new JLabel(judul);
        labelJudul.setFont(new Font("SansSerif", Font.BOLD, 22));
        labelJudul.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JPanel halaman = new JPanel(new BorderLayout());
        halaman.setBackground(KERTAS);
        halaman.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        halaman.add(labelJudul, BorderLayout.NORTH);
        return halaman;
    }

    private void bukaHalaman(String nama) {
        cardLayout.show(panelHalaman, nama);
        btnRak.setBackground(nama.equals("RAK") ? AKSEN : TINTA);
        btnForm.setBackground(nama.equals("FORM") ? AKSEN : TINTA);
        btnBuku.setBackground(nama.equals("BUKU") ? AKSEN : TINTA);
    }

    private void perbaruiTampilan() {
        panelKartu.removeAll();
        for (Perangkat p : daftarPerangkat) {
            panelKartu.add(buatKartu(p));
        }
        panelKartu.revalidate();
        panelKartu.repaint();

        if (comboPerangkat.getItemCount() == 0) {
            for (Perangkat p : daftarPerangkat) {
                comboPerangkat.addItem(p.getKode() + " " + p.getNama());
            }
        }

        int aktif = 0;
        String teks = String.format("%-8s %-18s %-12s %-20s %s%n", "KODE", "NAMA", "NIM", "PERANGKAT", "STATUS");
        for (Peminjaman pj : daftarPinjam) {
            String status = pj.isSudahKembali() ? "KEMBALI" : "DIPINJAM (" + pj.getJumlah() + ")";
            teks += String.format("%-8s %-18s %-12s %-20s %s%n", pj.getKode(), pj.getNamaPeminjam(),
                    pj.getNim(), pj.getNamaPerangkat(), status);
            if (!pj.isSudahKembali()) {
                aktif++;
            }
        }
        areaBuku.setText(teks);

        labelRingkasan.setText(aktif + " pinjaman aktif");
        tampilkanStok();
    }

    private void tampilkanStok() {
        if (comboPerangkat.getSelectedIndex() < 0) {
            return;
        }
        Perangkat p = daftarPerangkat.get(comboPerangkat.getSelectedIndex());
        labelStok.setText("Stok tersedia: " + p.getStokTersedia() + " unit");
        labelStok.setForeground(p.getStokTersedia() > 0 ? HIJAU : MERAH);
    }

    private void catatPeminjaman() {
        String nama = fieldNama.getText().trim();
        String nim = fieldNim.getText().trim();
        if (comboPerangkat.getSelectedIndex() < 0) {
            return;
        }
        Perangkat p = daftarPerangkat.get(comboPerangkat.getSelectedIndex());

        if (nama.isEmpty() || nim.isEmpty() || fieldJumlah.getText().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Nama, NIM, dan jumlah wajib diisi.",
                    "Data Belum Lengkap", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int jumlah = Integer.parseInt(fieldJumlah.getText());
        if (jumlah < 1 || jumlah > p.getStokTersedia()) {
            JOptionPane.showMessageDialog(frame, "Stok " + p.getNama() + " hanya " + p.getStokTersedia() + " unit.",
                    "Stok Tidak Cukup", JOptionPane.ERROR_MESSAGE);
            return;
        }

        kerjakan("Menyimpan peminjaman...", true, () -> {
            int id = peminjamanDao.pinjam(nama, nim, p.getKode(), jumlah);
            return String.format("Peminjaman PJM-%03d berhasil disimpan.", id);
        }, () -> {
            fieldNama.setText("");
            fieldNim.setText("");
            fieldJumlah.setText("1");
            cekSetuju.setSelected(false);
            bukaHalaman("BUKU");
        });
    }

    private void terimaPengembalian() {
        ArrayList<Peminjaman> pilihan = new ArrayList<>();
        for (Peminjaman pj : daftarPinjam) {
            if (!pj.isSudahKembali()) {
                pilihan.add(pj);
            }
        }
        if (pilihan.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Tidak ada perangkat yang sedang dipinjam.");
            return;
        }

        Peminjaman dipilih = (Peminjaman) JOptionPane.showInputDialog(frame, "Pilih kode transaksi:", "Pengembalian",
                JOptionPane.QUESTION_MESSAGE, null, pilihan.toArray(), pilihan.get(0));
        if (dipilih == null) {
            return;
        }
        kerjakan("Menyimpan pengembalian...", true, () -> {
            peminjamanDao.kembalikan(dipilih.getId());
            return dipilih.getKode() + " sudah dikembalikan, stok bertambah.";
        }, null);
    }

    private void hapusRiwayat() {
        ArrayList<Peminjaman> pilihan = new ArrayList<>();
        for (Peminjaman pj : daftarPinjam) {
            if (pj.isSudahKembali()) {
                pilihan.add(pj);
            }
        }
        if (pilihan.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Belum ada riwayat yang sudah dikembalikan.");
            return;
        }

        Peminjaman dipilih = (Peminjaman) JOptionPane.showInputDialog(frame, "Pilih riwayat yang dihapus:", "Hapus Riwayat",
                JOptionPane.QUESTION_MESSAGE, null, pilihan.toArray(), pilihan.get(0));
        if (dipilih == null) {
            return;
        }
        kerjakan("Menghapus riwayat...", true, () -> {
            peminjamanDao.hapus(dipilih.getId());
            return "Riwayat " + dipilih.getKode() + " dihapus.";
        }, null);
    }

    private interface Operasi {
        String jalankan() throws SQLException;
    }

    private void kerjakan(String pesanSibuk, boolean pakaiJeda, Operasi operasi, Runnable setelahBerhasil) {
        mulaiSibuk(pesanSibuk);

        new Thread(() -> {
            try {
                if (pakaiJeda) {
                    Thread.sleep(JEDA_SIMULASI);
                }
                String pesan = operasi.jalankan();
                ArrayList<Perangkat> perangkat = perangkatDao.semua();
                ArrayList<Peminjaman> pinjam = peminjamanDao.semua();

                SwingUtilities.invokeLater(() -> {
                    daftarPerangkat = perangkat;
                    daftarPinjam = pinjam;
                    selesaiSibuk();
                    perbaruiTampilan();
                    labelStatus.setText(pesan);
                    if (setelahBerhasil != null) {
                        setelahBerhasil.run();
                    }
                });
            } catch (IllegalStateException e) {
                SwingUtilities.invokeLater(() -> {
                    selesaiSibuk();
                    labelStatus.setText("Ditolak: " + e.getMessage());
                    JOptionPane.showMessageDialog(frame, e.getMessage(), "Tidak Bisa Diproses", JOptionPane.WARNING_MESSAGE);
                });
            } catch (SQLException e) {
                SwingUtilities.invokeLater(() -> {
                    selesaiSibuk();
                    labelStatus.setText("Gagal mengakses database.");
                    JOptionPane.showMessageDialog(frame, e.getMessage()
                            + "\n\nPastikan MySQL di Laragon sudah menyala dan sql/schema.sql sudah dijalankan.",
                            "Galat Database", JOptionPane.ERROR_MESSAGE);
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void muatData() {
        kerjakan("Memuat data dari database...", false, () -> "Terhubung ke database.", null);
    }

    private void mulaiSibuk(String pesan) {
        sibuk = true;
        btnCatat.setEnabled(false);
        btnKembali.setEnabled(false);
        btnHapus.setEnabled(false);
        labelStatus.setText(pesan);
    }

    private void selesaiSibuk() {
        sibuk = false;
        btnKembali.setEnabled(true);
        btnHapus.setEnabled(true);
        btnCatat.setEnabled(cekSetuju.isSelected());
    }

    private void keluar() {
        int jawab = JOptionPane.showConfirmDialog(frame, "Yakin ingin menutup aplikasi?",
                "Keluar", JOptionPane.YES_NO_OPTION);
        if (jawab == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private class TombolHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Object sumber = e.getSource();
            if (sumber == btnRak) {
                bukaHalaman("RAK");
            } else if (sumber == btnForm) {
                bukaHalaman("FORM");
            } else if (sumber == btnBuku) {
                bukaHalaman("BUKU");
            } else if (sumber == btnCatat) {
                catatPeminjaman();
            } else if (sumber == btnKembali) {
                terimaPengembalian();
            } else if (sumber == btnHapus) {
                hapusRiwayat();
            } else if (sumber == btnKeluar) {
                keluar();
            }
        }
    }

    private class PilihanHandler implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getSource() == cekSetuju) {
                btnCatat.setEnabled(cekSetuju.isSelected() && !sibuk);
            } else if (e.getStateChange() == ItemEvent.SELECTED) {
                tampilkanStok();
            }
        }
    }

    private class KartuHandler extends MouseAdapter {
        @Override
        public void mouseEntered(MouseEvent e) {
            JPanel kartu = (JPanel) e.getSource();
            kartu.setBorder(borderKartu(AKSEN));
            labelStatus.setText(kartu.getName() + " - klik untuk meminjam perangkat ini.");
        }

        @Override
        public void mouseExited(MouseEvent e) {
            JPanel kartu = (JPanel) e.getSource();
            kartu.setBorder(borderKartu(Color.LIGHT_GRAY));
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            JPanel kartu = (JPanel) e.getSource();
            for (int i = 0; i < daftarPerangkat.size(); i++) {
                if (daftarPerangkat.get(i).getKode().equals(kartu.getName())) {
                    comboPerangkat.setSelectedIndex(i);
                }
            }
            bukaHalaman("FORM");
        }
    }

    private class AngkaHandler extends KeyAdapter {
        @Override
        public void keyTyped(KeyEvent e) {
            if (!Character.isDigit(e.getKeyChar())) {
                e.consume();
            }
        }
    }

    private class TutupHandler extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            keluar();
        }
    }
}