import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Token {
    static DataInputStream entrada;
    static DataOutputStream salida;
    static boolean inicio = true;
    static String ip;
    static int nodo;
    static long token;

    static class Worker extends Thread {
        @Override
        public void run() {
            try {
                SSLServerSocketFactory socket_factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
                ServerSocket servidor = socket_factory.createServerSocket(20000 + nodo);
                Socket conexion = servidor.accept();
                entrada = new DataInputStream(conexion.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        System.setProperty("javax.net.ssl.keyStore", "keystore_servidor.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "1234567");
        System.setProperty("javax.net.ssl.trustStore", "keystore_cliente.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456");

        if (args.length != 2) {
            System.err.println("Se debe pasar como " +
                    "parametros el numero de nodo y la " +
                    "ip del siguiente nodo");
            System.exit(1);
        }

        nodo = Integer.parseInt(args[0]);
        ip = args[1];

        Worker w = new Worker();
        w.start();
        SSLSocketFactory cliente = (SSLSocketFactory) SSLSocketFactory.getDefault();
        Socket conexion;

        while (true) {
            try {
                conexion = cliente.createSocket(ip, 20000 + (nodo+1)%4);
                break;
            } catch (IOException e) {
                Thread.sleep(500);
            }
        }

        salida = new DataOutputStream(conexion.getOutputStream());
        w.join();

        while (true) {
            if (nodo == 0) {
                if (inicio) {
                    inicio = false;
                    token = 1;
                } else {
                    token = entrada.readLong() + 1;
                    System.out.println(nodo + " " + token);
                }
            } else {
                token = entrada.readLong() + 1;
                System.out.println(nodo + " " + token);
            }

            if (nodo == 0 && token >= 1000)
                break;

            salida.writeLong(token);

            if (nodo > 0 && token >= 998)
                break;
        }

        entrada.close();
        salida.close();
        conexion.close();
    }
}
