/*
  Servicio.java
  Servicio web tipo REST
  Carlos Pineda Guerrero, Octubre 2021
 */
package negocio;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.QueryParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.sql.*;
import javax.sql.DataSource;
import javax.naming.Context;
import javax.naming.InitialContext;

import java.util.ArrayList;
import com.google.gson.*;

// la URL del servicio web es http://localhost:8080/Servicio/rest/ws
// donde:
//	"Servicio" es el dominio del servicio web (es decir, el nombre de archivo Servicio.war)
//	"rest" se define en la etiqueta <url-pattern> de <servlet-mapping> en el archivo WEB-INF\web.xml
//	"ws" se define en la siguiente anotacin @Path de la clase Servicio
@Path("ws")
public class Servicio {
    static DataSource pool = null;

    static {
        try {
            Context ctx = new InitialContext();
            pool = (DataSource) ctx.lookup("java:comp/env/jdbc/datasource_Servicio");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static Gson j = new GsonBuilder()
            .registerTypeAdapter(byte[].class, new AdaptadorGsonBase64())
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
            .create();

    @POST
    @Path("alta_articulo")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response alta(@FormParam("articulo") Articulo articulo) throws Exception {
        Connection conexion = pool.getConnection();

        if (articulo.nombre == null || articulo.nombre.equals("")) {
            return Response.status(400).entity(j.toJson(new Error("Nombre vacio"))).build();
        }

        if (articulo.precio == null || articulo.precio.equals("")) {
            return Response.status(400).entity(j.toJson(new Error("Precio vacio"))).build();
        }

        if (articulo.cantidad == null || articulo.cantidad.equals("")) {
            return Response.status(400).entity(j.toJson(new Error("Cantidad vacia"))).build();
        }

        if (articulo.descripcion == null || articulo.descripcion.equals("")) {
            return Response.status(400).entity(j.toJson(new Error("Descripcion vacia"))).build();
        }

        try {
            conexion.setAutoCommit(false);
            PreparedStatement stmt_2 = conexion.prepareStatement(
                    "INSERT INTO articulos(id_articulo,nombre,precio,cantidad,descripcion) VALUES (0,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ResultSet keys = null;
            try {
                stmt_2.setString(1, articulo.nombre);
                stmt_2.setFloat(2, Float.parseFloat(articulo.precio));
                stmt_2.setInt(3, Integer.parseInt(articulo.cantidad));
                stmt_2.setString(4, articulo.descripcion);
                stmt_2.executeUpdate();
                keys = stmt_2.getGeneratedKeys();
                keys.next();
                articulo.id_articulo = keys.getInt(1);
            } finally {
                stmt_2.close();
            }

            if (articulo.foto != null) {
                PreparedStatement stmt_3 = conexion.prepareStatement("INSERT INTO fotos_articulos VALUES (0,?,?)");
                try {
                    stmt_3.setBytes(1, articulo.foto);
                    stmt_3.setInt(2, articulo.id_articulo);
                    stmt_3.executeUpdate();
                } finally {
                    stmt_3.close();
                }
            }

            conexion.commit();
        } catch (Exception e) {
            conexion.rollback();
            return Response.status(400).entity(j.toJson(new Error(e.getMessage()))).build();
        } finally {
            conexion.setAutoCommit(true);
            conexion.close();
        }
        return Response.ok().build();
    }
    
    @POST
    @Path("consulta_articulo")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response consulta(@FormParam("palabra") String palabra) throws Exception {
        Connection conexion = pool.getConnection();
        
        try {
            PreparedStatement stmt_1 = conexion.prepareStatement(
                    "SELECT a.id_articulo, a.nombre, a.precio, a.cantidad, a.descripcion, b.foto FROM articulos a " +
                    "LEFT OUTER JOIN fotos_articulos b ON a.id_articulo=b.id_articulo WHERE a.descripcion LIKE "
                    + "'%"+palabra+"%'"+"");

            try {
                ResultSet rs = stmt_1.executeQuery();
                ArrayList<Articulo> articulosArray = new ArrayList();
                try {
                    if (rs!=null){
                        while (rs.next()){
                            Articulo r = new Articulo();
                            r.id_articulo = rs.getInt(1);
                            r.nombre = rs.getString(2);
                            r.precio = rs.getString(3);
                            r.cantidad = rs.getString(4);
                            r.descripcion = rs.getString(5);
                            r.foto = rs.getBytes(6);
                            articulosArray.add(r);
                        }

                        return Response.ok().entity(j.toJson(articulosArray)).build();
                    }

                    return Response.status(400).entity(j.toJson(new Error("No existe el articulo"))).build();
                } finally {
                    rs.close();
                }
            } finally {
                stmt_1.close();
            }
        } catch (Exception e) {
            return Response.status(400).entity(j.toJson(new Error(e.getMessage()))).build();
        } finally {
            conexion.close();
        }
    }

    @POST
    @Path("alta_carrito")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response alta_carrito(@FormParam("id") int id, @FormParam("num") int num) throws Exception {
        Connection conexion = pool.getConnection();
        id = id + 1;
        int cantidad = 0;
        int nueva_cantidad = 0;

        try {
            conexion.setAutoCommit(false);
            PreparedStatement stmt_1 = conexion.prepareStatement("SELECT cantidad FROM articulos WHERE id_articulo=?");

            try {
              stmt_1.setInt(1, id);
              ResultSet rs = stmt_1.executeQuery();

              try {
                if (rs.next()) cantidad = rs.getInt("cantidad");

                if(num>cantidad) return Response.status(400).entity(j.toJson(
                            new Error("La cantidad rebasa las " + cantidad + " en inventario"))).build();
              } finally {
                rs.close();
              }
            } finally {
              stmt_1.close();
            }

            PreparedStatement stmt_2 = conexion.prepareStatement("INSERT INTO carrito_compra(id_articulo,cantidad) VALUES (?,?)");
            PreparedStatement stmt_3 = conexion.prepareStatement("UPDATE articulos SET cantidad = ? WHERE id_articulo= ?");
            nueva_cantidad=cantidad-num;

            try {
                stmt_2.setInt(1, id);
                stmt_2.setInt(2, num);
                stmt_3.setInt(1, nueva_cantidad);
                stmt_3.setInt(2, id);
                stmt_2.executeUpdate();
                stmt_3.executeUpdate();
            } finally {
                stmt_2.close();
                stmt_3.close();
            }
            conexion.commit();
        } catch (Exception e) {
            conexion.rollback();
            return Response.status(400).entity(j.toJson(new Error(e.getMessage()))).build();
        } finally {
            conexion.setAutoCommit(true);
            conexion.close();
        }

        return Response.ok().build();
    }

    @POST
    @Path("consulta_carrito")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response consulta_carrito(@FormParam("palabra") String palabra) throws Exception {
        Connection conexion = pool.getConnection();
        
        try {
            PreparedStatement stmt_1 = conexion.prepareStatement(
                    "SELECT a.nombre, a.precio, c.cantidad, a.descripcion, b.foto FROM articulos a LEFT OUTER JOIN " +
                    "fotos_articulos b ON a.id_articulo=b.id_articulo inner join carrito_compra c on " +
                    "a.id_articulo=c.id_articulo");

            try {
                ResultSet rs = stmt_1.executeQuery();
                ArrayList<Articulo> articulosArray = new ArrayList();
                try {
                    if (rs!=null){
                        while (rs.next()){
                            Articulo r = new Articulo();
                            r.nombre = rs.getString(1);
                            r.precio = rs.getString(2);
                            r.cantidad = rs.getString(3);
                            r.descripcion = rs.getString(4);
                            r.foto = rs.getBytes(5);
                            articulosArray.add(r);
                        }
                        return Response.ok().entity(j.toJson(articulosArray)).build();
                    }
                    return Response.status(400).entity(j.toJson(new Error("No hay nada en el carro de compras"))).build();
                } finally {
                    rs.close();
                }
            } finally {
                stmt_1.close();
            }
        } catch (Exception e) {
            return Response.status(400).entity(j.toJson(new Error(e.getMessage()))).build();
        } finally {
            conexion.close();
        }
    }
}
