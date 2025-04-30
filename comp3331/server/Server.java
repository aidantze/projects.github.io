import java.net.*;
import java.io.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import java.util.Map.Entry;
import java.lang.Boolean;
import java.lang.Integer;

public class Server {
    // Server socket
    private static DatagramSocket serverSocket = null;
    private static int serverPort;

    // Dictionary to store active users: {username: {<clientAddress: <clientPort>}}
    private static Map<String, Map<InetAddress, Integer>> activeUsers = new HashMap<>();

    // Lock for thread-safe operations on shared resources
    private static final ReentrantLock activeUsersLock = new ReentrantLock();

    // Path to file to retrieve and add credentials
    private static final String path = "credentials.txt";
    private static final String cwd = ".";

    // Regex for matching filenames
    private static final String threadRegex = "^[A-Za-z0-9]+$";
    private static final String fileSuffixRegex = "-[A-Za-z0-9.]+$";
    private static final String msgNoRegex = "^[0-9]+ .*";

    // Byte length for file transfer (transfer of UDP and TCP packets)
    private static final int recByteLength = 65535;
    private static final int pacByteLength = 1024;

    /**
     * ClientHandler class to handle individual client connections
     */
    static class ClientHandler extends Thread {
        private DatagramPacket packet;
        private InetAddress clientAddress;
        private int clientPort;
        private Map<InetAddress, Integer> clientDetails = new HashMap<>();

        public ClientHandler(DatagramPacket request) {
            HashMap<InetAddress, Integer> tmp = new HashMap<>();
            tmp.put(request.getAddress(), request.getPort());

            this.packet = request;
            this.clientAddress = request.getAddress();
            this.clientPort = request.getPort();
            this.clientDetails = tmp;
        }

        @Override
        public void run() {
            /**
             * Main method for client thread.
             * Handles all communication with a connected client.
             */
            String message = processPacket().trim();
            processMessage(message);
        }

        private String processPacket() {
            return new String(packet.getData(), packet.getOffset(), packet.getLength());
        }

        private void sendReply(String message) {
            try {
                byte[] buf = message.getBytes();
                DatagramPacket reply = new DatagramPacket(buf, buf.length, clientAddress, clientPort);
                serverSocket.send(reply);
            } catch (IOException e) {
                System.out.println("===== Error sending message " + e.getMessage() + " =====");
            }
        }

        /**
         * Add credentials to credentials.txt file by appending to the end of the file
         * 
         * @param uname    The username of the user requiring login
         * @param password The password of the user requiring login
         */
        private void addCredentials(String uname, String password) {
            // read from file, assume "credentials.txt" is name of file and exists in same
            // director
            try {

                File file = new File(path);
                FileWriter fw = new FileWriter(file, true); // true here will append to file
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(uname + " " + password);
                bw.newLine();
                bw.close();
                fw.close();

            } catch (IOException e) {
                System.out.println("===== Error adding user credentials: " + e.getMessage());
            }
        }

        private String getUsername() {
            for (Entry<String, Map<InetAddress, Integer>> entry : activeUsers.entrySet()) {
                if (entry.getValue().equals(this.clientDetails)) {
                    return entry.getKey();
                }
            }
            return null;
        }

        /**
         * Validate credentials by reading from credentials.txt file
         * 
         * @param credential The username or password of user trying to login
         * @param atUsername Determine whether credential is a username or password
         */
        private boolean validateUname(String uname) {
            // read from file, assume "credentials.txt" is name of file and exists in same
            // directory
            try {

                File file = new File(path);
                Scanner myReader = new Scanner(file);
                while (myReader.hasNextLine()) {
                    String body = myReader.nextLine();
                    String name = body.split(" ")[0];
                    if (name.equals(uname)) {
                        // username is available
                        myReader.close();
                        return true;
                    }
                }

                // either new username, or invalid username or password
                myReader.close();

            } catch (FileNotFoundException e) {
                System.out.println("===== Error validating user credentials: " + e.getMessage());
            }
            return false;
        }

        /**
         * Validate credentials by reading from credentials.txt file
         * 
         * @param credential The username or password of user trying to login
         * @param atUsername Determine whether credential is a username or password
         */
        private boolean validatePassword(String uname, String password, boolean newUser) {
            // read from file, assume "credentials.txt" is name of file and exists in same
            // directory
            try {

                File file = new File(path);
                Scanner myReader = new Scanner(file);
                while (myReader.hasNextLine()) {
                    String body = myReader.nextLine();
                    String name = body.split(" ")[0];
                    String pass = body.split(" ")[1];
                    // order of this is important to ensure we do not accidently validate a
                    // duplicate password
                    if (name.equals(uname) && pass.equals(password)) {
                        // password matches username
                        myReader.close();
                        return true;
                    }
                }

                myReader.close();
                if (newUser) {
                    return true;
                }

            } catch (FileNotFoundException e) {
                System.out.println("===== Error validating user credentials: " + e.getMessage());
            }
            return false;
        }

        /**
         * Process the username given
         * 
         * @param uname The username of user trying to login
         */
        private boolean processUname(String uname) {
            // check if username is already taken
            activeUsersLock.lock();
            if (activeUsers.containsKey(uname)) {
                sendReply(
                        "LOGIN ERROR: Username is already in use by another client. Please wait, or login with another username.\nEnter username:");
                // do not remove from activeUsers
                activeUsersLock.unlock();
                return false;
            }

            // Associate username with this client
            activeUsers.put(uname, this.clientDetails);

            // Prompt client to enter password for next step of login process
            if (!validateUname(uname)) {
                sendReply(
                        "You are registering as a new user. You will be required to enter a new password.\nEnter password:");
            } else {
                sendReply("Username found!\nEnter password:");
            }
            System.out.println("===== User is attempting to log in from " + clientAddress + ":" + clientPort);

            activeUsersLock.unlock();
            return true;
        }

        /**
         * Process username and/or password for account login
         * 
         * @param uname    The username of user trying to login
         * @param password The password of user trying to login
         * @param newUser  True if adding new user, false otherwise
         */
        private boolean processLogin(String uname, String password, boolean newUser) {
            // check for valid password
            if (!newUser && !validatePassword(uname, password, newUser)) {
                sendReply("LOGIN ERROR: Incorrect password. Try again.\nEnter username:");

                // remove user if added already
                if (activeUsers.containsKey(uname)) {
                    activeUsers.remove(uname);
                }
                return false;

            }

            if (newUser) {
                // add the new credentials to credentials.txt. At this stage, credential is
                // password
                addCredentials(uname, password);
                sendReply("REGISTRATION SUCCESS");
            } else {
                sendReply("LOGIN SUCCESS");
            }
            System.out.println("===== User '" + uname + "' has logged in from " + clientAddress + ":" + clientPort);
            return true;
        }

        /**
         * Get the regex string for a filename associated with the given thread
         * 
         * @param threadname The name of the thread
         */
        private String getFileRegex(String threadname) {
            return "^" + threadname + fileSuffixRegex;
        }

        /**
         * Get all threads by iterating through all files in current directory
         */
        private File[] getAllThreads() {
            File dir = new File(cwd);
            final Pattern p = Pattern.compile(threadRegex);
            FileFilter filter = new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return p.matcher(file.getName()).matches();
                }
            };
            File[] files = dir.listFiles(filter);
            return files;
        }

        /**
         * Get all thread files matching the threadname
         * 
         * @param threadname The name of the thread
         */
        private File[] getAllThreadFiles(String threadname) {
            File dir = new File(cwd);
            final Pattern p = Pattern.compile(getFileRegex(threadname));
            FileFilter filter = new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return p.matcher(file.getName()).matches();
                }
            };
            File[] files = dir.listFiles(filter);
            return files;
        }

        /**
         * Check if a thread already exists in current directory
         * 
         * @param threadname The name of the thread
         */
        private boolean threadExists(String threadname) {
            File[] files = getAllThreads();
            if (files == null) {
                return false;
            }
            for (File f : files) {
                if (f.getName().equals(threadname)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Check if a file already exists in a given thread
         * 
         * @param threadname The name of the thread
         * @param filename   The name of the file
         */
        private boolean fileExists(String threadname, String filename) {
            File[] files = getAllThreadFiles(threadname);
            String fname = threadname + "-" + filename;
            if (files == null) {
                return false;
            }
            for (File f : files) {
                if (f.getName().equals(fname)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Create a new file for a new thread
         * 
         * @param threadname The name of the thread
         * @param unname     The username of the user creating the thread
         */
        private void createThreadFile(String threadname, String uname) {
            try {
                File file = new File(threadname);
                file.createNewFile();
                FileWriter fw = new FileWriter(file, true); // true here will append to file
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(uname);
                bw.newLine();
                bw.close();
                fw.close();
            } catch (IOException e) {
                System.out.println("===== Error adding new thread: " + e.getMessage());
            }
        }

        /**
         * Find the thread file and return all its contents.
         * 
         * @param threadname The name of the thread which is the file to retrieve
         * @param noCreator  Boolean to separate out username that is creator of thread
         *                   or not
         */
        private String readThread(String threadname) {
            String contents = "";
            try {
                File file = new File(threadname);
                Scanner myReader = new Scanner(file);
                while (myReader.hasNextLine()) {
                    String data = myReader.nextLine();
                    contents += data + "\n";
                }
                myReader.close();
            } catch (FileNotFoundException e) {
                System.out.println("===== Error fetching thread messages: " + e.getMessage());
            }
            return contents;
        }

        /**
         * Delete thread file
         * 
         * @param threadname The name of the thread which is the file to retrieve
         */
        private void deleteThreadFile(String threadname) {
            // delete thread
            File file = new File(threadname);
            file.delete();

            // delete all files associated with the thread
            File[] files = getAllThreadFiles(threadname);
            for (File f : files) {
                f.delete();
            }
        }

        /**
         * Check if the username given was the original creator of the thread
         * 
         * @param threadname The name of the thread which is the file to retrieve
         * @param unname     The username of the user to verify
         */
        private boolean isCreator(String threadname, String uname) {
            try {

                File file = new File(threadname);

                // get first message
                Scanner myReader = new Scanner(file);
                String threadCreator = myReader.nextLine();
                myReader.close();
                return threadCreator.equals(uname);

            } catch (FileNotFoundException e) {
                System.out.println("===== Error fetching thread messages: " + e.getMessage());
                return false;
            }
        }

        /**
         * Add a message to the thread, returns message sent to thread in exact format
         * 
         * @param threadname     The name of the thread which is the file to retrieve
         * @param unname         The username of the user adding the message
         * @param messageContent The message to add to the thread
         */
        private String addMessage(String threadname, String uname, String messageContent) {
            try {
                File file = new File(threadname);

                // get last message number (last line starting with a number)
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                int lmsgno = 1;
                String currentLine = "";

                while ((currentLine = br.readLine()) != null) {
                    if (!currentLine.trim().isEmpty() && currentLine.matches(msgNoRegex)) {
                        lmsgno = Integer.parseInt(currentLine.split(" ", 2)[0]);
                    }
                }

                // get message format and append to file
                String msgToAdd = String.valueOf(lmsgno + 1) + " " + uname + ": " + messageContent;

                FileWriter fw = new FileWriter(file, true);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(msgToAdd);
                bw.newLine();
                bw.close();
                fw.close();

                return msgToAdd;
            } catch (IOException e) {
                return "ERROR";
            }
        }

        /**
         * Retrieve message from a thread, validates whether it exists or not, or
         * whether uname given sent the message. Assumes thread file exists.
         * 
         * @param threadname  The name of the thread which is the file to retrieve
         * @param messageNo   The message number of message to retrieve
         * @param checkSender Boolean to do additional uname check
         * @param unname      The username of the user to verify. Leave empty if
         *                    isSender is false (since Java doesn't support default
         *                    params)
         */
        private String getThreadMessage(String threadname, int messageNo, boolean checkSender, String uname) {
            try {

                File file = new File(threadname);

                // get message matching messageNo
                Scanner myReader = new Scanner(file);
                while (myReader.hasNextLine()) {
                    String msg = myReader.nextLine();
                    if (!msg.trim().isEmpty() && msg.matches(msgNoRegex)) {

                        String[] parts = msg.split(" ", 3);
                        int msgNo = Integer.parseInt(parts[0]);
                        String unameToCheck = parts[1].substring(0, parts[1].length() - 1); // get rid of ":" character
                                                                                            // at end of uname in msg

                        if (msgNo == messageNo) {
                            myReader.close();
                            if (checkSender && unameToCheck.equals(uname)) {
                                return msg;
                            }
                            if (checkSender) {
                                return "User did not send this message";
                            }
                            return msg;
                        }
                    }
                }

                myReader.close();
                return "Message does not exist";

            } catch (FileNotFoundException e) {
                System.out.println("===== Error getting message: " + e.getMessage());
                return "Message cannot be retrieved";
            }
        }

        /**
         * Add a message to the thread, returns message sent to thread in exact format
         * 
         * @param threadname The name of the thread which is the file to retrieve
         * @param oldMsg     The existing message to be edited
         * @param newMsg     The new message to replace the existing message
         */
        private String editMessage(String threadname, String oldMsg, String newMsg) {
            try {
                File file = new File(threadname);
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                StringBuffer inputBuffer = new StringBuffer();

                String line;
                while ((line = br.readLine()) != null) {
                    inputBuffer.append(line + "\n");
                }
                br.close();

                String inputStr = inputBuffer.toString();
                inputStr = inputStr.replace(oldMsg, newMsg);

                FileWriter fw = new FileWriter(file, false);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(inputStr);
                bw.close();
                fw.close();

                return "SUCCESS";
            } catch (Exception e) {
                return "ERROR";
            }
        }

        /**
         * Add a message to the thread, returns message sent to thread in exact format
         * 
         * @param threadname The name of the thread which is the file to retrieve
         * @param oldMsg     The existing message to delete
         */
        private String deleteMessage(String threadname, String oldMsg) {
            try {
                File file = new File(threadname);
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                StringBuffer inputBuffer = new StringBuffer();

                String line;
                while ((line = br.readLine()) != null) {
                    inputBuffer.append(line + "\n");
                }
                br.close();

                String inputStr = inputBuffer.toString();
                inputStr = inputStr.replace(oldMsg + "\n", "");

                FileWriter fw = new FileWriter(file, false);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(inputStr);
                bw.close();
                fw.close();

                return "SUCCESS";
            } catch (Exception e) {
                return "ERROR";
            }
        }

        /**
         * Send TCP request to upload file, and close after receiving response from
         * client with the file
         * Also adds notification inside the thread file that the user uploaded the file
         * 
         * @param threadname The name of the thread which is the file to retrieve
         * @param filename   The name of the file to upload
         * @param uname      The username of the user initiating the upload of the file
         */
        private String uploadFile(String threadname, String filename, String uname) {
            try {
                ServerSocket serverTCPSocket = new ServerSocket(serverPort);
                File myFile = new File(threadname + "-" + filename);
                myFile.createNewFile();

                // send confirmation to client that TCP connection is open
                sendReply("UPD: Ready to receive file.");

                // accept client TCP connection before proceeding
                Socket clientTCPSocket = serverTCPSocket.accept();

                // receive file from client and add to new file
                byte[] mybytearray = new byte[pacByteLength];
                InputStream is = clientTCPSocket.getInputStream();
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myFile));

                int bytesRead = is.read(mybytearray, 0, mybytearray.length);
                bos.write(mybytearray, 0, bytesRead);

                // close all connections
                bos.close();
                is.close();
                clientTCPSocket.close();
                serverTCPSocket.close();

                // Add notification to thread that user has uploaded file
                File file = new File(threadname);
                String msgToAdd = uname + " uploaded " + filename;

                // append to file
                FileWriter fw = new FileWriter(file, true);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(msgToAdd);
                bw.newLine();
                bw.close();
                fw.close();

                return "SUCCESS";
            } catch (IOException e) {
                return "ERROR";
            }
        }

        /**
         * Initiate TCP to send file to client, then close after receiving successful
         * file received.
         * 
         * @param threadname The name of the thread which is the file to retrieve
         * @param filename   The name of the file to upload
         */
        private String downloadFile(String threadname, String filename) {
            try {
                System.out.println(">>> Setting up TCP connection...");
                ServerSocket serverTCPSocket = new ServerSocket(serverPort);
                File myFile = new File(threadname + "-" + filename);

                // send confirmation to client that TCP connection is open
                sendReply("DWN: Ready to send file.");

                // accept client TCP connection before proceeding
                Socket clientTCPSocket = serverTCPSocket.accept();

                // send file to client
                byte[] mybytearray = new byte[(int) myFile.length()];
                FileInputStream fis = new FileInputStream(myFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                OutputStream os = clientTCPSocket.getOutputStream();

                bis.read(mybytearray, 0, mybytearray.length);
                os.write(mybytearray, 0, mybytearray.length);
                os.flush();

                // close all connections
                bis.close();
                fis.close();
                os.close();
                clientTCPSocket.close();
                serverTCPSocket.close();

                return "SUCCESS";
            } catch (IOException e) {
                return "ERROR";
            }
        }

        /**
         * Process messages received from client according to the protocol.
         * 
         * Protocol commands (not necessarily what user enters from client side):
         * - HEALTHCHECK: Verify that client can connect to server
         * - USERNAME <username>: Verify username for login
         * - PASSSWORD <username> <password>: Verify password for login
         * - CRT <threadname> <username>: Create new thread
         * - LST: List all thread
         * - RDT <threadname>: Read messages in thread
         * - RMV <threadname> <username>: Delete thread
         * - MSG <threadname> <username> <message>: Add message to thread
         * - EDT <threadname> <message-number> <username> <message>: Edit message in
         * thread
         * - DLT <threadname> <message-number> <username>: Delete message from thread
         * - UPD <threadname> <filename> <username>: Upload file to thread
         * - DWN <threadname> <filename> <username>: Download file from thread
         * - XIT <username>: Disconnect user from the server
         * 
         * @param message The message to be processed
         */
        private void processMessage(String message) {
            System.out.println("[recv] " + message + " from " + clientAddress + ":" + clientPort);

            // Split message into command and arguments
            String[] parts = message.split(" ", 2);
            String command = parts[0].toUpperCase();

            if (command.equals("HEALTHCHECK")) {
                // test status of service, in order to first establish a connection
                sendReply("ALIVE");

            } else if (command.equals("USERNAME")) {
                // login 1 - username
                if (parts.length < 2) {
                    sendReply("ERROR: Username cannot be empty.");
                    return;
                }

                // Parse username
                String[] msgParts = parts[1].split(" ", 2);
                if (msgParts.length != 1 || msgParts[0].trim().isEmpty()) {
                    sendReply("ERROR: Username cannot be empty.");
                    return;
                }

                String uname = msgParts[0].trim();

                processUname(uname);

            } else if (command.equals("PASSWORD")) {
                // login 2 - password
                if (parts.length < 2) {
                    sendReply("ERROR: Password cannot be empty.");
                    return;
                }

                // Parse username, password and newUser
                String[] msgParts = parts[1].split(" ", 4);
                if (msgParts.length != 3 || msgParts[0].trim().isEmpty()) {
                    sendReply("ERROR: Password cannot be empty.");
                    return;
                }

                String uname = msgParts[0].trim();
                String password = msgParts[1].trim();
                boolean newUser = Boolean.parseBoolean(msgParts[2].trim());
                processLogin(uname, password, newUser);

            } else if (command.equals("CRT")) {

                if (parts.length < 2) {
                    sendReply("ERROR: Usage: CRT <threadname>");
                    return;
                }

                // Parse threadtitle and username, ensure 2 arguments and threadtitle doesn't
                // contain spaces
                String[] msgParts = parts[1].split(" ", 2);
                if (msgParts.length != 2 || msgParts[0].trim().isEmpty()) {
                    sendReply("ERROR: Threadname cannot be empty. Threadname cannot contain spaces.");
                    return;
                }

                String threadname = msgParts[0].trim();
                String uname = msgParts[1].trim();

                // check if thread title already exists
                if (threadExists(threadname)) {
                    sendReply("ERROR: Threadname already exists. Please choose another name.");
                    return;
                }

                activeUsersLock.lock();

                // create new file for the thread. Title is threadname. First line is user that
                // created the thread
                // each line after will list messages
                createThreadFile(threadname, uname);

                // Send confirmation to user
                sendReply("SUCCESS: New thread " + threadname + " created.\n");

                System.out.println("===== New thread '" + threadname + "' created by '" + uname + "'");
                activeUsersLock.unlock();

            } else if (command.equals("LST")) {
                // list all threads
                if (parts.length != 1) {
                    sendReply("ERROR: Usage: LST");
                    return;
                }

                activeUsersLock.lock();

                File[] files = getAllThreads();
                if (files == null) {
                    sendReply("ERROR: Unexpected error occurred in checking for existing threads");
                    return;
                }

                if (files.length == 0) {
                    // do not return error msg, as there are no threads in the first place
                    sendReply("No threads to list.");
                } else {
                    String response = "List of active threads:\n";
                    for (File f : files) {
                        response += f.getName() + "\n";
                    }
                    sendReply(response);
                }
                System.out.println("===== '" + getUsername() + "' has requested to list all threads");
                activeUsersLock.unlock();

            } else if (command.equals("RDT")) {
                // read thread
                if (parts.length < 2) {
                    sendReply("ERROR: Usage: RDT <threadname>");
                    return;
                }

                // Parse threadtitle and username, ensure 2 arguments and threadtitle doesn't
                // contain spaces
                String[] msgParts = parts[1].split(" ", 2);
                if (msgParts.length != 1 || msgParts[0].trim().isEmpty()) {
                    sendReply("ERROR: Threadname cannot be empty.");
                    return;
                }

                String threadname = msgParts[0].trim();
                if (!threadExists(threadname)) {
                    sendReply("ERROR: Thread does not exist.");
                    return;
                }

                activeUsersLock.lock();
                String contents = readThread(threadname);

                if ((contents.trim().isEmpty()) || (contents.split("\n", 2).length == 1)
                        || (contents.split("\n", 2)[1].trim().isEmpty())) {
                    sendReply("Thread '" + threadname + "' is empty.\n");
                    System.out.println(
                            "===== '" + getUsername() + "' is reading thread '" + threadname + "' (which is empty)");
                    activeUsersLock.unlock();
                    return;
                }

                // separate out the username part of the thread
                String messages = contents.split("\n", 2)[1];
                sendReply("Contents of thread " + threadname + ":\n" + messages);
                System.out.println("===== '" + getUsername() + "' is reading thread '" + threadname + "'");
                activeUsersLock.unlock();

            } else if (command.equals("RMV")) {
                // remove thread
                if (parts.length < 2) {
                    sendReply("ERROR: Usage: RMV <threadname>");
                    return;
                }

                // Parse threadtitle and username, ensure 2 arguments and threadtitle doesn't
                // contain spaces
                String[] msgParts = parts[1].split(" ", 2);
                if (msgParts.length < 2 || msgParts[0].trim().isEmpty()) {
                    sendReply("ERROR: Threadname cannot be empty.");
                    return;
                }

                String threadname = msgParts[0].trim();
                if (!threadExists(threadname)) {
                    sendReply("ERROR: Thread does not exist.");
                    return;
                }
                String uname = msgParts[1].trim();
                if (!isCreator(threadname, uname)) {
                    sendReply("ERROR: You do not have permission to delete this thread.");
                    return;
                }

                activeUsersLock.lock();
                deleteThreadFile(threadname);
                sendReply("Thread '" + threadname + "' removed.\n");

                System.out.println("===== '" + getUsername() + "' has deleted thread '" + threadname + "'");
                activeUsersLock.unlock();

            } else if (command.equals("MSG")) {
                // add message
                if (parts.length < 2) {
                    sendReply("ERROR: Usage: MSG <threadname> <message>");
                    return;
                }

                // Parse target username and message content
                String[] msgParts = parts[1].split(" ", 3);
                if (msgParts.length < 3 || msgParts[2].trim().isEmpty()) {
                    sendReply("ERROR: Missing message content.");
                    return;
                }

                String threadname = msgParts[0].trim();
                if (!threadExists(threadname)) {
                    sendReply("ERROR: Thread does not exist.");
                    return;
                }
                String uname = msgParts[1].trim();
                String messageContent = msgParts[2].trim();

                // Check if target user exists and is active
                activeUsersLock.lock();

                String response = addMessage(threadname, uname, messageContent);

                if (response.equals("ERROR")) {
                    sendReply("ERROR: Unexpected error occured while trying to add message.");
                } else {
                    sendReply("Message sent to thread '" + threadname + "':\n" + response + "\n");
                    System.out
                            .println("===== Message sent from '" + getUsername() + "' to thread '" + threadname + "'");
                }
                activeUsersLock.unlock();

            } else if (command.equals("EDT")) {
                // edit message
                if (parts.length < 2) {
                    sendReply("ERROR: Usage: EDT <threadname> <message-number> <message>");
                    return;
                }

                // Parse target username and message content
                String[] msgParts = parts[1].split(" ", 4);
                if (msgParts.length < 4 || msgParts[3].trim().isEmpty()) {
                    sendReply("ERROR: Missing message content.");
                    return;
                }

                String threadname = msgParts[0].trim();
                if (!threadExists(threadname)) {
                    // try {
                    sendReply("ERROR: Thread does not exist.");
                    // } catch (IOException e) {
                    // System.out.println("===== Error sending error message: " + e.getMessage());
                    // }
                    return;
                }

                String messageNo = msgParts[1].trim();
                int msgNo;
                try {
                    msgNo = Integer.parseInt(messageNo);
                } catch (NumberFormatException ee) {
                    sendReply("ERROR: Message number is not a number.");
                    return;
                }

                String uname = msgParts[2].trim();
                String oldMsg = getThreadMessage(threadname, msgNo, true, uname);
                if (oldMsg.equals("Message does not exist")) {
                    sendReply("ERROR: Message does not exist in thread.");
                    return;
                } else if (oldMsg.equals("User did not send this message")) {
                    sendReply("ERROR: You do not have permission to edit this message.");
                    return;
                } else if (oldMsg.equals("Message cannot be retrieved")) {
                    sendReply("ERROR: Unexpected error occurred while trying to edit message.");
                    return;
                }

                String messageContent = msgParts[3].trim();

                activeUsersLock.lock();

                String newMsg = messageNo + " " + uname + ": " + messageContent;
                String response = editMessage(threadname, oldMsg, newMsg);

                if (response.equals("ERROR")) {
                    sendReply("ERROR: Unexpected error occured while trying to edit message.");
                } else {
                    sendReply("Message successfully edited in '" + threadname + "':\n" + newMsg + "\n");
                    System.out.println("===== Message " + msgNo + " edited by '" + getUsername() + "' in thread '"
                            + threadname + "'");
                }
                activeUsersLock.unlock();

            } else if (command.equals("DLT")) {
                // delete message
                if (parts.length < 2) {
                    sendReply("ERROR: Usage: MSG <threadname> <message>");
                    return;
                }

                // Parse target username and message content
                String[] msgParts = parts[1].split(" ", 3);
                if (msgParts.length < 3 || msgParts[2].trim().isEmpty()) {
                    sendReply("ERROR: Missing message content.");
                    return;
                }

                String threadname = msgParts[0].trim();
                if (!threadExists(threadname)) {
                    sendReply("ERROR: Thread does not exist.");
                    return;
                }

                String messageNo = msgParts[1].trim();
                int msgNo;
                try {
                    msgNo = Integer.parseInt(messageNo);
                } catch (NumberFormatException ee) {
                    sendReply("ERROR: Message number is not a number.");
                    return;
                }

                String uname = msgParts[2].trim();
                String oldMsg = getThreadMessage(threadname, msgNo, true, uname);
                if (oldMsg.equals("Message does not exist")) {
                    sendReply("ERROR: Message does not exist in thread.");
                    return;
                } else if (oldMsg.equals("User did not send this message")) {
                    sendReply("ERROR: You do not have permission to delete this message.");
                    return;
                } else if (oldMsg.equals("Message cannot be retrieved")) {
                    sendReply("ERROR: Unexpected error occurred while trying to edit message.");
                    return;
                }

                activeUsersLock.lock();

                String response = deleteMessage(threadname, oldMsg);

                if (response.equals("ERROR")) {
                    sendReply("ERROR: Unexpected error occured while trying to edit message.");
                } else {
                    sendReply("Message successfully deleted in '" + threadname + "'\n");
                    System.out.println("===== Message " + msgNo + " deleted by '" + getUsername() + "' in thread '"
                            + threadname + "'");
                }
                activeUsersLock.unlock();

            } else if (command.equals("UPD")) {
                // upload file
                if (parts.length < 2) {
                    sendReply("ERROR: Usage: UPD <threadname> <filename>");
                    return;
                }

                // Parse target username and message content
                String[] msgParts = parts[1].split(" ");
                if (msgParts.length != 3 || msgParts[2].trim().isEmpty()) {
                    sendReply("ERROR: Missing filename.");
                    return;
                }

                String threadname = msgParts[0].trim();
                if (!threadExists(threadname)) {
                    sendReply("ERROR: Thread does not exist.");
                    return;
                }

                String filename = msgParts[1].trim();
                if (fileExists(threadname, filename)) {
                    sendReply("ERROR: File already exists in thread. Please upload a different file.");
                    return;
                }

                String uname = msgParts[2].trim();

                activeUsersLock.lock();

                // TCP file transfer
                String response = uploadFile(threadname, filename, uname);

                if (response.equals("ERROR")) {
                    sendReply("ERROR: Unexpected error occured while trying to upload file.");
                } else {
                    sendReply("SUCCESS: File '" + filename + "' uploaded to thread '" + threadname + "'\n");
                    System.out.println(
                            "===== '" + getUsername() + "' has uploaded a file to thread '" + threadname + "'");
                }
                activeUsersLock.unlock();

            } else if (command.equals("DWN")) {
                // download file
                if (parts.length < 2) {
                    sendReply("ERROR: Usage: DWN <threadname> <filename>");
                    return;
                }

                // Parse target username and message content
                String[] msgParts = parts[1].split(" ");
                if (msgParts.length != 2 || msgParts[1].trim().isEmpty()) {
                    sendReply("ERROR: Missing filename.");
                    return;
                }

                String threadname = msgParts[0].trim();
                if (!threadExists(threadname)) {
                    sendReply("ERROR: Thread does not exist.");
                    return;
                }

                String filename = msgParts[1].trim();
                if (!fileExists(threadname, filename)) {
                    sendReply("ERROR: File does not exist in thread.");
                    return;
                }

                activeUsersLock.lock();

                // TCP file transfer
                String response = downloadFile(threadname, filename);

                if (response.equals("ERROR")) {
                    sendReply("ERROR: Unexpected error occured while trying to download file.");
                } else {
                    sendReply("SUCCESS: File '" + filename + "' downloaded from thread '" + threadname + "'\n");
                    System.out.println(
                            "===== '" + getUsername() + "' has downloaded a file from thread '" + threadname + "'");
                }

                // Send confirmation to sender
                sendReply("SUCCESS: '" + filename + "' uploaded to thread '" + threadname + "'\n");
                System.out.println("===== '" + getUsername() + "' Uploaded a file to thread '" + threadname + "'");
                activeUsersLock.unlock();

            } else if (command.equals("XIT")) {
                // logout of the system
                System.out.println(
                        "===== User " + getUsername() + " (" + clientAddress + ":" + clientPort + ") is exiting");

                // Remove from active users
                activeUsersLock.lock();

                // get client address and port Pair object from activeUsers
                String uname = null;
                for (Entry<String, Map<InetAddress, Integer>> entry : activeUsers.entrySet()) {
                    if (entry.getValue().equals(this.clientDetails)) {
                        uname = entry.getKey();
                        break;
                    }
                }
                if (uname != null) {
                    activeUsers.remove(uname);
                }
                activeUsersLock.unlock();
                sendReply("LOGOUT SUCCESS");

            } else {
                sendReply("ERROR: Invalid command.");
            }
        }
    }

    /**
     * Main server loop
     */
    public static void main(String[] args) {
        // Check command line arguments
        if (args.length != 1) {
            System.out.println("\n===== Error usage: java Server <SERVER_PORT> ======\n");
            return;
        }

        // Parse server port
        serverPort = Integer.parseInt(args[0]);

        try {
            // Create server socket
            serverSocket = new DatagramSocket(serverPort); // UDP
            System.out.println("\n===== Server is running on port " + serverPort + " =====");
            System.out.println("===== Waiting for connection requests from clients... =====");

            byte[] bytes = new byte[recByteLength];
            DatagramPacket request = null;

            // Main server loop for accepting connections
            while (true) {

                request = new DatagramPacket(bytes, bytes.length);
                serverSocket.receive(request);

                ClientHandler clientHandler = new ClientHandler(request);
                clientHandler.start();

                // await next packet
                bytes = new byte[recByteLength];
            }

        } catch (IOException e) {
            System.out.println("\n===== Error: " + e.getMessage() + " =====");
        } finally {

            // Clean up server socket
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            System.out.println("===== Server socket closed =====");
        }
    }
}
