import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;

public class Servidor {
    public static void main(String[] args) throws MalformedURLException, RemoteException {
        String url = "rmi://localhost/prueba";
        Multiplicador remota = new Multiplicador();
        Naming.rebind(url, remota);
    }
}
