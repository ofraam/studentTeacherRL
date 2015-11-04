package pacman.teaching;

import pacman.Experiments;
import pacman.entries.pacman.BasicRLPacMan;
import pacman.game.Constants.MOVE;
import pacman.utils.Stats;

/**
 * Asks for a fixed amount of advice in uncertain states.
 */
public class StudentPercentileUncertaintyAndMistakeAdvice extends TeachingStrategy {
	
	private int left; // Advice to give
	private int cutoff;
	
	public StudentPercentileUncertaintyAndMistakeAdvice(int c) {
		left = Experiments.BUDGET;
		cutoff = c;
	}

	/** When the state has widely varying Q-values. */
	public boolean giveAdvice(BasicRLPacMan student, MOVE _choice, MOVE _advice) {
		
		double[] qvalues = student.getQValues();
		double gap = Stats.max(qvalues) - Stats.min(qvalues);
//		System.out.println(gap);
		boolean uncertain = (gap < student.getNthQvalue(cutoff));
		
		if (uncertain) {
			
			boolean mistake = (_choice != _advice);

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
