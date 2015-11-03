package pacman.entries.pacman;

import java.util.Map;

import pacman.game.Constants.MOVE;

/**
 * Superclass for all basic learners (Q and Sarsa).
 */
public abstract class BasicRLPacMan extends RLPacMan {

	public abstract void setMove(MOVE move);
	public abstract MOVE[] getMoves();
	public abstract double[] getQValues();
	public abstract Map<MOVE,Double> getQValuesDict();
	public abstract FeatureSet getFeatures(MOVE move);
	public abstract void loadPolicy(String filename);
	public abstract double getAvgQdiff();
}
