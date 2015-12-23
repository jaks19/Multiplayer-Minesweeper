package minesweeper.server;

import java.io.*;
import java.net.*;
import java.util.*;

import minesweeper.Board;

/**
 * Multiplayer Minesweeper server.
 */
public class MinesweeperServer {

    //Constants:
    private static final int DEFAULT_PORT = 4444; 
    private static final int MAXIMUM_PORT = 65535; 
    private static final int DEFAULT_SIZE = 5;

    //Fields of this Class:
    private final ServerSocket serverSocket; // Socket for receiving incoming connections
    private final boolean debug; //True if server must NOT disconnect client after BOOM!
    private final Board board; // Board played on
    private int numPlayers = 0; // Number of clients connected

    // System thread safety argument:
    // - The Object board and the Integer numPlayers are the only mutable parts of the rep.
    //   A. numPlayers is never changed concurrently but rather sequentially in the default thread
    //   B. board is final and private, and it is a synchronized thread-safe data type
    //       Clients have to acquire the lock on the board Object to view or mutate it
    // - Hence, this ensures that the MinesweeperServer class is on overall thread-safe.

    // Abstraction function:
    // - A Minesweeper game session with numPlayers being the number of players playing
    // - this.board is the Minesweeper field being played on

    // Representation invariant:
    // - numPlayers >= 0

    // Safety from representation exposure:
    // - Only returned type is String which is immutable
    // - Fields are private and final (int is not final as it needs to be changed
    //      but it cannot be changed from outside this Class)

    /**
     * Make a MinesweeperServer that listens for connections on port
     *  and updates a Board object
     * 
     * @param port The port number, requires 0 <= port <= 65535
     * @param debug The debug mode flag
     * @param board The Minesweeper Board to be updated by this server
     * @throws IOException if an error occurs opening the server socket
     */
    public MinesweeperServer(int port, boolean debug, Board board) 
            throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.debug = debug;
        this.board = board;
    }

    /**
     * Run the server, listening for client connections and handling them.
     * Never returns unless an exception is thrown.
     * 
     * @throws IOException if the main server socket is broken
     *                     (IOExceptions from individual clients do *not* terminate serve())
     */
    public void serve() throws IOException {
        while (true) {
            // block until a client connects
            Socket socket = serverSocket.accept();
            // When a new client connects to socket, increment numPlayers
            numPlayers += 1;

            // handle the client
            // Give each client a thread and handle each in its own thread
            Thread clientHandler = new Thread(new Runnable(){
                public void run() {
                    try {
                        handleConnection(socket);
                    } catch (IOException ioe) {
                        ioe.printStackTrace(); // but don't terminate serve()
                    } finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }); clientHandler.start();
        }
    }

    /**
     * Handle a single client connection. Returns when client disconnects.
     * 
     * @param socket socket where the client is connected
     * @throws IOException if the connection encounters an error or terminates unexpectedly
     */
    private void handleConnection(Socket socket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.print("Welcome to Minesweeper. Board: " 
                + this.board.sizeList().get(0) + " columns by " 
                + this.board.sizeList().get(1) + " rows. Players: " 
                + this.numPlayers + " including you. "
                + "Type 'help' for help.\r\n");
        out.flush();
        try {
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                String output = handleRequest(line);
                if (!output.equals("bye")){
                    out.print(output);
                    out.flush();
                }
                if (output.equals("bye")
                        || (output.equals("BOOM!\r\n")
                                && this.debug == false)){
                    break;
                }
            }
        } finally {
            out.close();
            in.close();
            // Reach here when the client disconnects so decrement numPlayers
            numPlayers -= 1;
        }
    }

    /**
     * Handler for client input, performing requested operations and returning an output message.
     * 
     * @param input message from client
     * @return message to client
     */
    private String handleRequest(String input) {
        String regex = "(look)|(help)|(bye)|"
                + "(dig -?\\d+ -?\\d+)|(flag -?\\d+ -?\\d+)|(deflag -?\\d+ -?\\d+)";
        String helpMessage = "Available Actions: 'dig x y' or 'flag x y' or 'deflag x y' "
                + "where x and y are coordinates of the cell"
                + "\r\n" 
                + "Other Commands: 'look' : Shows the board, 'bye' : Ends game"
                + "\r\n";
        if (!input.matches(regex)) {
            // invalid input
            return helpMessage;
        }
        String[] tokens = input.split(" ");
        if (tokens[0].equals("look")) {
            // 'look' request
            return this.board.toString();
        } 
        else if (tokens[0].equals("help")) {
            // 'help' request
            return helpMessage;
        } 
        else if (tokens[0].equals("bye")) {
            // 'bye' request
            return "bye";
        } 
        else {
            String message = "";
            int x = Integer.parseInt(tokens[1]);
            int y = Integer.parseInt(tokens[2]);
            if (tokens[0].equals("dig")) {
                // 'dig x y' request
                message = this.board.dig(x, y) ? this.board.toString() : "BOOM!\r\n";
            }
            if (tokens[0].equals("flag")) {
                // 'flag x y' request
                this.board.flag(x, y);
                message = this.board.toString();
            } 
            else if (tokens[0].equals("deflag")) {
                // 'deflag x y' request
                this.board.deflag(x, y);
                message = this.board.toString();
            }
            return message;
        }
    }

    /**
     * Start a MinesweeperServer using the given arguments.
     * 
     * <br> Usage:
     *      MinesweeperServer [--debug | --no-debug] [--port PORT] [--size SIZE_X,SIZE_Y | --file FILE]
     * 
     * <br> The --debug argument means the server should run in debug mode. The server should disconnect a
     *      client after a BOOM message if and only if the --debug flag was NOT given.
     *      Using --no-debug is the same as using no flag at all.
     * <br> E.g. "MinesweeperServer --debug" starts the server in debug mode.
     * 
     * <br> PORT is an optional integer in the range 0 to 65535 inclusive, specifying the port the server
     *      should be listening on for incoming connections.
     * <br> E.g. "MinesweeperServer --port 1234" starts the server listening on port 1234.
     * 
     * <br> SIZE_X and SIZE_Y are optional integer arguments specifying that a random board of size
     *      SIZE_X*SIZE_Y should be generated.
     * <br> E.g. "MinesweeperServer --size 42,58" starts the server initialized with a random board of size
     *      42*58.
     * 
     * <br> FILE is an optional argument specifying a file pathname where a board has been stored. If this
     *      argument is given, the stored board should be loaded as the starting board.
     * <br> E.g. "MinesweeperServer --file boardfile.txt" starts the server initialized with the board stored
     *      in boardfile.txt.
     * 
     * <br> The board file format, for use with the "--file" option, is specified by the following grammar:
     * <pre>
     *   FILE ::= BOARD LINE+
     *   BOARD ::= X SPACE Y NEWLINE
     *   LINE ::= (VAL SPACE)* VAL NEWLINE
     *   VAL ::= 0 | 1
     *   X ::= INT
     *   Y ::= INT
     *   SPACE ::= " "
     *   NEWLINE ::= "\r?\n"
     *   INT ::= [0-9]+
     * </pre>
     * 
     * <br> If neither --file nor --size is given, generate a random board of size 10x10.
     * 
     * <br> Note that --file and --size may not be specified simultaneously.
     * 
     * @param args arguments as described
     */
    public static void main(String[] args) {
        // Command-line argument parsing is provided. Do not change this method.
        boolean debug = false;
        int port = DEFAULT_PORT;
        int sizeX = DEFAULT_SIZE;
        int sizeY = DEFAULT_SIZE;
        Optional<File> file = Optional.empty();

        Queue<String> arguments = new LinkedList<String>(Arrays.asList(args));
        try {
            while ( ! arguments.isEmpty()) {
                String flag = arguments.remove();
                try {
                    if (flag.equals("--debug")) {
                        debug = true;
                    } else if (flag.equals("--no-debug")) {
                        debug = false;
                    } else if (flag.equals("--port")) {
                        port = Integer.parseInt(arguments.remove());
                        if (port < 0 || port > MAXIMUM_PORT) {
                            throw new IllegalArgumentException("port " + port + " out of range");
                        }
                    } else if (flag.equals("--size")) {
                        String[] sizes = arguments.remove().split(",");
                        sizeX = Integer.parseInt(sizes[0]);
                        sizeY = Integer.parseInt(sizes[1]);
                        file = Optional.empty();
                    } else if (flag.equals("--file")) {
                        sizeX = -1;
                        sizeY = -1;
                        file = Optional.of(new File(arguments.remove()));
                        if ( ! file.get().isFile()) {
                            throw new IllegalArgumentException("file not found: \"" + file + "\"");
                        }
                    } else {
                        throw new IllegalArgumentException("unknown option: \"" + flag + "\"");
                    }
                } catch (NoSuchElementException nsee) {
                    throw new IllegalArgumentException("missing argument for " + flag);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("unable to parse number for " + flag);
                }
            }
        } catch (IllegalArgumentException iae) {
            System.err.println(iae.getMessage());
            System.err.println("usage: MinesweeperServer [--debug | --no-debug] [--port PORT] [--size SIZE_X,SIZE_Y | --file FILE]");
            return;
        }

        try {
            runMinesweeperServer(debug, file, sizeX, sizeY, port);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Start a MinesweeperServer running on the specified port, with either a random new board or a
     * board loaded from a file.
     * 
     * @param debug The server will disconnect a client after a BOOM message if and only if debug is false.
     * @param file If file.isPresent(), start with a board loaded from the specified file,
     *             according to the input file format defined in the documentation for main(..).
     * @param sizeX If (!file.isPresent()), start with a random board with width sizeX.
     * @param sizeY If (!file.isPresent()), start with a random board with height sizeY.
     * @param port The network port on which the server should listen.
     * @throws IOException if a network error occurs
     */
    public static void runMinesweeperServer(boolean debug, Optional<File> file, int sizeX, int sizeY, int port) throws IOException {
        Board boardCreated;
        if (file.isPresent()){
            boardCreated = new Board(file.get());
        } 
        else{
            boardCreated = new Board(sizeX, sizeY);
        }
        MinesweeperServer server = new MinesweeperServer(port, debug, boardCreated);
        server.serve();
    }
}
