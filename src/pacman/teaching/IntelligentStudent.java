package pacman.teaching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.sql.Timestamp;
import pacman.Experiments;
import pacman.controllers.KeyBoardInput;
import pacman.entries.pacman.BasicRLPacMan;
import pacman.entries.pacman.FeatureSet;
import pacman.entries.pacman.QFunction;
import pacman.entries.pacman.RLPacMan;
import pacman.entries.pacman.SarsaPacMan;
import pacman.game.Game;

import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.utils.DataFile;
import pacman.utils.FeatureVectorComparator;
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
	
	private boolean attentionShiftTeacher = false;
	
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
	
	private ArrayList<double[]> visitedStates;
	private HashMap<FeatureSet,Double> visitedKeys;
	private double avgNearestNeighbor;
	private double avgAllDists;
	private double coef; //for methods with coefficient
	
	private double avgQdiffFromPast = 0;
	
	private int totalAttention = 0;
	
	
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
		
		visitedStates = new ArrayList<double[]>();
		visitedKeys = new HashMap<FeatureSet, Double>();
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
		
		visitedStates = new ArrayList<double[]>();
		visitedKeys = new HashMap<FeatureSet, Double>();

	}
	
	public IntelligentStudent(BasicRLPacMan teacher, BasicRLPacMan student, TeachingStrategy strategy, String initiator, String askForAttentionStrategy, boolean teacherRelease) {
		this.teacher = teacher;
		this.student = student;
		this.strategy = strategy;
		this.initiator = initiator;
		this.prototype = student.getPrototype();
		this.askAttention = askForAttentionStrategy;
		this.attentionShiftTeacher=teacherRelease;
		
		
		if (this.askAttention.startsWith("importancePrediction"))
			startPredictions = Integer.parseInt(this.askAttention.substring(20));
		
		
		trainFile = Experiments.DIR+"/importanceClassifier/train";
		modelFile = Experiments.DIR+"/importanceClassifier/model";
		testFile = Experiments.DIR+"/importanceClassifier/test";
		classifyFile = Experiments.DIR+"/importanceClassifier/classify";
		
		priorTrainDataSize = 0;
		
		visitedStates = new ArrayList<double[]>();
		visitedKeys = new HashMap<FeatureSet, Double>();

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
		
		visitedStates = new ArrayList<double[]>();
		visitedKeys = new HashMap<FeatureSet, Double>();

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
		
//		if (!testMode)
//		{
//			this.updateAllDists();
//			this.updateAvgNearestNeighbor();
//		}
		
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
		if (!this.strategy.inUse())
			return false;
		if (this.askAttention.equals("never"))
			return false;
		if (this.askAttention.equals("always"))
			return true;
		if (this.askAttention.startsWith("avgCertainty"))
		{
			coef = Double.parseDouble(this.askAttention.substring(12));
			return isUncertainAvg(coef);
		}
		
		if (this.askAttention.startsWith("avgImportance"))
		{
			coef = Double.parseDouble(this.askAttention.substring(13));
			return isImportantAvg(coef);
		}
			
		
		if (this.askAttention.startsWith("importance"))
		{
			coef = Double.parseDouble(this.askAttention.substring(10));
			return isImportantThreshold(coef);
		}
		if (this.askAttention.startsWith("uncertaintyThreshold"))
		{
			coef = Double.parseDouble(this.askAttention.substring(20));
			return isUncertainThreshold(coef);
		}
//		if (this.askAttention.startsWith("importancePrediction"))
//			return predictedImportanceAsk(game, choice);
		if (this.askAttention.startsWith("unfamiliarNN"))
		{
			coef = Double.parseDouble(this.askAttention.substring(12));
			return isUnfamiliarNN(game,choice, coef);
		}
		
		if (this.askAttention.startsWith("cunfamiliarNN"))
		{
			coef = Double.parseDouble(this.askAttention.substring(13));
			return isUnfamiliarNNandUncertain(game,choice, coef);
		}
		if (this.askAttention.startsWith("unfamiliarPW"))
		{
			coef = Double.parseDouble(this.askAttention.substring(12));
			return isUnfamiliarPW(game,choice,coef);
		}
		if (this.askAttention.startsWith("random"))
		{
			coef = Double.parseDouble(this.askAttention.substring(6));
			return randomAtt(coef);
		}
		return false;
	}
	
	private boolean randomAtt(double threshold)
	{
		
		double rand = Math.random();
		if (rand<(threshold/100))
		{
			return true;
		}
		return false;
	}
	private boolean isUncertainAvg(double threshold)
	{
		double [] qvals = student.getQValues();
		double avgDiff = student.getAvgQdiff();
//		System.out.println("avg diff = "+Double.toString(avgDiff));
		double gap = Stats.max(qvals) - Stats.min(qvals);
//		System.out.println("gap = "+Double.toString(gap));
		if (gap<=avgDiff*threshold)
			return true;
		else
			return false;
	}
	

	private boolean isImportantAvg(double threshold)
	{
		double [] qvals = student.getQValues();
		double avgDiff = student.getAvgQdiff();
		double gap = Stats.max(qvals) - Stats.min(qvals);
		if (gap>avgDiff*threshold)
			return true;
		else
			return false;
	}
	
	private boolean isUncertainThreshold(double threshold)
	{
		double [] qvals = student.getQValues();
		double gap = Stats.max(qvals) - Stats.min(qvals);
		if (gap<threshold)
			return true;
		else
			return false;
	}
	
	private boolean isImportantThreshold(double threshold)
	{
		double [] qvals = student.getQValues();
		double gap = Stats.max(qvals) - Stats.min(qvals);
		if (gap>threshold)
			return true;
		else
			return false;
	}
	
	private boolean isUnfamiliarNNandUncertain(Game game, MOVE choice, double coef)
	{
		FeatureSet stateFeatures = prototype.extract(game, choice);
		double[] state = stateFeatures.getVAlues();
//		FeatureVectorComparator fvc = new FeatureVectorComparator(state);
		if (visitedStates.size()==0)
			return true;
		double dist;
		if (visitedKeys.containsKey(stateFeatures))
		{
			dist = visitedKeys.get(stateFeatures);
//			System.out.println("key there");
		}
		else
		{
			dist = Stats.nearestNeighborDist(this.visitedStates, state);
			visitedKeys.put(stateFeatures, dist);
		}
		
		double [] qvals = student.getQValues();
		double avgDiff = student.getAvgQdiff();
		double gap = Stats.max(qvals) - Stats.min(qvals);
		if (gap<avgDiff)
			{
			if (dist>avgNearestNeighbor*coef)
			{
//				System.out.println("asked");
				return true;
			}
			}
		else
			return false;
		return false;
	}
	
	
	private boolean isUnfamiliarNN(Game game, MOVE choice, double coef)
	{
		FeatureSet stateFeatures = prototype.extract(game, choice);
		double[] state = stateFeatures.getVAlues();
//		FeatureVectorComparator fvc = new FeatureVectorComparator(state);
		if (visitedStates.size()==0)
			return true;
		double dist;
		if (visitedKeys.containsKey(stateFeatures))
		{
			dist = visitedKeys.get(stateFeatures);
//			System.out.println("key there");
		}
		else
		{
			dist = Stats.nearestNeighborDist(this.visitedStates, state);
			visitedKeys.put(stateFeatures, dist);
		}
		
		
		if (dist*coef>avgNearestNeighbor) //TODO: other way around
		{
//			System.out.println("asked");
			return true;
		}
		return false;
	}
	
	
	
	private boolean isUnfamiliarPW(Game game, MOVE choice, double coef)
	{
		double[] state = prototype.extract(game, choice).getVAlues();
		FeatureVectorComparator fvc = new FeatureVectorComparator(state);
		if (visitedStates.size()==0)
			return true;
		Collections.sort(this.visitedStates,fvc);
		double dist = Stats.euclideanDistance(state, this.visitedStates.get(0));
		if (dist>avgAllDists*coef)
		{
			return true;
		}
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
		
		Timestamp start = new Timestamp(System.currentTimeMillis());
		Timestamp currTimestamp;

		episodeLength++;
		boolean ask =false;
		if (!testMode)
			ask = this.askForAttention(game, choice);
		if (!testMode & this.attentionShiftTeacher)
		{
			if (initiated)
				ask = true;
			else
				ask = this.askForAttention(game, choice);
		}

		if (!testMode && strategy.inUse() &&this.totalAttention<Experiments.ATTBUDGET) {
			if (ask)
			{
//				try {
//					System.in.read();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				this.attentionCount++;
				totalAttention++;
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
						FeatureSet state = this.prototype.extract(game, advice);
						if (!visitedKeys.containsKey(state))
						{
							this.visitedStates.add(state.getVAlues());
							this.visitedKeys.put(state, 0.0);
						}
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
						FeatureSet state = this.prototype.extract(game, advice);
						if (!visitedKeys.containsKey(state))
						{
							this.visitedStates.add(state.getVAlues());
							this.visitedKeys.put(state, 0.0);
						}
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
		FeatureSet state = this.prototype.extract(game, choice);
		if (!visitedKeys.containsKey(state))
		{
			this.visitedStates.add(state.getVAlues());
			this.visitedKeys.put(state, 0.0);
		}

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
	
	private void updateAvgNearestNeighbor()
	{

		avgNearestNeighbor = Stats.avgNearestNeighborDist(visitedStates);
	}
	
	private void updateAvgNearestNeighbor(ArrayList<double[]> chosen)
	{

		avgNearestNeighbor = Stats.avgNearestNeighborDist(chosen);
	}
	
	private void updateAllDists()
	{
		avgAllDists = Stats.avgPairwiseDist(visitedStates);
	}
	
	private void updateAllDists(ArrayList<double[]> chosen)
	{
		avgAllDists = Stats.avgPairwiseDist(chosen);
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


	
	public void loadVisitedState(String filename)
	{
		DataFile file = new DataFile(filename+"Avg");
		avgNearestNeighbor=Double.parseDouble(file.nextLine());
		avgAllDists=Double.parseDouble(file.nextLine());

		file.close();
		
		DataFile file2 = new DataFile(filename);
//		int count = 0;
//		double sumQdiffs = 0;
		while (file2.hasNextLine())
		{
			String line = file2.nextLine();
			String[] values = line.split(",");
			double[]vec = new double[values.length];
			
			for (int i=0;i<values.length;i++)
			{
				vec[i]=Double.parseDouble(values[i]);
				
			}
			this.visitedStates.add(vec);
//			System.out.println(Double.toString(Qfunction.evaluate(vec)));
//			sumQdiffs+=Qfunction.evaluate(vec);
//			
//			count++;
			
				
//			System.out.println(vec);
		}
//		this.avgQdiffFromPast=sumQdiffs/count;
		file2.close();
//		System.out.println("start update");
//		this.updateAvgNearestNeighbor();
//		System.out.println("done updating");
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

	@Override
	public void saveStates(String filename, double size) {
		DataFile file = new DataFile(filename);
		file.clear();
		double choose = size/visitedStates.size();
		ArrayList<double[]> chosen= new ArrayList<double[]>();
		for (double[] vec:this.visitedStates)
		{
			if (Math.random()<choose)
			{
				for (int i=0;i<vec.length;i++)
				{
					file.append(Double.toString(vec[i]));
					if (i<vec.length-1)
						file.append(",");
				}
				file.append("\n");
				chosen.add(vec);
			}
		}
		
		file.close();
		
		DataFile file2 = new DataFile(filename+"AvgDist");
		file2.clear();
		System.out.println("start update all pairwise");
		this.updateAllDists(chosen);
		System.out.println("done updating all pairwise");
		System.out.println("start update nearest neighbor");
		this.updateAvgNearestNeighbor(chosen);
		System.out.println("done updating nearest neighbor");
		file2.append(Double.toString(avgNearestNeighbor)+"\n");
		file2.append(Double.toString(avgAllDists)+"\n");
		file2.close();
		
	}
	
	public void saveQdiffsAvg(String filename)
	{
		DataFile file = new DataFile(filename);
		file.clear();
		System.out.println("avg q diff = "+Double.toString(student.getAvgQdiff()));
		file.append(Double.toString(student.getAvgQdiff()));
		file.close();
	}
	
	
}
