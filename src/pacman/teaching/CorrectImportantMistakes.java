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
	private boolean stateImportant;
	private boolean  lastStudentActionCorrect;
		
	public CorrectImportantMistakes(int t) {
		left = Experiments.BUDGET;
		threshold = t;
		stateImportant = false;
		lastStudentActionCorrect = false;
	}

	/** When the state has widely varying Q-values, and the student doesn't take the advice action. */
	public boolean giveAdvice(BasicRLPacMan teacher, MOVE choice, MOVE advice) {
		
		stateImportant = false; //reset importance
		double[] qvalues = teacher.getQValues();
		double gap = Stats.max(qvalues) - Stats.min(qvalues);
		boolean important = (gap > threshold);
		boolean mistake = (choice != advice);
		
		if (important) {
//			System.out.println("teacher important = true");
			stateImportant = true;
			

			if (mistake) {
				left--;
				lastStudentActionCorrect = false;
				return true;
			}
			else
				lastStudentActionCorrect = true;
				
		}
		else
			lastStudentActionCorrect = !mistake;
//		System.out.println("teacher important = false");
		return false;
	}
	
	public boolean lastStateImporant()
	{
		return stateImportant;
	}
	
	/** Until none left. */
	public boolean inUse() {
		return (left > 0);
	}

	@Override
	public boolean lastActionCorrect() {
		return lastStudentActionCorrect;
	}
}
