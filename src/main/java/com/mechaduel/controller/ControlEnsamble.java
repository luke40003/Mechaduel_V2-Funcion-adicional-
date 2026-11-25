package com.mechaduel.controller;

import com.mechaduel.MechaApplication;
import com.mechaduel.exception.RobotInvalidoException;
import com.mechaduel.model.GestorArchivos;
import com.mechaduel.model.Robot;
import com.mechaduel.view.EnsambleView;

import java.io.IOException;
import java.util.List;

public class ControlEnsamble {

    private final EnsambleView vista;

    public ControlEnsamble(EnsambleView vista) {
        this.vista = vista;
    }

    public List<Robot> obtenerRobotsGuardados() {
        return GestorArchivos.cargarListaRobotsBinario();
    }

    public Robot guardarRobot(String nombre, String atkTxt, String defTxt, String spdTxt, String nrgTxt, List<String> efectos) {
        try {
            Robot robot = construirRobot(nombre, atkTxt, defTxt, spdTxt, nrgTxt, efectos);
            GestorArchivos.guardarRobotBinario(robot);
            vista.mostrarMensaje("Robot '" + robot.getNombre() + "' guardado en robots.bin.");
            return robot;
        } catch (NumberFormatException e) {
            vista.mostrarAlerta("Error de formato", "Todos los atributos deben ser números enteros.");
        } catch (RobotInvalidoException e) {
            vista.mostrarAlerta("Robot no válido", e.getMessage());
        } catch (IOException e) {
            vista.mostrarAlerta("Error de archivo", "No se pudo guardar en robots.bin: " + e.getMessage());
        }
        return null;
    }

    public void confirmarYEnviarMecha(String nombre, String atkTxt, String defTxt, String spdTxt, String nrgTxt, List<String> efectos) {
        try {
            Robot miRobot = construirRobot(nombre, atkTxt, defTxt, spdTxt, nrgTxt, efectos);
            MechaApplication.miRobot = miRobot;

            // Si estamos en modo vs CPU (Conexión remota OFF), generamos el Mecha CPU al azar sin usar sockets
            if (MechaApplication.modoVsCpu) {
                MechaApplication.rivalRobot = Robot.generarRobotCPU();
                vista.irAEstrategia();
                return;
            }

            vista.bloquearConfirmacion(true);
            vista.mostrarMensaje("Enviando configuración y esperando al rival...");

            new Thread(() -> {
                try {
                    MechaApplication.conexion.enviar(miRobot.toProtocoloString());
                    String datosRival = MechaApplication.conexion.recibir();

                    if (datosRival == null) {
                        throw new IOException("El rival cerró la conexión.");
                    }

                    MechaApplication.rivalRobot = Robot.fromProtocoloString(datosRival);
                    vista.irAEstrategia();
                } catch (Exception e) {
                    vista.mostrarMensaje("Error de red: " + e.getMessage());
                    vista.bloquearConfirmacion(false);
                }
            }).start();

        } catch (NumberFormatException e) {
            vista.mostrarAlerta("Error de formato", "Todos los atributos deben ser números enteros.");
        } catch (RobotInvalidoException e) {
            vista.mostrarAlerta("Robot no válido", e.getMessage());
        }
    }

    private Robot construirRobot(String nombre, String atkTxt, String defTxt, String spdTxt, String nrgTxt, List<String> efectos)
            throws RobotInvalidoException {
        int atk = Integer.parseInt(atkTxt.trim());
        int def = Integer.parseInt(defTxt.trim());
        int spd = Integer.parseInt(spdTxt.trim());
        int nrg = Integer.parseInt(nrgTxt.trim());
        return new Robot(nombre, atk, def, spd, nrg, efectos);
    }
}
