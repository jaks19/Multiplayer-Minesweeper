package minesweeper.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Random;

import org.junit.Test;

/**
 * This Class provides a suite of tests for MinesweeperServer Class
 */
public class MinesweeperServerTest {

    /*
     * OVERALL TESTING STRATEGY:
     *  MinesweeperServer will be subjected to 2 groups of tests:
     *   1. Tests using all the functionality with 1 client connected
     *   2. Tests with many clients connected to test multiplayer features
     * 
     * 1. Tests using all the functionality with 1 client connected
     *  - Check welcome message
     *  - Use help
     *  - Use look
     *  - Dig, flag and deflag cells not found in board
     *  - Dig flag and deflag untouched cells in board
     *  - Dig dug and flagged cells
     *  - Flag dug and flagged cells
     *  - Deflag dug and flagged cells
     *  - Dig bombed cell
     *  - Use bye
     *  
     * 2. Tests with many clients connected to test multiplayer features
     *  - Connect 2 clients then connect a third and make sure he sees 3 players total count
     *  - Dig untouched cell by player1, check new board by player2
     *  - Flag untouched cell by player3, check new board by player1
     *  - Deflag flagged cell by player2, check new board by player3
     */

    // TEST SECTION 1 (Tests using all the functionality with 1 client connected)

    @Test(timeout = 10000)
    public void serverTest() throws IOException {
        Thread thread = startMinesweeperServer("ServerTestSingleAndMulti");
        Socket socket = connectToMinesweeperServer(thread);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // Check welcome message
        assertTrue("expected HELLO message", in.readLine().equals("Welcome to Minesweeper. "
                + "Board: 2 columns by 2 rows. Players: 1 including you. Type 'help' for help."));

        // Use help
        out.println("help");
        assertEquals("Available Actions: 'dig x y' or 'flag x y' or 'deflag x y' "
                + "where x and y are coordinates of the cell", in.readLine());
        assertEquals("Other Commands: 'look' : Shows the board, 'bye' : Ends game",
                in.readLine());

        // Use look
        out.println("look");
        assertEquals("- -", in.readLine());
        assertEquals("- -", in.readLine());

        // Dig, flag and deflag cells not found in board
        out.println("dig 9 9");
        assertEquals("- -", in.readLine());
        assertEquals("- -", in.readLine());

        out.println("flag -8 -9");
        assertEquals("- -", in.readLine());
        assertEquals("- -", in.readLine());

        out.println("deflag -5 5");
        assertEquals("- -", in.readLine());
        assertEquals("- -", in.readLine());

        // Dig flag and deflag untouched cells in board
        out.println("dig 0 0");
        assertEquals("1 -", in.readLine());
        assertEquals("- -", in.readLine());

        out.println("flag 1 0");
        assertEquals("1 F", in.readLine());
        assertEquals("- -", in.readLine());

        out.println("deflag 1 1");
        assertEquals("1 F", in.readLine());
        assertEquals("- -", in.readLine());

        // Dig dug and flagged cells
        out.println("dig 0 0");
        assertEquals("1 F", in.readLine());
        assertEquals("- -", in.readLine());

        out.println("dig 1 0");
        assertEquals("1 F", in.readLine());
        assertEquals("- -", in.readLine());

        // Flag dug and flagged cells
        out.println("flag 0 0");
        assertEquals("1 F", in.readLine());
        assertEquals("- -", in.readLine());

        out.println("flag 1 0");
        assertEquals("1 F", in.readLine());
        assertEquals("- -", in.readLine());

        // Deflag dug and flagged cells
        out.println("deflag 0 0");
        assertEquals("1 F", in.readLine());
        assertEquals("- -", in.readLine());

        out.println("deflag 1 0");
        assertEquals("1 -", in.readLine());
        assertEquals("- -", in.readLine());




        // TEST SECTION 2 (Tests with many clients connected to test multiplayer features) -----------------------------

        // Connect 2 clients then connect a third and make sure he sees 3 players total count
        Socket socket2 = connectToMinesweeperServer(thread); // Player 2
        BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
        PrintWriter out2 = new PrintWriter(socket2.getOutputStream(), true);

        assertTrue(in2.readLine().equals("Welcome to Minesweeper. "
                + "Board: 2 columns by 2 rows. Players: 2 including you. Type 'help' for help."));

        Socket socket3 = connectToMinesweeperServer(thread); // Player 3
        BufferedReader in3 = new BufferedReader(new InputStreamReader(socket3.getInputStream()));
        PrintWriter out3 = new PrintWriter(socket3.getOutputStream(), true);

        assertTrue(in3.readLine().equals("Welcome to Minesweeper. "
                + "Board: 2 columns by 2 rows. Players: 3 including you. Type 'help' for help."));

        // Dig untouched cell by player1, check new board by player2
        out.println("dig 1 0");
        in.readLine();
        in.readLine();
        out2.println("look");
        assertEquals("1 1", in2.readLine());
        assertEquals("- -", in2.readLine());

        // Flag untouched cell by player3, check new board by player1
        out3.println("flag 1 1");
        in3.readLine();
        in3.readLine();
        out.println("look");
        assertEquals("1 1", in.readLine());
        assertEquals("- F", in.readLine());

        // Deflag flagged cell by player2, check new board by player3
        out2.println("deflag 1 1");
        in2.readLine();
        in2.readLine();
        out3.println("look");
        assertEquals("1 1", in3.readLine());
        assertEquals("- -", in3.readLine());




        // Last part of TEST SECTION 1:

        // Dig bombed cell
        out.println("dig 1 1");
        assertEquals("BOOM!", in.readLine());

        // Use bye
        out.println("bye");
        socket.close();
        socket2.close();
        socket3.close();
        thread.interrupt();
    }




    // HELPER CODE
    // COPIED FROM AUTIGRADER TEST FILE TO GENERATE THE TESTS:
    private static final String LOCALHOST = "127.0.0.1";
    private static final int PORT = 4000 + new Random().nextInt(1 << 15);
    private static final int MAX_CONNECTION_ATTEMPTS = 10;
    private static final String BOARDS_PKG = "minesweeper/server/";

    /**
     * Connect to a MinesweeperServer and return the connected socket.
     * @param server abort connection attempts if the server thread dies
     * @return socket connected to the server
     * @throws IOException if the connection fails
     */
    private static Socket connectToMinesweeperServer(Thread server) throws IOException {
        int attempts = 0;
        while (true) {
            try {
                Socket socket = new Socket(LOCALHOST, PORT);
                socket.setSoTimeout(3000);
                return socket;
            } catch (ConnectException ce) {
                if ( ! server.isAlive()) {
                    throw new IOException("Server thread not running");
                }
                if (++attempts > MAX_CONNECTION_ATTEMPTS) {
                    throw new IOException("Exceeded max connection attempts", ce);
                }
                try { Thread.sleep(attempts * 10); } catch (InterruptedException ie) { }
            }
        }
    }

    /**
     * Start a MinesweeperServer in debug mode with a board file from BOARDS_PKG.
     * @param boardFile board to load
     * @return thread running the server
     * @throws IOException if the board file cannot be found
     */
    private static Thread startMinesweeperServer(String boardFile) throws IOException {
        final URL boardURL = ClassLoader.getSystemClassLoader().getResource(BOARDS_PKG + boardFile);
        if (boardURL == null) {
            throw new IOException("Failed to locate resource " + boardFile);
        }
        final String boardPath;
        try {
            boardPath = new File(boardURL.toURI()).getAbsolutePath();
        } catch (URISyntaxException urise) {
            throw new IOException("Invalid URL " + boardURL, urise);
        }
        final String[] args = new String[] {
                "--debug",
                "--port", Integer.toString(PORT),
                "--file", boardPath
        };
        Thread serverThread = new Thread(() -> MinesweeperServer.main(args));
        serverThread.start();
        return serverThread;
    }
}
