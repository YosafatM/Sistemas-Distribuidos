import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Chat {
    static final String K_IP = "230.0.0.0";
    static final int K_PORT = 10000;
    static final int K_SIZE = 40;

    static void envia_mensaje_multicast(byte[] buffer, String ip, int puerto) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        socket.send(new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ip), puerto));
        socket.close();
    }

    static byte[] recibe_mensaje_multicast(MulticastSocket socket, int longitud_mensaje) throws IOException {
        byte[] buffer = new byte[longitud_mensaje];
        DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
        socket.receive(paquete);
        return paquete.getData();
    }

    static String format(String base) throws IOException {
        int space = K_SIZE - base.length();
        StringBuilder repeated = new StringBuilder();

        while (repeated.length() != space)
            repeated.append(" ");

        return base + repeated;
    }

    static class Worker extends Thread {
        MulticastSocket socket;

        Worker() throws IOException {
            InetAddress ip_grupo = InetAddress.getByName(K_IP);
            socket = new MulticastSocket(K_PORT);
            socket.joinGroup(ip_grupo);
        }

        public void run() {
            try {
                while (true) {
                    byte[] buffer = recibe_mensaje_multicast(socket, K_SIZE);

                    System.out.println(new String(buffer, "UTF-8"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new Worker().start();
        String nombre = args[0];
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Ingrese el mensaje a enviar:");
            String linea = scanner.nextLine();
            String mensaje = format(nombre + ":" + linea);
            envia_mensaje_multicast(mensaje.getBytes("UTF-8"), K_IP, K_PORT);
        }
    }
}
