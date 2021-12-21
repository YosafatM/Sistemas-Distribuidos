import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ClienteMulticast {
    static byte[] recibe_mensaje(MulticastSocket socket, int longitud) throws IOException {
        byte[] buffer = new byte[longitud];
        DatagramPacket paquete = new DatagramPacket(buffer, longitud);
        socket.receive(paquete);
        return paquete.getData();
    }

    public static void main(String[] args) {
        try {
            InetAddress ip_grupo = InetAddress.getByName("230.0.0.0");
            MulticastSocket socket = new MulticastSocket(50000);
            socket.joinGroup(ip_grupo);

            byte[] a = recibe_mensaje(socket, 4);
            System.out.println(new String(a, StandardCharsets.UTF_8));

            byte[] buffer = recibe_mensaje(socket, 4 * 8);
            ByteBuffer b = ByteBuffer.wrap(buffer);

            for (int i = 0; i < 4; i++) {
                System.out.println(b.getDouble());
            }

            socket.leaveGroup(ip_grupo);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
