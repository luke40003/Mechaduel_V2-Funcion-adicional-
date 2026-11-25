package com.mechaduel.view;

import com.mechaduel.MechaApplication;
import com.mechaduel.controller.ControlCombate;
import com.mechaduel.model.Robot;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;

import java.util.Optional;

public class CombateView {

    @FXML private Label lblModoJuego;

    @FXML private Label lblMiNombre;
    @FXML private Label lblMiHp;
    @FXML private Label lblMiStats;
    @FXML private Label lblMiNrg;
    @FXML private Label lblMiEff;

    @FXML private Label lblRivalNombre;
    @FXML private Label lblRivalHp;
    @FXML private Label lblRivalStats;
    @FXML private Label lblRivalNrg;
    @FXML private Label lblRivalEff;

    @FXML private Label lblTurno;
    @FXML private Button btnAttack;
    @FXML private Button btnSpecial;
    @FXML private Button btnDodge;
    @FXML private Button btnHeal;
    @FXML private Button btnCharge;

    @FXML private ListView<String> lvHistorial;
    @FXML private HBox boxFinPartida;

    private ControlCombate control;

    @FXML
    public void initialize() {
        this.control = new ControlCombate(this);

        lblMiNombre.setText("MI MECHA: " + MechaApplication.miRobot.getNombre());
        lblRivalNombre.setText(MechaApplication.rivalRobot.getNombre().toUpperCase());

        control.iniciarCombate();
    }

    @FXML protected void onAttack() { control.seleccionarMovimientoManual("ATTACK"); }
    @FXML protected void onSpecial() { control.seleccionarMovimientoManual("SPECIAL"); }
    @FXML protected void onDodge() { control.seleccionarMovimientoManual("DODGE"); }
    @FXML protected void onHeal() { control.seleccionarMovimientoManual("HEAL"); }
    @FXML protected void onCharge() { control.seleccionarMovimientoManual("CHARGE"); }

    @FXML
    protected void onGuardarRobot() {
        control.guardarRobot();
    }

    @FXML
    protected void onGuardarMovimientos() {
        TextInputDialog dialog = new TextInputDialog("nueva");
        dialog.setTitle("Guardar Estrategia");
        dialog.setHeaderText(null);
        dialog.setContentText("Nombre para el archivo de estrategia:");

        Optional<String> resultado = dialog.showAndWait();
        if (resultado.isPresent() && !resultado.get().trim().isEmpty()) {
            control.guardarMovimientos(resultado.get().trim());
        }
    }

    // Métodos invocados por el Controlador para actualizar la Vista
    public void actualizarModoJuego(String textoModo) {
        Platform.runLater(() -> lblModoJuego.setText(textoModo));
    }

    public void actualizarStats(Robot miRobot, Robot rivalRobot) {
        Platform.runLater(() -> {
            lblMiHp.setText("HP: " + miRobot.getHp());
            lblMiStats.setText("ATK: " + miRobot.getAtk() + " | DEF: " + miRobot.getDef() + " | SPD: " + miRobot.getSpd());
            lblMiNrg.setText("NRG: " + miRobot.getNrg());
            lblMiEff.setText("EFF: " + miRobot.getEfectos());

            lblRivalHp.setText("HP: " + rivalRobot.getHp());
            lblRivalStats.setText("ATK: " + rivalRobot.getAtk() + " | DEF: " + rivalRobot.getDef() + " | SPD: " + rivalRobot.getSpd());
            lblRivalNrg.setText("NRG: " + rivalRobot.getNrg());
            lblRivalEff.setText("EFF: " + rivalRobot.getEfectos());
        });
    }

    public void actualizarTurno(String texto) {
        Platform.runLater(() -> lblTurno.setText(texto));
    }

    public void setBotonesHabilitados(boolean habilitar) {
        Platform.runLater(() -> {
            btnAttack.setDisable(!habilitar);
            btnSpecial.setDisable(!habilitar);
            btnDodge.setDisable(!habilitar);
            btnHeal.setDisable(!habilitar);
            btnCharge.setDisable(!habilitar);
        });
    }

    public void agregarLog(String texto) {
        Platform.runLater(() -> {
            lvHistorial.getItems().add(texto);
            lvHistorial.scrollTo(lvHistorial.getItems().size() - 1);
        });
    }

    public void mostrarFinPartida(String resultado) {
        Platform.runLater(() -> {
            setBotonesHabilitados(false);
            lblTurno.setText(resultado);
            boxFinPartida.setVisible(true);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Fin del Combate");
            alert.setHeaderText(null);
            alert.setContentText(resultado);
            alert.show();
        });
    }

    public void mostrarMensaje(String titulo, String texto) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(texto);
            alert.showAndWait();
        });
    }
}
