package utb.fai;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;

public class TcpClient {

    private static final String QUIT_COMMAND = "/QUIT";
    private static final int SLEEP_MILLIS = 20;
    private static final int CONNECT_TIMEOUT_MILLIS = 5000;
    private static final int PROBE_TIMEOUT_MILLIS = 1;
    private static final int BUFFER_SIZE = 4096;
    private static final int NO_DATA = -2;

    private final String ip;
    private final int port;

    // Sdílený příznak, podle kterého se obě vlákna ukončí
    private final AtomicBoolean running = new AtomicBoolean(false);

    private Socket socket;

    public TcpClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void run() {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), CONNECT_TIMEOUT_MILLIS);
            // Krátký timeout slouží jen ke zjištění, že protistrana zavřela spojení
            socket.setSoTimeout(PROBE_TIMEOUT_MILLIS);
        } catch (IOException | IllegalArgumentException e) {
            String target = ip + ":" + port;
            System.err.println("Nelze se pripojit k " + target + " - " + e.getMessage());
            closeSocket();
            return;
        }

        running.set(true);

        Thread serverReader = new Thread(this::readFromServer, "server-reader");
        Thread readerThread = new Thread(this::readFromClient, "client-reader");

        // Démon vlákna nikdy neudrží proces naživu
        serverReader.setDaemon(true);
        readerThread.setDaemon(true);

        serverReader.start();
        readerThread.start();

        try {
            serverReader.join();
            readerThread.join();
        } catch (InterruptedException e) {
            running.set(false);
            Thread.currentThread().interrupt();
        } finally {
            closeSocket();
        }
    }

    // Vlákno pro příjem: čte bajty ze soketu a beze změny je vypisuje
    private void readFromServer() {
        try {
            InputStream in = socket.getInputStream();
            byte[] buffer = new byte[BUFFER_SIZE];

            while (running.get()) {
                if (in.available() > 0) {
                    int count = in.read(buffer);
                    if (count == -1) {
                        break;
                    }
                    System.out.write(buffer, 0, count);
                    System.out.flush();
                    continue;
                }

                // available() po zavření spojení vrací 0, konec proto ověří krátké čtení
                int probedByte = probe(in);
                if (probedByte == -1) {
                    break;
                }
                if (probedByte == NO_DATA) {
                    Thread.sleep(SLEEP_MILLIS);
                    continue;
                }

                System.out.write(probedByte);
                System.out.flush();
            }
        } catch (IOException e) {
            if (running.get()) {
                System.err.println("Chyba pri prijmu dat: " + e.getMessage());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            running.set(false);
        }
    }

    // Pokus o přečtení jednoho bajtu, který se po uplynutí timeoutu vzdá
    private int probe(InputStream in) throws IOException {
        try {
            return in.read();
        } catch (SocketTimeoutException e) {
            return NO_DATA;
        }
    }

    // Vlákno pro odesílání: sbírá znaky ze vstupu a po ENTER odešle celý řádek
    private void readFromClient() {
        try {
            InputStream in = System.in;
            OutputStream out = socket.getOutputStream();
            ByteArrayOutputStream line = new ByteArrayOutputStream();

            while (running.get()) {
                if (in.available() <= 0) {
                    Thread.sleep(SLEEP_MILLIS);
                    continue;
                }

                int character = in.read();
                if (character == -1) {
                    break;
                }

                line.write(character);
                if (character != '\n') {
                    continue;
                }

                byte[] lineBytes = line.toByteArray();
                line.reset();

                // Příkaz /QUIT se neodesílá, jen ukončí klienta
                if (isQuitCommand(lineBytes)) {
                    break;
                }

                out.write(lineBytes);
                out.flush();
            }
        } catch (IOException e) {
            if (running.get()) {
                System.err.println("Chyba pri odesilani dat: " + e.getMessage());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            running.set(false);
        }
    }

    private boolean isQuitCommand(byte[] lineBytes) {
        String text = new String(lineBytes, Charset.defaultCharset());
        return QUIT_COMMAND.equals(text.replace("\r", "").replace("\n", ""));
    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Chyba pri zavirani soketu: " + e.getMessage());
        }
    }
}
