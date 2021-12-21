import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Tiempo {
    static String[] hosts;
    static int[] puertos;
    static int num_nodos;
    static int nodo;
    static long reloj_logico;
    static Object lock = new Object();

    static void envia_mensaje(long tiempo_logico, String host, int puerto) throws InterruptedException {
        while (true) {
            try {
                Socket cliente = new Socket(host, puerto);
                DataOutputStream salida = new DataOutputStream(cliente.getOutputStream());
                salida.writeLong(tiempo_logico);
                cliente.close();
                break;
            } catch (IOException e) {
                Thread.sleep(100);
            }
        }
    }

    static class Reloj extends Thread {
        @Override
        public void run() {
            while (true) {
                synchronized (lock) {
                    System.out.println(reloj_logico);

                    switch (nodo) {
                        case 0 -> reloj_logico += 4;
                        case 1 -> reloj_logico += 5;
                        case 2 -> reloj_logico += 6;
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class Worker extends Thread {
        Socket cliente;

        Worker(Socket cliente) {
            this.cliente = cliente;
        }

        @Override
        public void run() {
            long tiempo_recibido;

            try {
                DataInputStream entrada = new DataInputStream(cliente.getInputStream());
                tiempo_recibido = entrada.readLong();

                if (tiempo_recibido >= reloj_logico)
                    synchronized (lock) {
                        reloj_logico = tiempo_recibido + 1;
                    }
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
            System.err.println("Correr como: $java Tiempo <Num Nodo> <ip>:<puerto> ... <ip>:<puerto>");
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
            envia_mensaje(-1, hosts[i], puertos[i]);

        new Reloj().start();
    }
}
