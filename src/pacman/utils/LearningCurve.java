package pacman.utils;


/**
 * Represents a curve showing agent performance against episodes trained.
 */
public class LearningCurve {

	public final int length; // Total points
	public final int width; // Episodes per point
	private double[] score; // Agent performance
	private double[][] data; // Extra info

	/** Start with an empty curve. */
	public LearningCurve(int length, int width) {
		this.length = length;
		this.width = width;
		score = new double[length];
		data = new double[length][];
	}
	
	/** Load from a file. */
	public LearningCurve(int length, int width, String filename) {
		this(length, width);
		
		DataFile file = new DataFile(filename);
		for (int x=0; x<length; x++) {
			String[] line = file.nextLine().split("\t");
			score[x] = Double.parseDouble(line[1]);
			data[x] = new double[line.length-2];
			for (int d=0; d<data[x].length; d++)
				data[x][d] = Double.parseDouble(line[d+2]);
		}
		file.close();
	}
	
	/** Average together a set of curves with the same dimensions. */
	public LearningCurve(LearningCurve[] curves) {
		this(curves[0].length, curves[0].width);
		
		for (int x=0; x<length; x++) {
			data[x] = new double[curves[0].data[x].length];
			                     
			for (LearningCurve curve : curves) {
				score[x] += curve.score[x];
				for (int d=0; d<data[x].length; d++)
					data[x][d] += curve.data[x][d];
			}
			score[x] /= curves.length;
			for (int d=0; d<data[x].length; d++)
				data[x][d] /= curves.length;
		}
	}

	/** Assign a point. */
	public void set(int x, double score, double[] data) {
		this.score[x] = score;
		this.data[x] = data;
	}

	/** Compute area under the score curve. */
	public double area() {
		double area = 0;
		for (int x=0; x<length; x++)
			area += score[x];
		return area;
	}

	/** Save to a file. */
	public void save(String filename) {
		DataFile file = new DataFile(filename);
		file.clear();
		for (int x=0; x<length; x++) {
			file.append((x*width)+"\t"+score[x]);
			for (int d=0; d<data[x].length; d++)
				file.append("\t"+data[x][d]);
			file.append("\n");
		}
		file.close();
	}
}
