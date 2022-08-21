package edu.uob;

import edu.uob.exceptions.*;
import edu.uob.syntax.Parser;
import edu.uob.syntax.Tokenizer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.List;

/**
 * This class implements the DB server.
 */
public final class DBServer {
    private static final char END_OF_TRANSMISSION = 4;
    private final File databaseDirectory;
    private String databaseName;

    /**
     * KEEP this signature (i.e. {@code edu.uob.DBServer(File)}) otherwise we won't be able to mark
     * your submission correctly.
     *
     * <p>You MUST use the supplied {@code databaseDirectory} and only create/modify files in that
     * directory; it is an error to access files outside that directory.
     *
     * @param databaseDirectory The directory to use for storing any persistent database files such
     *                          that starting a new instance of the server with the same directory will restore all
     *                          databases. You may assume *exclusive* ownership of this directory for the lifetime of this
     *                          server instance.
     */
    public DBServer(File databaseDirectory) {
        this.databaseDirectory = databaseDirectory;
    }

    public static void main(String[] args) throws IOException {
        new DBServer(Paths.get("db").toAbsolutePath().toFile()).blockingListenOn(8888);
    }

    public File getDatabaseDirectory() {
        return databaseDirectory;
    }

    /**
     * KEEP this signature (i.e. {@code edu.uob.DBServer.handleCommand(String)}) otherwise we won't be
     * able to mark your submission correctly.
     *
     * <p>This method handles all incoming DB commands and carry out the corresponding actions.
     */
    public String handleCommand(String command) {
        List<String> tokens = Tokenizer.getTokens(command);
        String result;
        try {
            result = new Parser(tokens).getCommand().query(this);
        } catch (QueryException | ParserException | TableException | ConditionException | ValueException e) {
            result = "[ERROR] " + e.getMessage();
        }
        return result;
    }

    public String getDatabaseName() throws QueryException {
        if (databaseName != null) return databaseName;
        throw new QueryException.NoSpecificDatabaseException();
    }

    public void setDatabaseName(String name) {
        databaseName = name;
    }


    //  === Methods below are there to facilitate server related operations. ===

    /**
     * Starts a *blocking* socket server listening for new connections. This method blocks until the
     * current thread is interrupted.
     *
     * <p>This method isn't used for marking. You shouldn't have to modify this method, but you can if
     * you want to.
     *
     * @param portNumber The port to listen on.
     * @throws IOException If any IO related operation fails.
     */
    public void blockingListenOn(int portNumber) throws IOException {
        try (ServerSocket s = new ServerSocket(portNumber)) {
            System.out.println("Server listening on port " + portNumber);
            while (!Thread.interrupted()) {
                try {
                    blockingHandleConnection(s);
                } catch (IOException e) {
                    System.err.println("Server encountered a non-fatal IO error:");
                    e.printStackTrace();
                    System.err.println("Continuing...");
                }
            }
        }
    }

    /**
     * Handles an incoming connection from the socket server.
     *
     * <p>This method isn't used for marking. You shouldn't have to modify this method, but you can if
     * * you want to.
     *
     * @param serverSocket The client socket to read/write from.
     * @throws IOException If any IO related operation fails.
     */
    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept(); BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        s.getInputStream())); BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(s.getOutputStream()))) {

            System.out.println(
                    "Connection established: " + serverSocket.getInetAddress());
            while (!Thread.interrupted()) {
                String incomingCommand = reader.readLine();
                System.out.println("Received message: " + incomingCommand);
                String result = handleCommand(incomingCommand);
                writer.write(result);
                writer.write("\n" + END_OF_TRANSMISSION + "\n");
                writer.flush();
            }
        }
    }
}
