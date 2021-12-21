package Prueba;

import java.util.Arrays;

public class Prueba {
    private static final int N = 9;

    private static class Matriz{
        private static double checksum(double[][] matriz) {
            double sum = 0;

            for (double[] row : matriz)
                for (double element : row)
                    sum += element;

            return sum;
        }

        private static void trasponer(double[][] matriz) {
            for (int i = 0; i < matriz.length; i++)
                for (int j = 0; j < i; j++) {
                    double x = matriz[i][j];
                    matriz[i][j] = matriz[j][i];
                    matriz[j][i] = x;
                }
        }

        static double[][] separa_matriz(double[][] A, int inicio)  {
            double[][] M = new double[N/3][N];
            for (int i = 0; i < N/3; i++)
                for (int j = 0; j < N; j++)
                    M[i][j] = A[i + inicio][j];
            return M;
        }

        static void acomoda_matriz(double[][] C,double[][] A,int renglon,int columna)  {
            for (int i = 0; i < N/3; i++)
                for (int j = 0; j < N/3; j++)
                    C[i + renglon][j + columna] = A[i][j];
        }

        static double[][] multiplica_matrices(double[][] A, double[][] B) {
            int N = A[0].length;

            double[][] C = new double[N/3][N/3];
            for (int i = 0; i < N/3; i++)
                for (int j = 0; j < N/3; j++)
                    for (int k = 0; k < N; k++)
                        C[i][j] += A[i][k] * B[j][k];
            return C;
        }
    }

    public static void main(String[] args) {
        double[][] A = new double[N][N];
        double[][] B = new double[N][N];

        // Inicializar matrices
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++) {
                A[i][j] = i + 4*j;
                B[i][j] = i - 4*j;
            }

        Matriz.trasponer(B);

        double[][] A1 = Matriz.separa_matriz(A,0);
        double[][] A2 = Matriz.separa_matriz(A,N/3);
        double[][] A3 = Matriz.separa_matriz(A,2*N/3);
        double[][] B1 = Matriz.separa_matriz(B,0);
        double[][] B2 = Matriz.separa_matriz(B,N/3);
        double[][] B3 = Matriz.separa_matriz(B,2*N/3);

        double[][] C1 = Matriz.multiplica_matrices(A1,B1);
        double[][] C2 = Matriz.multiplica_matrices(A1,B2);
        double[][] C3 = Matriz.multiplica_matrices(A1,B3);
        double[][] C4 = Matriz.multiplica_matrices(A2,B1);
        double[][] C5 = Matriz.multiplica_matrices(A2,B2);
        double[][] C6 = Matriz.multiplica_matrices(A2,B3);
        double[][] C7 = Matriz.multiplica_matrices(A3,B1);
        double[][] C8 = Matriz.multiplica_matrices(A3,B2);
        double[][] C9 = Matriz.multiplica_matrices(A3,B3);

        double[][] C = new double[N][N];
        Matriz.acomoda_matriz(C,C1,0,0);
        Matriz.acomoda_matriz(C,C2,0,N/3);
        Matriz.acomoda_matriz(C,C3,0,2*N/3);
        Matriz.acomoda_matriz(C,C4,N/3,0);
        Matriz.acomoda_matriz(C,C5,N/3,N/3);
        Matriz.acomoda_matriz(C,C6,N/3,2*N/3);
        Matriz.acomoda_matriz(C,C7,2*N/3,0);
        Matriz.acomoda_matriz(C,C8,2*N/3,N/3);
        Matriz.acomoda_matriz(C,C9,2*N/3,2*N/3);

        if (N == 9) {
            System.out.println("Matriz A");
            for (double[] fila : A)
                System.out.println(Arrays.toString(fila));

            System.out.println("\nMatriz B");
            for (double[] fila : B)
                System.out.println(Arrays.toString(fila));

            System.out.println("\nMatriz C");
            for (double[] fila : C)
                System.out.println(Arrays.toString(fila));
        }

        System.out.println(Matriz.checksum(C));
    }
}
