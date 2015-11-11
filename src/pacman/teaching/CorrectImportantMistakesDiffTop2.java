package pacman.teaching;

import java.util.Map;

import pacman.Experiments;
import pacman.entries.pacman.BasicRLPacMan;
import pacman.game.Constants.MOVE;
import pacman.utils.Stats;

/**
 * Gives a fixed amount of advice in important states where the student makes a mistake.
 */
public class CorrectImportantMistakesDiffTop2 extends TeachingStrategy {
	
	private int left; // Advice to give
	private int threshold; // Of mistake importance
	private boolean  lastStudentActionCorrect;
		
	public CorrectImportantMistakesDiffTop2(int t) {
		left = Experiments.BUDGET;
		threshold = t;
		lastStudentActionCorrect = false;
	}

	/** When the state has widely varying Q-values, and the student doesn't take the advice action. */
	public boolean giveAdvice(BasicRLPacMan teacher, MOVE choice, MOVE advice) {
		
//		Map<MOVE,Double> qvalues = teacher.getQValuesDict();
		double[] qvalues = teacher.getQValues();
		double maxQ = -Integer.MAX_VALUE;
		double secQ = -Integer.MAX_VALUE;
		int maxQInd = 0;
		int secQInd = 0;
		for (int i =0;i<qvalues.length;i++)
		{
			if (qvalues[i]>maxQ)
			{
				maxQ = qvalues[i];
				maxQInd = i;
				
			}
		}
		for (int j = 0;j<qvalues.length;j++)
		{
			if (j!=maxQInd)
			{
				if (qvalues[j]>secQ)
				{
					secQ = qvalues[j];
					secQInd = j;	
				}
			}
		}
		
		double gap = qvalues[maxQInd] - qvalues[secQInd];
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean lastStateImporant() {
		// TODO Auto-generated method stub
		return false;
	}
}
