package me.cbitler.raidbot.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import me.cbitler.raidbot.utility.EnvVariables;

/**
 * Class for managing the SQLite database for this bot
 * @author Christopher Bitler
 * @author Franziska Mueller
 */
public class Database {
    private static final Logger log = LogManager.getLogger(Database.class);

    String databaseName;
    Connection connection;

    //Thee are the queries for creating the tables

    String raidTableInit = "CREATE TABLE IF NOT EXISTS raids (\n"
            + " raidId text PRIMARY KEY, \n"
            + " serverId text NOT NULL, \n"
            + " channelId text NOT NULL, \n"
            + " isDisplayShort text NOT NULL, \n"
            + " isOpenWorld text NOT NULL, \n"
            + " isFractalEvent text NOT NULL, \n"
            + " leader text NOT NULL, \n"
            + " `name` text NOT NULL, \n"
            + " `description` text, \n"
            + " `date` text NOT NULL, \n"
            + " `time` text NOT NULL, \n"
            + " roles text NOT NULL, \n"
            + " permittedRoles text);";

    String raidUsersTableInit = "CREATE TABLE IF NOT EXISTS raidUsers (\n"
            + " userId text, \n"
            + " username text, \n"
            + " spec text, \n"
            + " role text, \n"
            + " raidId text)";

    String raidUsersFlexRolesTableInit = "CREATE TABLE IF NOT EXISTS raidUsersFlexRoles (\n"
            + " userId text, \n"
            + " username text, \n"
            + " spec text, \n"
            + " role text, \n"
            + " raidId text)";

    String botServerSettingsInit = "CREATE TABLE IF NOT EXISTS serverSettings (\n"
            + " serverId text PRIMARY KEY, \n"
            + " raid_leader_role text, \n"
            + " fractal_creator_role text, \n"
            + " fractal_channel text, \n"
            + " archive_channel text, \n"
            + " auto_events_channel text, \n"
            + " predef_role_groups text, \n"
            + " role_templates text)";

    /**
     * Create a new database with the specific filename
     * @param databaseName The filename/location of the SQLite database
     */
    public Database() {
        this.databaseName = EnvVariables.getValue("DB_FILE_NAME");
        if(this.databaseName == null){
            this.databaseName = "db/events.db";
            var oldDbFile = Paths.get("events.db").toFile();
            if(oldDbFile.exists()) {
                oldDbFile.renameTo(Paths.get(this.databaseName).toFile());
            }
        }
        if(!this.databaseName.startsWith("/")){
            var absolutePath = Paths.get(EnvVariables.getContextPath(),this.databaseName).toAbsolutePath();
            var directoryPath = absolutePath.getParent();
            if(!directoryPath.toFile().exists()) {
                try {
                    Files.createDirectories(directoryPath);
                } catch (IOException e) {
                    log.error("Could not create parent directories for database file.", e);
                    System.exit(1);
                }
            }
            this.databaseName = absolutePath.toString();
        }
    }

    /**
     * Connect to the SQLite database and create the tables if they don't exist
     */
    public void connect() {
        log.info("Connecting to database on '" + this.databaseName + "'");
        String url = "jdbc:sqlite:" + databaseName;
        try {
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            log.error("Database connection error.", e);
            System.exit(1);
        }

        try {
            tableInits();
        } catch (SQLException e) {
            log.error("Couldn't create tables.", e);
            System.exit(1);
        }
    }

    /**
     * Run a query and return the results using the specified query and parameters
     * @param query The query with ?s where the parameters need to be placed
     * @param data The parameters to put in the query
     * @return QueryResult representing the statement used and the ResultSet
     * @throws SQLException
     */
    public QueryResult query(String query, String[] data) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(query);
        int i = 1;
        for(String input : data) {
            stmt.setObject(i, input);
            i++;
        }

        ResultSet rs = stmt.executeQuery();

        return new QueryResult(stmt, rs);
    }

    /**
     * Run an update query with the specified parameters
     * @param query The query with ?s where the parameters need to be placed
     * @param data The parameters to put in the query
     * @throws SQLException
     */
    public void update(String query, String[] data) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(query);
        int i = 1;
        for(String input : data) {
            stmt.setObject(i, input);
            i++;
        }

        stmt.execute();
        stmt.close();
    }

    /**
     * Create the database tables. Also alters the raid table to add the leader column if it doesn't exist.
     * @throws SQLException
     */
    public void tableInits() throws SQLException {
        connection.createStatement().execute(raidTableInit);
        connection.createStatement().execute(raidUsersTableInit);
        connection.createStatement().execute(raidUsersFlexRolesTableInit);
        connection.createStatement().execute(botServerSettingsInit);

        try {
            connection.createStatement().execute("ALTER TABLE raids ADD COLUMN leader text");
        } catch (Exception e) { }
        try {
            connection.createStatement().execute("ALTER TABLE raids ADD COLUMN `description` text");
        } catch (Exception e) { }
        try {
            connection.createStatement().execute("ALTER TABLE raids ADD COLUMN isOpenWorld text");
        } catch (Exception e) { }
        try {
            connection.createStatement().execute("ALTER TABLE raids ADD COLUMN isDisplayShort text");
        } catch (Exception e) { }
        try {
            connection.createStatement().execute("ALTER TABLE raids ADD COLUMN isFractalEvent text");
        } catch (Exception e) { }
        try {
            connection.createStatement().execute("ALTER TABLE raids ADD COLUMN permittedRoles text");
        } catch (Exception e) { }
        try {
            connection.createStatement().execute("ALTER TABLE serverSettings ADD COLUMN fractal_creator_role text");
        } catch (Exception e) { }
        try {
            connection.createStatement().execute("ALTER TABLE serverSettings ADD COLUMN fractal_channel text");
        } catch (Exception e) { }
        try {
            connection.createStatement().execute("ALTER TABLE serverSettings ADD COLUMN archive_channel text");
        } catch (Exception e) { }
        try {
            connection.createStatement().execute("ALTER TABLE serverSettings ADD COLUMN auto_events_channel text");
        } catch (Exception e) { }
        try {
            connection.createStatement().execute("ALTER TABLE serverSettings ADD COLUMN predef_role_groups text");
        } catch (Exception e) { }
        try {
            connection.createStatement().execute("ALTER TABLE serverSettings ADD COLUMN role_templates text");
        } catch (Exception e) { }
    }
}
