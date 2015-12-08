package pacman.teaching;

import pacman.Experiments;
import pacman.entries.pacman.BasicRLPacMan;
import pacman.game.Constants.MOVE;
import pacman.utils.Stats;

public class AskAttentionBasedOnCertainty extends AttentionStrategy{

	private int left; // Advice to give
	private int threshold; // Of action uncertainty
	
	public AskAttentionBasedOnCertainty(int threshold) {
		this.left = Experiments.ATTBUDGET;
		this.threshold = threshold;
	}
	
	@Override
	public boolean askForAdvice(IntelligentStudent student) { //TODO: not implemented
//		double[] qvalues = student.
//		double gap = Stats.max(qvalues) - Stats.min(qvalues);
////		System.out.println(gap);
//		boolean uncertain = (gap < threshold);
//		
//		if (uncertain) {
//			left--;
//			return true;
//				
//		}
		
		return false;
	}

	@Override
	public boolean inUse() {
		return (left > 0);
	}

}
