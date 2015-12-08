package pacman.teaching;

import pacman.Experiments;
import pacman.entries.pacman.BasicRLPacMan;
import pacman.game.Constants.MOVE;
import pacman.utils.Stats;

public class AskAttentionAlways extends AttentionStrategy{

	private int left; // Advice to give
	private int threshold; // Of action uncertainty
	
	public AskAttentionAlways(int threshold) {
		this.left = Experiments.ATTBUDGET;
		this.threshold = threshold;
	}
	
	@Override
	public boolean askForAdvice(IntelligentStudent student) {
		left--;
		return true;
	}

	@Override
	public boolean inUse() {
		return (left > 0);
	}

}
