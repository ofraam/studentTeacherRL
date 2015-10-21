package pacman.teaching;

import pacman.Experiments;
import pacman.entries.pacman.BasicRLPacMan;
import pacman.game.Constants.MOVE;
import pacman.utils.Stats;

/**
 * Gives a fixed amount of advice in important states.
 */
public class StudentUncertaintyAdvice extends TeachingStrategy {
	
	private int left; // Advice to give
	private int threshold; // Of action importance
	
	public StudentUncertaintyAdvice(int t) {
		left = Experiments.BUDGET;
		threshold = t;
	}

	/** When the state has widely varying Q-values. */
	public boolean giveAdvice(BasicRLPacMan teacher, MOVE _choice, MOVE _advice) {
		
		double[] qvalues = teacher.getQValues();
		double gap = Stats.max(qvalues) - Stats.min(qvalues);
		boolean important = (gap > threshold);
		
		if (important) {
			left--;
			return true;
		}
		
		return false;
	}
	
	/** Until none left. */
	public boolean inUse() {
		return (left > 0);
	}
}
