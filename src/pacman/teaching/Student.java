package pacman.teaching;

import pacman.entries.pacman.BasicRLPacMan;
import pacman.entries.pacman.RLPacMan;
import pacman.game.Game;
import pacman.game.Constants.MOVE;

/**
 * Superclass for all student learners.
 */
public class Student extends RLPacMan {

	private BasicRLPacMan teacher; // Gives advice
	private BasicRLPacMan student; // Takes advice
	private TeachingStrategy strategy; // Determines when advice is given
	
	private String initiator; //who is initiating advising opportunities 
	
	private boolean testMode; // When set, will not explore or learn or take advice
	private int adviceCount; // During the last episode
	
	public Student(BasicRLPacMan teacher, BasicRLPacMan student, TeachingStrategy strategy, String initiator) {
		this.teacher = teacher;
		this.student = student;
		this.strategy = strategy;
		this.initiator = initiator;
	}

	/** Prepare for the first move. */
	public void startEpisode(Game game, boolean testMode) {
		this.testMode = testMode;
		adviceCount = 0;
		student.startEpisode(game, testMode);
		
		if (!testMode && strategy.inUse()) {
			strategy.startEpisode();
			teacher.startEpisode(game, true);
		}
	}
	
	/** Choose a move, possibly with advice. */
	public MOVE getMove(Game game, long timeDue) {
		
		MOVE choice = student.getMove(game, timeDue);
		
		if (!testMode && strategy.inUse()) {
			MOVE advice = teacher.getMove(game, timeDue);
			
			if (initiator.equals("teacher"))
			{
				if (strategy.giveAdvice(teacher, choice, advice)) {
					student.setMove(advice);
					adviceCount++;
					return advice;
				}
			}
			if (initiator.equals("student"))
			{
				if (strategy.giveAdvice(student, choice, advice)) {
					student.setMove(advice);
					adviceCount++;
					return advice;
				}				
			}
		}

		return choice;
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
		
		double[] data = new double[extraData.length+1];
		data[0] = adviceCount;
		
		for (int d=0; d<extraData.length; d++)
			data[d+1] = extraData[d];
		
		return data;
	}
}
