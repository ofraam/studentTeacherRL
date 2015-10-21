package pacman;

import java.util.Random;

import pacman.game.Game;
import pacman.game.Constants.MOVE;

/**
 * A Q-learning agent that uses function approximation and eligibility traces.
 */
public class Agent {
	
	private QFunction Qfunction = new QFunction();
	private Random rng = new Random();

	private double epsilon = 0.05; // Exploration rate
	private double alpha = 0.001; // Learning rate
	private double gamma = 0.999; // Reward discount rate
	private double lambda = 0.9; // Eligibility decay rate
	
	/** Turns off exploration for watching. */
	public void stopExploring() {
		epsilon = 0;
	}

	/** Sets all eligibility traces to zero before beginning an episode. */
	public void startEpisode() {
		Qfunction.clearTraces();
	}
	
	/** Selects an action with an e-greedy strategy. */
	public MOVE chooseAction(Game game, MOVE[] actions) {	
		
		if (rng.nextDouble() < epsilon) {
			Qfunction.clearTraces();
			return actions[rng.nextInt(actions.length)];
		}
		else
			return policy(game, actions);
	}
	
	/** Selects the action the agent currently thinks is best. */
	public MOVE policy(Game s, MOVE[] actions) {
		
		FeatureSet[] features = new FeatureSet[actions.length];
		for (int i=0; i<actions.length; i++)
			features[i] = new FeatureSet(s, actions[i]);
		
		double[] qvalues = new double[actions.length];
		for (int i=0; i<actions.length; i++)
			qvalues[i] = Qfunction.evaluate(features[i]);

		int bestActionIndex = 0;
		for (int i=0; i<actions.length; i++)
			if (qvalues[i] > qvalues[bestActionIndex])
				bestActionIndex = i;
		
		Qfunction.addTraces(features[bestActionIndex]);
		return actions[bestActionIndex];
	}

	/** Update the Q-function according to this observation. */
	public void learn(Game s, MOVE a, Game sp, double r, MOVE[] nextActions) {
		
		FeatureSet[] features = new FeatureSet[nextActions.length];
		for (int i=0; i<nextActions.length; i++)
			features[i] = new FeatureSet(sp, nextActions[i]);
		
		double[] qvalues = new double[nextActions.length];
		for (int i=0; i<nextActions.length; i++)
			qvalues[i] = Qfunction.evaluate(features[i]);
		
		double maxNextQ = 0;
		for (int i=0; i<nextActions.length; i++)
			if (qvalues[i] > maxNextQ)
				maxNextQ = qvalues[i];

		double oldQ = Qfunction.evaluate(new FeatureSet(s,a));
		double delta = r - oldQ + gamma*maxNextQ;
		
		Qfunction.updateWeights(alpha*delta);
		Qfunction.decayTraces(gamma*lambda);
	}
}
