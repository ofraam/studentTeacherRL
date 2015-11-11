package pacman.teaching;

import pacman.Experiments;
import pacman.entries.pacman.BasicRLPacMan;
import pacman.game.Constants.MOVE;
import pacman.utils.Stats;

/**
 * Asks for a fixed amount of advice in uncertain states.
 */
public class StudentUncertaintyAndMistakeAdvice extends TeachingStrategy {
	
	private int left; // Advice to give
	private int threshold; // Of action uncertainty
	private boolean lastStudentActionCorrect;
	
	public StudentUncertaintyAndMistakeAdvice(int t) {
		left = Experiments.BUDGET;
		threshold = t;
		lastStudentActionCorrect=false;
	}

	/** When the state has widely varying Q-values. */
	public boolean giveAdvice(BasicRLPacMan student, MOVE _choice, MOVE _advice) {
		
		double[] qvalues = student.getQValues();
		double gap = Stats.max(qvalues) - Stats.min(qvalues);
//		System.out.println(gap);
		boolean uncertain = (gap < threshold);
		
		if (uncertain) {
			
			boolean mistake = (_choice != _advice);

			if (mistake) {
				left--;
				lastStudentActionCorrect=false;
				return true;
			}	
			else
				lastStudentActionCorrect=true;

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
