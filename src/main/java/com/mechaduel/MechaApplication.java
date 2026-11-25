package com.mechaduel;

import com.mechaduel.model.Robot;
import com.mechaduel.network.ConexionRed;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MechaApplication extends Application {

    private static Stage primaryStage;

    // Contexto compartido entre las pantallas del sistema (MVC)
    public static ConexionRed conexion;
    public static boolean esServidor;
    public static boolean modoVsCpu;
    public static Robot miRobot;
    public static Robot rivalRobot;
    public static String estrategiaSeleccionada;
    public static String estrategiaCpuSeleccionada;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setTitle("Mecha Duel");
        cambiarVista("conexion-view.fxml", 450, 360);
        primaryStage.show();
    }

    public static void cambiarVista(String fxml, double ancho, double alto) throws IOException {
        FXMLLoader loader = new FXMLLoader(MechaApplication.class.getResource(fxml));
        Scene scene = new Scene(loader.load(), ancho, alto);
        primaryStage.setScene(scene);
    }

    @Override
    public void stop() {
        if (conexion != null) {
            conexion.cerrar();
        }
    }
}
