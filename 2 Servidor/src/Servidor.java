import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

class Servidor{
    static void read(DataInputStream f, byte[] b, int posicion, int longitud) throws IOException {
        while (longitud > 0) {
            int n = f.read(b, posicion, longitud);
            posicion += n;
            longitud -= n;
        }
    }

    public static void main(String[] args) throws IOException {
        try {
            ServerSocket servidor = new ServerSocket(5000);
            Socket conexion = servidor.accept();
            DataOutputStream salida = new DataOutputStream( conexion.getOutputStream());
            DataInputStream entrada = new DataInputStream( conexion.getInputStream());

            long start = System.currentTimeMillis();
            for (int i = 0; i < 10000; i++)
                entrada.readDouble();
            System.out.println("Empaquetado: " + (System.currentTimeMillis() - start));

            byte[] a = new byte[10000*8];

            start = System.currentTimeMillis();
            read(entrada, a, 0,10000*8);
            ByteBuffer b = ByteBuffer.wrap(a);
            System.out.println("Byte Buffer: " + (System.currentTimeMillis() - start));

            salida.close();
            entrada.close();
            conexion.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}