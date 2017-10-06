package pacman;

import pacman.entries.pacman.*;
import pacman.entries.pacman.QFunction;
import pacman.game.Constants;
import pacman.game.Game;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by danamir on 28/07/2016.
 */
public class FirstStatesCollector extends StatesCollector {
    private int numGames;
    private int numStates;

    public FirstStatesCollector(int numGames, int numStates) {
        this.numGames = numGames;
        this.numStates = numStates;
    }


    @Override
    public Collection<GameState> collectStates(BasicRLPacMan pacman) {
        ArrayList<GameState> firstStates = new ArrayList<>();

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
                	if (firstStates.size()==numStates)
                		return firstStates;
                }

                Constants.MOVE[] moves = pacman.getMoves();


                if (c == 0 && firstStates.size() < numStates) {
                    pacman.entries.pacman.FeatureSet[] feat = new pacman.entries.pacman.FeatureSet[moves.length];
                    for (int j = 0; j < feat.length; j++) {
                        feat[j] = pacman.getFeatures(moves[j]);
                    }
                    GameState gs = new GameState(state, feat, gap);
//                    if (firstStates.size() == numStates) {
//                        firstStates.poll();
//                    }
                    firstStates.add(gs);
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
        return firstStates;
    }
}
