import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Numeros {
    static void read(DataInputStream f, byte[] b, int posicion, int longitud) throws IOException {
        while (longitud > 0) {
            int n = f.read(b, posicion, longitud);
            posicion += n;
            longitud -= n;
        }
    }

    static class Worker extends Thread {
        Socket conexion;

        Worker(Socket conexion) {
            this.conexion = conexion;
        }

        @Override
        public void run() {
            try {
                DataInputStream entrada = new DataInputStream(conexion.getInputStream());
                DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
                byte[] a = new byte[100*4];
                int sum = 0;

                read(entrada, a, 0,100*4);
                ByteBuffer b = ByteBuffer.wrap(a);

                for (int i = 0; i < 100; i++) {
                    sum = sum + b.getInt();
                }

                System.out.println(sum);
                entrada.close();
                salida.close();
                conexion.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Uso:");
            System.err.println("java Numeros <nodo>");
            System.exit(0);
        }

        int nodo = Integer.parseInt(args[0]);

        if (nodo == 0) {
            ServerSocket servidor = new ServerSocket(50000);
            Socket conexion = servidor.accept();
            Worker v = new Worker(conexion);
            v.start();
            v.join();
        } else {
            Socket conexion;

            while (true) {
                try {
                    conexion = new Socket("localhost", 50000);
                    break;
                } catch (IOException e) {
                    Thread.sleep(100);
                }
            }

            DataInputStream entrada = new DataInputStream(conexion.getInputStream());
            DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
            ByteBuffer b = ByteBuffer.allocate(100*4);
            int i = 0;
            int actual = 1;

            while (i < 100) {
                if (actual % 2 == 1) {
                    b.putInt(actual);
                    i++;
                }

                actual++;
            }

            salida.write(b.array());
            entrada.close();
            salida.close();
            conexion.close();
        }
    }
}
