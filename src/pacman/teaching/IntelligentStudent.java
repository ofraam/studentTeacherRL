package pacman.teaching;

import java.util.LinkedList;

import pacman.Experiments;
import pacman.entries.pacman.BasicRLPacMan;
import pacman.entries.pacman.FeatureSet;
import pacman.entries.pacman.RLPacMan;
import pacman.game.Game;
import pacman.game.Constants.MOVE;
import pacman.utils.SVM;

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
	private int episodeLength; //how many states visited in episode
	
	private boolean initiated = false; //is asking for advice already "in session"
	private FeatureSet prototype; //type of feature set used.
	
	private LinkedList<String> trainData = new LinkedList<String>(); // For SVM examples
	private String trainFile, modelFile, testFile, classifyFile; // SVM filenames
	private int episode = 0; // Of training
	
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
		adviceCount = 0;
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
		if (episode > 1) {
			SVM.trainImportance(trainData, trainFile, modelFile);
			trainData.clear();
			}
	}
	
	/** Choose a move, possibly with advice. */
	public MOVE getMove(Game game, long timeDue) {
		
		MOVE choice = student.getMove(game, timeDue);
		episodeLength++;
		boolean ask = true;
		if (this.attention!=null)
		{
			if (initiated)
				ask = true;
			else
				ask = this.attention.askForAdvice(student);
		}
		if (!testMode && strategy.inUse() & ask) {
			MOVE advice = teacher.getMove(game, timeDue);
			
			if (initiator.equals("teacher"))
			{
				if (strategy.giveAdvice(teacher, choice, advice)) {
					this.initiated = true;
					student.setMove(advice);
					adviceCount++;
					
//					try{
//					System.in.read();
//					}
//					catch(Exception e)
//					{
//						System.out.println("ex");
//					}
					
					return advice;
				}
				else
					this.initiated = false;
			}
			if (initiator.equals("student"))
			{
				if (strategy.giveAdvice(student, choice, advice)) {
					student.setMove(advice);
					adviceCount++;
					
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
			}
		}
		
		return choice;
	}
	
	/** Record training examples for the SVM to predict state importance. */
	private void AddImportanceExampleToClassifier(Game game, boolean important)
	{
		FeatureSet currentState = prototype.extract(game, MOVE.NEUTRAL);
		String targetClass = "-1";
		if (important)
			targetClass = "+1";
		trainData.addLast(SVM.exampleImportance(currentState, targetClass)); 
	}
	

	/** Predict the  move the student will make. */
	public boolean predictImportance(Game game) {
		FeatureSet currentState = prototype.extract(game, MOVE.NEUTRAL);
		String query = SVM.exampleImportance(currentState, "-1");
		double importance = SVM.predictImportance(query, testFile, modelFile, classifyFile);
		if (importance>0)
			return true;
		else
			return false;
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
		
		double[] data = new double[extraData.length+2];
		data[0] = adviceCount;
		data[1] = episodeLength;
		
		for (int d=0; d<extraData.length; d++)
			data[d+2] = extraData[d];
		
		return data;
	}
}
