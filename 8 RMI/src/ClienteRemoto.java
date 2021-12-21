import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class ClienteRemoto {
    public static void main(String[] args) throws MalformedURLException, NotBoundException, RemoteException {
        String url = "rmi://10.0.0.5/prueba";
        Remoto remoto = (Remoto) Naming.lookup(url);
        System.out.println(remoto.mayusculas("hola"));
        System.out.println(remoto.suma(1, 2));

        int[][] m = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        System.out.println(remoto.checksum(m));
    }
}
