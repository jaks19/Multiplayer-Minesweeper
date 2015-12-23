package minesweeper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * This Class represents a Minesweeper Board of a specific size
 * [Clients may synchronize with each other using the Board object itself]
 */
public class Board {

    //Constants:
    private static final int DEFAULT_SIZE = 10; // Default size of Board (same for length and width --> square)
    private static final int ZERO = 0;
    private static final double DIFFICULTY = 0.25; // Fraction of the board cells that are bombed

    //Fields of this ADT:
    private int length = DEFAULT_SIZE;
    private int width = DEFAULT_SIZE;
    private final String[][] board;
    private final boolean[][] bombs;

    // Abstraction function:
    // - A board of size length x width is represented as an array called board of arrays of String 
    // - Entry board[i][j] is the State of the cell at x-coordinate i and y-coordinate j
    // - The x and y coordinates both start at 0 where cell (0,0) is the upper-left corner cell
    // - The State of a cell can be "-" : Undug and unflagged cell
    //                              "2" : Dug, unbombed cell and contains bombs in 2 of the 8 cells around
    //                                    Number Can be any non-negative integer <= 8
    //                              "F" : Flagged cell
    // - this.length is the length of the board and this.width is the width of the board
    // - Placement of bombs on the board are contained in bombs array
    // - Entry bombs[i][j] gives a boolean. True means there is a bomb at cell with x-coordinate i and y-coordinate j

    // Representation invariant:
    // - this.length > 0, this.width > 0
    // - board.length == this.length
    // - board[i].length == this.width where 0 <= i < this.length
    // - bombs.length == this.length
    // - bombs[i].length == this.width where 0 <= i < this.length

    // Safety from representation exposure:
    // - All fields are private and not accessible from outside this class
    // - All returned data types by methods in the class are immutable e.g. String

    // Thread Safety:
    // - this.length and this.width are private(never accessed by any thread) and 
    //  there is no way to mutate these values (so interleaving does not affect those)
    // - All methods where the mutable part of the rep. is read or changed (board, bombs)
    //  are synchronized using the monitor pattern (same lock) this ensures that the Board object can only
    //  be mutated or viewed by one client at a time
    // - The above statement ensures that even though the array used is not intrinsically synchronized,
    //  concurrency does not cause any problem
    // - All other variables used are confined to the method in which they are used as they need not be 
    //  global for the purpose they serve

    /**
     * Constructs a new Board object based on direct length and width inputs
     *      length, width > 0
     * @param length The number of entries along the length of the board
     * @param width The number of entries along the width of the board
     */
    public Board(int length, int width){
        // Initialize length and width fields
        this.length = length;
        this.width = width;
        // Deal with board and bombs arrays
        this.board = new String[this.length][this.width];
        this.bombs = new boolean[this.length][this.width];
        int counterLength = 0;
        while (counterLength < this.length){
            int counterWidth = 0;
            while (counterWidth < this.width){
                // board initialization
                this.board[counterLength][counterWidth] = "-";
                // bombs initialization
                double r = new Random().nextDouble(); // Generate number between 0.0 and 1.0
                if (r > ZERO && r <= DIFFICULTY){ // 20% of cells are bombed
                    this.bombs[counterLength][counterWidth] = true;
                }
                else {this.bombs[counterLength][counterWidth] = false;}
                counterWidth += 1;
            }
            counterLength += 1;
        }
        checkRep();
    }

    /**
     * Construct a new Board object from a File 
     * @param filename The file directory where the file is found
     *          The file has 1's at bombed cells and 0 at safe cells
     */
    public Board(File filename) {
        try (BufferedReader input = new BufferedReader(new FileReader(filename));) {

            // Dealing with the first line:
            String line1 = input.readLine();
            // Make sure no space at start or end and just 1 space between X, Y
            verifySpaces(line1, 1);
            // Now that we are sure to have line 1 in the form: "X" + " " + "Y"
            List<String> leftAndRight = Arrays.asList(line1.split(" "));
            // Initialize this.board and this.bombs
            // After this we will never deal with sizing the board again
            this.length = Integer.parseInt(leftAndRight.get(0));
            this.width = Integer.parseInt(leftAndRight.get(1));
            this.board = new String[this.length][this.width];
            this.bombs = new boolean[this.length][this.width];

            // Dealing with the rest of the lines for bomb placement:
            // Each line read corresponds to one y-coordinate
            int y = 0;
            while(input.ready()){
                String line = input.readLine();
                // Make sure no space at start or end and has (this.width-1) spaces total
                verifySpaces(line, this.length - 1);
                List<String> content = Arrays.asList(line.split(" "));
                if (content.size() == this.length) {
                    // Each content is at the same y-coordinate but a new x-coordinate
                    int x = 0;
                    for (String cellContent : content){
                        // Initialize cell on board to Untouched state:
                        this.board[x][y] = "-";
                        // Initialize cell bomb content:
                        if (cellContent.equals("1")){
                            this.bombs[x][y] = true;
                        } 
                        else if (cellContent.equals("0")){
                            this.bombs[x][y] = false;
                        } 
                        // If entry not "0" or "1":
                        else{
                            throw new RuntimeException();
                        }
                        x += 1;
                    }
                } 
                else {
                    // If incorrect number of entries on one line (not corresponding to this.length)
                    throw new RuntimeException();
                }
                y += 1;
            }
            // If incorrect number of rows entered (not corresponding to this.width)
            if (y != this.width){ 
                throw new RuntimeException(); 
            }
            checkRep();
        } 
        catch (IOException io){
            // IO due to File Errors
            throw new RuntimeException();
        }
    }

    /**
     * Makes sure the Representation Invariant is respected
     */
    public synchronized void checkRep(){
        assert(this.length > 0 && this.width > 0);
        assert(board.length == this.length);
        assert(bombs.length == this.length);
        int i = 0;
        while (i < this.length){
            assert(board[i].length == this.width);
            assert(bombs[i].length == this.width);
            i += 1;
        }
    }

    /**
     * Digs a square entry from the board 
     * Modifies cell accordingly (and also surrounding cells of needed)
     * 
     * @param x The x-coordinate of the square to dig
     * @param y The y-coordinate of the square to dig
     * @return boolean True if dug a cell but no bomb exploded
     *          or False if bomb exploded
     */
    public synchronized boolean dig(int x, int y){ 
        boolean alive = true;
        if (!inBoard(x, y)
                || !isUntouched(x, y)){
            checkRep();
            return alive;
        }
        else{
            if (isBombed(x, y)){
                alive = false;
                unbombIt(x, y);
                bombExplodedUpdate(x, y);
            }
            recursiveUpdate(x, y);
            checkRep();
            return alive;
        }
    }

    /**
     * Flags a square entry from the board 
     * 
     * @param x The x-coordinate of the square to flag
     * @param y The y-coordinate of the square to flag
     * @return boolean True if something was mutated
     */
    public synchronized boolean flag(int x, int y){
        if (inBoard(x, y)
                && isUntouched(x, y)){
            board[x][y] = "F";
            checkRep();
            return true;
        }
        checkRep();
        return false;
    }

    /**
     * Deflags a square entry from the board 
     * 
     * @param x The x-coordinate of the square to deflag
     * @param y The y-coordinate of the square to deflag
     * @return boolean True if something was mutated
     */
    public synchronized boolean deflag(int x, int y){
        if (inBoard(x, y)
                && isFlagged(x, y)){
            board[x][y] = "-";
            return true;
        }
        checkRep();
        return false;
    }

    /**
     * Gives a String representation of the Board object
     * @return String the state of the Board object
     */
    @Override
    public synchronized String toString(){
        String returned = "";
        int counterWidth = 0;
        while (counterWidth < this.width){
            int counterLength = 0;
            while (counterLength < this.length){
                String entry = board[counterLength][counterWidth];
                if (!entry.equals("0")){
                    returned += entry + " ";
                }
                else{
                    returned += "  ";
                }
                counterLength += 1;
            }
            returned = returned.substring(0, returned.length() - 1);
            returned += "\r\n";
            counterWidth += 1;
        }
        checkRep();
        return returned;
    }

    // Board Objects are mutable so they use referential equality and hashCode
    //  (as stated in reading 15) so no need to override equals() or hashCode()

    // ---------------------------PRIVATE HELPER METHODS-------------------------------

    /**
     * Returns true if cell is untouched, meaning it is in its original state
     * 
     * @param x The x-coordinate of the cell
     * @param y The y-coordinate of the cell
     * @return boolean true if untouched else false
     */
    private synchronized boolean isUntouched(int x, int y){
        checkRep();
        return board[x][y].equals("-");
    }

    /**
     * Returns true if cell has been dug, meaning it is not untouched or flagged
     * 
     * @param x The x-coordinate of the cell
     * @param y The y-coordinate of the cell
     * @return boolean true if dug else false
     */
    private synchronized boolean isDug(int x, int y){
        checkRep();
        return (!board[x][y].equals("-")
                && !board[x][y].equals("F"));
    }

    /**
     * Returns true if cell is flagged
     * 
     * @param x The x-coordinate of the cell
     * @param y The y-coordinate of the cell
     * @return boolean true if flagged else false
     */
    private synchronized boolean isFlagged(int x, int y){
        checkRep();
        return board[x][y].equals("F");
    }

    /**
     * Returns true if cell contains a bomb
     * 
     * @param x The x-coordinate of the cell
     * @param y The y-coordinate of the cell
     * @return boolean true if it contains a bomb
     */
    private synchronized boolean isBombed(int x, int y){
        checkRep();
        return bombs[x][y] == true;
    }

    /**
     * Returns the number of bombs in the surrounding 8 cells
     * 
     * @param x The x-coordinate of this cell
     * @param y The y-coordinate of this cell
     * @return int The number of bombs
     */
    private synchronized int numAround(int x, int y){
        // Capture all surroundings' x-coords and y-coords
        List<Integer> xVal = surroundingXY(x, y).get(0);
        List<Integer> yVal = surroundingXY(x, y).get(1);

        // Now count bombs in the legal cells 
        int total = bombs[x][y] ? -1 : 0; // Because current cell is counted too
        for (int X : xVal){
            for (int Y : yVal){
                if (bombs[X][Y]){
                    total += 1;
                }
            }
        }
        checkRep();
        return total;
    }

    /**
     * Recursively reveals numbers on other cells when this cell
     *  is dug but has no bomb in surrouding 8 cells
     * 
     * @param x The x-coordinate of this cell
     * @param y The y-coordinate of this cell
     */
    private synchronized void recursiveUpdate(int x, int y){
        // base case
        if (numAround(x, y) != 0){ 
            board[x][y] = numAround(x, y) + "";
            checkRep();
            return;
        }
        else{ 
            board[x][y] = "0";
            // if no bomb around, expand one layer next
            // Capture all surroundings' x-coords and y-coords
            List<Integer> xVal = surroundingXY(x, y).get(0);
            List<Integer> yVal = surroundingXY(x, y).get(1);

            // update next layer's numbers
            for (int X : xVal){
                for (int Y : yVal){
                    if (!isDug(X, Y)){
                        recursiveUpdate(X, Y);
                    }
                }
            }
        }
        checkRep();
    }

    /**
     * When a cell was bombed, is dug and its bomb is destroyed, 
     *  modifies the numbers on dug cells immediately surrounding it
     * 
     * @param x The x-coordinate of this cell
     * @param y The y-coordinate of this cell
     * @return boolean True if something was changed
     */
    private synchronized boolean bombExplodedUpdate(int x, int y){
        // Capture all surroundings' x-coords and y-coords
        List<Integer> xVal = surroundingXY(x, y).get(0);
        List<Integer> yVal = surroundingXY(x, y).get(1);

        // update next layer's numbers
        for (int X : xVal){
            for (int Y : yVal){
                if (isDug(X, Y)){
                    board[X][Y] = ((Integer.parseInt(board[X][Y])) - 1) + "";
                }
            }
        }
        checkRep();
        return true;
    }

    /**
     * Checks if this cell is in the board 
     *      i.e. x, y >= 0
     *      i.e. x <= this.length - 1, y <= this.width - 1
     * 
     * @param x The x-coordinate of this cell
     * @param y The y-coordinate of this cell
     * @return boolean True if in board, False if outside board
     */
    private boolean inBoard(int x, int y){
        if (x >= 0 && y >= 0
                && x <= this.length - 1 
                && y <= this.width - 1){
            checkRep();
            return true;
        }
        else {
            checkRep();
            return false;
        }
    }

    /**
     * Returns a List of 2 Lists of Integers
     *  List[0] is the list of x values surrounding this cell
     *  List[1] is the list of y values surrounding this cell
     *  
     * @param x The x-coordinate of this cell
     * @param y The y-coordinate of this cell
     * @return List<List<Integer>> The list of lists of x and y values
     */
    private List<List<Integer>> surroundingXY(int x, int y){
        // Not synchronized part of rep. read here is immutable
        List<Integer> xVal = new ArrayList<Integer>();
        List<Integer> yVal = new ArrayList<Integer>();
        int counter = -1;
        while (counter <= 1){
            if ((x + counter) >= 0 
                    && (x + counter) < this.length){
                xVal.add(x + counter);
            }
            if ((y + counter) >= 0 
                    && (y + counter) < this.width){
                yVal.add(y + counter);
            }
            counter += 1;
        }
        List<List<Integer>> listReturned = new ArrayList<List<Integer>>();
        listReturned.add(0, xVal);
        listReturned.add(1, yVal);
        checkRep();
        return listReturned;
    }

    /**
     * Static method that counts how many spaces appear in a String 
     *  Used when initializing a Board from a File object
     *  
     * @param string The String whose number of spaces is to be found
     * @return int The number of spaces
     */
    private static int numSpaces(String string) {
        int numSpaces = 0;
        for (char character : string.toCharArray()) {
            if (character == ' '){ 
                numSpaces += 1; 
            }
        }
        return numSpaces;
    }

    /**
     * Static method that deals with spaces in each line we read from 
     *  File object when creating a board from a file
     * Makes sure that there is no space at the beginning or end of a line
     *  Afterwards, makes sure that the total number of spaces remaining
     *   equals the input number
     *   
     * @param string The text from the line read; The text to be checked
     * @throws RuntimeException if the number of spaces is not acceptable
     */
    private static void verifySpaces(String string, int numSpaces){
        // Cannot have a space at beginning or end of the line text
        if (string.substring(0,1).equals(" ") 
                || string.substring(string.length()-1).equals(" ")){
            throw new RuntimeException(); 
        }
        // Also MUST HAVE numSpaces spaces in all in this line text
        if (numSpaces(string) != numSpaces){ 
            throw new RuntimeException(); 
        }
    }

    // --------------------OTHER HELPER METHODS USEFUL FOR TESTING-------------------------------

    // These are also helper methods but they are made public because they 
    //  are useful to initialize a desired board with known bomb locations
    //  to enable predictable testing of main public methods above 

    /**
     * Turns a cell to a bombed cell and updates counts in all surrounding cells
     * 
     * @param x The x-coordinate of this cell
     * @param y The y-coordinate of this cell
     * @return boolean True if cell was unbombed before and something changed, else false
     */
    public synchronized boolean bombIt(int x, int y){
        if (bombs[x][y] == false){
            bombs[x][y] = true;
            checkRep();
            return true;
        }
        checkRep();
        return false;
    }

    /**
     * Turns a cell to an unbombed cell and updates counts in all surrounding cells
     * 
     * @param x The x-coordinate of this cell
     * @param y The y-coordinate of this cell
     * @return boolean True if cell was bombed before and something changed, else false
     */
    public synchronized boolean unbombIt(int x, int y){
        if (bombs[x][y] == true){
            bombs[x][y] = false;
            checkRep();
            return true;
        }
        checkRep();
        return false;
    }

    // These are useful in above implementations but also to see test results

    /**
     * Returns the state of a cell (untouched "-", flagged "F", dug "[0-8]")
     * 
     * @param x The x-coordinate of this cell
     * @param y The y-coordinate of this cell
     * @return String The state of the cell
     */
    public synchronized String state(int x, int y){
        checkRep();
        return board[x][y];
    }

    /**
     * Returns the size of this board in terms of total number of cells
     *
     * @return int The size of the board i.e. total number of cells
     */
    public int size(){
        // Not synchronized part of rep. read here is immutable
        checkRep();
        return (this.length * this.width);
    }

    /**
     * Returns the size of this board as a List where List[0] is the length
     *  and List[1] is the width
     *
     * @return ArrayList The list of length and width
     */
    public List<Integer> sizeList(){
        // Not synchronized part of rep. read here is immutable
        checkRep();
        return Arrays.asList(this.length, this.width);
    }
}