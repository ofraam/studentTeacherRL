package pacman.teaching;

import java.util.Random;

import pacman.Experiments;
import pacman.entries.pacman.BasicRLPacMan;
import pacman.game.Constants.MOVE;
import pacman.utils.Stats;

/**
 * Gives a fixed amount of advice in important states where the student makes a mistake.
 */
public class CorrectMistakesRandomly extends TeachingStrategy {
	
	private int left; // Advice to give
	private double correctProb; // Of mistake importance
		
	public CorrectMistakesRandomly(int t) {
		left = Experiments.BUDGET;
		correctProb = t/100.0;
	}

	/** When the state has widely varying Q-values, and the student doesn't take the advice action. */
	public boolean giveAdvice(BasicRLPacMan teacher, MOVE choice, MOVE advice) {
		Random rand = new Random();
		double value = rand.nextDouble();
		
		if (value<correctProb) {
		
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
