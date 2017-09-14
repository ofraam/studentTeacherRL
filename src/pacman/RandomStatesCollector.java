package pacman;

import pacman.entries.pacman.BasicRLPacMan;
import pacman.entries.pacman.RLPacMan;
import pacman.game.Constants;
import pacman.game.Game;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by danamir on 28/07/2016.
 */
public class RandomStatesCollector extends StatesCollector {
    private int numStates;
    private static final int chanceInt = 30000;
    public RandomStatesCollector(int numStates) {
        this.numStates = numStates;
    }


    @Override
    public Collection<GameState> collectStates(BasicRLPacMan pacman) {
        Collection<GameState> randStates = new ArrayList<>();
        Random random = new Random();

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
//                if (c > 0) {
//                    c--;
//                }
//                if (c == 1) {
//                    lastState.setTrajectory(new ArrayList<>(trajectory));
//                }

                Constants.MOVE[] moves = pacman.getMoves();

                if (randStates.size() < numStates){
//                if (c == 0 && random.nextInt(chanceInt) == 0 && randStates.size() < numStates) {
                    pacman.entries.pacman.FeatureSet[] feat = new pacman.entries.pacman.FeatureSet[moves.length];
                    for (int j = 0; j < feat.length; j++) {
                        feat[j] = pacman.getFeatures(moves[j]);
                    }
                    GameState gs = new GameState(state, feat, gap);
                    randStates.add(gs);
                    c = StatesCollector.STATES_INTERVAL;
                    lastState = gs;
                }

                game.advanceGame(pacman.getMove(game.copy(), -1), Experiments.ghosts.getMove(game.copy(), -1));
                pacman.processStep(game);
                length++;
            }
//            if (c>0){
//                lastState.setTrajectory(new ArrayList<>(trajectory));

//        }
        return randStates;
    }
}