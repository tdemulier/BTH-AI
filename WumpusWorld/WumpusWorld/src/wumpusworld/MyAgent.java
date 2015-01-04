package wumpusworld;

import java.util.ArrayList;

/**
 * Contans starting code for creating your own Wumpus World agent. Currently the
 * agent only make a random decision each turn.
 *
 * @author Johan Hagelbäck
 */
public class MyAgent implements Agent {

    public class Square {

        public boolean breeze;
        public boolean stench;
        public boolean glitter;
        public int pit; // value {-1,0,1,2} for {no idea, no pit, may be, sure}
        public int wumpus; // same
        public boolean explored;
        public boolean safe;
        public int x, y;

        public Square(int posX, int posY) {
            breeze = false;
            stench = false;
            glitter = false;
            pit = -1;
            wumpus = -1;
            explored = false;
            safe = false;
            x = posX;
            y = posY;
        }

        public void print() {
            System.out.print(" | b : " + breeze + ", s : " + stench + ", g : " + glitter + ", p : " + pit + ", w : " + wumpus + ", e : " + explored + ", s : " + safe + " | ");
        }
    }

    private World w;
    private int size;

    private Square[][] map;
    private boolean found_wumpus = false;
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

    private void sense() {
        map[posX][posY].explored = true;

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
        if (w.hasGlitter(posX + 1, posY + 1)) {
            System.out.println("I am in a Glitter");
            map[posX][posY].glitter = true;
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
        
        if (!found_wumpus) {
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
                                }
                            }
                        }
                    }
                }
            }
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (map[i][j].pit == 0 && map[i][j].wumpus == 0) {
                    map[i][j].safe = true;
                }
            }
        }
    }

    /**
     * Asks your solver agent to execute an action.
     */
    public void doAction() {
        //Location of the player
        posX = w.getPlayerX() - 1;
        posY = w.getPlayerY() - 1;

        sense();
        updateMap();

        for (int j = size - 1; j >= 0; j--) {
            System.out.println("\n");
            for (int i = 0; i < size; i++) {
                map[i][j].print();
            }
            System.out.println("\n");
        }

        //Basic action:
        //We are in a pit. Climb up.
        if (w.isInPit()) {
            w.doAction(World.A_CLIMB);
            return;
        }

        if (w.getDirection() == World.DIR_RIGHT) {
            System.out.println("I am facing Right");
        }
        if (w.getDirection() == World.DIR_LEFT) {
            System.out.println("I am facing Left");
        }
        if (w.getDirection() == World.DIR_UP) {
            System.out.println("I am facing Up");
        }
        if (w.getDirection() == World.DIR_DOWN) {
            System.out.println("I am facing Down");
        }

        //Random move actions
        int rnd = (int) (Math.random() * 5);
        if (rnd == 0) {
            w.doAction(World.A_TURN_LEFT);
            return;
        }
        if (rnd == 1) {
            w.doAction(World.A_TURN_RIGHT);
            return;
        }
        if (rnd >= 2) {
            w.doAction(World.A_MOVE);
            return;
        }
    }
}
