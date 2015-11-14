package pacman.teaching;

import java.util.LinkedList;

import pacman.Experiments;
import pacman.entries.pacman.BasicRLPacMan;
import pacman.entries.pacman.FeatureSet;
import pacman.entries.pacman.QFunction;
import pacman.entries.pacman.RLPacMan;
import pacman.game.Game;
import pacman.game.Constants.MOVE;
import pacman.utils.SVM;
import pacman.utils.Stats;

/**
 * Superclass for all student learners.
 */
public class IntelligentStudent extends RLPacMan {

	private BasicRLPacMan teacher; // Gives advice
	private BasicRLPacMan student; // Takes advice
	private TeachingStrategy strategy; // Determines when advice is given
	
	private String initiator; //who is initiating advising opportunities 
	private AttentionStrategy attention = null; //whether the student first needs to get teacher's attention
	
	private boolean testMode; // When set, will not explore or learn or take advice
	private int adviceCount; // During the last episode
	private int totalAdvice;
	private int attentionCount;
	private int episodeLength; //how many states visited in episode
	
	private boolean initiated = false; //is asking for advice already "in session"
	private FeatureSet prototype; //type of feature set used.
	
	private LinkedList<String> trainData = new LinkedList<String>(); // For SVM examples
	private String trainFile, modelFile, testFile, classifyFile; // SVM filenames
	private int episode = 0; // Of training
	
	private boolean trained = false;	
	private String askAttention;
	private double uncertaintyThreshold;
	private int startPredictions;
	private int priorTrainDataSize;
	
	private int numPredictedPos = 0;
	private int numPredictedNeg = 0;
	
	private double sumQpos = 0;
	private double sumQneg = 0;
	
	public IntelligentStudent(BasicRLPacMan teacher, BasicRLPacMan student, TeachingStrategy strategy, String initiator) {
		this.teacher = teacher;
		this.student = student;
		this.strategy = strategy;
		this.initiator = initiator;
		this.prototype = student.getPrototype();
		
		trainFile = Experiments.DIR+"/importanceClassifier/train";
		modelFile = Experiments.DIR+"/importanceClassifier/model";
		testFile = Experiments.DIR+"/importanceClassifier/test";
		classifyFile = Experiments.DIR+"/importanceClassifier/classify";
	}
	
	public IntelligentStudent(BasicRLPacMan teacher, BasicRLPacMan student, TeachingStrategy strategy, String initiator, String askForAttentionStrategy) {
		this.teacher = teacher;
		this.student = student;
		this.strategy = strategy;
		this.initiator = initiator;
		this.prototype = student.getPrototype();
		this.askAttention = askForAttentionStrategy;
		
		if (this.askAttention.startsWith("importancePrediction"))
			startPredictions = Integer.parseInt(this.askAttention.substring(20));
		
		trainFile = Experiments.DIR+"/importanceClassifier/train";
		modelFile = Experiments.DIR+"/importanceClassifier/model";
		testFile = Experiments.DIR+"/importanceClassifier/test";
		classifyFile = Experiments.DIR+"/importanceClassifier/classify";
		
		priorTrainDataSize = 0;
	}
	
	public IntelligentStudent(BasicRLPacMan teacher, BasicRLPacMan student, TeachingStrategy strategy, String initiator, AttentionStrategy attention) {
		this.teacher = teacher;
		this.student = student;
		this.strategy = strategy;
		this.initiator = initiator;
		this.attention = attention;
		this.prototype = student.getPrototype();
		
		trainFile = Experiments.DIR+"/importanceClassifier/train";
		modelFile = Experiments.DIR+"/importanceClassifier/model";
		testFile = Experiments.DIR+"/importanceClassifier/test";
		classifyFile = Experiments.DIR+"/importanceClassifier/classify";
	}

	/** Prepare for the first move. */
	public void startEpisode(Game game, boolean testMode) {
		this.testMode = testMode;
		int newTrainingExamples = trainData.size()-priorTrainDataSize;
		priorTrainDataSize = trainData.size();
		if (totalAdvice>0 & newTrainingExamples>10 & this.askAttention.startsWith("importancePrediction")) {
			SVM.trainImportance(trainData, trainFile, modelFile);
//			trainData.clear();
			trained = true;
			}
		adviceCount = 0;
		attentionCount = 0;
		episodeLength = 0;
		student.startEpisode(game, testMode);
		
		if (!testMode && strategy.inUse()) {
			strategy.startEpisode();
			teacher.startEpisode(game, true);
		}
		if (this.attention!=null)
		{
			this.attention.startEpisode();
		}
		
		//re-train SVM if needed
		episode++;
		
		if (sumQpos>0 & sumQneg>0)
			printAvgQsPosNeg();

	}
	
	private boolean askForAttention(Game game, MOVE choice)
	{
		if (this.askAttention.equals("always"))
			return true;
		if (this.askAttention.equals("avgCertainty"))
			return isUncertainAvg();
		if (this.askAttention.equals("uncertaintyThreshold"))
			return isUncertainThreshold(uncertaintyThreshold);
		if (this.askAttention.startsWith("importancePrediction"))
			return predictedImportanceAsk(game, choice);
		return false;
	}
	
	private boolean isUncertainAvg()
	{
		double [] qvals = student.getQValues();
		double avgDiff = student.getAvgQdiff();
		double gap = Stats.max(qvals) - Stats.min(qvals);
		if (gap>avgDiff)
			return true;
		else
			return false;
	}
	
	private boolean isUncertainThreshold(double threshold)
	{
		double [] qvals = student.getQValues();
		double gap = Stats.max(qvals) - Stats.min(qvals);
		if (gap>threshold)
			return true;
		else
			return false;
	}
	
	private boolean predictedImportanceAsk(Game game, MOVE choice)
	{
		if (totalAdvice<startPredictions)
			return true;
		if (this.episode>1 & !testMode & this.trained)
		{
			boolean imp = this.predictImportance(game, choice);
			return imp;
		}
		return false;
	}
	
	/** Choose a move, possibly with advice. */
	public MOVE getMove(Game game, long timeDue) {
		
		MOVE choice = student.getMove(game, timeDue);
//		if (this.episode>1 & !testMode & this.trained)
//		{
//			boolean imp = this.predictImportance(game, choice);
//			System.out.println("student important = "+imp);
//		}
		
		episodeLength++;
		boolean ask = this.askForAttention(game, choice);
//		if (this.attention!=null)
//		{
//			if (initiated)
//				ask = true;
//			else
//				ask = this.attention.askForAdvice(this);
//		}
		if (!testMode && strategy.inUse()) {
			if (ask)
			{
				this.attentionCount++;
				MOVE advice = teacher.getMove(game, timeDue);
			
				if (initiator.equals("teacher"))
				{
					if (strategy.giveAdvice(teacher, choice, advice)) {
						this.initiated = true;
						student.setMove(advice);
						student.recordAdvisedState(game,advice);
						adviceCount++;
						totalAdvice++;
						this.AddImportanceExampleToClassifier(game, choice, true);
						
//						try{
//						System.in.read();
//						}
//						catch(Exception e)
//						{
//							System.out.println("ex");
//						}
						
						return advice;
					}
					else
					{
						this.initiated = false;
						if (trainData.size()<1000)
							this.AddImportanceExampleToClassifier(game, choice, strategy.lastStateImporant());
					}
					
				}
				if (initiator.equals("student"))
				{
					if (strategy.giveAdvice(student, choice, advice)) {
						student.setMove(advice);
						student.recordAdvisedState(game,advice);
						adviceCount++;
						totalAdvice++;
						
	//					try{
	//
	//						System.in.read();
	//					}
	//					catch(Exception e)
	//					{
	//						System.out.println("ex");
	//					}
						
						return advice;
					}	
					else
					{
						this.initiated = false;
						this.AddImportanceExampleToClassifier(game, choice, strategy.lastActionCorrect());
					}
				}
			}
		}
		if (!testMode & trainData.size()<10000)
			this.AddImportanceExampleToClassifier(game,choice, false);
		
		return choice;
	}
	
	/** Record training examples for the SVM to predict state importance. */
	private void AddImportanceExampleToClassifier(Game game,MOVE move, boolean important)
	{
		FeatureSet currentState = prototype.extract(game, move);
		String targetClass = "-1";
		if (important)
			targetClass = "+1";
		trainData.addLast(SVM.exampleImportance(currentState, targetClass)); 
	}
	

	/** Predict the  move the student will make. */
	public boolean predictImportance(Game game, MOVE move) {
		FeatureSet currentState = prototype.extract(game, move);
		String query = SVM.exampleImportance(currentState, "-1");
		double importance = SVM.predictImportance(query, testFile, modelFile, classifyFile);
//		System.out.println(importance);
		if (importance>-1)
		{
			this.numPredictedPos++;
			double[]teacherQvals = teacher.getQValues();
			double impTrue = Stats.max(teacherQvals)-Stats.min(teacherQvals);
			sumQpos+= impTrue;
			return true;
		}
		else
		{
			this.numPredictedNeg++;
			double[]teacherQvals = teacher.getQValues();
			double impTrue = Stats.max(teacherQvals)-Stats.min(teacherQvals);
			sumQneg+=impTrue;
			return false;
		}
	}
	
	private void printAvgQsPosNeg()
	{
		double avgPos = sumQpos/numPredictedPos;
		double avgNeg = sumQneg/numPredictedNeg;
		System.out.println("avg pos = "+avgPos);
		System.out.println("avg neg = "+avgNeg);
	}
	
	/** Prepare for the next move. */
	public void processStep(Game game) {
		student.processStep(game);
		
		if (!testMode && strategy.inUse())
			teacher.processStep(game);
	}
	
	/** Save the current policy to a file. */
	public void savePolicy(String filename) {
		student.savePolicy(filename);
	}

	/** Report amount of advice given in the last episode,
	 *  along with any other data the strategy wants to record. */
	public double[] episodeData() {
		
		double[] extraData = strategy.episodeData();
		
		double[] data = new double[extraData.length+3];
		data[0] = adviceCount;
		data[1] = attentionCount;
		data[2] = episodeLength;
		
		for (int d=0; d<extraData.length; d++)
			data[d+2] = extraData[d];
		
		return data;
	}
}
