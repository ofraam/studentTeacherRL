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
	public abstract double[] getVAlues();
	// Same if all values are the same
	public  boolean equals(FeatureSet other) {
		if (this.size() != other.size())
			return false;
		
		for (int i=0; i<this.size(); i++)
			if (this.get(i) != other.get(i))
				return false;
		
		return true;
	}
	
	
	
	public  boolean equals(Object other) {
		FeatureSet otherFeatures = (FeatureSet)other;
		if (this.size() != otherFeatures.size())
			return false;
		
		for (int i=0; i<this.size(); i++)
			if (this.get(i) != otherFeatures.get(i))
				return false;
		
		return true;
	}
	
	public int hashCode()
	{
		int result=0;
		for (int i=0;i<this.size();i++)
		{
			result+=37*(Double.doubleToLongBits(this.get(i)));
		}
//		System.out.println(result);
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i=0;i<this.size();i++)
		{
			sb.append(this.get(i));
			if (i<this.size()-1)
			{
				sb.append(";");
			}
		}
		return ""+sb+"";
	}
}
