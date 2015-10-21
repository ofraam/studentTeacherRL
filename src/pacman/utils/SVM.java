package pacman.utils;

import java.util.LinkedList;

import pacman.entries.pacman.FeatureSet;

/**
 * Interface with SVM-light.
 */
public class SVM {

	/** An SVM training example showing the preference ranking of actions. */
	public static String example(FeatureSet[] features, int choiceIndex) {
		String example = "";
		for (int i=0; i<features.length; i++) {
			int rank = 1;
			if (choiceIndex >= 0 && features[i].equals(features[choiceIndex]))
				rank = 2;
			example += rank+" qid:QID";
			for (int j=0; j<features[i].size(); j++)
				example += " "+(j+1)+":"+features[i].get(j);
			example += "\n";
		}
		return example;
	}
	
	/** Build a predictor using this training data. */
	public static void train(LinkedList<String> trainData, String trainFile, String modelFile) {
		
		DataFile file = new DataFile(trainFile);
		file.clear();
		
		int qid = 0;
		for (String example : trainData) {
			qid++;
			file.append(example.replaceAll("QID", Integer.toString(qid)));
		}
		
		file.close();
		
		int exitValue = execute("myData/svm/svm_rank_learn -v 0 -c 1000 "+trainFile+" "+modelFile);
		if (exitValue != 0)
			System.out.println("Training failed!");
	}
	
	/** Make predictions for this test data. */
	public static double[] rank(String query, int size, String testFile, String modelFile, String classifyFile) {
		
		DataFile file = new DataFile(testFile);
		file.clear();
		file.append(query.replaceAll("QID", "0"));
		file.close();
		
		int exitValue = execute("myData/svm/svm_rank_classify -v 0 "+testFile+" "+modelFile+" "+classifyFile);
		if (exitValue != 0)
			System.out.println("Classification failed!");
		
		double[] ranks = new double[size];
		file = new DataFile(classifyFile);
		for (int i=0; i<size; i++)
			ranks[i] = Double.parseDouble(file.nextLine());
		file.close();
		
		return ranks;
	}
	
	/** Do a system call with no stdin/stdout/stderr for up to 30 seconds. */
	public static int execute(String command) {
		try {
			Process p = Runtime.getRuntime().exec(command);
			
			for (int i=0; i<3000; i++) {
				Thread.sleep(10);
				try {
					int exitValue = p.exitValue();
					p.getInputStream().close();
					p.getOutputStream().close();
					p.getErrorStream().close();
					return exitValue;

				} catch (IllegalThreadStateException e) {}
			}
			
			p.destroy();
			p.getInputStream().close();
			p.getOutputStream().close();
			p.getErrorStream().close();
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		return -1;
	}
}
