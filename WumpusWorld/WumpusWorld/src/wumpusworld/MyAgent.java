package wumpusworld;

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
        public boolean pit;
        public boolean wumpus;
        public boolean explored;
        public boolean safe;

        public Square() {
            breeze = false;
            stench = false;
            glitter = false;
            pit = false;
            wumpus = false;
            explored = false;
            safe = true;
        }
    }

    private World w;

    private Square[][] map;
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
        int size = world.getSize();
        map = new Square[size][size];
    }

    private void sense() {
        map[posX][posY].explored = true;

        //Test the environment
        if (w.hasBreeze(posX, posY)) {
            System.out.println("I am in a Breeze");
            map[posX][posY].breeze = true;
        }
        if (w.hasStench(posX, posY)) {
            System.out.println("I am in a Stench");
            map[posX][posY].stench = true;
        }
        if (w.hasPit(posX, posY)) {
            System.out.println("I am in a Pit");
            map[posX][posY].pit = true;
        }
        if (!w.hasPit(posX, posY)) {
            map[posX][posY].pit = true;
        }
        if (w.hasGlitter(posX, posY)) {
            System.out.println("I am in a Glitter");
            map[posX][posY].glitter = true;
        }
    }

    private square[] neighboors(){
        
    }

    private void updateMap() {
        if (breeze[posX][posY]) {

        }
    }

    /**
     * Asks your solver agent to execute an action.
     */
    public void doAction() {
        //Location of the player
        posX = w.getPlayerX();
        posY = w.getPlayerY();

        sense();
        updateMap();

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
    }
}
