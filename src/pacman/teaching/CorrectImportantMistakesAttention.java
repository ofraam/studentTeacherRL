package pacman.teaching;

import pacman.Experiments;
import pacman.entries.pacman.BasicRLPacMan;
import pacman.game.Constants.MOVE;
import pacman.utils.Stats;

/**
 * Gives a fixed amount of advice in important states where the student makes a mistake.
 */
public class CorrectImportantMistakesAttention extends TeachingStrategy {
	
	private int left; // Advice to give
	private double attention; // Of mistake importance
	private int threshold = 200;
	private boolean  lastStudentActionCorrect;
		
	public CorrectImportantMistakesAttention(int att, int t) {
		left = Experiments.BUDGET;
		attention = att/100.0;
		threshold = t;
		lastStudentActionCorrect = false;
	}

	/** When the state has widely varying Q-values, and the student doesn't take the advice action. */
	public boolean giveAdvice(BasicRLPacMan teacher, MOVE choice, MOVE advice) {
		
		if (attention<Math.random())
			return false;
		
		double[] qvalues = teacher.getQValues();
		double gap = Stats.max(qvalues) - Stats.min(qvalues);
		boolean important = (gap > threshold);

		if (important) {
		
			boolean mistake = (choice != advice);

			if (mistake) {
				left--;
				lastStudentActionCorrect = false;
				return true;
			}
			else
				lastStudentActionCorrect = true;
		}
		
		return false;
	}
	
	/** Until none left. */
	public boolean inUse() {
		return (left > 0);
	}

	@Override
	public boolean lastActionCorrect() {
		return lastStudentActionCorrect;
	}

	@Override
	public boolean lastStateImporant() {
		// TODO Auto-generated method stub
		return false;
	}
}
