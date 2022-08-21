package edu.uob.commands;

import edu.uob.DBServer;

import java.io.File;

public class CommandUSE extends Command {
    public CommandUSE(String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    public String query(DBServer server) {
        String result = "[OK]";
        File database = getDatabaseFile(server);
        if (!database.isDirectory()) {
            result = "[ERROR] Database " + databaseName + " not exist.";
        }
        server.setDatabaseName(databaseName);
        return result;
    }
}
