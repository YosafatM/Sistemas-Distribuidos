import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;


public class Cliente {
    private static final int N = Remoto.N;

    private static int checksum(int[][] matriz) {
        int sum = 0;

        for (int[] row : matriz)
            for (int element : row)
                sum += element;

        return sum;
    }

    private static void trasponer(int[][] matriz) {
        for (int i = 0; i < matriz.length; i++)
            for (int j = 0; j < i; j++) {
                int x = matriz[i][j];
                matriz[i][j] = matriz[j][i];
                matriz[j][i] = x;
            }
    }

    static int[][] separa_matriz(int[][] A,int inicio)  {
        int[][] M = new int[N/2][N];
        for (int i = 0; i < N/2; i++)
            for (int j = 0; j < N; j++)
                M[i][j] = A[i + inicio][j];
        return M;
    }

    static void acomoda_matriz(int[][] C,int[][] A,int renglon,int columna)  {
        for (int i = 0; i < N/2; i++)
            for (int j = 0; j < N/2; j++)
                C[i + renglon][j + columna] = A[i][j];
    }

    public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException {
        int[][] A = new int[N][N];
        int[][] B = new int[N][N];

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                A[i][j] = 2*i - j;
                B[i][j] = i + 2*j;
            }
        }

        trasponer(B);

        String url = "rmi://localhost/prueba";
        Remoto remoto = (Remoto) Naming.lookup(url);

        int[][] A1 = separa_matriz(A,0);
        int[][] A2 = separa_matriz(A,N/2);
        int[][] B1 = separa_matriz(B,0);
        int[][] B2 = separa_matriz(B,N/2);

        int[][] C1 = remoto.multiplica_matrices(A1,B1);
        int[][] C2 = remoto.multiplica_matrices(A1,B2);
        int[][] C3 = remoto.multiplica_matrices(A2,B1);
        int[][] C4 = remoto.multiplica_matrices(A2,B2);

        int[][] C = new int[N][N];
        acomoda_matriz(C,C1,0,0);
        acomoda_matriz(C,C2,0,N/2);
        acomoda_matriz(C,C3,N/2,0);
        acomoda_matriz(C,C4,N/2,N/2);

        System.out.println(checksum(C));
    }
}
