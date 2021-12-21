// Displaying the contents of the Authors table.

import java.sql.Connection;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class DisplayAuthors {
    static final String DATABASE_URL = "jdbc:mysql://localhost/books";

    public static void main(String args[]) {
        Connection connection = null; // manages connection
        Statement statement = null; // query statement
        ResultSet resultSet = null; // manages results

        try {
            connection = DriverManager.getConnection(DATABASE_URL, "usuario", "supercontrasena" );
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT AuthorID, FirstName, LastName FROM Authors" );
            ResultSetMetaData metadata = resultSet.getMetaData();
            int numberOfColumns = metadata.getColumnCount();
            System.out.println("Authors Table of Books Database: \n");

            for (int i = 1; i <= numberOfColumns; i++)
                System.out.printf("%-8s\t", metadata.getColumnName(i));
            System.out.println();

            while (resultSet.next()) {
                for (int i = 1; i <= numberOfColumns; i++)
                    System.out.printf("%-8s\t", resultSet.getObject(i));
                System.out.println();
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        } finally {
            try {
                resultSet.close();
                statement.close();
                connection.close();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
}
