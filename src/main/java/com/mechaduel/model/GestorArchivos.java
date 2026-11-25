package com.mechaduel.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GestorArchivos {

    public static final String MODO_MANUAL = "Manual (Jugar turno a turno)";
    private static final String ARCHIVO_ESTRATEGIA_DEFAULT = "estrategia.txt";
    private static final String ARCHIVO_ROBOT_BIN = "robots.bin";

    // Lista siempre la opción Manual primero, seguida de todos los archivos estrategia*.txt disponibles
    public static List<String> listarEstrategiasDisponibles() {
        List<String> opciones = new ArrayList<>();
        opciones.add(MODO_MANUAL);
        opciones.addAll(listarArchivosEstrategia());
        return opciones;
    }

    // Lista únicamente los archivos estrategia*.txt encontrados en la carpeta del proyecto
    public static List<String> listarArchivosEstrategia() {
        List<String> archivosTxt = new ArrayList<>();
        File directorioActual = new File(".");
        File[] archivos = directorioActual.listFiles();
        if (archivos != null) {
            for (File f : archivos) {
                String nombre = f.getName();
                if (f.isFile() && nombre.toLowerCase().startsWith("estrategia") && nombre.toLowerCase().endsWith(".txt")) {
                    archivosTxt.add(nombre);
                }
            }
        }
        return archivosTxt;
    }

    // Lee los movimientos de un archivo de estrategia específico
    public static List<String> leerEstrategia(String nombreArchivo) {
        List<String> movimientos = new ArrayList<>();
        if (nombreArchivo == null || nombreArchivo.equals(MODO_MANUAL)) {
            return movimientos;
        }

        File file = new File(nombreArchivo);
        if (!file.exists()) {
            return movimientos;
        }

        try (FileReader fr = new FileReader(file);
             Scanner sc = new Scanner(fr)) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine().trim().toUpperCase();
                if (linea.equals("ATTACK") || linea.equals("SPECIAL") ||
                    linea.equals("DODGE") || linea.equals("HEAL") || linea.equals("CHARGE")) {
                    movimientos.add(linea);
                }
            }
        } catch (IOException e) {
            System.out.println("Error leyendo " + nombreArchivo + ": " + e.getMessage());
        }
        return movimientos;
    }

    // Reinicia estrategia.txt al comenzar un combate en modo manual
    public static void limpiarEstrategiaManual() {
        try (FileWriter fw = new FileWriter(ARCHIVO_ESTRATEGIA_DEFAULT, false);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write("");
        } catch (IOException e) {
            System.out.println("Error inicializando estrategia.txt: " + e.getMessage());
        }
    }

    // Agrega un movimiento a estrategia.txt durante el combate (modo manual)
    public static void agregarMovimientoEstrategia(String movimiento) {
        try (FileWriter fw = new FileWriter(ARCHIVO_ESTRATEGIA_DEFAULT, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(movimiento);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error escribiendo en estrategia.txt: " + e.getMessage());
        }
    }

    // Guarda todos los movimientos realizados en un archivo .txt para futuras partidas
    public static void guardarHistorialMovimientos(String nombreArchivo, List<String> movimientos) throws IOException {
        if (!nombreArchivo.toLowerCase().startsWith("estrategia")) {
            nombreArchivo = "estrategia_" + nombreArchivo;
        }
        if (!nombreArchivo.toLowerCase().endsWith(".txt")) {
            nombreArchivo = nombreArchivo + ".txt";
        }

        try (FileWriter fw = new FileWriter(nombreArchivo);
             BufferedWriter bw = new BufferedWriter(fw)) {
            for (String mov : movimientos) {
                bw.write(mov);
                bw.newLine();
            }
        }
    }

    // Guarda o actualiza un robot dentro de la lista serializada en robots.bin
    public static void guardarRobotBinario(Robot nuevoRobot) throws IOException {
        nuevoRobot.reiniciarParaCombate();
        List<Robot> listaRobots = cargarListaRobotsBinario();

        boolean reemplazado = false;
        for (int i = 0; i < listaRobots.size(); i++) {
            if (listaRobots.get(i).getNombre().equalsIgnoreCase(nuevoRobot.getNombre())) {
                listaRobots.set(i, nuevoRobot);
                reemplazado = true;
                break;
            }
        }
        if (!reemplazado) {
            listaRobots.add(nuevoRobot);
        }

        try (FileOutputStream fos = new FileOutputStream(ARCHIVO_ROBOT_BIN);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(listaRobots);
        }
    }

    // Carga todos los robots guardados en robots.bin
    public static List<Robot> cargarListaRobotsBinario() {
        List<Robot> lista = new ArrayList<>();
        File file = new File(ARCHIVO_ROBOT_BIN);
        if (!file.exists()) {
            return lista;
        }

        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                lista = (List<Robot>) obj;
                for (Robot r : lista) {
                    r.reiniciarParaCombate();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Aviso leyendo robots.bin: " + e.getMessage());
        }
        return lista;
    }
}
