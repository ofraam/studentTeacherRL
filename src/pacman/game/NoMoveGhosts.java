package pacman.game;

import java.util.EnumMap;
import java.util.Random;

import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

/**
 * Usually chase when non-edible and run away when edible.
 * Have some probability of moving randomly in both cases.
 */
public class NoMoveGhosts extends Controller<EnumMap<GHOST,MOVE>>
{
	private Random rng = new Random();
	private static final double CONSISTENCY = 0.8;
	
	private EnumMap<GHOST,MOVE> myMoves = new EnumMap<GHOST,MOVE>(GHOST.class);

	/** Fill all ghost moves. */
	public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
				
		myMoves.clear();

//		for (GHOST ghost : GHOST.values())
//			if (game.doesGhostRequireAction(ghost))
//				myMoves.put(ghost,MOVE.NEUTRAL);

		return myMoves;
	}
	
	/** Fill one ghost move. */
	private MOVE getMove(GHOST ghost, Game game) {
		

		return MOVE.NEUTRAL;
	}
}
