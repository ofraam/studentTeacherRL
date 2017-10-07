package pacman;

import static pacman.game.Constants.DELAY;

import javax.imageio.stream.*;
import com.sun.imageio.plugins.gif.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

import pacman.controllers.KeyBoardInput;
import pacman.entries.ghosts.StandardGhosts;
import pacman.entries.pacman.*;
import pacman.entries.pacman.FeatureSet;
import pacman.entries.pacman.QFunction;
import pacman.game.Constants;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;
import pacman.game.NoMoveGhosts;
import pacman.teaching.AdviseAtFirst;
import pacman.teaching.AdviseAtFirstCorrect;
//import pacman.teaching.AdviseAtFirstCorrect;
//import pacman.teaching.AdviseAtFirstCorrect;
import pacman.teaching.AdviseImportantStates;
import pacman.teaching.AdviseRandom;
import pacman.teaching.AskAttentionBasedOnCertainty;
import pacman.teaching.AttentionStrategy;
import pacman.teaching.CorrectImportantMistakes;
import pacman.teaching.CorrectImportantMistakesAttention;
import pacman.teaching.CorrectImportantMistakesDiffStudent;
import pacman.teaching.CorrectMistakesRandomly;
import pacman.teaching.IntelligentStudent;
import pacman.teaching.PredictImportantMistakes;
import pacman.teaching.StudentAvgUncertaintyAndMistakeAdvice;
import pacman.teaching.StudentImportanceAndMistakeAdvice;
import pacman.teaching.StudentUncertaintyAdvice;
import pacman.teaching.StudentUncertaintyAndMistakeAdvice;
import pacman.teaching.StudentUncertaintyAndMistakeAdviceTop2;
import pacman.teaching.TeachingStrategy;
import pacman.utils.DataFile;
import pacman.utils.LearningCurve;
import pacman.utils.Stats;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

public class Experiments {
	
	public static String TEACHER = "customS"; // Teacher feature set and algorithm
	public static String STUDENT = "customS"; // Student feature set and algorithm

	public static String DIR = "myData/"+TEACHER+"/"+STUDENT; // Where to store data
	public static String SCREENS_DIR = "screenshots";
	
	public static int[] STUDENTS = {200,1000,2000}; // {20,50,100,200,300,400,1000,2000}
	public static int BUDGET = 1000; // Advice budget (1000)
	public static int ATTBUDGET =  Integer.MAX_VALUE; //train100= 72382, train 200=163589, train0=69846

	public static int REPEATS = 1; // Curves to average (30)
	public static int LENGTH = 1; // Points per curve (100)
	public static int TEST = 1; // Test episodes per point (30)

	public static int TRAIN = 2000; // Train episodes per point (10)

	public static Random rng = new Random();
//	public static StandardGhosts ghosts = new StandardGhosts();
	public static StandardGhosts ghosts = new StandardGhosts();
//	public static KeyBoardInput ki = new KeyBoardInput();
	
	/**
	 * Run experiments.
	 */
	public static void main(String[] args) {
//		System.out.println(0 % 5);
//		double[] test = new double[]{1,2};
//		test.add(1);
//		test.add(2);
//		System.out.println(test);
//		createHighlights();  ////////
//		createRandomHighlights();
		
		///------------------------
		String[] policies = {"student2000","student400","studentNoPowerPills1000","student200"}; 
		String[] collectors = {"min","rand","max","first"};
//		//"first",
//		for (String student:policies) {
//			String policyFile = "myData/customS/" + student + "/policy";
//			BasicRLPacMan pacman = (BasicRLPacMan) create("teacher", "teacher", "always", false);
//			pacman.loadPolicy(policyFile);
//			for (String col:collectors) {
//				createHighlightsGeneral(col, 50, 15, pacman, student);
//			}
//		}
//		
//		String[] policies1 = {"student400","studentNoPowerPills1000"}; 
//		String[] collectors1 = {"first"};
//		//"first",
//		for (String student1:policies1) {
//			String policyFile1 = "myData/customS/" + student1 + "/policy";
//			BasicRLPacMan pacman1 = (BasicRLPacMan) create("teacher", "teacher", "always", false);
//			pacman1.loadPolicy(policyFile1);
//			for (String col:collectors1) {
//				createHighlightsGeneral(col, 50, 15, pacman1, student1);
//			}
//		}
		//-------------------
//		try {
		int[] sizes = {5,10,15};
		for (String col:collectors) {
			for (String policy:policies) {
				for (int size:sizes) {
					mergeTrajectories("screenshots/"+col+"/"+policy,size);
				}
			}
		}
//			mergeTrajectories("screenshots/min/student200",15);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		//---
		
//		createFirstHighlights(2, 2);
//		plotGaps();
//		plotGapsWatch();
//		BasicRLPacMan pacman = (BasicRLPacMan) create("independent", "teacher"a,"something", true);
//		pacman.loadPolicy("myData/customS/student20/policy");
//
//		for (int i =0;i<20;i++)
//			watch(pacman);
//		int i = 1/0;
//		String filename = args[0];
//		DataFile file = new DataFile(filename);
//
//		while(file.hasNextLine())
//		{
//			String line = file.nextLine();
//			String[] params = line.split("\t");
//			String teachingStrategy = params[0];
//			String mode = params[1];
//			String attentionMode = params[2];
//			boolean teacherRelease = Boolean.parseBoolean(params[3]);
////			DIR= params[4];
//			System.out.println("starting");
//	 		train(teachingStrategy,0,mode, attentionMode, teacherRelease);
//		}
//		watch(create("independent", "teacher","something"));
//		
//		String teachingStrategy = args[0];
//		String mode = args[1];
//		String attentionMode = args[2];
//		boolean teacherRelease = Boolean.parseBoolean(args[3]);
////		
////		
//		System.out.println("starting");
//		advise250	teacher	always	false
// 		train("independent",0,"teacher", "always", false);
// 		
//		watch(create("advise100"));
//		rng = new Random(111);
//		train("cstuimp150", 0, "student");
//		rng = new Random(111);
//		train("askcstuunc2", 0, "teacher");
//		train("avgcstuunc",0,"student");
//		watch(create("independent", "teacher","something"));
//		plotGapsWatch();
	}
	
	private static void createHighlightsGeneral(String collector, int numGames, int numStates, BasicRLPacMan pacman, String dir) {
		StatesCollector sc = null;
		Collection<GameState> states = null;
		String saveDir = SCREENS_DIR + "/" + collector + "/" + dir;
		switch(collector) {
			case "max":
				sc = new MaxGapStatesCollector(numGames,numStates);
				states = sc.collectStates(pacman);
				break;
			case "min":
				sc = new MinGapMaxValCollector(numGames,numStates);
				states = sc.collectStates(pacman);
				break;
			case "rand":
				sc = new RandomStatesCollector(numGames,numStates);
				states = sc.collectStates(pacman);
				break;
			case "first":
				sc = new FirstStatesCollector(numGames,numStates);
				states = sc.collectStates(pacman);
				break;
		}
		
		File file = new File(saveDir);
		Boolean created = false;
		if (!file.exists()) {
			created = file.mkdirs();
//			System.out.println(created);
		}
		
		
		saveScreens(states,saveDir);
		int idx = 0;
		for (GameState state : states){
			saveTrajectory(state.getTrajectory(),saveDir,idx);
			idx++;
		}
	}
		

	private static void createHighlights(int numGames, int numStates) {
		BasicRLPacMan pacman = (BasicRLPacMan) create("teacher", "teacher", "always", false);
		pacman.loadPolicy("myData/customS/student400/policy");
		MaxGapStatesCollector msc = new MaxGapStatesCollector(numGames,numStates);
		Collection<GameState> states = msc.collectStates(pacman);
//		writeGapsOverLearning(states);
//		writeFeatures(states, pacman);

		saveScreens(states,"screenshots/gifsStudent400");
		int idx = 0;
		for (GameState state : states){
			saveTrajectory(state.getTrajectory(),"screenshots/gifsStudent400",idx);
			idx++;
		}
	}
	
	private static void createMinGapHighlights(int numGames, int numStates) {
		BasicRLPacMan pacman = (BasicRLPacMan) create("teacher", "teacher", "always", false);
		pacman.loadPolicy("myData/customS/student400/policy");
		MaxGapStatesCollector msc = new MaxGapStatesCollector(numGames,numStates);
		Collection<GameState> states = msc.collectStates(pacman);
//		writeGapsOverLearning(states);
//		writeFeatures(states, pacman);

		saveScreens(states,"screenshots/gifsStudent400");
		int idx = 0;
		for (GameState state : states){
			saveTrajectory(state.getTrajectory(),"screenshots/gifsStudent400",idx);
			idx++;
		}
	}
	
	private static void createFirstHighlights(int numGames, int numStates) {
		BasicRLPacMan pacman = (BasicRLPacMan) create("teacher", "teacher", "always", false);
		pacman.loadPolicy("myData/customS/student2000/policy");
		FirstStatesCollector fsc = new FirstStatesCollector(numGames,numStates);
		Collection<GameState> states = fsc.collectStates(pacman);
//		writeGapsOverLearning(states);
//		writeFeatures(states, pacman);

		saveScreens(states,"screenshots/first/gifsStudent2000");
		int idx = 0;
		for (GameState state : states){
			saveTrajectory(state.getTrajectory(),"screenshots/first/gifsStudent2000",idx);
			idx++;
		}
	}
	
	private static void createRandomHighlights(int numGames, int numStates) {
		BasicRLPacMan pacman = (BasicRLPacMan) create("teacher", "teacher", "always", false);
		pacman.loadPolicy("myData/customS/student2000/policy");
		RandomStatesCollector rsc = new RandomStatesCollector(numGames,numStates);
		Collection<GameState> states = rsc.collectStates(pacman);
//		writeGapsOverLearning(states);
//		writeFeatures(states, pacman);

		saveScreens(states,"screenshots/random/gifsStudent2000");
		int idx = 0;
		for (GameState state : states){
			saveTrajectory(state.getTrajectory(),"screenshots/random/gifsStudent2000",idx);
			idx++;
		}
	}
	
	


	private static void writeFeatures(Collection<GameState> states, BasicRLPacMan pacman) {
		File file = new File("HLdata/max_features");
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException i) {
			System.err.println(i.getMessage());
			System.exit(0);
		}
		QFunction func = pacman.getQfunc();
		double[] feat = {0,0,0,0,0,0,0};
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			for (GameState state : states) {
				double maxVal = func.evaluate(state.getFeatures()[0]);
				feat = state.getFeatures()[0].getVAlues();
				for (FeatureSet f : state.getFeatures()) {
					if (func.evaluate(f) > maxVal) {
						maxVal = func.evaluate(f);
						feat = f.getVAlues();
					}
				}
				for (double val:feat){
					bw.write(Double.toString(val)+",");
				}
				bw.write("\n");
			}
		} catch (IOException io){
			System.out.println(io.getMessage());
			System.exit(0);
		}
	}


	public static void writeGapsOverLearning(Collection<GameState> states){
		BasicRLPacMan pacman = (BasicRLPacMan) create("teacher", "teacher", "always", false);
		File file = new File("HLdata/states2.csv");
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException i) {
			System.err.println(i.getMessage());
			System.exit(0);
		}
		int idx = 0;
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			bw.write("state,student20,student50,student100,student200,student300,student400,student1000,student2000,max2000,min2000\n");
			for (GameState state : states) {
				bw.write(idx+",");
				idx++;
				for (int i : STUDENTS) {
					pacman.loadPolicy("myData/customS/student" + i + "/policy");
					QFunction func = pacman.getQfunc();
					double min = func.evaluate(state.getFeatures()[0]);
					double max = func.evaluate(state.getFeatures()[0]);
					for (FeatureSet feature : state.getFeatures()) {
						if (func.evaluate(feature) > max) {
							max = func.evaluate(feature);
						} else if (func.evaluate(feature) < min) {
							min = func.evaluate(feature);
						}
					}
					bw.write(Double.toString(max - min)+",");
					if (i == 2000){
						bw.write(Double.toString(max)+",");
						bw.write(Double.toString(min)+",");
					}
				}
				bw.write("\n");
			}
		} catch (IOException i){
			System.err.println(i.getMessage());
			System.exit(0);
		}
	}


	public static void writeConfig(String filename, String initiator, boolean teacherRelease)
	{
		DataFile file = new DataFile(filename);
		file.clear();
		
		file.append("budget = "+BUDGET+"\n");
		file.append("pacmanLives = "+ Constants.NUM_LIVES+"\n");
		file.append("initiator = "+initiator+"\n");
		file.append("teacher release = "+Boolean.toString(teacherRelease)+"\n");
		file.append(DIR);
		file.close();
	}

	/** Set up a learner. */
	public static RLPacMan create(String learner, String initiator, String attentionMode, boolean teacherRelease) {
		
		FeatureSet teacherProto = TEACHER.startsWith("custom") ? new CustomFeatureSet() : new DepthFeatureSet();
		FeatureSet studentProto = STUDENT.startsWith("custom") ? new CustomFeatureSet() : new DepthFeatureSet();

		// Lone teacher
		if (learner.startsWith("teacher")) {
			BasicRLPacMan teacher = TEACHER.endsWith("S") ? new SarsaPacMan(teacherProto) : new QPacMan(teacherProto);
			//teacher.loadPolicy("myData/"+TEACHER+"/teacher/policy");

			//teacher.loadPolicy("myData/"+TEACHER+"/student400/policy");

			return teacher;
		}
			
		// Lone student
		else if (learner.startsWith("independent")) {
			BasicRLPacMan student = STUDENT.endsWith("S") ? new SarsaPacMan(studentProto) : new QPacMan(studentProto);
//			student.loadPolicy("myData/"+TEACHER+"/teacherOpenMaze/policy");
//			student.loadPolicy("myData/"+TEACHER+"/student200/policy");

			return student;
//			return STUDENT.endsWith("S") ? new SarsaPacMan(studentProto) : new QPacMan(studentProto);
		}
		
		// Student-teacher pair
		else {
			BasicRLPacMan student = STUDENT.endsWith("S") ? new SarsaPacMan(studentProto) : new QPacMan(studentProto);
			BasicRLPacMan teacher = TEACHER.endsWith("S") ? new SarsaPacMan(teacherProto) : new QPacMan(teacherProto);
			teacher.loadPolicy("myData/"+TEACHER+"/teacher/policy");
			
			//TODO: what if student is not stupid
			student.loadPolicy("myData/"+TEACHER+"/student100/policy");

			
			
			// Front-load the advice budget
			if (learner.startsWith("baseline")) {
				TeachingStrategy strategy = new AdviseAtFirst();
//				return new Student(teacher, student, strategy, initiator);
				return new IntelligentStudent(teacher, student, strategy, initiator, attentionMode,teacherRelease);
			}
			
			if (learner.startsWith("cbaseline")) {
				TeachingStrategy strategy = new AdviseAtFirstCorrect();
//				return new Student(teacher, student, strategy, initiator);
				return new IntelligentStudent(teacher, student, strategy, initiator, attentionMode,teacherRelease);
			}
			
			// Advise in important states
			if (learner.startsWith("advise")) {
				int threshold = Integer.parseInt(learner.substring(6));
				TeachingStrategy strategy = new AdviseImportantStates(threshold);
//				return new IntelligentStudent(teacher, student, strategy, initiator);
				return new IntelligentStudent(teacher, student, strategy, initiator, attentionMode,teacherRelease);
			}
			
			// Correct important mistakes
			if (learner.startsWith("correct")) {
				int threshold = Integer.parseInt(learner.substring(7));
				TeachingStrategy strategy = new CorrectImportantMistakes(threshold);
//				return new IntelligentStudent(teacher, student, strategy, initiator);
				return new IntelligentStudent(teacher, student, strategy, initiator, attentionMode,teacherRelease);
			}
			
			// Correct important mistakes, but only if paying attention
			if (learner.startsWith("attcorrect")) {
				int att = Integer.parseInt(learner.substring(10,learner.length() - 3));
				int threshold = Integer.parseInt(learner.substring(learner.length() - 3));
				TeachingStrategy strategy = new CorrectImportantMistakesAttention(att,threshold);
//				return new Student(teacher, student, strategy, initiator);
				return new IntelligentStudent(teacher, student, strategy, initiator, attentionMode,teacherRelease);
			}
			
			// Correct important mistakes, based on diff between teacher and student action q-values
			if (learner.startsWith("dcorrect")) {
				int threshold = Integer.parseInt(learner.substring(8));
				TeachingStrategy strategy = new CorrectImportantMistakesDiffStudent(threshold);
//				return new Student(teacher, student, strategy, initiator);
				return new IntelligentStudent(teacher, student, strategy, initiator, attentionMode,teacherRelease);
			}

			
			// Advise in important states with predicted mistakes
			if (learner.startsWith("predict")) {
				int threshold = Integer.parseInt(learner.substring(7));
				TeachingStrategy strategy = new PredictImportantMistakes(threshold);
//				return new Student(teacher, student, strategy, initiator);
				return new IntelligentStudent(teacher, student, strategy, initiator, attentionMode,teacherRelease);
			}
			
			// Advise randomly
			if (learner.startsWith("random")) {
				int prob = Integer.parseInt(learner.substring(6));
				TeachingStrategy strategy = new AdviseRandom(prob);
//				return new Student(teacher, student, strategy, initiator);
				return new IntelligentStudent(teacher, student, strategy, initiator, attentionMode,teacherRelease);
			}
			
			// Correct mistakes randomly
			if (learner.startsWith("crandom")) {
				int prob = Integer.parseInt(learner.substring(7));
				TeachingStrategy strategy = new CorrectMistakesRandomly(prob);
//				return new Student(teacher, student, strategy, initiator);
				return new IntelligentStudent(teacher, student, strategy, initiator, attentionMode,teacherRelease);
			}
			
			//Student initiated advice based on uncertainty (low q-value diff)
			if (learner.startsWith("stuunc")) {
				int threshold = Integer.parseInt(learner.substring(6));
				TeachingStrategy strategy = new StudentUncertaintyAdvice(threshold);
//				return new Student(teacher, student, strategy, initiator);
				return new IntelligentStudent(teacher, student, strategy, initiator, attentionMode,teacherRelease);
			}	
			
			//Student initiated advice based on uncertainty (low q-value diff), only use advice if student was wrong
			if (learner.startsWith("cstuunc")) {
				int threshold = Integer.parseInt(learner.substring(7));
				TeachingStrategy strategy = new StudentUncertaintyAndMistakeAdvice(threshold);
//				return new Student(teacher, student, strategy, initiator);
				return new IntelligentStudent(teacher, student, strategy, initiator, attentionMode,teacherRelease);
			}	
			
			//Student initiated advice based on uncertainty (low q-value diff), only use advice if student was wrong
			if (learner.startsWith("ctstuunc")) {
				int threshold = Integer.parseInt(learner.substring(8));
				TeachingStrategy strategy = new StudentUncertaintyAndMistakeAdviceTop2(threshold);
//				return new Student(teacher, student, strategy, initiator);
				return new IntelligentStudent(teacher, student, strategy, initiator, attentionMode,teacherRelease);
			}
			
			//Student initiated advice based on uncertainty (lower q-value diff than average), only use advice if student was wrong
			if (learner.startsWith("avgcstuunc")) {
				TeachingStrategy strategy = new StudentAvgUncertaintyAndMistakeAdvice();
//				return new Student(teacher, student, strategy, initiator);
				return new IntelligentStudent(teacher, student, strategy, initiator, attentionMode,teacherRelease);
			}
			
			//Student initiated advice based on importance (high q-value diff), only use advice if student was wrong
			if (learner.startsWith("cstuimp")) {
				int threshold = Integer.parseInt(learner.substring(7));
				TeachingStrategy strategy = new StudentImportanceAndMistakeAdvice(threshold);
//				return new Student(teacher, student, strategy, initiator);
				return new IntelligentStudent(teacher, student, strategy, initiator, attentionMode,teacherRelease);
			}
			
			//Student initiated asking, but teacher decides whether to advise
			if (learner.startsWith("askcstuunc")) {
				int threshold = Integer.parseInt(learner.substring(10));
				TeachingStrategy strategy = new CorrectImportantMistakes(200);
				AttentionStrategy attent = new AskAttentionBasedOnCertainty(threshold);
//				return new Student(teacher, student, strategy, initiator, attent);
				return new IntelligentStudent(teacher, student, strategy, initiator, attentionMode,teacherRelease);
			}	
		}
		
		return null;
	}
	
	public static int findStart(String learnerCombined)
	{
		int startIndex=0;
		for (int i = 0;i<REPEATS;i++)
		{
			String filename = DIR+"/"+learnerCombined+"/curve"+i;
			File f = new File(filename);
			if (!f.exists())
				break;
			else
				startIndex++;
		}
		return startIndex;
	}
	
	/** Generate learning curves. */
	public static void train(String learner, int start, String initiator, String attentionMode, boolean teacherRelease) {
		String learnerCombined = learner+"_"+attentionMode+"_"+teacherRelease;
		// Make sure directory exists
		File file = new File(DIR+"/"+learnerCombined);
		Boolean created = false;
		if (!file.exists())
			created = file.mkdir();
			System.out.println(created);
		
		writeConfig(DIR+"/"+learnerCombined+"/config.txt", initiator, teacherRelease);	
			
		// Load old curves
		LearningCurve[] curves = new LearningCurve[REPEATS];
		start = findStart(learnerCombined);
		
		for (int i=0; i<start; i++)
			curves[i] = new LearningCurve(LENGTH+1, TRAIN, DIR+"/"+learnerCombined+"/curve"+i);
		
		// Begin new curves
		for (int i=start; i<REPEATS; i++) {
			curves[i] = new LearningCurve(LENGTH+1, TRAIN);
			
			System.out.println("Training "+DIR+"/"+learnerCombined+" "+i+"...");
			RLPacMan pacman = create(learner,initiator,attentionMode,teacherRelease);
//			pacman.loadVisitedState("myData/"+TEACHER+"/student100/visited");

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
//					try {
//						System.in.read();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
					double[] episodeData = pacman.episodeData();
					for (int d=0; d<data.length; d++)
						data[d] += episodeData[d];
//					data[data.length-1]+=epLength;
				}
				
				
				double score = evaluate(pacman, TEST);
				curves[i].set(x, score, data);
			}
			
			// Save new curve and policy
			pacman.savePolicy(DIR+"/"+learnerCombined+"/policy"+i);
//			pacman.saveStates(DIR+"/"+learnerCombined+"/visited"+i,4000);

			curves[i].save(DIR+"/"+learnerCombined+"/curve"+i);
			
			// Average all curves
			LearningCurve avgCurve = new LearningCurve(Arrays.copyOf(curves, i+1));
			avgCurve.save(DIR+"/"+learnerCombined+"/avg_curve");
		}
		
		System.out.println("Done.");
	}

	/** Train a learner for one more episode. */
	public static int episode(RLPacMan pacman) {
		int length = 0;
		Game game = new Game(rng.nextLong());
		pacman.startEpisode(game, false);

		while(!game.gameOver() & length<15000) { //TODO: note length limitation
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
//		gv.addKeyListener(ki);
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
			int length = 0;
			while(!game.gameOver() & length<15000) {
				game.advanceGame(pacman.getMove(game.copy(), -1), ghosts.getMove(game.copy(), -1));
				pacman.processStep(game);
				length++;
//				System.out.println(length);
			}
			
			sumScore += game.getScore();
		}

		return sumScore/width;
	}

	/** Observe a learner play a game. */
	public static void watch(RLPacMan pacman) {


		Game game=new Game(rng.nextLong());
		pacman.startEpisode(game, true);
		GameView gv=new GameView(game).showGame();
//		try {
//			System.in.read();
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		while(!game.gameOver()) {
			game.advanceGame(pacman.getMove(game.copy(), -1), ghosts.getMove(game.copy(), -1));
			pacman.processStep(game);
			BasicRLPacMan pc = (BasicRLPacMan) pacman;
			QFunction func = pc.getQfunc();
			double[] qvalues = pc.getQValues();
			Arrays.sort(qvalues);
			double gap = qvalues[qvalues.length - 1] - qvalues[0];
			System.out.println(Double.toString(gap));

			try{Thread.sleep(DELAY);}catch(Exception e){}
			gv.repaint();
		}
	}
	
	/** Select a teacher from the independent students. */
	public static void findBestTeacher() {
		
		double[] scores = new double[REPEATS];
		
		for (int i=0; i<REPEATS; i++) {
			BasicRLPacMan pacman = (BasicRLPacMan)create("independent", "teacher", "always", false);
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

		DataFile file = new DataFile("myData/"+TEACHER+"/teacherOpenMaze/gaps");
		file.clear();

		BasicRLPacMan pacman = (BasicRLPacMan)create("teacher", "teacher","always",false);
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

		//DataFile file = new DataFile("myData/"+TEACHER+"/teacherOpenMaze/gaps");
		//file.clear();

		BasicRLPacMan pacman = (BasicRLPacMan)create("teacher", "teacher", "always",false);
		double maxGap = 0;
		PriorityQueue<GameState> bestStates= new PriorityQueue<>(20, new GapComparator());
		ArrayList<pacman.entries.pacman.QFunction> qFuncsList = new ArrayList<>();
		//int[] scores = new int[20];


		for (int i=0; i<2200; i++) {
			Game game = new Game(rng.nextLong());
			pacman.startEpisode(game, false);
			int length = 0;
			//String state = "";
			int c = 0;
			Queue<String> trajectory = new ArrayBlockingQueue<String>(20);
			GameState lastState = new GameState();

			while(!game.gameOver() & length<20000) {
				String state = game.getGameState();

				if (trajectory.size() == 20) {
					trajectory.remove();
				}
				trajectory.offer(state);




				//	System.out.println(length +"gap: "+gap);

				//file.append(x+"\t"+gap+"\n");
				//System.out.println(gap);

				game.advanceGame(pacman.getMove(game.copy(), -1), ghosts.getMove(game.copy(), -1));
				pacman.processStep(game);
				length++;
			}
			if (i == 20 || i == 50 || i == 1000 || i == 2000){
				double[] qvalues = pacman.getQValues();
				pacman.getQfunc().save("myData/customS/student"+Integer.toString(i)+"/policy");
			}
//			scores[i%20] = game.getScore();
//			if (i > 20) {
//				int sum = 0;
//				for (int score : scores){
//					sum += score;
//				}
//				double ave = (double)sum/scores.length;
//				System.out.println("average score: " + ave);
//			}
//			System.out.println(Arrays.toString(pacman.getQfunc().getWeights()));
//
		}
		printBestGaps(bestStates);
		saveScreens(bestStates,"screenshots");
	//	saveTrajectory(bestStates.peek().getTrajectory(),"screenshots/trajectories");

		System.out.println("max gap: "+maxGap);

//		for (int j = 0;j < qFuncsList.size() ; j++ ){
//			System.out.println(qFuncsList.get(j));
//			System.out.println("turn: " + j +"\n");
//			for (ArrayList<FeatureSet> featList : features) {
//				System.out.println("--------");
//				for (FeatureSet feature : featList) {
//					System.out.println("*******");
//					System.out.println(qFuncsList.get(j).evaluate(feature));
//				}
//			}
//		}
//		System.out.println("choose screen");
//		Game game = new Game(rng.nextLong());
//		for (int j = 0; j < gameStates.size(); j++){
//			game.setGameState(gameStates.get(j));
//			GameView gv = new GameView(game).showGame();
//			gv.repaint();
//			BufferedImage bi = new BufferedImage(gv.getWidth(), gv.getHeight(),BufferedImage.TYPE_INT_RGB);
//			Graphics2D g2d = bi.createGraphics();
//			gv.paint(g2d);
//			try {
//				File outputFile = new File("screenshots/pic"+j+".png");
//				ImageIO.write(bi,"png",outputFile);
//
//			} catch (Exception e){
//				System.err.println(e.getMessage());
//			}
//		}
		//game.setGameState(state);
		//GameView gv=new GameView(game).showGame();
		//gv.repaint();
		//file.close();
	}
	public static void printBestGaps(Queue<GameState> bestStates){
		for (GameState gs: bestStates){
			System.out.println(gs.getQgap());
		}
	}
	public static void saveScreens(Collection<GameState> states, String dir){
		int idx = 0;
		for (GameState state: states){
			Game game = new Game(rng.nextLong());
			game.setGameState(state.getState());
			GameView gv = new GameView(game).showGame();
			BufferedImage bi = new BufferedImage(gv.getWidth(), gv.getHeight(),BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = bi.createGraphics();
			gv.paint(g2d);
			try {
				File outputFile = new File(dir+"/screen"+idx+".png");
				ImageIO.write(bi,"png",outputFile);
				gv.closeGame();

			} catch (Exception e){
				System.err.println(e.getMessage());
				gv.closeGame();
			}
			idx ++;
		}
	}
	
	
	public static void saveTrajectory(Collection<String> frames, String dir, int stateNum){
		int idx = 0;
		try {
			ImageOutputStream outGIF = new FileImageOutputStream( new File(dir+"/"+stateNum+".gif"));
			GifSequenceWriter gsw = new GifSequenceWriter(outGIF,BufferedImage.TYPE_INT_RGB, 75, false);
			for (String frame: frames) {
				Game game = new Game(rng.nextLong());
				game.setGameState(frame);
				GameView gv = new GameView(game).showGame();
				BufferedImage bi = new BufferedImage(gv.getWidth(), gv.getHeight(), BufferedImage.TYPE_INT_RGB);
				Graphics2D g2d = bi.createGraphics();
				gv.paint(g2d);
				gsw.writeToSequence(bi);
				gv.closeGame();
				

			}
			gsw.close();
			
			

		} catch (IOException io)
		{
			System.out.println(io.getMessage());
			System.exit(-1);
		}
	}
	
	public static void mergeTrajectories(String dir, int numGifs){
		int idx = 0;
		try {
			ImageOutputStream outGIF = new FileImageOutputStream( new File(dir+"/merged"+numGifs+".gif"));
			GifSequenceWriter gsw = new GifSequenceWriter(outGIF,BufferedImage.TYPE_INT_RGB, 75, false);
//			ArrayList<BufferedImage> allFrames = 
			for (int i=0;i<numGifs;i++) {
				ArrayList<BufferedImage> frames = getFrames(new File(dir + "/"+i+".gif"));
				for (BufferedImage frame:frames) {
					gsw.writeToSequence(frame);
				}

			}
			gsw.close();
			
		} catch (IOException io)
		{
			System.out.println(io.getMessage());
			System.exit(-1);
		}
	}
	
	public static ArrayList<BufferedImage> getFrames(File gif) throws IOException{
	    ArrayList<BufferedImage> frames = new ArrayList<BufferedImage>();
	    ImageReader ir = new GIFImageReader(new GIFImageReaderSpi());
	    ir.setInput(ImageIO.createImageInputStream(gif));
	    for(int i = 0; i < ir.getNumImages(true); i++)
	    	frames.add(ir.read(i));

	    return frames;
	}


	/** Test SVM choice prediction. */
	public static void testSVM() {
			
		BasicRLPacMan student = (BasicRLPacMan)create("independent", "teacher", "always",false);
		BasicRLPacMan teacher = (BasicRLPacMan)create("teacher", "teacher", "always",false);
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
