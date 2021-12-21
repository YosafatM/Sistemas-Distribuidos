import javax.net.ssl.SSLSocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

class ClienteSSL {
    public static void main(String[] args) throws Exception {
        System.setProperty("javax.net.ssl.trustStore", "C:/Users/yosaf/Desktop/Septimo/Distribuidos/Certificado/keystore_cliente.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456");

        SSLSocketFactory cliente = (SSLSocketFactory) SSLSocketFactory.getDefault();
        Socket conexion = cliente.createSocket("localhost", 50000);
        DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
        DataInputStream entrada = new DataInputStream(conexion.getInputStream());
        salida.writeDouble(123456789.123456789);
        salida.close();
        entrada.close();
        conexion.close();
    }
}
