import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

class Cliente{


 public static void main(String [] args) throws Exception{
  URL url = new URL("http://sisdis.sytes.net:8080/Servicio/rest/ws/expresion");
  HttpURLConnection conexion = (HttpURLConnection) url.openConnection();

  conexion.setDoOutput(true);

  conexion.setRequestMethod("POST");

  conexion.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
  
  String parametros = "a=" + URLEncoder.encode("b+(c&d)*(b=d)%c", StandardCharsets.UTF_8);
  parametros += "&b=" + URLEncoder.encode("30", StandardCharsets.UTF_8);
  parametros += "&c=" + URLEncoder.encode("60", StandardCharsets.UTF_8);
  parametros += "&d=" + URLEncoder.encode("59", StandardCharsets.UTF_8);

  OutputStream os = conexion.getOutputStream();

  os.write(parametros.getBytes());
  os.flush();

  if(conexion.getResponseCode() == 200){

   BufferedReader br = new BufferedReader(new InputStreamReader((conexion.getInputStream())));

   String respuesta;

   while ((respuesta = br.readLine()) != null ) System.out.println(respuesta);
   
  }else{
   BufferedReader br = new BufferedReader(new InputStreamReader((conexion.getErrorStream())));
   String respuesta;
   while((respuesta = br.readLine()) != null) System.out.println(respuesta);
  }
  conexion.disconnect();
 }
}
