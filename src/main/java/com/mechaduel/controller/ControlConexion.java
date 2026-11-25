package com.mechaduel.controller;

import com.mechaduel.MechaApplication;
import com.mechaduel.network.ConexionRed;
import com.mechaduel.view.ConexionView;

import java.io.IOException;

public class ControlConexion {

    private final ConexionView vista;

    public ControlConexion(ConexionView vista) {
        this.vista = vista;
    }

    public void iniciarServidor(String puertoTexto) {
        int puerto;
        try {
            puerto = Integer.parseInt(puertoTexto.trim());
        } catch (NumberFormatException e) {
            vista.mostrarEstado("Error: El puerto debe ser un número.");
            return;
        }

        vista.bloquearControles(true);
        vista.mostrarEstado("Esperando conexión en puerto " + puerto + "...");

        new Thread(() -> {
            try {
                ConexionRed red = new ConexionRed();
                red.iniciarServidor(puerto);
                MechaApplication.conexion = red;
                MechaApplication.esServidor = true;
                MechaApplication.modoVsCpu = false;
                vista.irAEnsamble();
            } catch (IOException e) {
                vista.mostrarEstado("Error al iniciar servidor: " + e.getMessage());
                vista.bloquearControles(false);
            }
        }).start();
    }

    public void conectarCliente(String ipTexto, String puertoTexto) {
        String ip = ipTexto.trim();
        int puerto;
        try {
            puerto = Integer.parseInt(puertoTexto.trim());
        } catch (NumberFormatException e) {
            vista.mostrarEstado("Error: El puerto debe ser un número.");
            return;
        }

        vista.bloquearControles(true);
        vista.mostrarEstado("Conectando a " + ip + ":" + puerto + "...");

        new Thread(() -> {
            try {
                ConexionRed red = new ConexionRed();
                red.conectarCliente(ip, puerto);
                MechaApplication.conexion = red;
                MechaApplication.esServidor = false;
                MechaApplication.modoVsCpu = false;
                vista.irAEnsamble();
            } catch (IOException e) {
                vista.mostrarEstado("No se pudo conectar: " + e.getMessage());
                vista.bloquearControles(false);
            }
        }).start();
    }

    // Modo Juego vs CPU mediante archivo de estrategia (Conexión Remota OFF)
    public void iniciarModoVsCpuArchivo() {
        MechaApplication.conexion = null;
        MechaApplication.esServidor = true;
        MechaApplication.modoVsCpu = true;
        vista.irAEnsamble();
    }
}
