package com.mechaduel.view;

import com.mechaduel.MechaApplication;
import com.mechaduel.controller.ControlConexion;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;

public class ConexionView {

    @FXML private TextField txtIp;
    @FXML private TextField txtPuerto;
    @FXML private Button btnServidor;
    @FXML private Button btnCliente;
    @FXML private Button btnVsCpuArchivo;
    @FXML private Label lblEstado;

    private ControlConexion control;

    @FXML
    public void initialize() {
        this.control = new ControlConexion(this);
    }

    @FXML
    protected void onIniciarServidor() {
        control.iniciarServidor(txtPuerto.getText());
    }

    @FXML
    protected void onConectarCliente() {
        control.conectarCliente(txtIp.getText(), txtPuerto.getText());
    }

    @FXML
    protected void onJugarVsCpuArchivo() {
        control.iniciarModoVsCpuArchivo();
    }

    // Métodos invocados por el Controlador para actualizar la Vista
    public void mostrarEstado(String texto) {
        Platform.runLater(() -> lblEstado.setText(texto));
    }

    public void bloquearControles(boolean bloquear) {
        Platform.runLater(() -> {
            btnServidor.setDisable(bloquear);
            btnCliente.setDisable(bloquear);
            btnVsCpuArchivo.setDisable(bloquear);
            txtIp.setDisable(bloquear);
            txtPuerto.setDisable(bloquear);
        });
    }

    public void irAEnsamble() {
        Platform.runLater(() -> {
            try {
                MechaApplication.cambiarVista("ensamble-view.fxml", 480, 495);
            } catch (IOException e) {
                lblEstado.setText("Error cargando vista de ensamble.");
            }
        });
    }
}
