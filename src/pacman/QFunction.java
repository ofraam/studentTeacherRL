package pacman;


/**
 * A linear function of the feature values.
 */
public class QFunction {

	private double[] weights; // Weight vector
	private double bias; // For a constant feature
	
	private double[] eligibility; // Traces
	private double ebias; // For a constant feature
	
	/** All weights and traces start at zero. */
	public QFunction() {
		weights = new double[FeatureSet.SIZE];
		eligibility = new double[FeatureSet.SIZE];
	}
	
	/** Set all traces to zero. */
	public void clearTraces() {
		for (int i=0; i<eligibility.length; i++)
			eligibility[i] = 0;
		ebias = 0;
	}

	/** Compute Q(s,a) given features for s,a. */
	public double evaluate(FeatureSet features) {
		double sum = bias;
		for (int i=0; i<weights.length; i++)
			sum += (features.get(i) * weights[i]); 
		return sum;
	}
	
	/** Increase traces proportional to feature size. */
	public void addTraces(FeatureSet features) {
		for (int i=0; i<eligibility.length; i++)
			eligibility[i] += features.get(i);
		ebias++;
	}

	/** Update weights according to an observation. */
	public void updateWeights(double update) {
		for (int i=0; i<weights.length; i++)
			weights[i] += (update * eligibility[i]);
		bias += (update * ebias);
	}
	
	/** Decrease all traces after a step. */
	public void decayTraces(double decay) {
		for (int i=0; i<eligibility.length; i++)
			eligibility[i] *= decay;
		ebias *= decay;
	}
}
