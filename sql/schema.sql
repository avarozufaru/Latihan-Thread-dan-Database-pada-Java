-- ============================================================
-- Skema database Meja Pinjam Lab (tahap Thread dan Database)
-- Jalankan sekali di MySQL/MariaDB Laragon (phpMyAdmin tab SQL,
-- HeidiSQL, atau klien mysql). Aman dijalankan berulang kali:
-- tabel tidak dibuat ulang dan data awal tidak digandakan.
-- ============================================================

CREATE DATABASE IF NOT EXISTS meja_pinjam_lab
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE meja_pinjam_lab;

-- Tabel induk: perangkat laboratorium beserta stoknya
CREATE TABLE IF NOT EXISTS perangkat (
    kode           VARCHAR(10) NOT NULL,
    nama           VARCHAR(60) NOT NULL,
    kategori       VARCHAR(30) NOT NULL,
    stok_total     INT         NOT NULL,
    stok_tersedia  INT         NOT NULL,
    PRIMARY KEY (kode),
    -- stok tidak boleh minus dan tidak boleh melebihi total
    CONSTRAINT cek_stok CHECK (stok_tersedia >= 0 AND stok_tersedia <= stok_total)
) ENGINE = InnoDB;

-- Tabel anak: setiap baris adalah satu transaksi peminjaman
CREATE TABLE IF NOT EXISTS peminjaman (
    id              INT         NOT NULL AUTO_INCREMENT,
    nama_peminjam   VARCHAR(60) NOT NULL,
    nim             VARCHAR(20) NOT NULL,
    kode_perangkat  VARCHAR(10) NOT NULL,
    jumlah          INT         NOT NULL,
    sudah_kembali   TINYINT(1)  NOT NULL DEFAULT 0,
    waktu_pinjam    DATETIME    NOT NULL,
    waktu_kembali   DATETIME    NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_pinjam_perangkat FOREIGN KEY (kode_perangkat)
        REFERENCES perangkat (kode) ON UPDATE CASCADE
) ENGINE = InnoDB;

-- Data awal: enam perangkat yang sama dengan latihan GUI sebelumnya
INSERT IGNORE INTO perangkat (kode, nama, kategori, stok_total, stok_tersedia) VALUES
    ('INV-01', 'PC Rakitan Core i5', 'Komputer',  6,  6),
    ('INV-02', 'Raspberry Pi 4',     'Komputer',  4,  4),
    ('INV-03', 'Switch 8 Port',      'Jaringan',  3,  3),
    ('INV-04', 'Kabel UTP Cat6',     'Jaringan', 10, 10),
    ('INV-05', 'Proyektor Portabel', 'Periferal', 2,  2),
    ('INV-06', 'Webcam USB',         'Periferal', 1,  1);
