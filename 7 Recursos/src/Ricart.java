import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.LinkedList;

public class Ricart {
    static final int K_SIZE = 64 + 2*32;
    static String[] hosts;
    static int[] puertos;
    static int num_nodos;
    static int nodo;

    static long reloj_logico;
    static Object lock = new Object();

    static int confirmados = 0;
    static LinkedList<Integer> cola = new LinkedList<>();

    enum Respuesta {
        OK(0), SOLICITUD(1), SALUDO(2);

        int valor;

        Respuesta(int valor) {
            this.valor = valor;
        }

        static Respuesta get(int valor){
            Respuesta label;

            switch (valor) {
                case 0 -> label = OK;
                case 1 -> label = SOLICITUD;
                default -> label = SALUDO;
            }

            return label;
        }
    }

    static void leer_mensaje(DataInputStream f, byte[] b, int posicion, int longitud) throws IOException {
        while (longitud > 0) {
            int n = f.read(b, posicion, longitud);
            posicion += n;
            longitud -= n;
        }
    }

    static void envia_mensaje(long tiempo_logico, Respuesta tipo, int nodo_destinatario) throws InterruptedException {
        while (true) {
            try {
                Socket cliente = new Socket(hosts[nodo_destinatario], puertos[nodo_destinatario]);
                DataOutputStream salida = new DataOutputStream(cliente.getOutputStream());

                ByteBuffer b = ByteBuffer.allocate(K_SIZE);
                byte[] array = b.array();
                b.putLong(tiempo_logico);
                b.putInt(nodo);
                b.putInt(tipo.valor);
                salida.write(array, 0, array.length);

                cliente.close();
                break;
            } catch (IOException e) {
                Thread.sleep(100);
            }
        }
    }

    static void solicita_recurso() throws InterruptedException {
        for (int i = 1; i < num_nodos; i++)
            envia_mensaje(-1, Respuesta.SOLICITUD, i);
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
                    switch (nodo) {
                        case 0 -> {if (reloj_logico == 8) solicita_recurso();}
                        case 1 -> {if (reloj_logico == 10) solicita_recurso();}
                        case 2 -> {if (reloj_logico == 12) solicita_recurso();}
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (reloj_logico == 8 && nodo == 0) {
                    try {
                        solicita_recurso();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
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
            try {
                DataInputStream entrada = new DataInputStream(cliente.getInputStream());

                byte[] array = new byte[K_SIZE];
                leer_mensaje(entrada, array, 0, K_SIZE);
                ByteBuffer b = ByteBuffer.wrap(array);
                long tiempo_recibido = b.getLong();
                int nodo_remitente = b.getInt();
                Respuesta tipo = Respuesta.get(b.getInt());

                // Ricart
                if (tiempo_recibido < reloj_logico && tipo == Respuesta.SOLICITUD)
                    envia_mensaje(reloj_logico, Respuesta.OK, nodo_remitente);
                else
                    cola.add(nodo_remitente);

                if (tipo == Respuesta.OK) {
                    confirmados += 1;

                    if (confirmados == num_nodos) {
                        confirmados = 0;
                        Thread.sleep(3000);
                        envia_mensaje(tiempo_recibido, cola.getFirst());
                    }
                }

                // Lamport
                if (tiempo_recibido >= reloj_logico)
                    synchronized (lock) {
                        reloj_logico = tiempo_recibido + 1;
                    }

                entrada.close();
            } catch (IOException | InterruptedException e) {
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
            System.err.println("Correr como: $java Ricart <Num Nodo> <ip>:<puerto> ... <ip>:<puerto>");
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
            envia_mensaje(-1, Respuesta.SALUDO, i);

        new Reloj().start();
    }
}
