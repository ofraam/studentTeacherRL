package pacman.utils;

import java.util.ArrayList;
import java.util.Collections;

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
	
	/** Estimate variance in an array of doubles. */
	public static double variance(double[] array) {
		double mean = Stats.average(array);
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
	
	/** Find the second highest in an array of doubles. */
	public static double second(double[] array) {
		double maxQ = -Integer.MAX_VALUE;
		double secQ = -Integer.MAX_VALUE;
		int maxQInd = 0;
		int secQInd = 0;
		for (int i =0;i<array.length;i++)
		{
			if (array[i]>maxQ)
			{
				maxQ = array[i];
				maxQInd = i;
				
			}
		}
		for (int j = 0;j<array.length;j++)
		{
			if (j!=maxQInd)
			{
				if (array[j]>secQ)
				{
					secQ = array[j];
					secQInd = j;	
				}
			}
		}
		return secQ;
	}
	
	public static double euclideanDistance(double[] x, double[] y){
		double dist = 0;
		double sumSquares = 0;
		for (int i =0;i<x.length;i++)
		{
			sumSquares+=Math.pow((x[i]-y[i]), 2);
		}
		dist = Math.sqrt(sumSquares);
		return dist;
			
	}
	
	public static double range(double[] x)
	{
		return Stats.max(x)-Stats.min(x);
	}
	
	
	public static double nearestNeighborDist(ArrayList<double[]> dataset, double[] vec)
	{
		double minDist = Integer.MAX_VALUE;
		double currDist;
		for (double[] currVec:dataset)
		{
			currDist = Stats.euclideanDistance(currVec, vec);
			if (currDist<minDist & currDist>0)
			{
				minDist = currDist;
			}
		}
		return minDist;
	}
	
	public static double avgNearestNeighborDist(ArrayList<double[]> dataset)
	{
		double avgNNdist = 0;
		double sumNNdist = 0;
		for (double[] vec:dataset)
		{
			sumNNdist+=Stats.nearestNeighborDist(dataset, vec);
		}
		avgNNdist=sumNNdist/dataset.size();
		return avgNNdist;
	}
	
	public static double avgPairwiseDist(ArrayList<double[]> dataset)
	{
		double avgDist = 0;
		double sumDist = 0;
		double comps = 0;
		for (int i =0;i<dataset.size();i++)
		{
			for (int j =i+1;j<dataset.size();j++)
			{
				sumDist+=Stats.euclideanDistance(dataset.get(i), dataset.get(j));
				comps++;
			}
		}
		avgDist=sumDist/comps;
		return avgDist;
	}
	
}
