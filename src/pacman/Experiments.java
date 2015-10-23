package pacman;

import static pacman.game.Constants.DELAY;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;

import pacman.entries.ghosts.StandardGhosts;
import pacman.entries.pacman.BasicRLPacMan;
import pacman.entries.pacman.CustomFeatureSet;
import pacman.entries.pacman.DepthFeatureSet;
import pacman.entries.pacman.FeatureSet;
import pacman.entries.pacman.QPacMan;
import pacman.entries.pacman.RLPacMan;
import pacman.entries.pacman.SarsaPacMan;
import pacman.game.Game;
import pacman.game.GameView;
import pacman.game.Constants.MOVE;
import pacman.teaching.AdviseAtFirst;
import pacman.teaching.AdviseImportantStates;
import pacman.teaching.AdviseRandom;
import pacman.teaching.CorrectImportantMistakes;
import pacman.teaching.CorrectMistakesRandomly;
import pacman.teaching.PredictImportantMistakes;
import pacman.teaching.Student;
import pacman.teaching.StudentImportanceAndMistakeAdvice;
import pacman.teaching.StudentUncertaintyAdvice;
import pacman.teaching.StudentUncertaintyAndMistakeAdvice;
import pacman.teaching.TeachingStrategy;
import pacman.utils.DataFile;
import pacman.utils.LearningCurve;
import pacman.utils.Stats;

public class Experiments {
	
	public static String TEACHER = "customS"; // Teacher feature set and algorithm
	public static String STUDENT = "customS"; // Student feature set and algorithm
	public static String DIR = "OfraData/"+TEACHER+"/"+STUDENT; // Where to store data
	
	
	public static int BUDGET = 1000; // Advice budget (1000)
	public static int REPEATS = 3; // Curves to average (30)
	public static int LENGTH = 10; // Points per curve (100)
	public static int TEST = 15; // Test episodes per point (30)
	public static int TRAIN = 50; // Train episodes per point

	public static Random rng = new Random();
	public static StandardGhosts ghosts = new StandardGhosts();

	/**
	 * Run experiments.
	 */
	public static void main(String[] args) {


//		watch(create("advise100"));
//		rng = new Random(111);
//		train("cstuimp150", 0, "student");
//		rng = new Random(111);
//		train("correct200", 0, "teacher");
		watch(create("independent", "teacher"));
//		plotGapsWatch();
	}

	/** Set up a learner. */
	public static RLPacMan create(String learner, String initiator) {
		
		FeatureSet teacherProto = TEACHER.startsWith("custom") ? new CustomFeatureSet() : new DepthFeatureSet();
		FeatureSet studentProto = STUDENT.startsWith("custom") ? new CustomFeatureSet() : new DepthFeatureSet();

		// Lone teacher
		if (learner.startsWith("teacher")) {
			BasicRLPacMan teacher = TEACHER.endsWith("S") ? new SarsaPacMan(teacherProto) : new QPacMan(teacherProto);
			teacher.loadPolicy("myData/"+TEACHER+"/teacher/policy");
			return teacher;
		}
			
		// Lone student
		else if (learner.startsWith("independent")) {
			return STUDENT.endsWith("S") ? new SarsaPacMan(studentProto) : new QPacMan(studentProto);
		}
		
		// Student-teacher pair
		else {
			BasicRLPacMan student = STUDENT.endsWith("S") ? new SarsaPacMan(studentProto) : new QPacMan(studentProto);
			BasicRLPacMan teacher = TEACHER.endsWith("S") ? new SarsaPacMan(teacherProto) : new QPacMan(teacherProto);
			teacher.loadPolicy("myData/"+TEACHER+"/teacher/policy");
			
			// Front-load the advice budget
			if (learner.startsWith("baseline")) {
				TeachingStrategy strategy = new AdviseAtFirst();
				return new Student(teacher, student, strategy, initiator);
			}
			
			// Advise in important states
			if (learner.startsWith("advise")) {
				int threshold = Integer.parseInt(learner.substring(6));
				TeachingStrategy strategy = new AdviseImportantStates(threshold);
				return new Student(teacher, student, strategy, initiator);
			}
			
			// Correct important mistakes
			if (learner.startsWith("correct")) {
				int threshold = Integer.parseInt(learner.substring(7));
				TeachingStrategy strategy = new CorrectImportantMistakes(threshold);
				return new Student(teacher, student, strategy, initiator);
			}

			
			// Advise in important states with predicted mistakes
			if (learner.startsWith("predict")) {
				int threshold = Integer.parseInt(learner.substring(7));
				TeachingStrategy strategy = new PredictImportantMistakes(threshold);
				return new Student(teacher, student, strategy, initiator);
			}
			
			// Advise randomly
			if (learner.startsWith("random")) {
				int prob = Integer.parseInt(learner.substring(6));
				TeachingStrategy strategy = new AdviseRandom(prob);
				return new Student(teacher, student, strategy, initiator);
			}
			
			// Correct mistakes randomly
			if (learner.startsWith("crandom")) {
				int prob = Integer.parseInt(learner.substring(7));
				TeachingStrategy strategy = new CorrectMistakesRandomly(prob);
				return new Student(teacher, student, strategy, initiator);
			}
			
			//Student initiated advice based on uncertainty (low q-value diff)
			if (learner.startsWith("stuunc")) {
				int threshold = Integer.parseInt(learner.substring(6));
				TeachingStrategy strategy = new StudentUncertaintyAdvice(threshold);
				return new Student(teacher, student, strategy, initiator);
			}	
			
			//Student initiated advice based on uncertainty (low q-value diff), only use advice if student was wrong
			if (learner.startsWith("cstuunc")) {
				int threshold = Integer.parseInt(learner.substring(7));
				TeachingStrategy strategy = new StudentUncertaintyAndMistakeAdvice(threshold);
				return new Student(teacher, student, strategy, initiator);
			}	
			
			//Student initiated advice based on importance (high q-value diff), only use advice if student was wrong
			if (learner.startsWith("cstuimp")) {
				int threshold = Integer.parseInt(learner.substring(7));
				TeachingStrategy strategy = new StudentImportanceAndMistakeAdvice(threshold);
				return new Student(teacher, student, strategy, initiator);
			}
		}
		
		return null;
	}
	
	/** Generate learning curves. */
	public static void train(String learner, int start, String initiator) {
		
		// Make sure directory exists
		File file = new File(DIR+"/"+learner);
		Boolean created = false;
		if (!file.exists())
			created = file.mkdir();
			System.out.println(created);
		
		// Load old curves
		LearningCurve[] curves = new LearningCurve[REPEATS];
		for (int i=0; i<start; i++)
			curves[i] = new LearningCurve(LENGTH+1, TRAIN, DIR+"/"+learner+"/curve"+i);
		
		// Begin new curves
		for (int i=start; i<REPEATS; i++) {
			curves[i] = new LearningCurve(LENGTH+1, TRAIN);
			
			System.out.println("Training "+DIR+"/"+learner+" "+i+"...");
			RLPacMan pacman = create(learner,initiator);
			
			// First point
			double[] initialData = pacman.episodeData();
			double initialScore = evaluate(pacman, TEST);
			curves[i].set(0, initialScore, initialData);
			
			// Rest of the points
			for (int x=1; x<=LENGTH; x++) {
				double[] data = new double[initialData.length];
				
				for (int y=0; y<TRAIN; y++) {
					//int epLength = episode(pacman);
					int epLength = episode(pacman);
					
					double[] episodeData = pacman.episodeData();
					for (int d=0; d<data.length; d++)
						data[d] += episodeData[d];
//					data[data.length-1]+=epLength;
				}
				
				
				double score = evaluate(pacman, TEST);
				curves[i].set(x, score, data);
			}
			
			// Save new curve and policy
			pacman.savePolicy(DIR+"/"+learner+"/policy"+i);
			curves[i].save(DIR+"/"+learner+"/curve"+i);
			
			// Average all curves
			LearningCurve avgCurve = new LearningCurve(Arrays.copyOf(curves, i+1));
			avgCurve.save(DIR+"/"+learner+"/avg_curve");
		}
		
		System.out.println("Done.");
	}

	/** Train a learner for one more episode. */
	public static int episode(RLPacMan pacman) {
		int length = 0;
		Game game = new Game(rng.nextLong());
		pacman.startEpisode(game, false);

		while(!game.gameOver()) {
			game.advanceGame(pacman.getMove(game.copy(), -1), ghosts.getMove(game.copy(), -1));
			pacman.processStep(game);
			length++;
		}
		return length;
	}
	
	/** Train a learner for one more episode. */
	public static int episodeWatch(RLPacMan pacman) {
		int length = 0;
		Game game = new Game(rng.nextLong());
		pacman.startEpisode(game, false);
		GameView gv=new GameView(game).showGame();

		while(!game.gameOver()) {
			game.advanceGame(pacman.getMove(game.copy(), -1), ghosts.getMove(game.copy(), -1));
			pacman.processStep(game);
			
			gv.repaint();
			
			length++;
		}
		return length;
	}

	/** Estimate the current performance of a learner. */
	public static double evaluate(RLPacMan pacman, int width) {
		
		double sumScore = 0;
		
		for(int i=0; i<width; i++) {
			Game game = new Game(rng.nextLong());
			pacman.startEpisode(game, true);

			while(!game.gameOver()) {
				game.advanceGame(pacman.getMove(game.copy(), -1), ghosts.getMove(game.copy(), -1));
				pacman.processStep(game);
			}
			
			sumScore += game.getScore();
		}

		return sumScore/width;
	}

	/** Observe a learner play a game. */
	public static void watch(RLPacMan pacman) {
		
		Game game=new Game(0);
		pacman.startEpisode(game, true);
		GameView gv=new GameView(game).showGame();

		while(!game.gameOver()) {
			game.advanceGame(pacman.getMove(game.copy(), -1), ghosts.getMove(game.copy(), -1));
			pacman.processStep(game);
			
			try{Thread.sleep(DELAY);}catch(Exception e){}
			gv.repaint();
		}
	}
	
	/** Select a teacher from the independent students. */
	public static void findBestTeacher() {
		
		double[] scores = new double[REPEATS];
		
		for (int i=0; i<REPEATS; i++) {
			BasicRLPacMan pacman = (BasicRLPacMan)create("independent", "teacher");
			pacman.loadPolicy(DIR+"/independent/policy"+i);
			scores[i] = evaluate(pacman, 500);
			System.out.println(DIR+"/independent/policy"+i+": "+scores[i]);
		}
		
		int bestPolicy = 0;
		for (int i=0; i<REPEATS; i++)
			if (scores[i] > scores[bestPolicy])
				bestPolicy = i;
		
		System.out.println("Best: "+DIR+"/independent/policy"+bestPolicy);
	}
	
	/** Make a plottable file of Q-value gaps over a few episodes. */
	public static void plotGapsWatch() {

		DataFile file = new DataFile("myData/"+TEACHER+"/teacher/gaps");
		file.clear();

		BasicRLPacMan pacman = (BasicRLPacMan)create("teacher", "teacher");
		int x = 0;

		for (int i=0; i<1; i++) {
			Game game = new Game(rng.nextLong());
			pacman.startEpisode(game, true);
			GameView gv=new GameView(game).showGame();
			while(!game.gameOver()) {

				double[] qvalues = pacman.getQValues();
				Arrays.sort(qvalues);
				double gap = qvalues[qvalues.length-1] - qvalues[0];

				file.append(x+"\t"+gap+"\n");
				x++;

				game.advanceGame(pacman.getMove(game.copy(), -1), ghosts.getMove(game.copy(), -1));
				pacman.processStep(game);
				try{Thread.sleep(DELAY);}catch(Exception e){}
				System.out.println(gap);
				gv.repaint();
				if (gap>200)
				{
					try{
					System.in.read();
					}
					catch(Exception e)
					{
						System.out.println("ex");
					}
					
				
				}
			}
		}

		file.close();
	}
	
	/** Make a plottable file of Q-value gaps over a few episodes. */
	public static void plotGaps() {

		DataFile file = new DataFile("myData/"+TEACHER+"/teacher/gaps");
		file.clear();

		BasicRLPacMan pacman = (BasicRLPacMan)create("teacher", "teacher");
		int x = 0;

		for (int i=0; i<1; i++) {
			Game game = new Game(rng.nextLong());
			pacman.startEpisode(game, true);

			while(!game.gameOver()) {

				double[] qvalues = pacman.getQValues();
				Arrays.sort(qvalues);
				double gap = qvalues[qvalues.length-1] - qvalues[0];

				file.append(x+"\t"+gap+"\n");
				x++;

				game.advanceGame(pacman.getMove(game.copy(), -1), ghosts.getMove(game.copy(), -1));
				pacman.processStep(game);
			}
		}

		file.close();
	}	
	
	/** Test SVM choice prediction. */
	public static void testSVM() {
			
		BasicRLPacMan student = (BasicRLPacMan)create("independent", "teacher");
		BasicRLPacMan teacher = (BasicRLPacMan)create("teacher", "teacher");
		PredictImportantMistakes strategy = new PredictImportantMistakes(0);
		
		for (int i=0; i<300; i++) {
			Game game = new Game(rng.nextLong());
			student.startEpisode(game, false);
			teacher.startEpisode(game, true);
			
			strategy.startEpisode();
			int right = 0, wrong = 0, truePos = 0, falseNeg = 0, falsePos = 0;
			
			while(!game.gameOver()) {
				MOVE advice = teacher.getMove(game, -1);
				MOVE choice = student.getMove(game, -1);
				strategy.recordExample(teacher, choice);
				
				if (i > 0) {
					MOVE guess = strategy.predictChoice(teacher);
					boolean predict = (guess != advice);
					boolean mistake = (choice != advice);
					
					if (guess == choice)
						right++;
					else
						wrong++;
					
					if (mistake && predict)
						truePos++;
					else if (mistake && !predict)
						falseNeg++;
					else if (!mistake && predict)
						falsePos++;
				}
				
				game.advanceGame(choice, ghosts.getMove(game.copy(), -1));
				student.processStep(game);
				teacher.processStep(game);
			}
			
			if (i > 0) {
				double accuracy = right/(double)(right+wrong);
				double precision = truePos/(double)(truePos+falsePos);
				double recall = truePos/(double)(truePos+falseNeg);
				
				DecimalFormat f = new DecimalFormat("#.##");
				System.out.println("During episode "+i+": a="+f.format(accuracy)+", p="+f.format(precision)+", r="+f.format(recall));
			}
		}
	}
	
	/** Compare areas under two types of learning curves. */
	public static void compare(String dir1, String dir2) {
		
		LearningCurve[] curves1 = new LearningCurve[REPEATS];
		for (int i=0; i<REPEATS; i++)
			curves1[i] = new LearningCurve(LENGTH+1, TRAIN, "myData/"+dir1+"/curve"+i);
		
		double[] areas1 = new double[REPEATS];
		for (int i=0; i<REPEATS; i++)
			areas1[i] = curves1[i].area();
		
		LearningCurve[] curves2 = new LearningCurve[REPEATS];
		for (int i=0; i<REPEATS; i++)
			curves2[i] = new LearningCurve(LENGTH+1, TRAIN, "myData/"+dir2+"/curve"+i);
		
		double[] areas2 = new double[REPEATS];
		for (int i=0; i<REPEATS; i++)
			areas2[i] = curves2[i].area();
		
		double t0 = Stats.t(areas1, areas2);
		double dof = Stats.dof(areas1, areas2);
		System.out.println(dir1+" > "+dir2+" with 95% confidence if:");
		System.out.println(t0+" > t_0.05_"+dof);
	}
}
