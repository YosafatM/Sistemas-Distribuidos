import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;

public class ServidorRemoto {
    public static void main(String[] args) throws RemoteException, MalformedURLException {
        String url = "rmi://localhost/prueba";
        ClaseRemota remota = new ClaseRemota();
        Naming.rebind(url, remota);
    }
}
