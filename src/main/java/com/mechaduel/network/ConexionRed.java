package com.mechaduel.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ConexionRed {
    private ServerSocket serverSocket;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public void iniciarServidor(int puerto) throws IOException {
        serverSocket = new ServerSocket(puerto);
        socket = serverSocket.accept();
        inicializarFlujos();
    }

    public void conectarCliente(String ip, int puerto) throws IOException {
        socket = new Socket(ip, puerto);
        inicializarFlujos();
    }

    private void inicializarFlujos() throws IOException {
        InputStreamReader reader = new InputStreamReader(socket.getInputStream());
        in = new BufferedReader(reader);
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void enviar(String mensaje) {
        if (out != null) {
            out.println(mensaje);
        }
    }

    public String recibir() throws IOException {
        if (in != null) {
            return in.readLine();
        }
        return null;
    }

    public void cerrar() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            System.out.println("Error cerrando conexión: " + e.getMessage());
        }
    }
}
