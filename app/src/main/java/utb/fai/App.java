package utb.fai;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class App {

    private static final String SERVER_MODE = "server";
    private static final int SERVER_PORT = 9999;
    private static final int DEFAULT_PORT = 23;
    private static final int MIN_PORT = 0;
    private static final int MAX_PORT = 65535;
    private static final int INVALID_PORT = -1;

    public static void main(String[] args) {
        // Jediný parametr "server" spustí testovací server z hodiny
        if (isServerMode(args)) {
            runServer();
            return;
        }

        if (!hasValidArguments(args)) {
            System.err.println("Pouziti: App <ip adresa> [port] | App server");
            return;
        }

        String ip = args[0];
        int port = readPort(args);
        if (port == INVALID_PORT) {
            return;
        }

        TcpClient client = new TcpClient(ip, port);
        client.run();
    }

    private static boolean isServerMode(String[] args) {
        return args.length == 1 && SERVER_MODE.equals(args[0]);
    }

    private static boolean hasValidArguments(String[] args) {
        boolean validCount = args.length == 1 || args.length == 2;
        return validCount && !args[0].isBlank();
    }

    // Vrátí port z parametrů, nebo INVALID_PORT po vypsání chyby
    private static int readPort(String[] args) {
        // Bez zadaného portu se použije standardní telnet port
        if (args.length < 2) {
            return DEFAULT_PORT;
        }

        int port;
        try {
            port = Integer.parseInt(args[1].trim());
        } catch (NumberFormatException e) {
            System.err.println("Neplatny port: " + args[1]);
            return INVALID_PORT;
        }

        if (port < MIN_PORT || port > MAX_PORT) {
            System.err.println("Port mimo rozsah " + MIN_PORT + "-" + MAX_PORT + ": " + port);
            return INVALID_PORT;
        }

        return port;
    }

    // Jednoduchý server z hodiny, každého klienta obsluhuje vlastní vlákno
    private static void runServer() {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server bezi na portu:" + serverSocket.getLocalPort());

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Pripojil se novy klient: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket);
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
