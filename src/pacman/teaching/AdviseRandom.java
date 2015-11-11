package pacman.teaching;

import java.util.Random;

import pacman.Experiments;
import pacman.entries.pacman.BasicRLPacMan;
import pacman.game.Constants.MOVE;
import pacman.utils.Stats;

/**
 * Gives a fixed amount of advice in important states.
 */
public class AdviseRandom extends TeachingStrategy {
	
	private int left; // Advice to give
	private double adviceProbability; // Of random intervention
	
	public AdviseRandom(int t) {
		left = Experiments.BUDGET;
		adviceProbability = t/100.0;
	}

	/** random decision. */
	public boolean giveAdvice(BasicRLPacMan teacher, MOVE _choice, MOVE _advice) {
		Random rand = new Random();
		double value = rand.nextDouble();

		if (value<adviceProbability) {
			left--;
			return true;
		}
		
		return false;
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
