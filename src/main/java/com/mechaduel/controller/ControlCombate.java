package com.mechaduel.controller;

import com.mechaduel.MechaApplication;
import com.mechaduel.model.GestorArchivos;
import com.mechaduel.model.Robot;
import com.mechaduel.view.CombateView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ControlCombate {

    private final CombateView vista;
    private final Robot miRobot;
    private final Robot rivalRobot;

    private boolean modoAutomatico;
    private List<String> estrategiaProgramada = new ArrayList<>();
    private int indiceEstrategia = 0;

    // Estrategia elegida por la CPU desde archivo
    private List<String> estrategiaCpu = new ArrayList<>();
    private int indiceEstrategiaCpu = 0;

    private final List<String> misMovimientosRealizados = new ArrayList<>();
    private final Object lockMovimiento = new Object();
    private String movimientoSeleccionado = null;
    private boolean alternarTurnoCeroSpd = true;

    public ControlCombate(CombateView vista) {
        this.vista = vista;
        this.miRobot = MechaApplication.miRobot;
        this.rivalRobot = MechaApplication.rivalRobot;
    }

    public void iniciarCombate() {
        List<String> opciones = GestorArchivos.listarEstrategiasDisponibles();
        String elegida = MechaApplication.estrategiaSeleccionada;

        if (elegida != null && opciones.contains(elegida) && !elegida.equals(GestorArchivos.MODO_MANUAL)) {
            activarModoAutomatico(elegida);
        } else {
            activarModoManual();
            GestorArchivos.limpiarEstrategiaManual();
        }

        if (MechaApplication.modoVsCpu) {
            String archivoCpu = MechaApplication.estrategiaCpuSeleccionada;
            estrategiaCpu = GestorArchivos.leerEstrategia(archivoCpu);
            if (estrategiaCpu.isEmpty()) {
                estrategiaCpu = Arrays.asList("ATTACK", "CHARGE", "SPECIAL", "HEAL");
                archivoCpu = "por defecto";
            }
            indiceEstrategiaCpu = 0;
            String modoJugador = modoAutomatico ? "Auto (" + elegida + ")" : "Manual";
            vista.actualizarModoJuego("Modo: vs CPU (" + archivoCpu + ") | Tú: " + modoJugador);
            vista.agregarLog("[CPU] La CPU eligió el archivo de estrategia: " + archivoCpu);
        }

        vista.actualizarStats(miRobot, rivalRobot);
        ejecutarBucleCombate();
    }

    private void activarModoManual() {
        modoAutomatico = false;
        vista.actualizarModoJuego("Modo: Manual (guardando en estrategia.txt)");
    }

    private void activarModoAutomatico(String archivoEstrategia) {
        List<String> movs = GestorArchivos.leerEstrategia(archivoEstrategia);
        if (movs.isEmpty()) {
            activarModoManual();
            vista.agregarLog("[Aviso] El archivo " + archivoEstrategia + " estaba vacío. Cambiado a Manual.");
        } else {
            estrategiaProgramada = movs;
            indiceEstrategia = 0;
            modoAutomatico = true;
            vista.actualizarModoJuego("Modo: Automático (" + archivoEstrategia + ")");
        }
    }

    private void ejecutarBucleCombate() {
        new Thread(() -> {
            try {
                while (miRobot.getHp() > 0 && rivalRobot.getHp() > 0) {
                    boolean esMiTurno = calcularSiguienteTurno();

                    if (esMiTurno) {
                        aplicarEfectosPasivos(miRobot, rivalRobot, miRobot.getNombre());
                    } else {
                        aplicarEfectosPasivos(rivalRobot, miRobot, rivalRobot.getNombre());
                    }

                    vista.actualizarStats(miRobot, rivalRobot);

                    if (miRobot.getHp() <= 0 || rivalRobot.getHp() <= 0) {
                        break;
                    }

                    if (esMiTurno) {
                        ejecutarMiTurno();
                    } else {
                        ejecutarTurnoRival();
                    }

                    vista.actualizarStats(miRobot, rivalRobot);
                }

                finalizarPartida();
            } catch (Exception e) {
                vista.agregarLog("Error en combate: " + e.getMessage());
            }
        }).start();
    }

    // Cálculo de turnos: suma SPD hasta llegar a 100, luego resta 100
    private boolean calcularSiguienteTurno() {
        if (miRobot.getSpd() == 0 && rivalRobot.getSpd() == 0) {
            boolean turno = alternarTurnoCeroSpd ? MechaApplication.esServidor : !MechaApplication.esServidor;
            alternarTurnoCeroSpd = !alternarTurnoCeroSpd;
            return turno;
        }

        while (miRobot.getTurnoAcumulado() < 100 && rivalRobot.getTurnoAcumulado() < 100) {
            miRobot.setTurnoAcumulado(miRobot.getTurnoAcumulado() + miRobot.getSpd());
            rivalRobot.setTurnoAcumulado(rivalRobot.getTurnoAcumulado() + rivalRobot.getSpd());
        }

        boolean esMiTurno;
        if (miRobot.getTurnoAcumulado() >= 100 && rivalRobot.getTurnoAcumulado() >= 100) {
            if (miRobot.getTurnoAcumulado() != rivalRobot.getTurnoAcumulado()) {
                esMiTurno = miRobot.getTurnoAcumulado() > rivalRobot.getTurnoAcumulado();
            } else if (miRobot.getSpd() != rivalRobot.getSpd()) {
                esMiTurno = miRobot.getSpd() > rivalRobot.getSpd();
            } else {
                esMiTurno = MechaApplication.esServidor;
            }
        } else {
            esMiTurno = miRobot.getTurnoAcumulado() >= 100;
        }

        if (esMiTurno) {
            miRobot.setTurnoAcumulado(miRobot.getTurnoAcumulado() - 100);
        } else {
            rivalRobot.setTurnoAcumulado(rivalRobot.getTurnoAcumulado() - 100);
        }
        return esMiTurno;
    }

    private void aplicarEfectosPasivos(Robot activo, Robot oponente, String nombreActivo) {
        for (String eff : activo.getEfectos()) {
            switch (eff) {
                case "Q": // Quemadura: -3 HP al enemigo
                    oponente.recibirDanio(3);
                    vista.agregarLog("[Efecto Q] " + nombreActivo + " quema al oponente (-3 HP).");
                    break;
                case "R": // Regeneración: +3 HP propio
                    activo.curar(3);
                    vista.agregarLog("[Efecto R] " + nombreActivo + " regenera salud (+3 HP).");
                    break;
                case "N": // Núcleo Activo: +2 NRG propio
                    activo.cargarEnergia(2);
                    vista.agregarLog("[Efecto N] " + nombreActivo + " recarga energía (+2 NRG).");
                    break;
            }
        }
    }

    private void ejecutarMiTurno() throws Exception {
        vista.actualizarTurno("¡ES TU TURNO!");
        String movimiento;

        if (modoAutomatico) {
            Thread.sleep(800);
            movimiento = estrategiaProgramada.get(indiceEstrategia % estrategiaProgramada.size());
            indiceEstrategia++;
        } else {
            movimiento = esperarSeleccionUsuario();
            GestorArchivos.agregarMovimientoEstrategia(movimiento);
        }

        misMovimientosRealizados.add(movimiento);

        // Si estamos en modo vs CPU (Conexión remota OFF), resolvemos localmente sin sockets
        if (MechaApplication.modoVsCpu) {
            resolverMiAccionLocalContraCpu(movimiento);
            return;
        }

        MechaApplication.conexion.enviar(movimiento);

        if (movimiento.equals("ATTACK") || movimiento.equals("SPECIAL")) {
            String respuesta = MechaApplication.conexion.recibir(); // "HIT" o "DODGE"
            if ("HIT".equals(respuesta)) {
                int danio = movimiento.equals("ATTACK")
                        ? miRobot.calcularDanioNormal(rivalRobot)
                        : miRobot.calcularDanioEspecial(rivalRobot);
                rivalRobot.recibirDanio(danio);
                vista.agregarLog("Tú -> " + movimiento + " : HIT (-" + danio + " HP al rival)");
            } else {
                vista.agregarLog("Tú -> " + movimiento + " : DODGE (El rival esquivó el ataque)");
            }
            if (movimiento.equals("SPECIAL")) {
                miRobot.setNrg(0);
            }
        } else if (movimiento.equals("DODGE")) {
            miRobot.incrementarDodge();
            vista.agregarLog("Tú -> DODGE (Probabilidad de esquiva: " + miRobot.getProbabilidadDodge() + "%)");
        } else if (movimiento.equals("HEAL")) {
            miRobot.curar(10);
            vista.agregarLog("Tú -> HEAL (+10 HP)");
        } else if (movimiento.equals("CHARGE")) {
            miRobot.cargarEnergia(10);
            vista.agregarLog("Tú -> CHARGE (+10 NRG)");
        }
    }

    private void resolverMiAccionLocalContraCpu(String movimiento) {
        if (movimiento.equals("ATTACK") || movimiento.equals("SPECIAL")) {
            boolean esquivado = rivalRobot.intentarEsquivar();
            if (esquivado) {
                vista.agregarLog("Tú -> " + movimiento + " : DODGE (La CPU esquivó el ataque)");
            } else {
                int danio = movimiento.equals("ATTACK")
                        ? miRobot.calcularDanioNormal(rivalRobot)
                        : miRobot.calcularDanioEspecial(rivalRobot);
                rivalRobot.recibirDanio(danio);
                vista.agregarLog("Tú -> " + movimiento + " : HIT (-" + danio + " HP a la CPU)");
            }
            if (movimiento.equals("SPECIAL")) {
                miRobot.setNrg(0);
            }
        } else if (movimiento.equals("DODGE")) {
            miRobot.incrementarDodge();
            vista.agregarLog("Tú -> DODGE (Probabilidad de esquiva: " + miRobot.getProbabilidadDodge() + "%)");
        } else if (movimiento.equals("HEAL")) {
            miRobot.curar(10);
            vista.agregarLog("Tú -> HEAL (+10 HP)");
        } else if (movimiento.equals("CHARGE")) {
            miRobot.cargarEnergia(10);
            vista.agregarLog("Tú -> CHARGE (+10 NRG)");
        }
    }

    private void ejecutarTurnoRival() throws Exception {
        // Turno de la CPU usando exclusivamente las acciones del archivo elegido por la CPU
        if (MechaApplication.modoVsCpu) {
            vista.actualizarTurno("Turno de la CPU...");
            Thread.sleep(800);
            String movCpu = estrategiaCpu.get(indiceEstrategiaCpu % estrategiaCpu.size());
            indiceEstrategiaCpu++;

            if (movCpu.equals("ATTACK") || movCpu.equals("SPECIAL")) {
                boolean esquivado = miRobot.intentarEsquivar();
                if (esquivado) {
                    vista.agregarLog("CPU -> " + movCpu + " : DODGE (¡Esquivaste el ataque!)");
                } else {
                    int danio = movCpu.equals("ATTACK")
                            ? rivalRobot.calcularDanioNormal(miRobot)
                            : rivalRobot.calcularDanioEspecial(miRobot);
                    miRobot.recibirDanio(danio);
                    vista.agregarLog("CPU -> " + movCpu + " : HIT (-" + danio + " HP)");
                }
                if (movCpu.equals("SPECIAL")) {
                    rivalRobot.setNrg(0);
                }
            } else if (movCpu.equals("DODGE")) {
                rivalRobot.incrementarDodge();
                vista.agregarLog("CPU -> DODGE");
            } else if (movCpu.equals("HEAL")) {
                rivalRobot.curar(10);
                vista.agregarLog("CPU -> HEAL (+10 HP)");
            } else if (movCpu.equals("CHARGE")) {
                rivalRobot.cargarEnergia(10);
                vista.agregarLog("CPU -> CHARGE (+10 NRG)");
            }
            return;
        }

        vista.actualizarTurno("Turno del rival...");
        String movRival = MechaApplication.conexion.recibir();
        if (movRival == null) {
            throw new IOException("Conexión perdida con el rival.");
        }

        if (movRival.equals("ATTACK") || movRival.equals("SPECIAL")) {
            boolean esquivado = miRobot.intentarEsquivar();
            if (esquivado) {
                MechaApplication.conexion.enviar("DODGE");
                vista.agregarLog("Rival -> " + movRival + " : DODGE (¡Esquivaste el ataque!)");
            } else {
                int danio = movRival.equals("ATTACK")
                        ? rivalRobot.calcularDanioNormal(miRobot)
                        : rivalRobot.calcularDanioEspecial(miRobot);
                miRobot.recibirDanio(danio);
                MechaApplication.conexion.enviar("HIT");
                vista.agregarLog("Rival -> " + movRival + " : HIT (-" + danio + " HP)");
            }
            if (movRival.equals("SPECIAL")) {
                rivalRobot.setNrg(0);
            }
        } else if (movRival.equals("DODGE")) {
            rivalRobot.incrementarDodge();
            vista.agregarLog("Rival -> DODGE");
        } else if (movRival.equals("HEAL")) {
            rivalRobot.curar(10);
            vista.agregarLog("Rival -> HEAL (+10 HP)");
        } else if (movRival.equals("CHARGE")) {
            rivalRobot.cargarEnergia(10);
            vista.agregarLog("Rival -> CHARGE (+10 NRG)");
        }
    }

    private String esperarSeleccionUsuario() throws InterruptedException {
        synchronized (lockMovimiento) {
            movimientoSeleccionado = null;
            vista.setBotonesHabilitados(true);
            while (movimientoSeleccionado == null) {
                lockMovimiento.wait();
            }
            vista.setBotonesHabilitados(false);
            return movimientoSeleccionado;
        }
    }

    public void seleccionarMovimientoManual(String mov) {
        synchronized (lockMovimiento) {
            movimientoSeleccionado = mov;
            lockMovimiento.notifyAll();
        }
    }

    private void finalizarPartida() {
        String resultado = (miRobot.getHp() > 0) ? "¡VICTORIA! Has ganado el combate." : "DERROTA. Tu Mecha fue destruido.";
        vista.agregarLog("=== FIN DE LA PARTIDA: " + resultado + " ===");
        vista.mostrarFinPartida(resultado);
    }

    public void guardarRobot() {
        try {
            GestorArchivos.guardarRobotBinario(miRobot);
            vista.mostrarMensaje("Guardado", "Robot '" + miRobot.getNombre() + "' guardado en robots.bin.");
        } catch (IOException e) {
            vista.mostrarMensaje("Error", "No se pudo guardar robots.bin: " + e.getMessage());
        }
    }

    public void guardarMovimientos(String nombreArchivo) {
        try {
            GestorArchivos.guardarHistorialMovimientos(nombreArchivo, misMovimientosRealizados);
            vista.mostrarMensaje("Guardado", "Movimientos guardados correctamente para futuras partidas.");
        } catch (IOException e) {
            vista.mostrarMensaje("Error", "No se pudo guardar el archivo de movimientos: " + e.getMessage());
        }
    }
}
