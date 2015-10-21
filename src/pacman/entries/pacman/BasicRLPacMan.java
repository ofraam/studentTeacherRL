package pacman.entries.pacman;

import pacman.game.Constants.MOVE;

/**
 * Superclass for all basic learners (Q and Sarsa).
 */
public abstract class BasicRLPacMan extends RLPacMan {

	public abstract void setMove(MOVE move);
	public abstract MOVE[] getMoves();
	public abstract double[] getQValues();
	public abstract FeatureSet getFeatures(MOVE move);
	public abstract void loadPolicy(String filename);
}
