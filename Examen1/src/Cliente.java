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
            Socket conexion = new Socket("sisdis.sytes.net", 50001);
            DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
            DataInputStream entrada = new DataInputStream(conexion.getInputStream());
            long t1 = 1632744960, t2, t3, t4, Tres;
            t2 = entrada.readLong();
            t3 = entrada.readLong();
            t4 = t3 + 3;
            Tres= ((t4-t1)-(t3-t2))/2;
            System.out.println(Tres+t3);
            salida.close();
            entrada.close();
            conexion.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
