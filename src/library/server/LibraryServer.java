package library.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LibraryServer {
	private final int PORT;
    private final UserManager userManager;
    private final RecordManager recordManager;

    public LibraryServer(int PORT, String usersPath, String recordsPath) {
        this.PORT = PORT; // set port
        this.userManager = new UserManager(usersPath); //init user manager with persistence path
        this.recordManager = new RecordManager(recordsPath); //same
    }

    //start the server; blocks and listens for clients.
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("LibraryServer listening on port " + PORT);
            ExecutorService executor = Executors.newFixedThreadPool(20);
            while (true) {
                Socket clientSocket = serverSocket.accept(); // wait for connection
                System.out.println("Accepted connection from " + clientSocket.getRemoteSocketAddress());
                executor.submit(new ClientHandler(clientSocket, userManager, recordManager));
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    
    public static void main(String[] args) {
        int PORT = 5000;
        String usersPath = "users.ser"; // path for users
        String recordsPath = "records.ser"; //same but for records
        LibraryServer server = new LibraryServer(PORT, usersPath, recordsPath);
        server.start();
    }
}
