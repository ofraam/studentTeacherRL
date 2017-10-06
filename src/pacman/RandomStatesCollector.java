package pacman;

import pacman.entries.pacman.BasicRLPacMan;
import pacman.entries.pacman.RLPacMan;
import pacman.game.Constants;
import pacman.game.Game;

import java.util.*;
import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by danamir on 28/07/2016.
 * uses reservoir sampling to get random trajectories
 */
public class RandomStatesCollector extends StatesCollector {
	private int numGames;
    private int numStates;
    private static final int chanceInt = 30000;
    public RandomStatesCollector(int numGames, int numStates) {
    	this.numGames = numGames;
        this.numStates = numStates;
    }


    @Override
    public Collection<GameState> collectStates(BasicRLPacMan pacman) {
    	ArrayList<GameState> randStates = new ArrayList<>();
        Random random = new Random();
        int idx=0;
        for (int i = 1; i < numGames; i++) { //play games
            Game game = new Game(Experiments.rng.nextLong());
            pacman.startEpisode(game, true);
            int length = 0;
            int c = 0;
            
            Queue<String> trajectory = new ArrayBlockingQueue<>(TRAJ_LENGTH);
            GameState lastState = new GameState();

            while (!game.gameOver() & length < 20000) {
                String state = game.getGameState();

                if (trajectory.size() == TRAJ_LENGTH) {
                    trajectory.remove();
                }
                trajectory.offer(state);

                double[] qvalues = pacman.getQValues();
                Arrays.sort(qvalues);
                double gap = qvalues[qvalues.length - 1] - qvalues[qvalues.length - 2];
                if (c > 0) {
                    c--;
                }
                if (c == 40) {
                    lastState.setTrajectory(new ArrayList<>(trajectory));
                }

                Constants.MOVE[] moves = pacman.getMoves();

//                if (randStates.size() < numStates){
                int r = 0;
                if (idx>0)
                {
                	r = random.nextInt(idx)+1; //for reservoir sampling
//                	System.out.println("r = "+r);
                }
                
                if (c == 0) {
                	idx++;
	                if ((randStates.size() < numStates) || (r < numStates)) {
	                    pacman.entries.pacman.FeatureSet[] feat = new pacman.entries.pacman.FeatureSet[moves.length];
	                    for (int j = 0; j < feat.length; j++) {
	                        feat[j] = pacman.getFeatures(moves[j]);
	                    }
	                    GameState gs = new GameState(state, feat, gap);
	                    if (randStates.size()<numStates) {//we are still in first k states, just add
	                    	randStates.add(gs);
//	                    	System.out.println("just adding");
	                    }
	                    else { //need to replace an element
	                    	randStates.set(r, gs);
//	                    	System.out.println("replacing item "+ r + "; idx = "+ idx);
	                    }
	                    c = StatesCollector.STATES_INTERVAL;
	                    lastState = gs;
	                    
	                }
                }

                game.advanceGame(pacman.getMove(game.copy(), -1), Experiments.ghosts.getMove(game.copy(), -1));
                pacman.processStep(game);
                length++;
            }
            if (c>0){
                lastState.setTrajectory(new ArrayList<>(trajectory));

            }
        }
            
        return randStates;
    }
}
