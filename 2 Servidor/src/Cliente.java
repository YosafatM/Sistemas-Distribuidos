import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Cliente {

    static void read(DataInputStream f, byte[] b, int posicion, int longitud) throws Exception{
        while(longitud > 0){
            int n = f.read(b,posicion,longitud);
            posicion +=n;
            longitud -=n;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        Socket conexion = new Socket("localhost", 50000);
        DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
        DataInputStream entrada = new DataInputStream(conexion.getInputStream());
        salida.writeInt(123);
        salida.writeDouble(1234567890.1234567890);
        salida.write("Hola".getBytes());

        byte[] buffer = new byte[4];
        read(entrada,buffer,0,4);
        System.out.println(new String(buffer, "UTF-8"));

        int numDoubles = 10000;
        //writeDouble
        long iniciowD = System.currentTimeMillis();
        for(int i = 0; i < numDoubles; i++)salida.writeDouble(1.0 + i);
        long finwD = System.currentTimeMillis();
        System.out.println("Se tardo en enviar " + (finwD-iniciowD) + " milisegundos usando writeDouble");
        //Fin writeDouble

        //ByteBuffer
        ByteBuffer b = ByteBuffer.allocate(numDoubles*8); //5 doubles * tamaño del double, 8 bytes
        for(int i = 0; i < numDoubles; i++)b.putDouble(1.0 + i);
        byte[] a = b.array();
        long inicio = System.currentTimeMillis();
        salida.write(a);
        long fin = System.currentTimeMillis();
        System.out.println("Se tardo en enviar " + (fin-inicio) + " milisegundos usando ByteBuffer");
        //Fin ByteBuffer
        salida.close();
        entrada.close();
        conexion.close();
    }

}
