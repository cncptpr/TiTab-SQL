package me.cncptpr.dbverbindung.core.dbconnection;


import me.cncptpr.console.Console;
import me.cncptpr.dbverbindung.Main;
import me.cncptpr.dbverbindung.core.save.Config;

import java.sql.*;
import java.util.ArrayList;

public class DBConnection implements AutoCloseable {

    public static boolean canConnect() {
        try (DBConnection ignored = new DBConnection()){
            return true;
        } catch (SQLException er) {
            if (er.getMessage().contains("No suitable driver found")) {
                Console.debug("Der scheiß #r #red JDBC Driver #r geht schon wieder nicht!!!!!!!!!!!!\n");
            }
            return false;
        }
    }

    public static boolean isJDBCDriverThere() {
        try (DBConnection ignored = new DBConnection()){
            return true;
        } catch (SQLException er) {
            if (er.getMessage().contains("No suitable driver found")) {
                Console.info("Der scheiß #r #red JDBC Driver #r geht schon wieder nicht!!!!!!!!!!!!\n");
                return false;
            }
            return true;
        }
    }

    private final Connection connection;

    public DBConnection() throws SQLException {
        Credentials credentials = Credentials.fromDefaultSettings();
        String url = String.format("jdbc:mysql://%s/%s", credentials.ip, credentials.database);
        connection = DriverManager.getConnection(url, credentials.username, credentials.password);
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException ignored) {}
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        return connection.getMetaData();
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return connection.prepareStatement(sql);
    }

    public String[] getAllDatabases() throws SQLException {
        ResultSet result = connection.getMetaData().getCatalogs();
        ArrayList<String> databases = new ArrayList<>();
        while (result.next()) {
            databases.add(result.getString("TABLE_CAT"));
        }
        result.close();
        return databases.toArray(new String[0]);
    }

    private record Credentials (String ip,String database,String username, String password) {

        public static Credentials fromDefaultSettings () {
            return fromSettings(Main.CONFIG);
        }
        public static Credentials fromSettings (Config settings) {
            return new Credentials(settings.getString("ip"), settings.getString("database_current"), settings.getString("username"), settings.getString("password"));
        }
    }
}

