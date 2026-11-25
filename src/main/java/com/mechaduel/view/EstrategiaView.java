package com.mechaduel.view;

import com.mechaduel.MechaApplication;
import com.mechaduel.controller.ControlEstrategia;
import com.mechaduel.model.GestorArchivos;
import com.mechaduel.model.Robot;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.util.List;

public class EstrategiaView {

    @FXML private Label lblMiNombre;
    @FXML private Label lblMiStats;
    @FXML private Label lblMiNrg;
    @FXML private Label lblMiEff;

    @FXML private Label lblRivalNombre;
    @FXML private Label lblRivalStats;
    @FXML private Label lblRivalNrg;
    @FXML private Label lblRivalEff;

    @FXML private ComboBox<String> cmbEstrategia;
    @FXML private ListView<String> lvVistaPrevia;
    @FXML private Button btnIniciarCombate;
    @FXML private Label lblEstado;

    private ControlEstrategia control;

    @FXML
    public void initialize() {
        this.control = new ControlEstrategia(this);

        Robot miRobot = MechaApplication.miRobot;
        Robot rivalRobot = MechaApplication.rivalRobot;

        lblMiNombre.setText("MI MECHA: " + miRobot.getNombre());
        lblMiStats.setText("HP: " + miRobot.getHp() + " | ATK: " + miRobot.getAtk()
                + " | DEF: " + miRobot.getDef() + " | SPD: " + miRobot.getSpd());
        lblMiNrg.setText("NRG: " + miRobot.getNrg());
        lblMiEff.setText("EFF: " + miRobot.getEfectos());

        lblRivalNombre.setText(rivalRobot.getNombre().toUpperCase());
        lblRivalStats.setText("HP: " + rivalRobot.getHp() + " | ATK: " + rivalRobot.getAtk()
                + " | DEF: " + rivalRobot.getDef() + " | SPD: " + rivalRobot.getSpd());
        lblRivalNrg.setText("NRG: " + rivalRobot.getNrg());
        lblRivalEff.setText("EFF: " + rivalRobot.getEfectos());

        cmbEstrategia.getItems().addAll(control.obtenerOpcionesEstrategia());
        cmbEstrategia.setValue(GestorArchivos.MODO_MANUAL);

        if (MechaApplication.modoVsCpu) {
            lblEstado.setText("Conexión remota OFF. Elige tu estrategia (la CPU elegirá un archivo).");
        }

        actualizarVistaPrevia();
    }

    @FXML
    protected void onSeleccionarEstrategia() {
        actualizarVistaPrevia();
    }

    private void actualizarVistaPrevia() {
        lvVistaPrevia.getItems().clear();
        List<String> lineas = control.obtenerVistaPrevia(cmbEstrategia.getValue());
        lvVistaPrevia.getItems().addAll(lineas);
    }

    @FXML
    protected void onIniciarCombate() {
        control.confirmarEstrategiaYComenzar(cmbEstrategia.getValue());
    }

    // Métodos invocados por el Controlador para actualizar la Vista
    public void bloquearControles(boolean bloquear) {
        Platform.runLater(() -> {
            btnIniciarCombate.setDisable(bloquear);
            cmbEstrategia.setDisable(bloquear);
        });
    }

    public void mostrarEstado(String texto) {
        Platform.runLater(() -> lblEstado.setText(texto));
    }

    public void irACombate() {
        Platform.runLater(() -> {
            try {
                MechaApplication.cambiarVista("combate-view.fxml", 580, 560);
            } catch (IOException e) {
                lblEstado.setText("Error al abrir la arena de combate.");
            }
        });
    }
}
