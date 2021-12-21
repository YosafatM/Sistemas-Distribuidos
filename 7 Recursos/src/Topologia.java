import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Topologia {
    static String[] hosts;
    static int[] puertos;
    static int num_nodos;
    static int nodo;

    static class Worker extends Thread {
        Socket cliente;

        Worker(Socket cliente) {
            this.cliente = cliente;
        }

        @Override
        public void run() {
            System.out.println("Inició el el thread Worker");
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

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Correr como: $java Topologia <Num Nodo> <ip>:<puerto> ... <ip>:<puerto>");
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
    }
}
