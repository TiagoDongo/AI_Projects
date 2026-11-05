package pacman.entries.pacman;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 */

/**
 * Monte Carlo Tree Search algorithm implementing UCT method
 * Run main method to test
 *
 * @author D.Vitonis
 *
 */
public class UCT {

    /*
     * Maze used to control the game
     */
    public Map maze;
    private Random random = new Random();

    /*
     * rootNode is the starting point of the present state
     */
    Node rootNode;

    /*
     * currentNode refers to the node we work at every step
     */
    Node currentNode;

    /*
     * Exploration coefficient
     */
    private float C = (float) (1.0/Math.sqrt(2));

    /*
     * Computational limit
     */
    protected final int maxIterations = 100;


    /**
     * Constructor
     * Initialize the maze game
     */
    UCT(){
        maze = new Map();
        maze.resetMaze();
    }

    /**
     * run the UCT search and find the optimal action for the root node state
     * @return
     * @throws InterruptedException
     */
    public int runUCT() throws InterruptedException{

        /*
         * Create root node with the present state
         */
        rootNode = new Node(maze.map.clone());

        /*
         * In computational budget limits apply search
         */
        int iterations = 0;
        while(!Terminate(iterations)){
            TreePolicy();
            float reward = DefaultPolicy();
            Backpropagate(reward);
            iterations ++;
        }

        /*
         * Get the action that directs to the best node
         */
        currentNode = rootNode;
        //rootNode is the one we are working with
        //and we apply the exploitation of it to find the child with the highest average reward
        int bestAction = 0;
        BestChild(0); // pure exploitation (C=0): the child with the highest average reward
        if (currentNode != null){
            bestAction = currentNode.parentAction;
        } else {
            bestAction = UntriedAction(rootNode); // no child was created: fall back to the first valid action
        }

        return bestAction;
    }

    /**
     * Expand the nonterminal nodes with one available child.
     * Chose a node to expand with BestChild(C) method
     */
    private void TreePolicy() {
        currentNode = rootNode;
        // go down the tree until a node that can still be expanded (or a terminal state) is found
        while (!TerminalState(currentNode.state)){
            if (!FullyExpanded(currentNode)){
                Expand(); // adds one new child and makes it the current node
                return;
            }
            BestChild(C); // otherwise move to the most promising child
        }
    }

    /**
     * Simulation of the game. Choose random actions up until the game is over (goal reached or dead)
     * @return reward (1 for win, 0 for loss)
     */
    private float DefaultPolicy() {
        char[] st = currentNode.state.clone();
        while(!TerminalState(st)){
            int action = RandomAction(st);
            st = maze.getNextState(action, st);
            int ghostAction = RandomGhostAction(st);
            st = maze.getNextGhostState(ghostAction, st);
        }
        return maze.getReward(st);
    }

    /**
     * Assign the received reward to every parent of the parent up to the rootNode
     * Increase the visited count of every node included in backpropagation
     * @param reward
     */
    private void Backpropagate(float reward) {
        // from the simulated node up to the root: add the reward and count the visit
        Node n = currentNode;
        while (n != null){
            n.reward += reward;
            n.timesvisited++;
            n = n.parent;
        }
    }

    /**
     * Check if the node is fully expanded
     * @param nt
     * @return
     */
    private boolean FullyExpanded(Node nt) {
        return UntriedAction(nt) == -1;
    }

    /**
     * Check if the state is the end of the game
     * @param state
     * @return
     */
    private boolean TerminalState(char[] state) {
        return maze.isGoalReached(state) || maze.isAvatarDead(state);
    }

    /**
     * Choose the best child according to the UCT value
     * Assign it as a currentNode
     * @param c
     */
    private void BestChild(float c) {
        Node nt = currentNode;
        Node bestChild = null;
        float bestValue = -Float.MAX_VALUE;
        for (Node child : nt.children){
            float value = UCTvalue(child, c);
            if (value > bestValue){
                bestValue = value;
                bestChild = child;
            }
        }



        currentNode = bestChild;
    }

    /**
     * Calculate UCT value for the best child choosing
     * @param n
     * @param c
     * @return
     */
    private float UCTvalue(Node n, float c) {
        if (n.timesvisited == 0)
            return Float.MAX_VALUE; // never visited: try it first
        // exploitation: average reward of the node
        float average = n.reward / n.timesvisited;
        // exploration: bigger for nodes that were visited few times compared to their parent
        float exploration = 2 * c * (float) Math.sqrt(Math.log(n.parent.timesvisited) / n.timesvisited);
        return average + exploration;
    }

    /**
     * Expand the current node by adding new child to the currentNode
     */
    private void Expand() {
        /*
         * Choose untried action
         */
        int action = UntriedAction(currentNode);

        /*
         * Create a child, populate it and add fit to the node
         */
        Node child = new Node(maze.getNextGhostState(RandomGhostAction(maze.getNextState(action, currentNode.state)),
                maze.getNextState(action, currentNode.state)));
        child.parent = currentNode;
        child.parentAction = action;
        currentNode.children.add(child);
        currentNode = child; // the new child is the node that will be simulated

    }

    /**
     * Returns the first untried action of the node
     * @param n
     * @return
     */
    private int UntriedAction(Node n) {
        outer:
        for (int i=0;i<4;i++){
            for (int k=0;k<n.children.size();k++){
                if (n.children.get(k).parentAction == i){
                    continue outer;
                }
            }
            if (maze.isValidMove(i, n.state))
                return i;
        }
        return -1;
    }

    /**
     * Check if the algorithm is to be terminated, e.g. reached number of iterations limit
     * @param i
     * @return
     */
    private boolean Terminate(int i) {
        if (i>maxIterations) return true;
        return false;
    }

    /**
     * Used in game simulation to pick random action for the agent
     * @ param state st
     * @ return action
     */
    private int RandomAction(char[] st) {
        int action = random.nextInt(4);
        while (!maze.isValidMove(action,st)){
            action = random.nextInt(4);
        }
        return action;
    }

    /**
     * Used in game simulation to pick random action for the ghost
     * @param st
     * @return action
     */
    private int RandomGhostAction(char[] st) {
        int action = random.nextInt(4);
        while (!maze.isValidGhostMove(action,st)){
            action = random.nextInt(4);
        }
        return action;
    }

    /**
     * Class to store node information, e.g.
     * state, children, parent, accumulative reward, visited times
     * @author dariusv
     *
     */
    private class Node{

        public char[] state;
        public List<Node> children = new ArrayList<Node>();
        public Node parent = null;
        public int parentAction=-1;
        public float reward =0;
        public int timesvisited = 0;


        Node(char[] state){
            this.state = state;
        }
    }


    /**
     * Class used to store and control the maze
     *
     * @author A.Liapis
     * @modified D.Vitonis
     */
    public class Map{

        // --- variables
        protected final int mapsize = 8;

        protected final char[] startingmap={ '@',' ',' ',' ',' ',' ',' ',' ',
                ' ',' ',' ',' ',' ',' ',' ',' ',
                ' ',' ',' ',' ',' ',' ',' ',' ',
                ' ',' ',' ',' ',' ',' ',' ',' '
                ,' ',' ',' ','#',' ',' ','G',' '
                ,' ',' ',' ',' ',' ',' ',' ',' '
                ,' ',' ',' ',' ',' ',' ',' ',' '
                ,' ',' ',' ',' ',' ',' ',' ',' '
        };
        protected char[] map;

        // --- movement constants
        public static final int UP=0;
        public static final int RIGHT=1;
        public static final int DOWN=2;
        public static final int LEFT=3;

        public char[] getMap(){
            return (char[])map.clone();
        }

        /**
         * Assign reward 1 if won, 0 if lost
         * @param st
         * @return
         */
        public float getReward(char[] st) {
            if (isGoalReached(st))
                return 1;
            if (isAvatarDead(st)){
                return 0;
            }
            return 0;
        }

        public void resetMaze(){
            map = (char[])startingmap.clone();
        }

        /**
         * Returns the map state which results from an initial map state after an
         * action is applied. In case the action is invalid, the returned map is the
         * same as the initial one (no move).
         * @param action taken by the avatar ('@')
         * @param map before the action is taken
         * @return resulting map after the action is taken
         */
        public char[] getNextState(int action, char[] map){
            char[] nextMap = (char[])map.clone();
            // get location of '@'
            int avatarIndex = getAvatarIndex(map);
            if(avatarIndex==-1){
                return nextMap; // no effect
            }
            int nextAvatarIndex = getNextAvatarIndex(action, avatarIndex);
            if(nextAvatarIndex>=0 && nextAvatarIndex<map.length){
                if(nextMap[nextAvatarIndex]!='#'){
                    // change the map
                    nextMap[avatarIndex]=' ';
                    nextMap[nextAvatarIndex]='@';
                }
            }
            return nextMap;
        }

        public char[] getNextState(int action){
            char[] nextMap = (char[])map.clone();
            // get location of '@'
            int avatarIndex = getAvatarIndex(map);
            if(avatarIndex==-1){
                return nextMap; // no effect
            }
            int nextAvatarIndex = getNextAvatarIndex(action, avatarIndex);
            //System.out.println(avatarIndex+" "+nextAvatarIndex);
            if(nextAvatarIndex>=0 && nextAvatarIndex<map.length){
                if(nextMap[nextAvatarIndex]!='#'){
                    // change the map
                    nextMap[avatarIndex]=' ';
                    nextMap[nextAvatarIndex]='@';
                }
            }
            return nextMap;
        }

        public char[] getNextGhostState(int action){
            char[] nextMap = (char[])map.clone();
            // get location of '#'
            int ghostIndex = getGhostIndex(map);
            if(ghostIndex==-1){
                return nextMap; // no effect
            }
            int nextGhostIndex = getNextAvatarIndex(action, ghostIndex);
            //System.out.println(avatarIndex+" "+nextAvatarIndex);
            if(nextGhostIndex>=0 && nextGhostIndex<map.length){
                if(nextMap[nextGhostIndex]!='G' && nextMap[nextGhostIndex]!='@'){
                    // change the map
                    nextMap[ghostIndex]=' ';
                    nextMap[nextGhostIndex]='#';
                }
            }
            return nextMap;
        }

        public char[] getNextGhostState(int action,char[] map){
            char[] nextMap = (char[])map.clone();
            // get location of '#'
            int ghostIndex = getGhostIndex(map);
            if(ghostIndex==-1){
                return nextMap; // no effect
            }
            int nextGhostIndex = getNextAvatarIndex(action, ghostIndex);
            //System.out.println(avatarIndex+" "+nextAvatarIndex);
            if(nextGhostIndex>=0 && nextGhostIndex<map.length){
                if(nextMap[nextGhostIndex]!='G' && nextMap[nextGhostIndex]!='@'){
                    // change the map
                    nextMap[ghostIndex]=' ';
                    nextMap[nextGhostIndex]='#';
                }
            }
            return nextMap;
        }

        public void goToNextState(int action){
            map=getNextState(action);
        }

        public void goToNextGhostState(int action){
            map=getNextGhostState(action);
        }

        public boolean isValidMove(int action){
            int avatarIndex = getAvatarIndex(map);
            if(avatarIndex==-1){
                return false; // no effect
            }
            int nextAvatarIndex = getNextAvatarIndex(action, avatarIndex);
            if(nextAvatarIndex>=0 && nextAvatarIndex<map.length && avatarIndex!=nextAvatarIndex){
                if(map[nextAvatarIndex]!='#'){
                    return true;
                }
            }
            return false;
        }

        public boolean isValidMove(int action, char[] map){
            int avatarIndex = getAvatarIndex(map);
            if(avatarIndex==-1){
                return false; // no effect
            }
            int nextAvatarIndex = getNextAvatarIndex(action, avatarIndex);
            if(nextAvatarIndex>=0 && nextAvatarIndex<map.length && avatarIndex!=nextAvatarIndex){
                if(map[nextAvatarIndex]!='#'){
                    return true;
                }
            }
            return false;
        }

        public boolean isValidGhostMove(int action, char[] map){
            int ghostIndex = getGhostIndex(map);
            if(ghostIndex==-1){
                return false; // no effect
            }
            int nextGhostIndex = getNextAvatarIndex(action, ghostIndex);
            if(nextGhostIndex>=0 && nextGhostIndex<map.length
                    && ghostIndex!=nextGhostIndex && nextGhostIndex!=getGoalIndex(map)){
                if(map[nextGhostIndex]!='@'){
                    return true;
                }
            }
            return false;
        }

        public boolean isValidGhostMove(int action){
            int ghostIndex = getGhostIndex(map);
            if(ghostIndex==-1){
                return false; // no effect
            }
            int nextGhostIndex = getNextAvatarIndex(action, ghostIndex);
            if(nextGhostIndex>=0 && nextGhostIndex<map.length
                    && ghostIndex!=nextGhostIndex && nextGhostIndex!=getGoalIndex(map)){
                if(map[nextGhostIndex]!='@'){
                    return true;
                }
            }
            return false;
        }

        public int getAvatarIndex(){
            int avatarIndex = -1;
            for(int i=0;i<map.length;i++){
                if(map[i]=='@'){ avatarIndex=i; }
            }
            return avatarIndex;
        }

        public int getAvatarIndex(char[] map){
            int avatarIndex = -1;
            for(int i=0;i<map.length;i++){
                if(map[i]=='@'){ avatarIndex=i; }
            }
            return avatarIndex;
        }

        public int getGhostIndex(char[] map){
            int ghostIndex = -1;
            for(int i=0;i<map.length;i++){
                if(map[i]=='#'){ ghostIndex=i; }
            }
            return ghostIndex;
        }

        public int getGhostIndex(){
            int ghostIndex = -1;
            for(int i=0;i<map.length;i++){
                if(map[i]=='#'){ ghostIndex=i; }
            }
            return ghostIndex;
        }

        public int getGoalIndex(char[] map){
            int goalIndex = -1;
            for(int i=0;i<map.length;i++){
                if(map[i]=='#'){ goalIndex=i; }
            }
            return goalIndex;
        }

        public boolean isGoalReached(){
            int goalIndex = -1;
            for(int i=0;i<map.length;i++){
                if(map[i]=='G'){ goalIndex=i; }
            }
            return (goalIndex==-1);
        }

        public boolean isAvatarDead(char[] map){

            int currentAvatarIndex = getAvatarIndex(map);
            int currentGhostIndex = getGhostIndex(map);

            int avatarx = currentAvatarIndex%8;
            int avatary = currentAvatarIndex/8;

            int ghostx = currentGhostIndex%8;
            int ghosty = currentGhostIndex/8;

            if (Math.abs(avatarx - ghostx)<2 && Math.abs(avatary - ghosty)<2){
//	        System.out.println("dead");
                return true;
            }

            return false;

        }

        public boolean isGoalReached(char[] map){
            int goalIndex = -1;
            for(int i=0;i<map.length;i++){
                if(map[i]=='G'){ goalIndex=i; }
            }
            return (goalIndex==-1);
        }

        public int getNextAvatarIndex(int action, int currentAvatarIndex){
            int x = currentAvatarIndex%8;
            int y = currentAvatarIndex/8;
            if(action==UP){
                y--;
            }
            if(action==RIGHT){
                x++;
            } else if(action==DOWN){
                y++;
            } else if(action==LEFT){
                x--;
            }
            if(x<0 || y<0 || x>=8 || y>=8){
                return currentAvatarIndex; // no move
            }
            return x+8*y;
        }

        public void printMap(){
            for(int i=0;i<map.length;i++){
                if(i%8==0){
                    System.out.println("+-+-+-+-+-+-+-+-+");
                }
                System.out.print("|"+map[i]);
                if(i%8==7){
                    System.out.println("|");
                }
            }
            System.out.println("+-+-+-+-+-+-+-+-+");
        }

        public void printMap(char[] map){
            for(int i=0;i<map.length;i++){
                if(i%8==0){
                    System.out.println("+-+-+-+-+-+-+-+-+");
                }
                System.out.print("|"+map[i]);
                if(i%8==7){
                    System.out.println("|");
                }
            }
            System.out.println("+-+-+-+-+-+-+-+-+");
        }

        public String getMoveName(int action){
            String result = "ERROR";
            if(action==UP){
                result="UP";
            } else if(action==RIGHT){
                result="RIGHT";
            } else if(action==DOWN){
                result="DOWN";
            } else if(action==LEFT){
                result="LEFT";
            }
            return result;
        }

        public int getGoodGhostAction(char[] map) {
            int currentAvatarIndex = getAvatarIndex(map);
            int currentGhostIndex = getGhostIndex(map);

            int avatarx = currentAvatarIndex%8;
            int avatary = currentAvatarIndex/8;

            int ghostx = currentGhostIndex%8;
            int ghosty = currentGhostIndex/8;

            int manhatanDistance = Math.abs(ghosty-avatary)+Math.abs(ghostx-avatarx);

            int goodAction = 0;
            for (int i=0;i<4;i++){
                int possition = getNextAvatarIndex(i,currentGhostIndex);
                ghostx = possition%8;
                ghosty = possition/8;
                int newmanhatan = Math.abs(ghosty-avatary)+Math.abs(ghostx-avatarx);
                if (newmanhatan < manhatanDistance){
                    goodAction = i;
                    manhatanDistance = newmanhatan;
                }
            }
            return goodAction;
        }

    }

    /**
     * UCT maze solving test
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {

        UCT uct = new UCT();

        while(true){
            // PRINT MAP
            uct.maze.printMap();
            // CHECK IF WON OR LOST, THEN RESET
            if(uct.maze.isGoalReached()){
                System.out.println("GOAL REACHED");
                uct.maze.resetMaze();
                return;
            }

            if(uct.maze.isAvatarDead(uct.maze.map)){
                System.out.println("AVATAR DEAD");
                uct.maze.resetMaze();
                return;
            }

            //FIND THE OPTIMAL ACTION VIA UTC
            int bestAction = uct.runUCT();

            //ADVANCE THE GAME WITH MOVES OF AGENT AND GHOST
            uct.maze.goToNextState(bestAction);
            int bestGhostAction = uct.random.nextInt(4);
            while (!uct.maze.isValidGhostMove(bestGhostAction)){
                bestGhostAction = uct.random.nextInt(4);
            }
            uct.maze.goToNextGhostState(bestGhostAction);

            //TRACK THE GAME VISUALY
            Thread.sleep(1000);
        }

    }

}