package Cliente;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.google.gson.GsonBuilder;
import com.google.gson.Gson;

public class Cliente {
    static final String K_IP = "20.120.28.42";
    static final String K_BASE_URL = "http://"+ K_IP +":8080/Servicio/rest/ws/";

    //Peticiones
    public static void alta_usuario(Usuario usuario) throws IOException {
        URL url = new URL(K_BASE_URL + "alta_usuario");
        HttpURLConnection conexion = makePost(url, null, usuario);

        if (conexion.getResponseCode() == 200) {
            BufferedReader br = new BufferedReader(new InputStreamReader((conexion.getInputStream())));
            String response = br.readLine();

            if (response != null)
                System.out.println("Se dio de alta el usuario con Id: " + response);
        } else {
            BufferedReader br = new BufferedReader(new InputStreamReader((conexion.getErrorStream())));
            String response = br.readLine();
            if (response != null)
                System.out.println(response);
            System.out.println("Error HTTP: " + conexion.getResponseCode());
        }

        conexion.disconnect();

    }

    public static void consultar_usuario(int id_usuario) throws IOException {
        URL url = new URL(K_BASE_URL + "consulta_usuario");
        String parametros = "id_usuario=" + URLEncoder.encode(String.valueOf(id_usuario), StandardCharsets.UTF_8);
        HttpURLConnection conexion = makePost(url, parametros, null);

        if (conexion.getResponseCode() == 200) {
            BufferedReader br = new BufferedReader(new InputStreamReader((conexion.getInputStream())));
            BufferedReader esc = new BufferedReader(new InputStreamReader(System.in));
            String response;
            char res;

            Gson j = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
            response = br.readLine();
            if (response != null){
                Usuario user = (Usuario) j.fromJson(response, Usuario.class);
                System.out.println("Nombre: " + user.nombre);
                System.out.println("Apellido Paterno: " + user.apellido_paterno);
                System.out.println("Apellido Materno: " + user.apellido_materno);
                System.out.println("Telefono: " + user.telefono);
                System.out.println("Fecha: " + user.fecha_nacimiento);
                System.out.println("Genero: " + user.genero);
                System.out.println("Desea modificar los datos del usuario (s/n)?");
                res = esc.readLine().charAt(0);

                if(res == 's') modificar(user);
                conexion.disconnect();
            }
        } else {
            BufferedReader lector = new BufferedReader(new InputStreamReader((conexion.getErrorStream())));
            String response;
            response = lector.readLine();

            if (response != null) System.out.println(response);
            conexion.disconnect();
            System.out.println("Error HTTP: " + conexion.getResponseCode());
        }

    }

    public static void modifica_usuario(Usuario usuario) throws IOException {
        URL url = new URL(K_BASE_URL + "modifica_usuario");
        HttpURLConnection conexion = makePost(url, null, usuario);

        if (conexion.getResponseCode() == 200) {
            System.out.println("El usuario ha sido modificado");
        } else {
            System.out.println("Error HTTP: " + conexion.getResponseCode());
        }

        conexion.disconnect();
    }

    public static void borrar_usuario(int id_usuario) throws IOException {
        System.out.println(id_usuario);

        URL url = new URL(K_BASE_URL + "borra_usuario");
        String parametros = "id_usuario=" + URLEncoder.encode(String.valueOf(id_usuario), StandardCharsets.UTF_8);
        HttpURLConnection conexion = makePost(url, parametros, null);

        if (conexion.getResponseCode() == 200) {
            System.out.println("El usuario ha sido borrado");
        } else {
            BufferedReader br = new BufferedReader(new InputStreamReader((conexion.getErrorStream())));
            String response = br.readLine();
            if (response != null) System.out.println(response);
            System.out.println("Error HTTP: " + conexion.getResponseCode());
        }
        conexion.disconnect();
    }

    //Utilidades
    private static void modificar(Usuario user) throws IOException {
        BufferedReader lector = new BufferedReader(new InputStreamReader(System.in));
        Usuario usuario = readUsuario(lector);
        usuario.id_usuario=user.id_usuario;

        usuario.genero = lector.readLine();
        if (usuario.email == null || usuario.email.equals("")) usuario.email = user.email;
        if (usuario.nombre == null || usuario.nombre.equals("")) usuario.nombre = user.nombre;
        if (usuario.apellido_paterno == null || usuario.apellido_paterno.equals("")) usuario.apellido_paterno = user.apellido_paterno;
        if (usuario.apellido_materno == null || usuario.apellido_materno.equals("")) usuario.apellido_materno = user.apellido_materno;
        if (usuario.fecha_nacimiento == null || usuario.fecha_nacimiento.equals("")) usuario.fecha_nacimiento = user.fecha_nacimiento;
        if (usuario.telefono == null || usuario.telefono.equals("")) usuario.telefono = user.telefono;
        if (usuario.genero == null || usuario.genero.equals("")) usuario.genero = user.genero;

        modifica_usuario(usuario);
    }

    private static Usuario readUsuario(BufferedReader lector) throws IOException {
        Usuario usuario = new Usuario();

        System.out.print("Email: ");
        usuario.email = lector.readLine();
        System.out.print("Nombre: ");
        usuario.nombre = lector.readLine();
        System.out.print("Apellido Paterno: ");
        usuario.apellido_paterno = lector.readLine();
        System.out.print("Apellido Materno: ");
        usuario.apellido_materno = lector.readLine();
        System.out.print("Fecha de Nacimiento: ");
        usuario.fecha_nacimiento = lector.readLine();
        System.out.print("Telefono: ");
        usuario.telefono = lector.readLine();
        System.out.print("Genero (M/F): ");
        usuario.genero = lector.readLine();
        usuario.foto = null;

        return usuario;
    }

    private static HttpURLConnection makePost(URL url, String params, Usuario usuario) throws IOException {
        HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
        conexion.setDoOutput(true);
        conexion.setRequestMethod("POST");
        conexion.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        if (usuario != null) {
            GsonBuilder builder = new GsonBuilder();
            builder.serializeNulls();
            Gson gson = builder.create();
            String body = gson.toJson(usuario);
            params = "usuario=" + URLEncoder.encode(body, StandardCharsets.UTF_8);
        }

        OutputStream os = conexion.getOutputStream();
        os.write(params.getBytes());
        os.flush();

        return conexion;
    }

    public static void main(String[] args) throws Exception {
        while (true) {
            BufferedReader lector = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("\nMENU\n");
            System.out.println("a. Alta usuario");
            System.out.println("b. Consulta usuario");
            System.out.println("c. Borra usuario");
            System.out.println("d. Salir");
            System.out.print("\nOpcion: ");

            char opcion = lector.readLine().charAt(0);

            switch (opcion) {
                case 'a' -> {
                    System.out.println("Alta de usuario");
                    Usuario usuario = readUsuario(lector);
                    alta_usuario(usuario);
                }
                case 'b' -> {
                    System.out.println("Consultar usuario");
                    System.out.print("Ingresa el ID de usuario: ");
                    consultar_usuario(Integer.parseInt(lector.readLine()));
                }
                case 'c' -> {
                    System.out.println("Borrar usuario");
                    System.out.print("Ingresa el ID de usuario: ");
                    borrar_usuario(Integer.parseInt(lector.readLine()));
                }
                case 'd' -> {
                    lector.close();
                    System.exit(0);
                }
                default -> System.out.println("Opcion no valida");
            }
        }
    }
}