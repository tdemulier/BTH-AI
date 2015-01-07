package wumpusworld;

import java.util.ArrayList;

/**
 * Contans starting code for creating your own Wumpus World agent. Currently the
 * agent only make a random decision each turn.
 *
 * @author Johan Hagelbäck
 */
public class MyAgent implements Agent {

    private class Square {

        private boolean breeze;
        private boolean stench;
        private int pit; // value {-1,0,1,2} for {no idea, no pit, may be, sure}
        private int wumpus; // same
        private boolean explored;
        private boolean safe;
        private final int x, y;

        private Square(int posX, int posY) {
            breeze = false;
            stench = false;
            pit = -1;
            wumpus = -1;
            explored = false;
            safe = false;
            x = posX;
            y = posY;
        }

        private void print() {
            System.out.print(" | b : " + breeze + ", s : " + stench + ", p : " + pit + ", w : " + wumpus + ", e : " + explored + ", s : " + safe + " | ");
        }
    }

    private World w;
    private final int size;
    private static final int WUMPUS_VALUE_SURE = -2000;
    private static final int WUMPUS_VALUE_NOTSURE = -1500;
    private static final int PIT_VALUE_SURE = -1000;
    private static final int PIT_VALUE_NOTSURE = -500;
    private static final int ACTION_VALUE = -1;
    private static final int ALREADY_VISITED_SQUARE = -10000;
    private static final int HUNT_WUMPUS = 500;

    private Square target;
    private Square[][] map;
    private boolean found_wumpus = false;
    private Square wumpusSquare;
    private boolean killed_wumpus = false;
    private int posX, posY;

    /**
     * Creates a new instance of your solver agent.
     *
     * @param world Current world state
     */
    public MyAgent(World world) {
        w = world;

        // init the internal map representation
        size = world.getSize();
        map = new Square[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                map[i][j] = new Square(i, j);
            }
        }
    }

    private static boolean[][] cloneVisited(boolean[][] array) {
        int rows = array.length;

        //clone the 'shallow' structure of array
        boolean[][] newArray = (boolean[][]) array.clone();
        //clone the 'deep' structure of array
        for (int row = 0; row < rows; row++) {
            newArray[row] = (boolean[]) array[row].clone();
        }

        return newArray;
    }

    private void sense() {
        //Test the environment
        if (w.hasBreeze(posX + 1, posY + 1)) {
            System.out.println("I am in a Breeze");
            map[posX][posY].breeze = true;
        }
        if (w.hasStench(posX + 1, posY + 1)) {
            System.out.println("I am in a Stench");
            map[posX][posY].stench = true;
        }
        if (w.hasPit(posX + 1, posY + 1)) {
            System.out.println("I am in a Pit");
            map[posX][posY].pit = 2;
        }
        if (!w.hasPit(posX + 1, posY + 1)) {
            map[posX][posY].pit = 0;
        }
        if (w.hasWumpus(posX + 1, posY + 1)) {
            System.out.println("I am in a Wumpus");
            map[posX][posY].wumpus = 2;
        }
        if (!w.hasWumpus(posX + 1, posY + 1)) {
            map[posX][posY].wumpus = 0;
        }
        if (!w.wumpusAlive()) {
            killed_wumpus = true;
        }
    }

    private ArrayList<Square> neighboors(int x, int y) {
        ArrayList<Square> neighboors = new ArrayList<>();
        if (x > 0 && x < size) {
            neighboors.add(map[x - 1][y]);
        }
        if (x >= 0 && x < size - 1) {
            neighboors.add(map[x + 1][y]);
        }
        if (y > 0 && y < size) {
            neighboors.add(map[x][y - 1]);
        }
        if (y >= 0 && y < size - 1) {
            neighboors.add(map[x][y + 1]);
        }

        return neighboors;
    }

    private void updateMap() {
        if (!map[posX][posY].explored) {
            map[posX][posY].explored = true;
        }

        ArrayList<Square> neighboors = neighboors(posX, posY);

        if (map[posX][posY].breeze) {
            for (Square s : neighboors) {
                if (!s.explored && s.pit != 2 && s.pit != 0) {
                    s.safe = false;
                    s.pit = 1;
                }
            }
        }

        if (map[posX][posY].stench && !found_wumpus) {
            for (Square s : neighboors) {
                if (!s.explored && s.wumpus != 0) {
                    s.safe = false;
                    s.wumpus = 1;
                }
            }
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (map[i][j].breeze) {
                    ArrayList<Square> neighboors2 = neighboors(i, j);
                    for (Square s : neighboors2) {
                        ArrayList<Square> neighboors3 = neighboors(s.x, s.y);
                        for (Square s2 : neighboors3) {
                            if (!s2.breeze && s2.explored) {
                                s.pit = 0;
                            }
                        }
                    }
                }
            }
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (map[i][j].breeze) {
                    ArrayList<Square> neighboors2 = neighboors(i, j);
                    int nopits = 0;
                    for (Square s : neighboors2) {
                        if (s.pit == 0) {
                            nopits++;
                        }
                    }
                    if (nopits == neighboors2.size() - 1) {
                        for (Square s : neighboors2) {
                            if (s.pit != 0) {
                                s.pit = 2;
                            }
                        }
                    }
                }
            }
        }

        boolean reset_wumpus_square = true;
        if (!found_wumpus) {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (map[i][j].stench) {
                        ArrayList<Square> neighboors2 = neighboors(i, j);
                        for (Square s : neighboors2) {
                            ArrayList<Square> neighboors3 = neighboors(s.x, s.y);
                            for (Square s2 : neighboors3) {
                                if (!s2.stench && s2.explored) {
                                    s.wumpus = 0;
                                }
                            }
                        }
                    }
                }
            }
            
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (map[i][j].stench) {
                        ArrayList<Square> neighboors2 = neighboors(i, j);
                        int nowumpus = 0;
                        for (Square s : neighboors2) {
                            if (s.wumpus == 0) {
                                nowumpus++;
                            }
                        }
                        if (nowumpus == neighboors2.size() - 1) {
                            for (Square s : neighboors2) {
                                if (s.wumpus != 0) {
                                    s.wumpus = 2;
                                    found_wumpus = true;
                                    wumpusSquare = s;
                                }
                            }
                        }
                    }
                }
            }

            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    ArrayList<Square> neighboors2 = neighboors(i, j);
                    int nbstench = 0;
                    for (Square s : neighboors2) {
                        if (s.stench) {
                            nbstench++;
                        }
                    }
                    if (nbstench > 1) {
                        map[i][j].wumpus = 2;
                        found_wumpus = true;
                        wumpusSquare = map[i][j];
                    }
                }
            }
        } else if (found_wumpus && reset_wumpus_square) {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (map[i][j].wumpus != 2) {
                        map[i][j].wumpus = 0;
                    }
                }

            }
            reset_wumpus_square = false;
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (map[i][j].pit == 0 && map[i][j].wumpus == 0) {
                    map[i][j].safe = true;
                }
            }
        }
    }

    private int squareScore(Square s) {
        int score = 0;
        if (s.wumpus == 1) {
            score += WUMPUS_VALUE_NOTSURE;
        }
        if (s.wumpus == 2) {
            score += WUMPUS_VALUE_SURE;
        }
        if (s.pit == 1) {
            score += PIT_VALUE_NOTSURE;
        }
        if (s.pit == 2) {
            score += PIT_VALUE_SURE;
        }
        return score;
    }

    private int movingScore(Square s0, Square s1, Square s2) {
        int nbAction = 0;
        if (s0 == s1) {
            if (((w.getDirection() == World.DIR_RIGHT || w.getDirection() == World.DIR_LEFT) && s1.x == s2.x)
                    || ((w.getDirection() == World.DIR_UP || w.getDirection() == World.DIR_DOWN) && s1.y == s2.y)) {
                nbAction += 2;
            } else if ((w.getDirection() == World.DIR_RIGHT && s1.y == s2.y && s1.x < s2.x)
                    || (w.getDirection() == World.DIR_LEFT && s1.y == s2.y && s1.x > s2.x)
                    || (w.getDirection() == World.DIR_UP && s1.x == s2.x && s1.y < s2.y)
                    || (w.getDirection() == World.DIR_DOWN && s1.x == s2.x && s1.y > s2.y)) {
                nbAction += 1;
            } else {
                nbAction += 3;
            }
        } else {
            if (s1.pit == 2 || s1.pit == 1) {
                nbAction += 1;
            }
            if (s0 == s2) {
                nbAction += 3;
            } else if ((s0.x == s1.x && s1.x == s2.x) || (s0.y == s1.y && s1.y == s2.y)) {
                nbAction += 1;
            } else {
                nbAction += 2;
            }
        }
        return nbAction * ACTION_VALUE;
    }

    private int globalScore(Square s0, Square s1, boolean[][] visited) {
        int score = Integer.MIN_VALUE;

        if (visited[s1.x][s1.y]) {
            score = ALREADY_VISITED_SQUARE;
        } else if (s1.explored) {
            visited[s1.x][s1.y] = true;
            ArrayList<Square> neighboors = neighboors(s1.x, s1.y);
            for (Square s2 : neighboors) {
                int temp = squareScore(s2) + movingScore(s0, s1, s2) + globalScore(s1, s2, cloneVisited(visited));
                if (temp > score) {
                    score = temp;
                    if (s0 == s1) {
                        target = s2;
                    }
                }
            }
        } else if (found_wumpus && !killed_wumpus && (wumpusSquare.x == s1.x || wumpusSquare.y == s1.y)) {
            score = HUNT_WUMPUS;
        } else {
            score = 0;
        }
        return score;
    }

    private void bestAction() {
        String action;
        int score = globalScore(map[posX][posY], map[posX][posY], new boolean[size][size]);

        if (w.isInPit()) {
            action = World.A_CLIMB;
        } else if (w.hasGlitter(posX + 1, posY + 1)) {
            action = World.A_GRAB;
        } else if (found_wumpus && !killed_wumpus && (wumpusSquare.x == posX || wumpusSquare.y == posY)) {
            if ((w.getDirection() == World.DIR_RIGHT && posY == wumpusSquare.y && posX < wumpusSquare.x)
                    || (w.getDirection() == World.DIR_LEFT && posY == wumpusSquare.y && posX > wumpusSquare.x)
                    || (w.getDirection() == World.DIR_UP && posX == wumpusSquare.x && posY < wumpusSquare.y)
                    || (w.getDirection() == World.DIR_DOWN && posX == wumpusSquare.x && posY > wumpusSquare.y)) {
                action = World.A_SHOOT;
            } else if ((w.getDirection() == World.DIR_RIGHT && posY > wumpusSquare.y && posX == wumpusSquare.x)
                    || (w.getDirection() == World.DIR_LEFT && posY < wumpusSquare.y && posX == wumpusSquare.x)
                    || (w.getDirection() == World.DIR_UP && posX < wumpusSquare.x && posY == wumpusSquare.y)
                    || (w.getDirection() == World.DIR_DOWN && posX > wumpusSquare.x && posY == wumpusSquare.y)) {
                action = World.A_TURN_RIGHT;
            } else {
                action = World.A_TURN_LEFT;
            }
        } else if ((w.getDirection() == World.DIR_RIGHT && posY == target.y && posX < target.x)
                || (w.getDirection() == World.DIR_LEFT && posY == target.y && posX > target.x)
                || (w.getDirection() == World.DIR_UP && posX == target.x && posY < target.y)
                || (w.getDirection() == World.DIR_DOWN && posX == target.x && posY > target.y)) {
            action = World.A_MOVE;
        } else if ((w.getDirection() == World.DIR_RIGHT && posY > target.y && posX == target.x)
                || (w.getDirection() == World.DIR_LEFT && posY < target.y && posX == target.x)
                || (w.getDirection() == World.DIR_UP && posX < target.x && posY == target.y)
                || (w.getDirection() == World.DIR_DOWN && posX > target.x && posY == target.y)) {
            action = World.A_TURN_RIGHT;
        } else {
            action = World.A_TURN_LEFT;
        }

        w.doAction(action);
        System.out.println("\n" + action + " " + target.x + " " + target.y);
        System.out.println(score);
    }

    /**
     * Asks your solver agent to execute an action.
     */
    @Override
    public void doAction() {
        //Location of the player
        posX = w.getPlayerX() - 1;
        posY = w.getPlayerY() - 1;

        sense();
        updateMap();
        bestAction();

        for (int j = size - 1; j >= 0; j--) {
            System.out.println("");
            for (int i = 0; i < size; i++) {
                map[i][j].print();
            }
        }
        System.out.println("");
    }
}
