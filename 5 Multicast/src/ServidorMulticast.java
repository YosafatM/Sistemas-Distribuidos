import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ServidorMulticast {
    static void envia_mensaje(byte[] buffer, String ip, int puerto) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress ip_grupo = InetAddress.getByName(ip);
        DatagramPacket paquete = new DatagramPacket(buffer, buffer.length, ip_grupo, puerto);
        socket.send(paquete);
        socket.close();
    }

    public static void main(String[] args) {
        try {
            envia_mensaje("Hola".getBytes(StandardCharsets.UTF_8), "230.0.0.0", 50000);

            ByteBuffer buffer = ByteBuffer.allocate(4 * 8);
            buffer.putDouble(1.1);
            buffer.putDouble(1.2);
            buffer.putDouble(1.3);
            buffer.putDouble(1.4);
            envia_mensaje(buffer.array(), "230.0.0.0", 50000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
