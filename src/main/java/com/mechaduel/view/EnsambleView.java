package com.mechaduel.view;

import com.mechaduel.MechaApplication;
import com.mechaduel.controller.ControlEnsamble;
import com.mechaduel.model.Robot;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EnsambleView {

    @FXML private ComboBox<Robot> cmbRobots;
    @FXML private TextField txtNombre;
    @FXML private TextField txtAtk;
    @FXML private TextField txtDef;
    @FXML private TextField txtSpd;
    @FXML private TextField txtNrg;
    @FXML private ToggleGroup tgEfectos;
    @FXML private RadioButton rbNinguno;
    @FXML private RadioButton rbQuemadura;
    @FXML private RadioButton rbRegeneracion;
    @FXML private RadioButton rbNucleo;
    @FXML private Button btnConfirmar;
    @FXML private Label lblMensaje;

    private ControlEnsamble control;

    @FXML
    public void initialize() {
        this.control = new ControlEnsamble(this);
        recargarComboRobots();
        if (MechaApplication.modoVsCpu) {
            btnConfirmar.setText("Confirmar Mecha y Generar CPU");
        }
    }

    public void recargarComboRobots() {
        cmbRobots.getItems().clear();
        cmbRobots.getItems().addAll(control.obtenerRobotsGuardados());
    }

    @FXML
    protected void onSeleccionarRobot() {
        Robot seleccionado = cmbRobots.getValue();
        if (seleccionado != null) {
            txtNombre.setText(seleccionado.getNombre());
            txtAtk.setText(String.valueOf(seleccionado.getAtk()));
            txtDef.setText(String.valueOf(seleccionado.getDef()));
            txtSpd.setText(String.valueOf(seleccionado.getSpd()));
            txtNrg.setText(String.valueOf(seleccionado.getNrg()));

            List<String> eff = seleccionado.getEfectos();
            if (eff.contains("Q")) {
                rbQuemadura.setSelected(true);
            } else if (eff.contains("R")) {
                rbRegeneracion.setSelected(true);
            } else if (eff.contains("N")) {
                rbNucleo.setSelected(true);
            } else {
                rbNinguno.setSelected(true);
            }

            lblMensaje.setText("Cargado: " + seleccionado.getNombre());
        }
    }

    @FXML
    protected void onGuardarRobotPrevia() {
        Robot guardado = control.guardarRobot(
                txtNombre.getText(),
                txtAtk.getText(),
                txtDef.getText(),
                txtSpd.getText(),
                txtNrg.getText(),
                obtenerEfectoSeleccionado()
        );
        if (guardado != null) {
            recargarComboRobots();
            cmbRobots.setValue(guardado);
        }
    }

    @FXML
    protected void onConfirmarMecha() {
        control.confirmarYEnviarMecha(
                txtNombre.getText(),
                txtAtk.getText(),
                txtDef.getText(),
                txtSpd.getText(),
                txtNrg.getText(),
                obtenerEfectoSeleccionado()
        );
    }

    private List<String> obtenerEfectoSeleccionado() {
        List<String> efectos = new ArrayList<>();
        if (rbQuemadura.isSelected()) {
            efectos.add("Q");
        } else if (rbRegeneracion.isSelected()) {
            efectos.add("R");
        } else if (rbNucleo.isSelected()) {
            efectos.add("N");
        }
        return efectos;
    }

    // Métodos invocados por el Controlador para actualizar la Vista
    public void mostrarMensaje(String texto) {
        Platform.runLater(() -> lblMensaje.setText(texto));
    }

    public void bloquearConfirmacion(boolean bloquear) {
        Platform.runLater(() -> btnConfirmar.setDisable(bloquear));
    }

    public void mostrarAlerta(String titulo, String contenido) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(contenido);
            alert.showAndWait();
        });
    }

    public void irAEstrategia() {
        Platform.runLater(() -> {
            try {
                MechaApplication.cambiarVista("estrategia-view.fxml", 520, 460);
            } catch (IOException e) {
                lblMensaje.setText("Error al abrir pantalla de estrategia.");
            }
        });
    }
}
