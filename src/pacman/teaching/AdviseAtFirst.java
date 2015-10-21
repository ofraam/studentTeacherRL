package pacman.teaching;

import pacman.Experiments;
import pacman.entries.pacman.BasicRLPacMan;
import pacman.game.Constants.MOVE;

/**
 * Gives a fixed amount of front-loaded advice.
 */
public class AdviseAtFirst extends TeachingStrategy {
	
	private int left; // Advice to give
	
	public AdviseAtFirst() {
		left = Experiments.BUDGET;
	}

	/** When there's some left. */
	public boolean giveAdvice(BasicRLPacMan teacher, MOVE _choice, MOVE _advice) {
		left--;
		return true;
	}
	
	/** Until none left. */
	public boolean inUse() {
		return (left > 0);
	}
}
