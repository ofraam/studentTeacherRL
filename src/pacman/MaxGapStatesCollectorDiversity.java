package pacman;

import pacman.entries.pacman.*;
import pacman.entries.pacman.FeatureSet;
import pacman.entries.pacman.QFunction;
import pacman.game.Constants;
import pacman.game.Game;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by danamir on 28/07/2016.
 */
public class MaxGapStatesCollectorDiversity extends StatesCollector {
    private int numGames;
    private int numStates;

    public MaxGapStatesCollectorDiversity(int numGames, int numStates) {
        this.numGames = numGames;
        this.numStates = numStates;
    }


    @Override
    public Collection<GameState> collectStates(BasicRLPacMan pacman) {
//        PriorityQueue<GameState> bestStates = new PriorityQueue<>(numStates, new GapComparator());
        ArrayList<GameState> bestStates = new ArrayList<>();
        for (int i = 1; i < numGames; i++) { //play games
            Game game = new Game(Experiments.rng.nextLong());
            pacman.startEpisode(game, true);
            int length = 0;
            int c = 0;
            Queue<String> trajectory = new ArrayBlockingQueue<>(TRAJ_LENGTH);
            GameState lastState = new GameState();

            while (!game.gameOver() & length < 20000) { //play a single game
                String state = game.getGameState();

                if (trajectory.size() == TRAJ_LENGTH) {
                    trajectory.remove();
                }
                trajectory.offer(state);

                double[] qvalues = pacman.getQValues();
                Arrays.sort(qvalues);
                
                
                double gap = qvalues[qvalues.length - 1] - qvalues[0];
                if (c > 0) {
                    c--;
                }
                if (c == 40) {
                    lastState.setTrajectory(new ArrayList<>(trajectory));
                }

                Constants.MOVE[] moves = pacman.getMoves();


                if (c == 0) {
                	pacman.entries.pacman.FeatureSet fsBest = pacman.getFeatures(pacman.getMove(game.copy(), -1));
                    pacman.entries.pacman.FeatureSet[] feat = new pacman.entries.pacman.FeatureSet[1];
//                    for (int j = 0; j < feat.length; j++) {
                    feat[0] = fsBest;
                    
                    
//                    }
                    GameState gs = new GameState(state, feat, gap);
                    
                    if (bestStates.size()<numStates)
                    	bestStates.add(gs);
                    else {
                    	int idx = checkReplaceState(bestStates, gs);
                    }
                    c = StatesCollector.STATES_INTERVAL;
                    lastState = gs;
                }

                game.advanceGame(pacman.getMove(game.copy(), -1), Experiments.ghosts.getMove(game.copy(), -1));
                pacman.processStep(game);
                length++;
            }
            if (c>0){
                lastState.setTrajectory(new ArrayList<>(trajectory));
            }
        }
        return bestStates;
    }
    
    public static int checkReplaceState(ArrayList<GameState> states, GameState newState) {
    	int idx = 0;
		FeatureSet[] newStateFeatures = newState.getFeatures();
		FeatureSet newfs = newStateFeatures[0];
		double minDist = 1000000;
    	for (int i=0;i<states.size();i++) {
    		FeatureSet[] currStateFeatures = states.get(i).getFeatures();
    		FeatureSet currfs = currStateFeatures[0];
    		double dist = 0;
    		double sumSquares = 0;
    		for (int j =0;j<currfs.size();j++)
    		{
    			sumSquares+=Math.pow((currfs.get(j)-newfs.get(j)), 2);
    		}
    		dist = Math.sqrt(sumSquares);
    		if (dist<minDist) {
    			minDist = dist;
    			idx = i;
    		}
    	}
    	
    	if (states.get(idx).getQgap()<newState.getQgap()) {
    		states.set(idx, newState);
//    		System.out.println("replaced state!");
    		return idx;
    	}
    	
    	return -1;
    	
    	
    }
    
    
}
