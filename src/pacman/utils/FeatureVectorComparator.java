package pacman.utils;

import java.util.Comparator;

public class FeatureVectorComparator implements Comparator<double []>{
	double[] baseVector;
	
	public FeatureVectorComparator(double [] compareWith)
	{
		baseVector = compareWith;
	}

	@Override
	public int compare(double[] o1, double[] o2) {
		// TODO Auto-generated method stub
		double dist1 = Stats.euclideanDistance(o1, baseVector);
		double dist2 = Stats.euclideanDistance(o2, baseVector);
		
		if (dist1>dist2)
			return 1;
		if (dist1==dist2)
			return 0;
		return -1;
	}

}
