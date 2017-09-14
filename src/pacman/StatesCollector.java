package pacman;

import pacman.entries.pacman.BasicRLPacMan;
import pacman.entries.pacman.RLPacMan;
import pacman.game.Game;

import java.util.Collection;

/**
 * Created by danamir on 28/07/2016.
 */
public abstract class StatesCollector {
    protected final int TRAJ_LENGTH = 40;
    public static final int STATES_INTERVAL = 50;


    public abstract Collection<GameState> collectStates(BasicRLPacMan pacman);

}
