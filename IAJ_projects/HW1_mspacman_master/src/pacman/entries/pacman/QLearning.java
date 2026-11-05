package pacman.entries.pacman;

import java.util.Random;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Q Learning sample class <br/>
 * <b>The goal of this code sample is for the character @ to reach the goal area G</b> <br/>
 * compile using "javac QLearning.java" <br/>
 * test using "java QLearning" <br/>
 *
 * @author A.Liapis
 */
public class QLearning {
    /**
     * The heart of the Q-learning algorithm, the Qtable contains the table
     * which maps states, actions and their Q values. This class has elaborate
     * documentation, and should be the focus of the students' body of work
     * for the purposes of this tutorial.
     *
     * @author A.Liapis
     */
    public class Qtable {
        /**
         * for creating random numbers
         */
        Random randomGenerator;
        /**
         * the table variable stores the Q-table, where the state is saved
         * directly as the actual map. Each map state has an array of Q values
         * for all the actions available for that state.
         */
        HashMap<char[], float[]> table;
        /**
         * the actionRange variable determines the number of actions available
         * at any map state, and therefore the number of Q values in each entry
         * of the Q-table.
         */
        int actionRange;

        // E-GREEDY Q-LEARNING SPECIFIC VARIABLES
        /**
         * for e-greedy Q-learning, when taking an action a random number is
         * checked against the explorationChance variable: if the number is
         * below the explorationChance, then exploration takes place picking
         * an action at random. Note that the explorationChance is not a final
         * because it is customary that the exploration chance changes as the
         * training goes on.
         */
        float explorationChance=0.4f;
        /**
         * the discount factor is saved as the gammaValue variable. The
         * discount factor determines the importance of future rewards.
         * If the gammaValue is 0 then the AI will only consider immediate
         * rewards, while with a gammaValue near 1 (but below 1) the AI will
         * try to maximize the long-term reward even if it is many moves away.
         */
        float gammaValue=0.9f;
        /**
         * the learningRate determines how new information affects accumulated
         * information from previous instances. If the learningRate is 1, then
         * the new information completely overrides any previous information.
         * Note that the learningRate is not a final because it is
         * customary that the exploration chance changes as the
         * training goes on.
         */
        float learningRate=0.15f;

        //PREVIOUS STATE AND ACTION VARIABLES
        /**
         * Since in Q-learning the updates to the Q values are made ONE STEP
         * LATE, the state of the world when the action resulting in the reward
         * was made must be stored.
         */
        char[] prevState;
        /**
         * Since in Q-learning the updates to the Q values are made ONE STEP
         * LATE, the index of the action which resulted in the reward must be
         * stored.
         */
        int prevAction;

        /**
         * Q table constructor, initiates variables.
         * @param actionRange the number of actions available at any map state
         */
        Qtable(int actionRange){
            randomGenerator = new Random();
            this.actionRange=actionRange;
            table = new HashMap<char[], float[]>();
        }

        /**
         * For this example, the getNextAction function uses an e-greedy
         * approach, having exploration happen if the exploration chance
         * is rolled.
         *
         * @param map the current map (state)
         * @return prevAction the action to be taken by the calling progam
         */
        int getNextAction(char[] map){
            prevState=(char[])map.clone();
            if(randomGenerator.nextFloat()<explorationChance){
                prevAction=explore();
            } else {
                prevAction=getBestAction(map);
            }
            return prevAction;
        }

        /**
         * The getBestAction function uses a greedy approach for finding
         * the best action to take. Note that if all Q values for the current
         * state are equal (such as all 0 if the state has never been visited
         * before), then getBestAction will always choose the same action.
         * If such an action is invalid, this may lead to a deadlock as the
         * map state never changes: for situations like these, exploration
         * can get the algorithm out of this deadlock.
         *
         * @param map the current map (state)
         * @return best the action with the highest Q value
         */
        int getBestAction(char[] map){
            float[] qValues = getActionsQValues(map);
            int best = 0;
            for(int i=1;i<qValues.length;i++){
                if(qValues[i]>qValues[best]){
                    best=i;
                }
            }
            return best;
        }

        /**
         * The explore function is called for e-greedy algorithms.
         * It can choose an action at random from all available,
         * or can put more weight towards actions that have not been taken
         * as often as the others (most unknown).
         *
         * @return index of action to take
         */
        int explore(){
            return randomGenerator.nextInt(actionRange);
        }

        /**
         * The updateQvalue is the heart of the Q-learning algorithm. Based on
         * the reward gained by taking the action prevAction while being in the
         * state prevState, the updateQvalue must update the Q value of that
         * {prevState, prevAction} entry in the Q table. In order to do that,
         * the Q value of the best action of the current map state must also
         * be calculated.
         *
         * @param reward at the current map state
         * @param map the current map state (for finding the best action of the
         * current map state)
         */
        void updateQvalue(int reward, char[] map){
            // Q value of the action that was taken in the previous state
            float[] prevQValues = getActionsQValues(prevState);
            // best Q value available in the current state (0 for the goal state, which is never updated)
            float[] currentQValues = getActionsQValues(map);
            float maxQ = currentQValues[0];
            for(int i=1;i<currentQValues.length;i++){
                if(currentQValues[i]>maxQ){
                    maxQ=currentQValues[i];
                }
            }
            // Q(s,a) = Q(s,a) + learningRate * ( reward + gamma * max Q(s',a') - Q(s,a) )
            prevQValues[prevAction] += learningRate * (reward + gammaValue*maxQ - prevQValues[prevAction]);
        }

        /**
         * The getActionsQValues function returns an array of Q values for
         * all the actions available at any state. Note that if the current
         * map state does not already exist in the Q table (never visited
         * before), then it is initiated with Q values of 0 for all of the
         * available actions.
         *
         * @param map the current map (state)
         * @return an array of Q values for all the actions available at any state
         */
        float[] getActionsQValues(char[] map){
            int qIndex = indexOf(map);
            if(qIndex==-1){
                float[] initialActions = new float[actionRange];
                for(int i=0;i<actionRange;i++) initialActions[i]=0.f;
                table.put(map, initialActions);
                return initialActions;
            }

            float[] actions = (float[])(table.values().toArray()[qIndex]);
            return actions;
        }
        /**
         * printQtable is included for debugging purposes and uses the
         * action labels used in the maze class (even though the Qtable
         * is written so that it can more generic).
         */
        void printQtable(){
            Iterator iterator = table.keySet().iterator();
            while (iterator.hasNext()) {
                char[] key = (char[])iterator.next();
                float[] values = getValues(key);

                System.out.print(key[0]+""+key[1]+""+key[2]);
                System.out.println("  UP   RIGHT  DOWN  LEFT" );
                System.out.print(key[3]+""+key[4]+""+key[5]);
                System.out.println(": " + values[0]+"   "+values[1]+"   "+values[2]+"   "+values[3]);
                System.out.println(key[6]+""+key[7]+""+key[8]);
            }
        }

        /**
         * Helper function to find the index in the Q table of a given map state.
         *
         * @param map the current map (state)
         * @return the index of the Qtable entry, otherwise -1 if it is not found
         */
        int indexOf(char[] map){
            int result = 0;
            Iterator iterator = table.keySet().iterator();
            while (iterator.hasNext()) {
                char[] key = (char[])iterator.next();
                boolean match=true;
                for(int i=0;i<key.length;i++){
                    if(key[i]!=map[i]){
                        match=false;
                    }
                }
                if(match){
                    return result;
                }
                result++;
            }
            return -1;
        }

        /**
         * Helper function to find the Q-values of a given map state.
         *
         * @param map the current map (state)
         * @return the Q-values stored of the Qtable entry of the map state, otherwise null if it is not found
         */
        float[] getValues(char[] map){
            int qIndex = indexOf(map);
            if(qIndex!=-1){
                return (float[])(table.values().toArray()[qIndex]);
            }
            return null;
        }
    };

    // --- variables
    protected final char[] startingmap={ '@',' ','#',
            ' ','#','G',
            ' ',' ',' ' };
    protected char[] map;

    // --- movement constants
    public static final int UP=0;
    public static final int RIGHT=1;
    public static final int DOWN=2;
    public static final int LEFT=3;

    public QLearning(){
        resetMaze();
    }

    public char[] getMap(){
        return (char[])map.clone();
    }

    public void resetMaze(){
        map = (char[])startingmap.clone();
    }

    public int getActionRange(){
        return 4;
    }

    /**
     * Returns the map state which results from an initial map state after an
     * action is applied. In case the action is invalid, the returned map is the
     * same as the initial one (no move).
     * @param action taken by the avatar ('@')
     * @param map current map before the action is taken
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
        if(nextAvatarIndex>=0 && nextAvatarIndex<map.length){
            if(nextMap[nextAvatarIndex]!='#'){
                // change the map
                nextMap[avatarIndex]=' ';
                nextMap[nextAvatarIndex]='@';
            }
        }
        return nextMap;
    }

    public void goToNextState(int action){
        map=getNextState(action);
    }

    public boolean isValidMove(int action){
        char[] nextMap=getNextState(action);
        return (nextMap==map);
    }

    public boolean isValidMove(int action, char[] map){
        char[] nextMap=getNextState(action, map);
        return (nextMap==map);
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

    public boolean isGoalReached(){
        int goalIndex = -1;
        for(int i=0;i<map.length;i++){
            if(map[i]=='G'){ goalIndex=i; }
        }
        return (goalIndex==-1);
    }

    public boolean isGoalReached(char[] map){
        int goalIndex = -1;
        for(int i=0;i<map.length;i++){
            if(map[i]=='G'){ goalIndex=i; }
        }
        return (goalIndex==-1);
    }

    public int getNextAvatarIndex(int action, int currentAvatarIndex){
        int x = currentAvatarIndex%3;
        int y = currentAvatarIndex/3;
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
        if(x<0 || y<0 || x>=3 || y>=3){
            return currentAvatarIndex; // no move
        }
        return x+3*y;
    }

    public void printMap(){
        for(int i=0;i<map.length;i++){
            if(i%3==0){
                System.out.println("+-+-+-+");
            }
            System.out.print("|"+map[i]);
            if(i%3==2){
                System.out.println("|");
            }
        }
        System.out.println("+-+-+-+");
    }

    public void printMap(char[] map){
        for(int i=0;i<map.length;i++){
            if(i%3==0){
                System.out.println("+-+-+-+");
            }
            System.out.print("|"+map[i]);
            if(i%3==2){
                System.out.println("|");
            }
        }
        System.out.println("+-+-+-+");
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

    void runLearningLoop() throws Exception {
        Qtable q = new Qtable(getActionRange());
        int moveCounter=0;

        while(true){
            // PRINT MAP
            System.out.println("MOVE "+moveCounter);
            printMap();
            // CHECK IF WON, THEN RESET
            if(isGoalReached()){
                System.out.println("GOAL REACHED IN "+moveCounter+" MOVES!");
                resetMaze();
                moveCounter=0;
                return;
            }

            // DETERMINE ACTION
            int action = q.getNextAction(getMap());
            System.out.println("MOVING: "+getMoveName(action));
            goToNextState(action);
            moveCounter++;

            // REWARDS AND ADJUSTMENT OF WEIGHTS SHOULD TAKE PLACE HERE
            // +100 for reaching the goal, -1 for every other move (this also penalizes bumping into walls)
            int reward = isGoalReached() ? 100 : -1;
            q.updateQvalue(reward, getMap());


            // COMMENT THE SLEEP FUNCTION IF YOU NEED FAST TRAINING WITHOUT
            // NEEDING TO ACTUALLY SEE IT PROGRESS
            //Thread.sleep(1000);
        }
    }


    /**
     * Q-learning maze-solving testing method
     * @param args
     */
    public static void main(String args[]) {
        QLearning app = new QLearning();
        try{
            app.runLearningLoop();
        } catch (Exception e){
            System.out.println("Thread.sleep interrupted!");
        }
    }
};
