import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Cliente {
    static void read(DataInputStream f, byte[] b, int posicion, int longitud) throws IOException {
        while (longitud > 0) {
            int n = f.read(b, posicion, longitud);
            posicion += n;
            longitud -= n;
        }
    }

    public static void main(String[] args) {
        try {
            Socket s = new Socket("localhost", 5000);
            DataOutputStream salida = new DataOutputStream(s.getOutputStream());
            DataInputStream entrada = new DataInputStream(s.getInputStream());

            long start = System.currentTimeMillis();
            for (int i = 0; i < 10000; i++)
                salida.writeDouble(i);
            System.out.println("Empaquetado: " + (System.currentTimeMillis() - start));

            ByteBuffer b = ByteBuffer.allocate(10000*8);

            for (int i = 0; i < 10000; i++)
                b.putDouble(i);

            start = System.currentTimeMillis();
            salida.write(b.array());
            System.out.println("Byte Buffer: " + (System.currentTimeMillis() - start));

            salida.close();
            entrada.close();
            s.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
