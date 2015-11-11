package pacman.teaching;

import pacman.entries.pacman.BasicRLPacMan;
import pacman.game.Constants.MOVE;

/**
 * Determines whether advice is given.
 */
public abstract class AttentionStrategy {
	
	public abstract boolean askForAdvice(IntelligentStudent student);
	public abstract boolean inUse();
	
	public void startEpisode() {} // Override to do start-of-episode stuff
		
	public double[] episodeData() { // Override to add data to learning curves
		double[] data = new double[0];
		return data;
	}
}
