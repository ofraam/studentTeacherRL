package pacman.teaching;

import java.io.IOException;

import pacman.Experiments;
import pacman.entries.pacman.BasicRLPacMan;
import pacman.game.Constants.MOVE;
import pacman.utils.Stats;

/**
 * Gives a fixed amount of advice in important states.
 */
public class AdviseImportantStates extends TeachingStrategy {
	
	private int left; // Advice to give
	private int threshold; // Of action importance
	
	public AdviseImportantStates(int t) {
		left = Experiments.BUDGET;
		threshold = t;
	}

	/** When the state has widely varying Q-values. */
	public boolean giveAdvice(BasicRLPacMan teacher, MOVE _choice, MOVE _advice) {
		
		double[] qvalues = teacher.getQValues();
		double gap = Stats.max(qvalues) - Stats.min(qvalues);
		boolean important = (gap > threshold);
		
		if (important) {
//			System.out.println("teacher important = true");
//			try {
//				System.in.read();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			left--;
			return true;
		}
//		System.out.println("teacher important = false");
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
