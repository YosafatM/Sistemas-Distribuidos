import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class Multiplicador {
    static private final int N = 10;
    static private final int K_PORT = 50000;
    static private final String IP = "104.211.54.13";
    static private final int K_NODOS_CLIENTE = 4;

    private static class Matriz {
        private static long checksum(long[][] matriz) {
            long sum = 0;

            for (long[] row : matriz)
                for (long element : row)
                    sum += element;

            return sum;
        }

        private static long[][] inicializar(boolean isA) {
            long[][] matriz = new long[N][N];

            for (int i = 0; i < N; i++)
                for (int j = 0; j < N; j++)
                    matriz[i][j] = isA ? i+2L*j : i-2L*j;

            return matriz;
        }

        private static long[][] trasponer(long[][] matriz) {
            for (int i = 0; i < matriz.length; i++)
                for (int j = 0; j < i; j++)
                {
                    long x = matriz[i][j];
                    matriz[i][j] = matriz[j][i];
                    matriz[j][i] = x;
                }

            return matriz;
        }

        private static long[][] partir(long[][] matriz, boolean superior) {
            int size = matriz.length/2;
            long[][] particion = new long[size][matriz.length];

            System.arraycopy(matriz, superior ? 0 : size, particion, 0, size);

            return particion;
        }

        private static long[][] unir(long[][] P1, long[][] P2, long[][] P3, long[][] P4) {
            long[][] matriz = new long[N][N];

            for (int i = 0; i < N / 2; i++) {
                for (int j = 0; j < N / 2; j++) {
                    matriz[i][j] = P1[i][j];
                    matriz[i + N/2][j] = P2[i][j];
                    matriz[i][j + N/2] = P3[i][j];
                    matriz[i + N/2][j + N/2] = P4[i][j];
                }
            }

            return matriz;
        }

        private static long[][] multiplicar(long[][] A, long[][] B) {
            int filasA = A.length;
            int filasB = B.length;
            int columnasB = B[0].length;
            long[][] C = new long[filasA][filasA];

            for (int i = 0; i < filasA; i++)
                for (int j = 0; j < filasB; j++)
                    for (int k = 0; k < columnasB; k++)
                        C[i][j] += A[i][k] * B[j][k];

            return C;
        }

        private static void imprimir(long[][] matriz) {
            for (long[] row : matriz)
                System.out.println(Arrays.toString(row));
        }
    }

    private static class Servidor {
        private final ServerSocket servidor;
        private final Socket[] clientes = new Socket[K_NODOS_CLIENTE];

        Servidor(int port) throws IOException {
            servidor = new ServerSocket(port);

            for (int i = 0; i < 4; i++) {
                clientes[i] = servidor.accept();
            }
        }

        private void cerrar() throws IOException {
            for (Socket cliente : clientes)
                cliente.close();

            servidor.close();
        }

        private long[][] recibirMatriz(int nodo) throws IOException, ClassNotFoundException {
            ObjectInputStream entrada = new ObjectInputStream(clientes[nodo-1].getInputStream());
            return (long[][]) entrada.readObject();
        }

        private void enviarMatriz(long[][] matriz, int nodo) throws IOException {
            ObjectOutputStream salida = new ObjectOutputStream(clientes[nodo-1].getOutputStream());
            salida.writeObject(matriz);
        }
    }

    private static class Cliente {
        private Socket cliente;

        Cliente(String ip, int port) throws InterruptedException {
            while (true)
                try {
                    cliente = new Socket(ip, port);
                    break;
                } catch (IOException e) {
                    Thread.sleep(1000);
                }
        }

        private long[][] recibirMatriz() throws IOException, ClassNotFoundException {
            ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());
            return (long[][]) entrada.readObject();
        }

        private void enviarMatriz(long[][] C) throws IOException {
            ObjectOutputStream salida = new ObjectOutputStream(cliente.getOutputStream());
            salida.writeObject(C);
            salida.close();
            cliente.close();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        if (args.length < 1) {
            System.err.println("Falta el número de nodo");
            System.exit(1);
        }

        int nodo = Integer.parseInt(args[0]);

        if (nodo == 0) {
            Servidor conexion = new Servidor(K_PORT);
            long[][] A, B, C, A1, A2, B1, B2, C1, C2, C3, C4;
            A = Matriz.inicializar(true);
            B = Matriz.trasponer(Matriz.inicializar(false));

            A1 = Matriz.partir(A, true);
            A2 = Matriz.partir(A, false);
            B1 = Matriz.partir(B, true);
            B2 = Matriz.partir(B, false);

            // Para el nodo 1
            conexion.enviarMatriz(A1, 1);
            conexion.enviarMatriz(B1, 1);

            // Para el nodo 2
            conexion.enviarMatriz(A1, 2);
            conexion.enviarMatriz(B2, 2);

            // Para el nodo 3
            conexion.enviarMatriz(A2, 3);
            conexion.enviarMatriz(B1, 3);

            // Para el nodo 4
            conexion.enviarMatriz(A2, 4);
            conexion.enviarMatriz(B2, 4);

            C1 = conexion.recibirMatriz(1);
            C2 = conexion.recibirMatriz(2);
            C3 = conexion.recibirMatriz(3);
            C4 = conexion.recibirMatriz(4);
            C = Matriz.unir(C1, C2, C3, C4);
            System.out.println("Checksum: " + Matriz.checksum(C));

            if (N == 10) {
                System.out.println("\nA");
                Matriz.imprimir(A);

                System.out.println("\nB");
                Matriz.imprimir(B);

                System.out.println("\nC");
                Matriz.imprimir(C);
            }
            conexion.cerrar();
        } else {
            Cliente conexion = new Cliente(IP, K_PORT);
            long[][] particionA = conexion.recibirMatriz();
            long[][] particionB = conexion.recibirMatriz();
            long[][] particionC = Matriz.multiplicar(particionA, particionB);
            conexion.enviarMatriz(particionC);
        }
    }
}
