import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Bully {
    static String[] hosts;
    static int[] puertos;
    static int num_nodos;
    static int nodo;

    static final int K_SIZE = 64;
    static final String K_COORDINADOR = "COORDINADOR";
    static final String K_ELECCION = "ELECCION";
    static final String K_OK = "OK";
    static int coordinador_actual = 0;
    static int contador_ok = 0;

    static void envia_mensaje(int nodo_destinatario) throws InterruptedException {
        while (true) {
            try {
                String host = hosts[nodo_destinatario];
                int puerto = puertos[nodo_destinatario];
                Socket cliente = new Socket(host, puerto);
                DataOutputStream salida = new DataOutputStream(cliente.getOutputStream());

                ByteBuffer b = ByteBuffer.allocate(K_SIZE);
                byte[] array = b.array();
                b.putLong(-1);
                salida.write(array, 0, array.length);

                cliente.close();
                break;
            } catch (IOException e) {
                Thread.sleep(100);
            }
        }
    }

    static String envia_mensaje_eleccion(String host, int puerto) {
        try {
            Socket servidor = new Socket(host, puerto);
            DataOutputStream salida = new DataOutputStream(servidor.getOutputStream());
            DataInputStream entrada = new DataInputStream(servidor.getInputStream());

            salida.writeUTF(K_ELECCION);
            return entrada.readUTF();
        } catch (IOException e) {
            return "";
        }
    }

    static void envia_mensaje_coordinador(String host, int puerto) {
        try {
            Socket servidor = new Socket(host, puerto);
            DataOutputStream salida = new DataOutputStream(servidor.getOutputStream());

            salida.writeUTF(K_COORDINADOR);
            salida.writeInt(nodo);
            servidor.close();
        } catch (IOException ignored) {

        }
    }

    static void eleccion(int nodo) {
        Object lock = new Object();
        String mensaje;

        for (int i = nodo; i < num_nodos; i++) {
            mensaje = envia_mensaje_eleccion(hosts[i], puertos[i]);

            if (mensaje.equals(K_OK)) synchronized (lock) { contador_ok++; }

            if (contador_ok == 0)
                for (int j = 0; j < num_nodos; j++)
                    envia_mensaje_coordinador(hosts[i], puertos[i]);
        }
    }

    static class Worker extends Thread {
        Socket cliente;

        Worker(Socket cliente) {
            this.cliente = cliente;
        }

        @Override
        public void run() {
            System.out.println("Inició el el thread Worker");
            try {
                DataInputStream entrada = new DataInputStream(cliente.getInputStream());
                DataOutputStream salida = new DataOutputStream(cliente.getOutputStream());
                String mensaje = entrada.readUTF();

                if (mensaje.equals(K_ELECCION)) {
                    salida.writeUTF(K_OK);
                    eleccion(nodo);
                } else
                    coordinador_actual = entrada.readInt();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class Servidor extends Thread {
        @Override
        public void run() {
            try {
                ServerSocket servidor = new ServerSocket(puertos[nodo]);

                while (true) {
                    Socket cliente = servidor.accept();
                    new Worker(cliente).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        if (args.length < 2) {
            System.err.println("Correr como: $java Bully <Num Nodo> <ip>:<puerto> ... <ip>:<puerto>");
            System.exit(1);
        }

        nodo = Integer.parseInt(args[0]);
        num_nodos = args.length - 1;
        hosts = new String[num_nodos];
        puertos = new int[num_nodos];

        for (int i = 1; i < args.length; i++) {
            String[] partes = args[i].split(":");
            hosts[i-1] = partes[0];
            puertos[i-1] = Integer.parseInt(partes[1]);
        }

        new Servidor().start();

        for (int i = 1; i < num_nodos; i++)
            envia_mensaje(i);
    }
}
