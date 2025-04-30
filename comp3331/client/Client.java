/*
 * z5360925
 * COMP3331 Assignment
 * SockForums client: a basic forum application that uses
 * - UDP
 * - TCP
 * - multi-threading
 * 
 * This is the client code. To use, run in a terminal
 * `javac Client.java`
 * `java Client <port-number>`
 * 
 * Ensure the port number is the same as the server.
 * Refer to Server.java for server implementation
 * */

import java.net.*;
import java.io.*;
import java.util.*;

public class Client {
    // Server address information
    private static InetAddress serverAddress;
    private static int serverPort;

    // Global variables
    private static DatagramSocket clientSocket = null;
    private static boolean clientAlive = false;
    private static boolean atUsername = true;
    private static boolean isLoggedIn = false;
    private static boolean newUser = false;
    private static String username = null;
    private static String password = null;
    private static DataInputStream dataInputStream = null;
    private static DataOutputStream dataOutputStream = null;
    private static String tcpFile = null;

    private static final int recByteLength = 65535;
    private static final int pacByteLength = 1024;

    /**
     * Thread for handling incoming messages from the server
     */
    static class ReceiveThread extends Thread {
        private boolean alive;
        private boolean printPrompt;

        public ReceiveThread() {
            this.alive = true;
            this.printPrompt = true;
        }

        @Override
        public void run() {
            /**
             * Main method for receive thread.
             * Continuously receives and displays messages from the server.
             */
            byte[] bytes = new byte[recByteLength];
            DatagramPacket request = null;
            while (alive && clientAlive) {
                try {

                    if (isLoggedIn) {
                        displayMenu();
                    }

                    if (printPrompt) {
                        System.out.print("🧦 "); // Reprint the prompt
                    } else {
                        printPrompt = true;
                    }

                    request = new DatagramPacket(bytes, bytes.length);
                    clientSocket.receive(request);

                    String message = processPacket(request);

                    // Check if connection is still valid
                    if (message == null) {
                        System.out.println("\n===== Server disconnected =====");
                        clientAlive = false;
                        break;
                    }

                    // handle login, logout and invalid responses
                    if (message.equals("LOGIN SUCCESS")) {
                        System.out.println("\n" + message);
                        isLoggedIn = true;
                        System.out.println("Welcome back, " + username + "\n");

                    } else if (message.equals("REGISTRATION SUCCESS")) {
                        System.out.println("\n" + message);
                        isLoggedIn = true;
                        System.out.println("Welcome to SockForums, " + username + "\n");

                    } else if (message
                            .equals("ERROR: You have been logged out. Please login again.\nEnter username:")) {
                        System.out.println("\n" + message);
                        isLoggedIn = false;
                        atUsername = true;

                    } else if (message.startsWith("You are registering as a new user.")) {
                        System.out.println("\n" + message);
                        newUser = true;

                    } else if (message.startsWith("UPD: Ready to receive file.")) {
                        uploadFile();

                        // wait for confirmation from server to open TCP and send file
                        printPrompt = false;
                        continue;
                    } else if (message.startsWith("DWN: Ready to send file.")) {
                        downloadFile();

                        // wait for confirmation from server to open TCP and receive file
                        printPrompt = false;
                        continue;
                    } else if (message.startsWith("LOGIN ERROR:")) {
                        System.out.println("\n" + message);
                        isLoggedIn = false;
                        atUsername = true;
                        username = null;

                    } else {
                        System.out.println("\n" + message);
                    }

                    bytes = new byte[recByteLength];

                } catch (EOFException e) {
                    System.out.println("\n===== Server closed the connection =====");
                    clientAlive = false;
                    break;
                } catch (IOException e) {
                    // ignore during "safe closure" (XIT command invalidates client)
                    if (clientAlive) {
                        System.out.println("\n===== Error receiving message: " + e.getMessage() + " =====");
                        clientAlive = false;
                    }
                    break;
                }
            }
        }
    }

    private static String processPacket(DatagramPacket packet) {
        return new String(packet.getData(), packet.getOffset(), packet.getLength());
    }

    private static void sendMessage(String message) {
        try {
            byte[] buf = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddress, serverPort);
            clientSocket.send(packet);
        } catch (IOException e) {
            System.out.println("===== Error sending message " + e.getMessage() + " =====");
        }
    }

    private static void printErrorMsg(String message) {
        System.out.println("\n" + message + "\n");
        displayMenu();
        System.out.print("🧦 ");
    }

    /**
     * Establish connection with the chat server
     */
    private static boolean connectToServer() {
        try {
            clientSocket = new DatagramSocket();

            clientAlive = true;

            // attempt to connect to server
            sendMessage("HEALTHCHECK");

            // await response from healthcheck directly
            byte[] bytes = new byte[recByteLength];
            DatagramPacket request = new DatagramPacket(bytes, bytes.length);
            clientSocket.receive(request);

            String message = processPacket(request);

            // Check if connection is still valid
            if (message == null) {
                System.out.println("\n===== Server disconnected =====");
                clientAlive = false;
                return false;
            }

            return message.equals("ALIVE");

        } catch (Exception e) {
            printErrorMsg("===== Error connecting to server: " + e.getMessage() + " =====");
            return false;
        }
    }

    /**
     * Send login request to the server
     * 
     * @param input The input string to process during login (either username or
     *              password)
     */
    private static void login(String input) {
        if (!clientAlive || clientSocket == null) {
            printErrorMsg("===== Not connected to server =====");
            return;
        }

        if (input.trim().isEmpty()) {
            printErrorMsg("===== Username or password cannot be blank =====");
            return;
        }

        // Set the global username or password variable
        if (atUsername) {
            sendMessage("USERNAME " + input);
            username = input;
            atUsername = false;
        } else {
            sendMessage("PASSWORD " + username + " " + input + " " + newUser);
        }
    }

    /**
     * Send request for creation of new thread to server
     * 
     * @param threadname The name of the thread
     */
    private static void createThread(String threadname) {
        String crtCommand = "CRT " + threadname + " " + username;

        // Send the command to the server
        sendMessage(crtCommand);
    }

    /**
     * Send request to list all threads to server
     */
    private static void listThreads() {
        // Send the command to the server
        sendMessage("LST");
    }

    /**
     * Send request to read the messages in a thread
     * 
     * @param threadname The name of the thread
     */
    private static void readThread(String threadname) {
        String rdtCommand = "RDT " + threadname;

        // Send the command to the server
        sendMessage(rdtCommand);
    }

    /**
     * Send request to delete a thread
     * 
     * @param threadname The name of the thread
     */
    private static void deleteThread(String threadname) {
        String rmvCommand = "RMV " + threadname + " " + username;

        // Send the command to the server
        sendMessage(rmvCommand);
    }

    /**
     * Send request to add a message to a thread
     * 
     * @param threadname The name of the thread
     * @param message    The message to add to the thread
     */
    private static void addMessage(String threadname, String message) {
        String msgCommand = "MSG " + threadname + " " + username + " " + message;

        // Send the command to the server
        sendMessage(msgCommand);
    }

    /**
     * Send request to edit a message in a thread
     * 
     * @param threadname The name of the thread
     * @param messageNo  The message number of the message to edit
     * @param message    The message to use in place of the previous message
     */
    private static void editMessage(String threadname, String messageNo, String message) {
        String edtCommand = "EDT " + threadname + " " + messageNo + " " + username + " " + message;

        // Send the command to the server
        sendMessage(edtCommand);
    }

    /**
     * Send request to delete a message from a thread
     * 
     * @param threadname The name of the thread
     * @param messageNo  The message number of the message to delete
     */
    private static void deleteMessage(String threadname, String messageNo) {
        String dltCommand = "DLT " + threadname + " " + messageNo + " " + username;

        // Send the command to the server
        sendMessage(dltCommand);
    }

    /**
     * Request upload file so server can open TCP connection
     * 
     * @param threadname The name of the thread
     * @param filename   The name of the file to request for upload
     */
    private static void requestUpload(String threadname, String filename) {
        String updCommand = "UPD " + threadname + " " + filename + " " + username;

        // Send the command to the server
        sendMessage(updCommand);

        tcpFile = filename;
    }

    /**
     * Upload file by sending it via TCP connection to server. Then close TCP
     * connection
     * File to upload stored in tcpFile global variable
     */
    private static void uploadFile() {
        try {
            File myFile = new File(tcpFile);

            // establish TCP connection with server
            Socket serverTCPSocket = new Socket(serverAddress, serverPort);

            // send file to server
            byte[] mybytearray = new byte[(int) myFile.length()];
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
            bis.read(mybytearray, 0, mybytearray.length);

            OutputStream os = serverTCPSocket.getOutputStream();
            os.write(mybytearray, 0, mybytearray.length);
            os.flush();

            // close all connections
            bis.close();
            os.close();
            serverTCPSocket.close();

        } catch (FileNotFoundException e) {
            printErrorMsg("===== Error uploading file: " + e.getMessage() + " =====");
        } catch (IOException e) {
            printErrorMsg("===== Error uploading file: " + e.getMessage() + " =====");
        }
    }

    /**
     * Request download file so server can open TCP connection
     * 
     * @param threadname The name of the thread
     * @param threadname The name of the file to request for download
     */
    private static void requestDownload(String threadname, String filename) {
        String dwnCommand = "DWN " + threadname + " " + filename;

        // Send the command to the server
        sendMessage(dwnCommand);

        tcpFile = filename;
    }

    /**
     * Send request to download file via TCP connection by opening TCP connection
     * File to download stored in tcpFile global variable
     */
    private static void downloadFile() {
        try {
            File file = new File(tcpFile);
            file.createNewFile();

            // establish TCP connection with server
            Socket serverTCPSocket = new Socket(serverAddress, serverPort);

            // receive file from server and add to new file
            byte[] mybytearray = new byte[pacByteLength];
            InputStream is = serverTCPSocket.getInputStream();
            FileOutputStream fos = new FileOutputStream(file); // do not include threadname
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            int bytesRead = is.read(mybytearray, 0, mybytearray.length);
            bos.write(mybytearray, 0, bytesRead);

            // close all connections
            bos.close();
            fos.close();
            is.close();
            serverTCPSocket.close();

        } catch (FileNotFoundException e) {
            printErrorMsg("===== Error uploading file: " + e.getMessage() + " =====");
        } catch (IOException e) {
            printErrorMsg("===== Error uploading file: " + e.getMessage() + " =====");
        }
    }

    /**
     * Disconnect from the chat server
     */
    private static void exitChat() {
        sendMessage("XIT");
        System.out.println("\nLogging out...");
        System.out.println("===== Thank you for using SockForums =====\n");

        // Set clientAlive to false
        clientAlive = false;

        // Clean up resources
        clientSocket.close();

        System.out.println("===== Disconnected from server =====");
    }

    /**
     * Display the available commands
     */
    private static void displayMenu() {
        System.out.println("\n===== Available Commands =====\n" +
                "CRT <threadname> - create a new thread\n" +
                "LST - list all threads\n" +
                "RMV <threadname> - remove a thread\n" +
                "MSG <threadname> <message> - add a message to a thread\n" +
                "RDT <threadname> - read all messsages in a thread\n" +
                "EDT <threadname> <message-number> <message> - edit a message in a thread\n" +
                "DLT <threadname> <message-number> - delete a message from a thread\n" +
                "UPD <threadname> <filename> - upload a file to a thread\n" +
                "DWN <threadname> <filename> - download a file from a thread\n" +
                "XIT - logout\n" +
                "================================\n");
    }

    /**
     * Parse and process user input. Returns true when user wants to exit
     * 
     * @param userInput The input string to parse
     */
    private static boolean parseUserInput(String userInput) {
        if (userInput == null || userInput.isEmpty()) {
            return false;
        }

        if (!isLoggedIn) {
            printErrorMsg("===== You must log in first =====");
            return false;
        }

        if (!clientAlive || clientSocket == null) {
            printErrorMsg("===== Not connected to server =====");
            return false;
        }

        // Parse the command and arguments. First argument is command
        String[] parts = userInput.split(" ", 2);
        String command = parts[0].toUpperCase();

        // Call the appropriate function based on the command
        if (command.equals("CRT")) {
            if (parts.length < 2) {
                printErrorMsg("===== Usage: CRT <threadname> =====");
                return false;
            }

            String[] msgParts = parts[1].split(" ");
            if (msgParts.length != 1) {
                printErrorMsg("===== Usage: CRT <threadname> =====");
                return false;
            }

            String threadname = msgParts[0];
            createThread(threadname);

        } else if (command.equals("LST")) {
            if (parts.length != 1) {
                printErrorMsg("===== Usage: LST =====");
                return false;
            }

            listThreads();

        } else if (command.equals("RDT")) {
            if (parts.length < 2) {
                printErrorMsg("===== Usage: RDT <threadname> =====");
                return false;
            }

            String[] msgParts = parts[1].split(" ");
            if (msgParts.length != 1) {
                printErrorMsg("===== Usage: RDT <threadname> =====");
                return false;
            }

            String threadname = msgParts[0];
            readThread(threadname);

        } else if (command.equals("RMV")) {
            if (parts.length < 2) {
                printErrorMsg("===== Usage: RMV <threadname> =====");
                return false;
            }

            String[] msgParts = parts[1].split(" ");
            if (msgParts.length != 1) {
                printErrorMsg("===== Usage: RMV <threadname> =====");
                return false;
            }

            String threadname = msgParts[0];
            deleteThread(threadname);

        } else if (command.equals("MSG")) {
            if (parts.length < 2) {
                printErrorMsg("===== Usage: MSG <threadname> <message> =====");
                return false;
            }

            String[] msgParts = parts[1].split(" ", 2);
            if (msgParts.length != 2) {
                printErrorMsg("===== Usage: MSG <threadname> <message> =====");
                return false;
            }

            String threadname = msgParts[0];
            String messageContents = msgParts[1];
            addMessage(threadname, messageContents);

        } else if (command.equals("EDT")) {
            if (parts.length < 2) {
                printErrorMsg("===== Usage: EDT <threadname> <message-number> <message> =====");
                return false;
            }

            String[] msgParts = parts[1].split(" ", 3);
            if (msgParts.length != 3) {
                printErrorMsg("===== Usage: EDT <threadname> <message-number> <message> =====");
                return false;
            }

            String threadname = msgParts[0];
            String messageNo = msgParts[1];
            String messageContents = msgParts[2];
            editMessage(threadname, messageNo, messageContents);

        } else if (command.equals("DLT")) {
            if (parts.length < 2) {
                printErrorMsg("===== Usage: DLT <threadname> <message-number> =====");
                return false;
            }

            String[] msgParts = parts[1].split(" ");
            if (msgParts.length != 2) {
                printErrorMsg("===== Usage: DLT <threadname> <message-number> =====");
                return false;
            }

            String threadname = msgParts[0];
            String messageNo = msgParts[1];
            deleteMessage(threadname, messageNo);

        } else if (command.equals("UPD")) {
            if (parts.length < 2 || parts[1].isEmpty()) {
                printErrorMsg("===== Usage: UPD <threadname> <filename> =====");
                return false;
            }

            String[] msgParts = parts[1].split(" ");
            if (msgParts.length != 2) {
                printErrorMsg("===== Usage: UPD <threadname> <filename> =====");
                return false;
            }

            String threadname = msgParts[0];
            String filename = msgParts[1];
            requestUpload(threadname, filename);

        } else if (command.equals("DWN")) {
            if (parts.length < 2 || parts[1].isEmpty()) {
                printErrorMsg("===== Usage: DWN <threadname> <filename> =====");
                return false;
            }

            String[] msgParts = parts[1].split(" ");
            if (msgParts.length != 2) {
                printErrorMsg("===== Usage: DWN <threadname> <filename> =====");
                return false;
            }

            String threadname = msgParts[0];
            String filename = msgParts[1];
            requestDownload(threadname, filename);

        } else if (command.equals("XIT")) {
            exitChat();
            return true;

        } else {
            System.out.println("\n===== Unknown command: " + command + " =====");
            displayMenu();
            System.out.print("🧦 ");
        }
        return false;
    }

    /**
     * Main function to handle the chat client
     */
    public static void main(String[] args) {
        // Check command line arguments
        if (args.length != 1) {
            System.out.println("\n===== Error usage: java Client <SERVER_PORT> ======\n");
            return;
        }

        // Parse server address and port
        try {
            serverAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.out.println("===== Error connecting to server " + e.getMessage() + " =====");
        }
        serverPort = Integer.parseInt(args[0]);

        System.out.println("Connecting to the server...\n");

        // Connect to the server
        if (!connectToServer()) {
            System.out.println("Failed to connect to server. Exiting...");
            return;
        }

        System.out.println("Welcome to SockForums! To use the application, you need to login.\nEnter username:");

        ReceiveThread rt = new ReceiveThread();
        rt.start();

        // Main loop for user interaction
        Scanner scanner = new Scanner(System.in);
        try {
            while (clientAlive) {

                String userInput = scanner.nextLine();

                if (!isLoggedIn) {
                    login(userInput);
                } else {
                    if (parseUserInput(userInput)) {
                        rt.interrupt();
                        break;
                    }
                    ;
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            // Clean up resources
            scanner.close();
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        }
    }
}
