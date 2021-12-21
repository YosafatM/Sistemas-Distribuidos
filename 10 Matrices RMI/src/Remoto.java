import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Remoto extends Remote {
    static final int N = 6;

    public int[][] multiplica_matrices(int[][] A, int[][] B) throws RemoteException;
}
