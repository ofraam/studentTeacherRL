package pacman.teaching;

import pacman.Experiments;
import pacman.entries.pacman.BasicRLPacMan;
import pacman.game.Constants.MOVE;
import pacman.utils.Stats;

/**
 * Gives a fixed amount of advice in important states where the student makes a mistake.
 */
public class CorrectImportantMistakes extends TeachingStrategy {
	
	private int left; // Advice to give
	private int threshold; // Of mistake importance
		
	public CorrectImportantMistakes(int t) {
		left = Experiments.BUDGET;
		threshold = t;
	}

	/** When the state has widely varying Q-values, and the student doesn't take the advice action. */
	public boolean giveAdvice(BasicRLPacMan teacher, MOVE choice, MOVE advice) {
		
		double[] qvalues = teacher.getQValues();
		double gap = Stats.max(qvalues) - Stats.min(qvalues);
		boolean important = (gap > threshold);

		if (important) {
		
			boolean mistake = (choice != advice);

			if (mistake) {
				left--;
				return true;
			}
		}
		
		return false;
	}
	
	/** Until none left. */
	public boolean inUse() {
		return (left > 0);
	}
}
