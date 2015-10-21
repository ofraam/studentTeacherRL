package pacman.teaching;

import java.util.LinkedList;

import pacman.Experiments;
import pacman.entries.pacman.BasicRLPacMan;
import pacman.entries.pacman.FeatureSet;
import pacman.game.Constants.MOVE;
import pacman.utils.SVM;
import pacman.utils.Stats;

/**
 * Gives a fixed amount of advice in important states where we predict the student will make a mistake.
 */
public class PredictImportantMistakes extends TeachingStrategy {

	private int left; // Advice to give
	private int threshold; // Of mistake importance
	private int episode; // Of training
	
	private LinkedList<String> trainData = new LinkedList<String>(); // For SVM examples
	private String trainFile, modelFile, testFile, classifyFile; // SVM filenames
	private int right, wrong, truePos, falseNeg, falsePos; // In SVM predictions during the last episode

	public PredictImportantMistakes(int t) {
		left = Experiments.BUDGET;
		threshold = t;
		
		trainFile = Experiments.DIR+"/predict"+threshold+"/train";
		modelFile = Experiments.DIR+"/predict"+threshold+"/model";
		testFile = Experiments.DIR+"/predict"+threshold+"/test";
		classifyFile = Experiments.DIR+"/predict"+threshold+"/classify";
	}

	/** When the state has widely varying Q-values, and we predict the student won't take the advice action. */
	public boolean giveAdvice(BasicRLPacMan teacher, MOVE choice, MOVE advice) {
		
		// Can't predict before the first episode
		if (episode > 1) {
			
			double[] qvalues = teacher.getQValues();
			double gap = Stats.max(qvalues) - Stats.min(qvalues);
			boolean important = (gap > threshold);
	
			if (important) {
				
				MOVE guess = predictChoice(teacher);
				boolean predict = (guess != advice);
				boolean mistake = (choice != advice);

				// Bookkeeping
				if (guess == choice)
					right++;
				else
					wrong++;

				// Bookkeeping
				if (mistake && predict)
					truePos++;
				else if (mistake && !predict)
					falseNeg++;
				else if (!mistake && predict)
					falsePos++;

				if (predict) {
					left--;
					return true;
				}
			}
		}

		// If not advising, create a training example
		recordExample(teacher, choice);
		return false;
	}

	/** Until none left. */
	public boolean inUse() {
		return (left > 0);
	}

	/** Build a new predictor. */
	public void startEpisode() {
		episode++;
		
		if (episode > 1) {
			
			SVM.train(trainData, trainFile, modelFile);
			trainData.clear();
			
			right = wrong = truePos = falseNeg = falsePos = 0;
		}
	}
	
	/** Put extra data into the learning curve. */
	public double[] episodeData() {
		double[] data = {right, wrong, truePos, falseNeg, falsePos};
		if (!inUse())
			right = wrong = truePos = falseNeg = falsePos = 0;
		return data;
	}
	
	/** Record training examples for the SVM to predict student choices. */
	public void recordExample(BasicRLPacMan teacher, MOVE choice) {
		
		MOVE[] moves = teacher.getMoves();
		FeatureSet[] features = new FeatureSet[moves.length];
		for (int i=0; i<moves.length; i++)
			features[i] = teacher.getFeatures(moves[i]);
		
		int choiceIndex = -1;
		for (int i=0; i<moves.length; i++)
			if (moves[i] == choice)
				choiceIndex = i;
		
		trainData.addLast(SVM.example(features, choiceIndex));
	}
	
	/** Predict the  move the student will make. */
	public MOVE predictChoice(BasicRLPacMan teacher) {
		
		MOVE[] moves = teacher.getMoves();
		FeatureSet[] features = new FeatureSet[moves.length];
		for (int i=0; i<moves.length; i++)
			features[i] = teacher.getFeatures(moves[i]);
		
		String query = SVM.example(features, -1);
		double[] ranks = SVM.rank(query, moves.length, testFile, modelFile, classifyFile);
		
		int maxIndex = 0;
		for (int i=0; i<moves.length; i++)
			if (ranks[i] > ranks[maxIndex])
				maxIndex = i;
		
		return moves[maxIndex];
	}
}
