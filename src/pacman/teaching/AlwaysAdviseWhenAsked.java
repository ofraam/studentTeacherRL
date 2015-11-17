package pacman.teaching;

import pacman.Experiments;
import pacman.entries.pacman.BasicRLPacMan;
import pacman.game.Constants.MOVE;

/**
 * Gives a fixed amount of front-loaded advice.
 */
public class AlwaysAdviseWhenAsked extends TeachingStrategy {
	
	private int left; // Advice to give
	
	public AlwaysAdviseWhenAsked() {
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

	@Override
	public boolean lastActionCorrect() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean lastStateImporant() {
		// TODO Auto-generated method stub
		return false;
	}
}
