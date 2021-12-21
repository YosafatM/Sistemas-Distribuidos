import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ClaseRemota extends UnicastRemoteObject implements Remoto {
    protected ClaseRemota() throws RemoteException {
        super();
    }

    @Override
    public String mayusculas(String name) throws RemoteException {
        return name.toUpperCase();
    }

    @Override
    public int suma(int a, int b) throws RemoteException {
        return a + b;
    }

    @Override
    public long checksum(int[][] m) throws RemoteException {
        long total = 0;

        for (int[] row:m)
            for (int dato:row)
                total += dato;

        return total;
    }
}
