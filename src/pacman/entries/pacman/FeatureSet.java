package pacman.entries.pacman;

import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Superclass for feature sets.
 */
public abstract class FeatureSet {

	public abstract int size();
	public abstract double get(int i);
	public abstract FeatureSet extract(Game game, MOVE move);
	
	// Same if all values are the same
	public boolean equals(FeatureSet other) {
		if (this.size() != other.size())
			return false;
		
		for (int i=0; i<this.size(); i++)
			if (this.get(i) != other.get(i))
				return false;
		
		return true;
	}
}
