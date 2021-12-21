import javax.net.ssl.SSLServerSocketFactory;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Servidor {
    static class Hilo extends Thread {
        private final Socket conexion;

        public Hilo(Socket conexion) {
            this.conexion = conexion;
        }

        @Override
        public void run() {
            try {
                DataInputStream entrada = new DataInputStream(conexion.getInputStream());
                String nombre = entrada.readUTF();
                int length = entrada.readInt();
                ByteBuffer buffer = ByteBuffer.allocate(length * 8);
                byte[] array = buffer.array();
                entrada.read(array);
                Servidor.escribe_archivo("./servidor/" + nombre, array);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static void escribe_archivo(String archivo, byte[] buffer) throws IOException {
        try (FileOutputStream f = new FileOutputStream(archivo)) {
            f.write(buffer);
        }
    }

    public static void main(String[] args) {
        System.setProperty("javax.net.ssl.keyStore", "keystore_servidor.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "1234567");

        try {
            SSLServerSocketFactory socket_factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            ServerSocket socket = socket_factory.createServerSocket(50000);

            while (true) {
                Hilo hilo = new Hilo(socket.accept());
                hilo.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
