package ai;

import ai.Global;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import static java.lang.System.currentTimeMillis;
import java.util.ArrayList;
import java.util.Random;
import kalaha.*;

/**
 * This is the main class for your Kalaha AI bot. Currently it only makes a
 * random, valid move each turn.
 *
 * @author Johan Hagelbäck
 */
public class AIClient implements Runnable {

    private int player;
    private JTextArea text;

    private PrintWriter out;
    private BufferedReader in;
    private Thread thr;
    private Socket socket;
    private boolean running;
    private boolean connected;
    private boolean stop = false;
    private int moveNb = 1;

    /**
     * Creates a new client.
     */
    public AIClient() {
        player = -1;
        connected = false;

        //This is some necessary client stuff. You don't need
        //to change anything here.
        initGUI();

        try {
            addText("Connecting to localhost:" + KalahaMain.port);
            socket = new Socket("localhost", KalahaMain.port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            addText("Done");
            connected = true;
        } catch (Exception ex) {
            addText("Unable to connect to server");
            return;
        }
    }

    /**
     * Starts the client thread.
     */
    public void start() {
        //Don't change this
        if (connected) {
            thr = new Thread(this);
            thr.start();
        }
    }

    /**
     * Creates the GUI.
     */
    private void initGUI() {
        //Client GUI stuff. You don't need to change this.
        JFrame frame = new JFrame("My AI Client");
        frame.setLocation(Global.getClientXpos(), 445);
        frame.setSize(new Dimension(420, 250));
        frame.getContentPane().setLayout(new FlowLayout());

        text = new JTextArea();
        JScrollPane pane = new JScrollPane(text);
        pane.setPreferredSize(new Dimension(400, 210));

        frame.getContentPane().add(pane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setVisible(true);
    }

    /**
     * Adds a text string to the GUI textarea.
     *
     * @param txt The text to add
     */
    public void addText(String txt) {
        //Don't change this
        text.append(txt + "\n");
        text.setCaretPosition(text.getDocument().getLength());
    }

    /**
     * Thread for server communication. Checks when it is this client's turn to
     * make a move.
     */
    public void run() {
        String reply;
        running = true;

        try {
            while (running) {
                //Checks which player you are. No need to change this.
                if (player == -1) {
                    out.println(Commands.HELLO);
                    reply = in.readLine();

                    String tokens[] = reply.split(" ");
                    player = Integer.parseInt(tokens[1]);

                    addText("I am player " + player);
                }

                //Check if game has ended. No need to change this.
                out.println(Commands.WINNER);
                reply = in.readLine();
                if (reply.equals("1") || reply.equals("2")) {
                    int w = Integer.parseInt(reply);
                    if (w == player) {
                        addText("I won!");
                    } else {
                        addText("I lost...");
                    }
                    running = false;
                }
                if (reply.equals("0")) {
                    addText("Even game!");
                    running = false;
                }

                //Check if it is my turn. If so, do a move
                out.println(Commands.NEXT_PLAYER);
                reply = in.readLine();
                if (!reply.equals(Errors.GAME_NOT_FULL) && running) {
                    int nextPlayer = Integer.parseInt(reply);

                    if (nextPlayer == player) {
                        out.println(Commands.BOARD);
                        String currentBoardStr = in.readLine();
                        boolean validMove = false;
                        while (!validMove) {
                            long startT = System.currentTimeMillis();
                            //This is the call to the function for making a move.
                            //You only need to change the contents in the getMove()
                            //function.
                            GameState currentBoard = new GameState(currentBoardStr);
                            int cMove = getMove(currentBoard);

                            //Timer stuff
                            long tot = System.currentTimeMillis() - startT;
                            double e = (double) tot / (double) 1000;

                            out.println(Commands.MOVE + " " + cMove + " " + player);
                            reply = in.readLine();
                            if (!reply.startsWith("ERROR")) {
                                validMove = true;
                                addText("Made move " + cMove + " in " + e + " secs");
                            }
                        }
                    }
                }

                //Wait
                Thread.sleep(100);
            }
        } catch (Exception ex) {
            running = false;
        }

        try {
            socket.close();
            addText("Disconnected from server");
        } catch (Exception ex) {
            addText("Error closing connection: " + ex.getMessage());
        }
    }

    /**
     * This is the method that makes a move each time it is your turn. Here you
     * need to change the call to the random method to your Minimax search.
     *
     * @param currentBoard The current board state
     * @return Move to make (1-6)
     */
    public int getMove(GameState currentBoard) {
        GameState clonedBoard = currentBoard.clone();
        GameNode root;
        int maxLevel = 2;
        
        // used to check the time left to perform the calculation
        long start = currentTimeMillis();
        long lastDuration = 0;
        
        // minMax value calculated for the current maxLevel
        int currentMax;

        // iterative deepening is performed here, if we have enough time left, we go deeper
        do {
            root = new GameNode(clonedBoard, 0);
            currentMax = expandTree(root, maxLevel);
            lastDuration = currentTimeMillis() - lastDuration - start;
            System.out.println("depth : " + maxLevel + " in " + lastDuration + "ms");
            maxLevel++;
        } while (currentTimeMillis() + Math.pow(lastDuration, 1.35) - start < 5000 && !stop);

        // this part is to choose a random move when we have some moves with the same minMax value
        ArrayList<Integer> possibleMoves = new ArrayList<>();
        for (GameNode c : root.children) {
            if (c.minMax == currentMax) {
                possibleMoves.add(c.move);
            }
        }
        Random rand = new Random();
        int randomNum = rand.nextInt(possibleMoves.size());
        int move = possibleMoves.get(randomNum);
        
        System.out.println("move choosen : " + move);
        System.out.println(currentBoard.getSeeds(move, player) + " pubbles moved");
        System.out.println("move n° " + moveNb++ + " / player " + player);
        System.out.println("-------");

        return move;
    }

    public int expandTree(GameNode node, int maxLevel) {

        int validMoves = node.state.getNoValidMoves(node.state.getNextPlayer());
        Integer minMax = null;
        
        if (node.level < maxLevel && validMoves > 0) {  //general case of the recursive fonction            
            for (int move = 1; move <= 6 && !pruning(minMax, node); move++) {
                if (node.state.moveIsPossible(move)) {
                    GameNode child = node.addChild(move);
                    minMax = minMax(node.state, minMax, expandTree(child, maxLevel));
                }
            }
            
        } else if (validMoves == 0) { // stop case = leaf of the game tree            
            stop = true;
            minMax = utility(node.state);
        
        
        } else { // stop case = node of the game tree but maxLevel reached            
            stop = false;
            minMax = evaluation(node.state);
        }
        
        node.minMax = minMax;
        return minMax;
    }

    public boolean pruning(Integer minMax, GameNode node) {
        if (node.parent != null && minMax != null) {
            return (node.state.getNextPlayer() == player) ? minMax <= node.parent.minMax : minMax >= node.parent.minMax;
        } else {
            return false;
        }
    }

    public int minMax(GameState state, Integer currentMinMax, int value) {
        if (currentMinMax == null) {
            return value;
        } else {
            return (state.getNextPlayer() == player) ? Math.max(currentMinMax, value) : Math.min(currentMinMax, value);
        }
    }

    public int utility(GameState state) {
        return (player == 1) ? state.getScore(1) - state.getScore(2) : state.getScore(2) - state.getScore(1);
    }

    public int evaluation(GameState state) {
        return utility(state);
    }

    // object used to store the game tree
    public class GameNode {

        public GameNode parent;
        public int minMax;
        public int move;
        public int level;
        public GameState state;
        public ArrayList<GameNode> children = new ArrayList<>();

        public GameNode(GameState state, int level) {
            this.state = state;
            this.level = level;
        }

        public GameNode(GameState state, int level, int move) {
            this.state = state;
            this.level = level;
            this.move = move;
        }

        public GameNode addChild(int move) {
            GameState childState = this.state.clone();
            childState.makeMove(move);
            GameNode child = new GameNode(childState, this.level + 1, move);

            child.parent = this;
            children.add(child);

            return child;
        }
    }

    /**
     * Returns a random ambo number (1-6) used when making a random move.
     *
     * @return Random ambo number
     */
    public int getRandom() {
        return 1 + (int) (Math.random() * 6);
    }
}
