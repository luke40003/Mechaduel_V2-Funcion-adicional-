package com.mechaduel.controller;

import com.mechaduel.MechaApplication;
import com.mechaduel.model.GestorArchivos;
import com.mechaduel.view.EstrategiaView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ControlEstrategia {

    private final EstrategiaView vista;

    public ControlEstrategia(EstrategiaView vista) {
        this.vista = vista;
    }

    public List<String> obtenerOpcionesEstrategia() {
        return GestorArchivos.listarEstrategiasDisponibles();
    }

    public List<String> obtenerVistaPrevia(String estrategiaSeleccionada) {
        List<String> lineas = new ArrayList<>();
        if (estrategiaSeleccionada == null || estrategiaSeleccionada.equals(GestorArchivos.MODO_MANUAL)) {
            lineas.add("Modo Manual: Tú elegirás cada acción turno a turno.");
            lineas.add("Tus movimientos se irán guardando en estrategia.txt.");
        } else {
            List<String> movimientos = GestorArchivos.leerEstrategia(estrategiaSeleccionada);
            if (movimientos.isEmpty()) {
                lineas.add("(Archivo vacío: se jugará en modo manual)");
            } else {
                for (int i = 0; i < movimientos.size(); i++) {
                    lineas.add((i + 1) + ". " + movimientos.get(i));
                }
            }
        }
        return lineas;
    }

    public void confirmarEstrategiaYComenzar(String estrategiaElegida) {
        MechaApplication.estrategiaSeleccionada = estrategiaElegida;

        // En modo vs CPU (Conexión remota OFF), la propia CPU elige al azar uno de los archivos estrategia*.txt
        if (MechaApplication.modoVsCpu) {
            List<String> archivosDisponibles = GestorArchivos.listarArchivosEstrategia();
            if (!archivosDisponibles.isEmpty()) {
                int indiceAzar = new Random().nextInt(archivosDisponibles.size());
                MechaApplication.estrategiaCpuSeleccionada = archivosDisponibles.get(indiceAzar);
            } else {
                MechaApplication.estrategiaCpuSeleccionada = null;
            }
            vista.irACombate();
            return;
        }

        vista.bloquearControles(true);
        vista.mostrarEstado("Esperando a que el rival confirme su estrategia...");

        new Thread(() -> {
            try {
                MechaApplication.conexion.enviar("LISTO");
                String respuesta = MechaApplication.conexion.recibir();

                if (respuesta == null) {
                    throw new IOException("El rival cerró la conexión.");
                }

                vista.irACombate();
            } catch (IOException e) {
                vista.mostrarEstado("Error de red: " + e.getMessage());
                vista.bloquearControles(false);
            }
        }).start();
    }
}
