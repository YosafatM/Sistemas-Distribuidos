import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Pi {
    static final Object obj = new Object();
    static float pi = 0;

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
                float suma = entrada.readFloat();

                synchronized (obj) {
                    pi = suma + pi;
                }

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
            System.err.println("java PI <nodo>");
            System.exit(0);
        }

        int nodo = Integer.parseInt(args[0]);

        if (nodo == 0) {
            ServerSocket servidor = new ServerSocket(10000);
            Worker[] v = new Worker[4];

            for (int i = 0; i < 4; i++) {
                Socket conexion = servidor.accept();
                v[i] = new Worker(conexion);
                v[i].start();
            }

            for (int i = 0; i < 4; i++) {
                v[i].join();
            }

            System.out.println(pi);
        } else {
            Socket conexion;

            while (true) {
                try {
                    conexion = new Socket("localhost", 10000);
                    break;
                } catch (IOException e) {
                    Thread.sleep(100);
                }
            }

            DataInputStream entrada = new DataInputStream(conexion.getInputStream());
            DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
            float suma = 0;

            for (int i = 0; i < 1000000; i++) {
                suma = (float) (4.0 / (8 * i + 2 * (nodo - 2) + 3) + suma);
            }

            suma = nodo % 2 == 0 ? -suma : suma;
            salida.writeFloat(suma);
            entrada.close();
            salida.close();
            conexion.close();
        }
    }
}
