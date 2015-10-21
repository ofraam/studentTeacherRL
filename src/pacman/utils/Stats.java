package pacman.utils;

/**
 * Statistical utilities.
 */
public class Stats {
	
	/** Compute a t-statistic for two samples that may differ in size and variance. */
	public static double t(double[] x1, double[] x2) {

		int n1 = x1.length;
		int n2 = x2.length;

		double u1 = average(x1);
		double u2 = average(x2);
		
		double v1 = variance(x1, u1);
		double v2 = variance(x2, u2);
		
		return (u1 - u2) / Math.sqrt(v1/n1 + v2/n2);
	}
	
	/** Compute the degrees of freedom for the above t-test. */
	public static double dof(double[] x1, double[] x2) {

		int n1 = x1.length;
		int n2 = x2.length;

		double u1 = average(x1);
		double u2 = average(x2);
		
		double v1 = variance(x1, u1);
		double v2 = variance(x2, u2);
		
		double term1 = v1/n1;
		double term2 = v2/n2;

		return Math.pow(term1+term2, 2) / (term1*term1/(n1-1) + term2*term2/(n2-1));
	}

	/** Average an array of doubles. */
	public static double average(double[] array) {
		double sum = 0;
		for (double x : array) {
			sum += x;
		}
		return sum / array.length;
	}

	/** Estimate variance in an array of doubles. */
	public static double variance(double[] array, double mean) {
		double sum = 0;
		for (double x : array) {
			double v = x - mean;
			sum += v*v;
		}
		return sum / (array.length - 1);
	}
	
	/** Find the minimum in an array of doubles. */
	public static double min(double[] array) {
		double min = array[0];
		for (double x : array) {
			if (x < min)
				min = x;
		}
		return min;
	}
	
	/** Find the maximum in an array of doubles. */
	public static double max(double[] array) {
		double max = array[0];
		for (double x : array) {
			if (x > max)
				max = x;
		}
		return max;
	}
}
