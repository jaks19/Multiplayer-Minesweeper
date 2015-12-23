package minesweeper;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * This Class provides a test suite for Board.java
 */
public class BoardTest {

    /*
     * OVERALL TESTING STRATEGY:
     * Board.java contains the following public methods:
     *  - Constructor
     *  - flag()
     *  - deflag()
     *  - dig()
     *  
     * They will be tested individually below and since they make use
     *      of private methods from Board.java, these private methods are
     *      also indirectly tested for correctness
     *  
     * Tests will be done on the smallest board possible for the test and the 
     *      results of the tests are checked through toString()
     */

    // TESTING CONSTRUCTOR
    /*
     * Testing Strategy:
     *  - Create board of minimum size : 1 x 1
     *  - Create bigger square board : 4 x 4
     *  - Create bigger rectangular board : 4 x 2
     */

    @Test
    // Create board of minimum size : 1 x 1
    public void testConstructor1x1() {
        Board testBoard = new Board(1, 1);
        assertEquals(testBoard.size(), 1);
    }

    @Test
    // Create bigger square board : 4 x 4
    public void testConstructor4x4() {
        Board testBoard = new Board(4, 4);
        assertEquals(testBoard.size(), 16);
    }

    @Test
    // Create bigger rectangular board : 4 x 2
    public void testConstructor4x2() {
        Board testBoard = new Board(4, 2);
        assertEquals(testBoard.size(), 8);
    }

    // TESTING FLAG()
    /*
     * Testing Strategy:
     *  - Cell is untouched
     *  - Cell is already flagged
     *  - Cell is dug 
     *  - Cell is out of board (x,y +ve)
     *  - Cell coordinates are negative
     */

    @Test
    // Cell is untouched
    public void testFlagUntouched() {
        //Board Initialization 
        Board testBoard = new Board(1, 1);
        //Tests
        assertTrue(testBoard.state(0, 0).equals("-"));
        testBoard.flag(0, 0);
        assertTrue(testBoard.state(0, 0).equals("F"));
    }

    @Test
    // Cell is already flagged
    public void testFlagFlagged() {
        //Board Initialization 
        Board testBoard = new Board(1, 1);
        testBoard.flag(0, 0);
        //Tests
        assertTrue(testBoard.state(0, 0).equals("F"));
        testBoard.flag(0, 0);
        assertTrue(testBoard.state(0, 0).equals("F"));
    }

    @Test
    // Cell is dug
    public void testFlagDug() {
        //Board Initialization 
        Board testBoard = new Board(1, 1);
        testBoard.dig(0, 0);
        //Tests
        String previousState = testBoard.state(0, 0);
        testBoard.flag(0, 0);
        assertTrue(testBoard.state(0, 0).equals(previousState));
    }

    @Test
    // Cell is out of board (x,y +ve)
    public void testFlagOutOfBoard() {
        //Board Initialization 
        Board testBoard = new Board(1, 1);
        //Tests
        String previousState = testBoard.state(0, 0);
        testBoard.flag(5, 5);
        assertTrue(testBoard.state(0, 0).equals(previousState));
    }

    @Test
    // Cell coordinates are negative
    public void testFlagCoordsNegative() {
        //Board Initialization 
        Board testBoard = new Board(1, 1);
        //Tests
        String previousState = testBoard.state(0, 0);
        testBoard.flag(-1, -1);
        assertTrue(testBoard.state(0, 0).equals(previousState));
    }

    // TESTING DEFLAG()
    /*
     * Testing Strategy:
     *  - Cell is flagged
     *  - Cell is untouched
     *  - Cell is dug 
     *  - Cell is out of board (x,y +ve)
     *  - Cell coordinates negative
     */

    @Test
    // Cell is flagged
    public void testDeflagFlagged() {
        //Board Initialization 
        Board testBoard = new Board(1, 1);
        testBoard.flag(0, 0);
        //Tests
        assertTrue(testBoard.state(0, 0).equals("F"));
        testBoard.deflag(0, 0);
        assertTrue(testBoard.state(0, 0).equals("-"));
    }

    @Test
    // Cell is untouched
    public void testDeflagUntouched() {
        //Board Initialization 
        Board testBoard = new Board(1, 1);
        //Tests
        assertTrue(testBoard.state(0, 0).equals("-"));
        testBoard.deflag(0, 0);
        assertTrue(testBoard.state(0, 0).equals("-"));
    }

    @Test
    // Cell is dug
    public void testDeflagDug() {
        //Board Initialization 
        Board testBoard = new Board(1, 1);
        testBoard.dig(0, 0);
        //Tests
        String previousState = testBoard.state(0, 0);
        testBoard.deflag(0, 0);
        assertTrue(testBoard.state(0, 0).equals(previousState));
    }

    @Test
    // Cell is out of board (x,y +ve)
    public void testDeflagOutOfBoard() {
        //Board Initialization 
        Board testBoard = new Board(1, 1);
        //Tests
        String previousState = testBoard.state(0, 0);
        testBoard.deflag(5, 5);
        assertTrue(testBoard.state(0, 0).equals(previousState));
    }

    @Test
    // Cell coordinates are negative
    public void testDeflagCoordsNegative() {
        //Board Initialization 
        Board testBoard = new Board(1, 1);
        //Tests
        String previousState = testBoard.state(0, 0);
        testBoard.deflag(-1, -1);
        assertTrue(testBoard.state(0, 0).equals(previousState));
    }

    // TESTING DIG()
    /*
     * Testing Strategy:
     * A. On 1x1 grid (to check basic functionality on one cell)
     *  - Cell is untouched
     *      > Cell unbombed
     *      > Cell bombed
     *  - Cell is flagged
     *  - Cell is dug
     *  - Cell is out of board (x,y +ve)
     *  - Cell coordinates negative
     *  
     * B. On bigger grid (e.g. 4 x 4) with cell to dig being untouched
     *  - Bombs present in surrounding undug cells (Dig center and corner)
     *  - Bombs not present in immediate surrounding undug cells (1 recursion)
     *  - Bombs not present in immediate surrounding undug cells and some undug 
     *      cells have in turn no bombs in immediate surrounding undug cells (>1 recursion)
     *  - Cell dug is bombed, some surrounding cells dug and holding numbers (so numbers need to change)
     */

    // 1 x 1 GRID

    @Test
    // Cell untouched, unbombed
    public void testDigUntouchedUnbombed() {
        //Board Initialization 
        Board testBoard = new Board(1, 1);
        testBoard.unbombIt(0, 0);
        //Tests
        testBoard.dig(0, 0);
        assertTrue(testBoard.state(0, 0).equals("0"));
    }

    @Test
    // Cell untouched, bombed
    public void testDigUntouchedBombed() {
        //Board Initialization 
        Board testBoard = new Board(1, 1);
        testBoard.bombIt(0, 0);
        //Tests
        testBoard.dig(0, 0);
        assertTrue(testBoard.state(0, 0).equals("0"));
    }

    @Test
    // Cell is flagged
    public void testDigFlagged() {
        //Board Initialization 
        Board testBoard = new Board(1, 1);
        testBoard.flag(0, 0);
        //Tests
        String previousState = testBoard.state(0, 0);
        testBoard.dig(0, 0);
        assertTrue(testBoard.state(0, 0).equals(previousState));
    }

    @Test
    // Cell is dug
    public void testDigDug() {
        //Board Initialization 
        Board testBoard = new Board(1, 1);
        testBoard.dig(0, 0);
        //Tests
        String previousState = testBoard.state(0, 0);
        testBoard.dig(0, 0);
        assertTrue(testBoard.state(0, 0).equals(previousState));
    }

    @Test
    // Cell is out of board (x,y +ve)
    public void testDigOutOfBoard() {
        //Board Initialization 
        Board testBoard = new Board(1, 1);
        //Tests
        String previousState = testBoard.state(0, 0);
        testBoard.dig(5, 5);
        assertTrue(testBoard.state(0, 0).equals(previousState));
    }

    @Test
    // Cell coordinates negative
    public void testDigCoordsNegative() {
        //Board Initialization 
        Board testBoard = new Board(1, 1);
        //Tests
        String previousState = testBoard.state(0, 0);
        testBoard.dig(-1, -1);
        assertTrue(testBoard.state(0, 0).equals(previousState));
    }

    // BIGGER GRID

    @Test
    // Bombs present in surrounding undug cells (dig center)
    public void testDigBigBombsInSurroundingCenter() {
        //Board Initialization 
        /*
         * B . .
         * . . .
         * . . B
         */
        Board testBoard = new Board(3, 3);
        testBoard.bombIt(0, 0);
        testBoard.unbombIt(1, 0);
        testBoard.unbombIt(2, 0);
        testBoard.unbombIt(0, 1);
        testBoard.unbombIt(1, 1);
        testBoard.unbombIt(2, 1);
        testBoard.unbombIt(0, 2);
        testBoard.unbombIt(1, 2);
        testBoard.bombIt(2, 2);
        //Tests
        assertTrue(testBoard.state(1, 1).equals("-"));
        testBoard.dig(1, 1);
        assertTrue(testBoard.state(1, 1).equals("2"));
    }

    @Test
    // Bombs present in surrounding undug cells (dig corner)
    public void testDigBigBombsInSurroundingCorner() {
        //Board Initialization 
        /*
         * B . .
         * . B .
         * . . B
         */
        Board testBoard = new Board(3, 3);
        testBoard.bombIt(0, 0);
        testBoard.unbombIt(1, 0);
        testBoard.unbombIt(2, 0);
        testBoard.unbombIt(0, 1);
        testBoard.bombIt(1, 1);
        testBoard.unbombIt(2, 1);
        testBoard.unbombIt(0, 2);
        testBoard.unbombIt(1, 2);
        testBoard.bombIt(2, 2);

        //Tests
        assertTrue(testBoard.state(2, 2).equals("-"));
        testBoard.dig(2, 2);
        assertTrue(testBoard.state(2, 2).equals("1"));
    }

    @Test
    // Bombs not present in immediate surrounding undug cells (1 recursion)
    public void testDigBigBombsNotInSurrounding() {
        //Board Initialization 
        /*
         * B - - 2   dig (3,3)     B - - 2
         * - - B B  ----------->   - - B B     Both B's and -'s are untouched except B's are bombed untouched
         * B - - -                 B - 3 2
         * 2 B - -                 2 B 1 0 
         */
        Board testBoard = new Board(4, 4);
        testBoard.bombIt(0, 0);
        testBoard.unbombIt(1, 0);
        testBoard.unbombIt(2, 0);
        testBoard.unbombIt(3, 0);
        testBoard.unbombIt(0, 1);
        testBoard.unbombIt(1, 1);
        testBoard.bombIt(2, 1);
        testBoard.bombIt(3, 1);
        testBoard.bombIt(0, 2);
        testBoard.unbombIt(1, 2);
        testBoard.unbombIt(2, 2);
        testBoard.unbombIt(3, 2);
        testBoard.unbombIt(0, 3);
        testBoard.bombIt(1, 3);
        testBoard.unbombIt(2, 3);
        testBoard.unbombIt(3, 3);

        testBoard.dig(3, 0);
        testBoard.dig(0, 3);

        //Tests
        assertTrue(testBoard.state(0, 0).equals("-"));
        assertTrue(testBoard.state(1, 0).equals("-"));
        assertTrue(testBoard.state(2, 0).equals("-"));
        assertTrue(testBoard.state(3, 0).equals("2"));
        assertTrue(testBoard.state(0, 1).equals("-"));
        assertTrue(testBoard.state(1, 1).equals("-"));
        assertTrue(testBoard.state(2, 1).equals("-"));
        assertTrue(testBoard.state(3, 1).equals("-"));
        assertTrue(testBoard.state(0, 2).equals("-"));
        assertTrue(testBoard.state(1, 2).equals("-"));
        assertTrue(testBoard.state(2, 2).equals("-"));
        assertTrue(testBoard.state(3, 2).equals("-"));
        assertTrue(testBoard.state(0, 3).equals("2"));
        assertTrue(testBoard.state(1, 3).equals("-"));
        assertTrue(testBoard.state(2, 3).equals("-"));
        assertTrue(testBoard.state(3, 3).equals("-"));
        testBoard.dig(3, 3);
        assertTrue(testBoard.state(0, 0).equals("-"));
        assertTrue(testBoard.state(1, 0).equals("-"));
        assertTrue(testBoard.state(2, 0).equals("-"));
        assertTrue(testBoard.state(3, 0).equals("2"));
        assertTrue(testBoard.state(0, 1).equals("-"));
        assertTrue(testBoard.state(1, 1).equals("-"));
        assertTrue(testBoard.state(2, 1).equals("-"));
        assertTrue(testBoard.state(3, 1).equals("-"));
        assertTrue(testBoard.state(0, 2).equals("-"));
        assertTrue(testBoard.state(1, 2).equals("-"));
        assertTrue(testBoard.state(2, 2).equals("3"));
        assertTrue(testBoard.state(3, 2).equals("2"));
        assertTrue(testBoard.state(0, 3).equals("2"));
        assertTrue(testBoard.state(1, 3).equals("-"));
        assertTrue(testBoard.state(2, 3).equals("1"));
        assertTrue(testBoard.state(3, 3).equals("0"));
    }

    @Test
    // Bombs not present in immediate surrounding undug cells and some undug 
    //  cells have in turn no bombs in immediate surrounding undug cells (>1 recursion)
    public void testDigBigBombsNotInSurroundingPower2() {
        //Board Initialization 
        /*
         * B - - -   dig (3,3)     B 1 0 0
         * - - - -  ----------->   - 2 0 0     Both B's and -'s are untouched except B's are bombed untouched
         * B - - -                 B 1 0 0
         * - - - -                 - 1 0 0 
         */
        Board testBoard = new Board(4, 4);
        testBoard.bombIt(0, 0);
        testBoard.unbombIt(1, 0);
        testBoard.unbombIt(2, 0);
        testBoard.unbombIt(3, 0);
        testBoard.unbombIt(0, 1);
        testBoard.unbombIt(1, 1);
        testBoard.unbombIt(2, 1);
        testBoard.unbombIt(3, 1);
        testBoard.bombIt(0, 2);
        testBoard.unbombIt(1, 2);
        testBoard.unbombIt(2, 2);
        testBoard.unbombIt(3, 2);
        testBoard.unbombIt(0, 3);
        testBoard.unbombIt(1, 3);
        testBoard.unbombIt(2, 3);
        testBoard.unbombIt(3, 3);

        //Tests
        assertTrue(testBoard.state(0, 0).equals("-"));
        assertTrue(testBoard.state(1, 0).equals("-"));
        assertTrue(testBoard.state(2, 0).equals("-"));
        assertTrue(testBoard.state(3, 0).equals("-"));
        assertTrue(testBoard.state(0, 1).equals("-"));
        assertTrue(testBoard.state(1, 1).equals("-"));
        assertTrue(testBoard.state(2, 1).equals("-"));
        assertTrue(testBoard.state(3, 1).equals("-"));
        assertTrue(testBoard.state(0, 2).equals("-"));
        assertTrue(testBoard.state(1, 2).equals("-"));
        assertTrue(testBoard.state(2, 2).equals("-"));
        assertTrue(testBoard.state(3, 2).equals("-"));
        assertTrue(testBoard.state(0, 3).equals("-"));
        assertTrue(testBoard.state(1, 3).equals("-"));
        assertTrue(testBoard.state(2, 3).equals("-"));
        assertTrue(testBoard.state(3, 3).equals("-"));
        testBoard.dig(3, 3);
        assertTrue(testBoard.state(0, 0).equals("-"));
        assertTrue(testBoard.state(1, 0).equals("1"));
        assertTrue(testBoard.state(2, 0).equals("0"));
        assertTrue(testBoard.state(3, 0).equals("0"));
        assertTrue(testBoard.state(0, 1).equals("-"));
        assertTrue(testBoard.state(1, 1).equals("2"));
        assertTrue(testBoard.state(2, 1).equals("0"));
        assertTrue(testBoard.state(3, 1).equals("0"));
        assertTrue(testBoard.state(0, 2).equals("-"));
        assertTrue(testBoard.state(1, 2).equals("1"));
        assertTrue(testBoard.state(2, 2).equals("0"));
        assertTrue(testBoard.state(3, 2).equals("0"));
        assertTrue(testBoard.state(0, 3).equals("-"));
        assertTrue(testBoard.state(1, 3).equals("1"));
        assertTrue(testBoard.state(2, 3).equals("0"));
        assertTrue(testBoard.state(3, 3).equals("0"));
    }

    @Test
    //Cell dug is bombed, some surrounding cells dug and holding numbers (so numbers need to change)
    public void testDigCellBombedSurroundingDugCellsNumbersChange() {
        //Board Initialization 
        /*
         * B 2 1   dig (1,1)     B 1 0
         * 2 B 2  ----------->   1 2 1
         * 1 2 B                 0 1 B
         */
        Board testBoard = new Board(3, 3);
        testBoard.bombIt(0, 0);
        testBoard.unbombIt(1, 0);
        testBoard.unbombIt(2, 0);
        testBoard.unbombIt(0, 1);
        testBoard.bombIt(1, 1);
        testBoard.unbombIt(2, 1);
        testBoard.unbombIt(0, 2);
        testBoard.unbombIt(1, 2);
        testBoard.bombIt(2, 2);

        testBoard.dig(1, 0);
        testBoard.dig(2, 0);
        testBoard.dig(0, 1);
        testBoard.dig(2, 1);
        testBoard.dig(0, 2);
        testBoard.dig(1, 2);

        //Tests
        assertTrue(testBoard.state(0, 0).equals("-"));
        assertTrue(testBoard.state(1, 0).equals("2"));
        assertTrue(testBoard.state(2, 0).equals("1"));
        assertTrue(testBoard.state(0, 1).equals("2"));
        assertTrue(testBoard.state(1, 1).equals("-"));
        assertTrue(testBoard.state(2, 1).equals("2"));
        assertTrue(testBoard.state(0, 2).equals("1"));
        assertTrue(testBoard.state(1, 2).equals("2"));
        assertTrue(testBoard.state(2, 2).equals("-"));
        testBoard.dig(1, 1);
        assertTrue(testBoard.state(0, 0).equals("-"));
        assertTrue(testBoard.state(1, 0).equals("1"));
        assertTrue(testBoard.state(2, 0).equals("0"));
        assertTrue(testBoard.state(0, 1).equals("1"));
        assertTrue(testBoard.state(1, 1).equals("2"));
        assertTrue(testBoard.state(2, 1).equals("1"));
        assertTrue(testBoard.state(0, 2).equals("0"));
        assertTrue(testBoard.state(1, 2).equals("1"));
        assertTrue(testBoard.state(2, 2).equals("-"));
    }
}
