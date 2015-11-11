package pacman.teaching;

import java.util.Map;

import pacman.Experiments;
import pacman.entries.pacman.BasicRLPacMan;
import pacman.game.Constants.MOVE;
import pacman.utils.Stats;

/**
 * Gives a fixed amount of advice in important states where the student makes a mistake.
 */
public class CorrectImportantMistakesDiffStudent extends TeachingStrategy {
	
	private int left; // Advice to give
	private int threshold; // Of mistake importance
	private boolean  lastStudentActionCorrect;
		
	public CorrectImportantMistakesDiffStudent(int t) {
		left = Experiments.BUDGET;
		threshold = t;
		lastStudentActionCorrect = false;
	}

	/** When the state has widely varying Q-values, and the student doesn't take the advice action. */
	public boolean giveAdvice(BasicRLPacMan teacher, MOVE choice, MOVE advice) {
		
		Map<MOVE,Double> qvalues = teacher.getQValuesDict();
		
		double gap = qvalues.get(advice) - qvalues.get(choice);
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
