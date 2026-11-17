package controller;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import data.ProdukRepository;
import model.Produk;

public class KatalogController {

    @FXML
    private TextField namaField;
    @FXML
    private TextField hargaField;
    @FXML
    private ComboBox<String> fotoBox;
    @FXML
    private Label pesanLabel;
    @FXML
    private Label jumlahLabel;
    @FXML
    private TilePane katalogPane;

    private final ProdukRepository repository = new ProdukRepository();

    private Long produkDipilih = null;

    @FXML
    private void initialize() {
        fotoBox.getItems().add("roti.png");
        fotoBox.getItems().add("susu.png");
        fotoBox.getItems().add("kopi.png");
        fotoBox.setValue("roti.png");

        try {
            showData();
        } catch (SQLException e) {
            pesanLabel.setText("Gagal membaca database. Lihat Output.");
            e.printStackTrace();
        }
    }

    private void showData() throws SQLException {
        ArrayList<Produk> daftar = repository.getAll();
        katalogPane.getChildren().clear();

        for (Produk produk : daftar) {
            VBox kartu = createProductCard(produk);
            katalogPane.getChildren().add(kartu);
        }

        jumlahLabel.setText("Jumlah produk: " + daftar.size());
    }

    private VBox createProductCard(Produk produk) {
        ImageView fotoView = new ImageView();
        fotoView.setFitWidth(170);
        fotoView.setFitHeight(110);
        fotoView.setPreserveRatio(true);

        StackPane bingkai = new StackPane();
        bingkai.setPrefSize(180, 120);
        bingkai.setMinSize(180, 120);
        bingkai.setMaxSize(180, 120);

        URL lokasi = getClass().getResource("/image/" + produk.getFoto());

        if (lokasi != null) {
            Image gambar = new Image(lokasi.toExternalForm(),
                    170, 110, true, true);

            if (!gambar.isError()) {
                fotoView.setImage(gambar);
                bingkai.getChildren().add(fotoView);
            } else {
                bingkai.getChildren().add(new Label("Gambar tidak terbaca"));
            }
        } else {
            bingkai.getChildren().add(new Label("Gambar tidak tersedia"));
        }

        Label namaLabel = new Label(produk.getNama());
        namaLabel.setWrapText(true);
        namaLabel.getStyleClass().add("nama-produk");

        Label hargaLabel = new Label("Rp" + produk.getHarga());

        Label idLabel = new Label("ID: " + produk.getId());
        idLabel.getStyleClass().add("muted");

        VBox kartu = new VBox();
        kartu.setSpacing(8);
        kartu.setMinWidth(220);
        kartu.setPrefWidth(220);
        kartu.setMaxWidth(220);
        kartu.getStyleClass().add("kartu-produk");

        kartu.getChildren().add(bingkai);
        kartu.getChildren().add(namaLabel);
        kartu.getChildren().add(hargaLabel);
        kartu.getChildren().add(idLabel);

        Button editButton = new Button("Edit");
        editButton.setOnAction(e -> editProduct(produk.getId()));

        Button deleteButton = new Button("Hapus");
        deleteButton.setOnAction(e -> deleteProduct(produk.getId()));

        HBox tombolBox = new HBox(8);
        tombolBox.getChildren().add(editButton);
        tombolBox.getChildren().add(deleteButton);

        kartu.getChildren().add(tombolBox);

        return kartu;
    }

    @FXML
    private void saveProduct() {
        Produk produk;

        try {
            String nama = namaField.getText();
            long harga = Long.parseLong(hargaField.getText().trim());
            String foto = fotoBox.getValue();

            if (produkDipilih == null) {
                produk = new Produk(nama, harga, foto);
            } else {
                produk = new Produk(produkDipilih, nama, harga, foto);
            }

        } catch (NumberFormatException e) {
            pesanLabel.setText("Harga harus angka bulat, contoh 12000.");
            return;
        } catch (IllegalArgumentException e) {
            pesanLabel.setText(e.getMessage());
            return;
        }

        try {
            if (produkDipilih == null) {
                repository.add(produk);
            } else {
                repository.update(produk);
            }

        } catch (SQLException e) {
            pesanLabel.setText("Gagal menyimpan. Lihat Output.");
            e.printStackTrace();
            return;
        }

        boolean sedangUpdate = produkDipilih != null;

        produkDipilih = null;
        namaField.clear();
        hargaField.clear();

        try {
            showData();

            if (sedangUpdate) {
                pesanLabel.setText("Produk berhasil diperbarui.");
            } else {
                pesanLabel.setText("Produk berhasil disimpan.");
            }

        } catch (SQLException e) {
            pesanLabel.setText("Data tersimpan, tetapi katalog gagal dimuat ulang.");
            e.printStackTrace();
        }
    }

    private void editProduct(long id) {
        try {
            Produk produk = repository.getProduk(id);

            if (produk == null) {
                pesanLabel.setText("Produk tidak ditemukan.");
                return;
            }

            produkDipilih = produk.getId();

            namaField.setText(produk.getNama());
            hargaField.setText(String.valueOf(produk.getHarga()));
            fotoBox.setValue(produk.getFoto());

            pesanLabel.setText(
                    "Silakan ubah data produk, lalu klik Simpan."
            );

        } catch (SQLException e) {
            pesanLabel.setText("Gagal mengambil data produk.");
            e.printStackTrace();
        }
    }

    private void deleteProduct(long id) {
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);

        konfirmasi.setTitle("Hapus Produk");
        konfirmasi.setHeaderText(
                "Apakah kamu yakin ingin menghapus produk ini?"
        );
        konfirmasi.setContentText(
                "Data produk akan dihapus dari katalog."
        );

        if (konfirmasi.showAndWait().orElse(ButtonType.CANCEL)
                != ButtonType.OK) {
            return;
        }

        try {
            repository.delete(id);

            if (produkDipilih != null && produkDipilih == id) {
                produkDipilih = null;
                namaField.clear();
                hargaField.clear();
                fotoBox.setValue("roti.png");
            }

            showData();
            pesanLabel.setText("Produk berhasil dihapus.");

        } catch (SQLException e) {
            pesanLabel.setText("Gagal menghapus produk.");
            e.printStackTrace();
        }
    }
}
