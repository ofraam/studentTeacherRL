package pacman.entries.pacman;

import java.awt.AlphaComposite;
import java.util.Collection;
import java.util.Collections;

import pacman.utils.DataFile;

/**
 * A linear function of the feature values.
 */
public class QFunction {

	private double[] weights; // Weight vector
	private double bias; // For a constant feature
	
	private double[] eligibility; // Traces
	private double ebias; // For a constant feature
	
	/** Start with everything at zero. */
	public QFunction(FeatureSet prototype) {
		weights = new double[prototype.size()];
		eligibility = new double[prototype.size()];
	}

	public double[] getWeights()
	{
		return weights;
	}
	
	public double getBias()
	{
		return bias;
	}
	/** Load initial settings from a file. */
	public QFunction(FeatureSet prototype, String filename) {
		this(prototype);
		
		DataFile file = new DataFile(filename);
		bias = Double.parseDouble(file.nextLine());
		for (int i=0; i<weights.length; i++)
			weights[i] = Double.parseDouble(file.nextLine());
		file.close();
	}

	/** Estimate the Q-value given the features for an action. */
	public double evaluate(FeatureSet features) {
		double sum = bias;
		for (int i=0; i<weights.length; i++)
			sum += (features.get(i) * weights[i]); 
		return sum;
	}
	
	public double evaluate(double[] features) {
		double sum = bias;
		for (int i=0; i<weights.length; i++)
			sum += (features[i] * weights[i]); 
		return sum;
	}

	/** Gradient-descent weight update - without eligibility traces. */
	public void updateWeights(double update, FeatureSet features) {
		for (int i=0; i<weights.length; i++)
			weights[i] += (update * features.get(i));
		bias += update;
		
	}
	
	/** Gradient-descent weight update - with eligibility traces. */
	public void updateWeights(double update) {
		for (int i=0; i<weights.length; i++)
		{
			weights[i] += (update * eligibility[i]);
		}
		bias += (update * ebias);		
	}
	
	/** Zero out the eligibility traces. */
	public void clearTraces() {
		for (int i=0; i<eligibility.length; i++)
			eligibility[i] = 0;
		ebias = 0;
	}
	
	/** Decrease the eligibility traces. */
	public void decayTraces(double update) {
		for (int i=0; i<eligibility.length; i++)
			eligibility[i] *= update;
		ebias *= update;
	}
	
	/** Increase the eligibility traces. */
	public void addTraces(FeatureSet features) {
		for (int i=0; i<eligibility.length; i++)
			eligibility[i] += features.get(i);
		ebias++;
	}
	private double sumArray(double[] toSum)
	{
		double sum = 0;
		for (int i = 0;i<toSum.length;i++)
			sum+=toSum[i];
		return sum;
	}
	
	public void maxUpdate(FeatureSet advisedAction, FeatureSet maxAction, double alpha)
	{
//		System.out.println("---before---");
//		System.out.println("max action q-value = "+this.evaluate(maxAction));
//		System.out.println("advised action q-value = "+this.evaluate(advisedAction));
		double diff = this.evaluate(maxAction)-this.evaluate(advisedAction);

//		System.out.println("diff = "+diff);
		double[] newWeights = new double[weights.length];
		for (int i =0;i<weights.length;i++)
		{
			newWeights[i] = weights[i];
			for (int j=0;j<weights.length;j++)
			{
				if (j!=i)
				{
					newWeights[i] = newWeights[i] + alpha * (weights[j]*((maxAction.get(j)-advisedAction.get(j))*(maxAction.get(i)-advisedAction.get(i))));
				}
			}
		}
		double[] weightsBefore = weights;
		weights = newWeights; 
		
//		System.out.println("---after---");
//		System.out.println("max action q-value = "+this.evaluate(maxAction));
//		System.out.println("advised action q-value = "+this.evaluate(advisedAction));
		double diffAfter = this.evaluate(maxAction)-this.evaluate(advisedAction);
//		System.out.println("diffAfter = "+diffAfter);
//		if (Math.abs(diffAfter)>Math.abs(diff))
//			System.out.println("not good!");
//		else
//			System.out.println("good");
//		for (int k = 0;k<weights.length;k++)
//		{
//			weights[k]=newWeights[k];
//		}
	}
	
	public void maxUpdate(double[] advisedAction, double[] maxAction, double alpha)
	{

		double[] newWeights = new double[weights.length];
		for (int i =0;i<weights.length;i++)
		{
			newWeights[i] = weights[i];
			for (int j=0;j<weights.length;j++)
			{
				if (j!=i)
				{
					newWeights[i] = newWeights[i] + alpha * (weights[j]*((maxAction[j]-advisedAction[j])*(maxAction[i]-advisedAction[i])));
				}
			}
		}
		double[] weightsBefore = weights;
		
		double sumWeightsOld = 0;
		for (int k=0;k<weights.length;k++)
			sumWeightsOld+=weights[k];
		
		double sumWeightsNew = 0;
		for (int k=0;k<weights.length;k++)
			sumWeightsNew+=newWeights[k];
		
		for (int i=0;i<weights.length;i++)
		{
			weights[i]=(newWeights[i])*sumWeightsOld/sumWeightsNew;
		}
		
		double sumWeightsAfter = 0;
		for (int k=0;k<weights.length;k++)
			sumWeightsAfter+=weights[k];
//		weights = newWeights; 
		System.out.println(sumWeightsOld==sumWeightsAfter);
		System.out.println(sumWeightsOld-sumWeightsAfter);
	}
	
	public void maxUpdateWrong(FeatureSet advisedAction, FeatureSet maxAction, double alpha)
	{
//		System.out.println("---before---");
//		System.out.println("max action q-value = "+this.evaluate(maxAction));
//		System.out.println("advised action q-value = "+this.evaluate(advisedAction));
		double diff = this.evaluate(maxAction)-this.evaluate(advisedAction);
		this.updateWeights(alpha*diff);
//		System.out.println("diff = "+diff);
//		double[] newWeights = new double[weights.length];
//		for (int i =0;i<weights.length;i++)
//		{
//			newWeights[i] = weights[i];
//			for (int j=0;j<weights.length;j++)
//			{
//				if (j!=i)
//				{
//					newWeights[i] += alpha * (weights[j]*((maxAction.get(j)-advisedAction.get(j))*(maxAction.get(i)-advisedAction.get(i))));
//					
//				}
//			}
//		}
//		weights = newWeights; 
		
//		System.out.println("---after---");
//		System.out.println("max action q-value = "+this.evaluate(maxAction));
//		System.out.println("advised action q-value = "+this.evaluate(advisedAction));
		double diffAfter = this.evaluate(maxAction)-this.evaluate(advisedAction);
//		System.out.println("diffAfter = "+diffAfter);
		if (diffAfter>diff)
			System.out.println("not good!");
		else
			System.out.println("good");
//		for (int k = 0;k<weights.length;k++)
//		{
//			weights[k]=newWeights[k];
//		}
	}
	

	
	/** Save to a file. */
	public void save(String filename) {
		DataFile file = new DataFile(filename);
		file.clear();
		file.append(bias+"\n");
		for (double w : weights)
			file.append(w+"\n");
		file.close();
	}
}
