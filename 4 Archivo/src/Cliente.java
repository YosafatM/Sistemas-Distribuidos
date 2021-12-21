import javax.net.ssl.SSLSocketFactory;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Cliente {
    public static void main(String[] args) {
        System.setProperty("javax.net.ssl.trustStore", "keystore_cliente.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456");

        try {
            if (args.length != 1) {
                System.err.println("Debe pasar el argumento del nombre del archivo");
                System.exit(1);
            }

            String archivo = args[0];
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            Socket socket = factory.createSocket("localhost", 50000);
            DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
            salida.writeUTF(archivo);
            byte[] buffer = Cliente.lee_archivo(archivo);
            salida.writeInt(buffer.length);
            salida.write(buffer);
            System.out.println(new String(buffer, StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static byte[] lee_archivo(String archivo) throws IOException {
        FileInputStream f = new FileInputStream(archivo);
        byte[] buffer;

        try {
            buffer = new byte[f.available()];
            f.read(buffer);
        } finally {
            f.close();
        }

        return buffer;
    }
}
