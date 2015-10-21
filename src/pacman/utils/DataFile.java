package pacman.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * Wrapper to simplify file creating, reading, and writing.
 */
public class DataFile {
	
	private String filename;
	private File file;
	private FileWriter writer;
	private Scanner reader;
	
	/** Point to a file, or create one if it doesn't already exist. */
	public DataFile(String filename) {
		this.filename = filename;
		
		try {
			file = new File(filename);
			if (!file.exists())
				file.createNewFile();
		}
		catch (Exception e) {
			System.out.println("ERROR: could not open "+filename);
			System.exit(0);
		}
	}
	
	/** Clear any data already in this file. */
	public void clear() {
		try {
			file.delete();
			file.createNewFile();
		} catch (Exception e) {
			System.out.println("ERROR: could not clear "+filename);
			System.exit(0);
		}
	}
	
	/** Append data to this file. */
	public void append(String data) {
		try {
			if (writer == null)
				writer = new FileWriter(file, true);
			writer.write(data);
		}
		catch (Exception e) {
	        System.out.println("ERROR: could not write to "+filename);
			System.exit(0);
	    }
	}
	
	/** Check if this file has more lines. */
	public boolean hasNextLine() {
		try {
			if (reader == null)
				reader = new Scanner(file);
		} catch (Exception e) {
        	System.out.println("ERROR: could not read from "+filename);
			System.exit(0);
        }
		
		return reader.hasNextLine();
	}
	
	/** Get a line from this file. */
	public String nextLine() {
		try {
			if (reader == null)
				reader = new Scanner(file);
		} catch (Exception e) {
        	System.out.println("ERROR: could not read from "+filename);
			System.exit(0);
        }
		
		return reader.nextLine();
	}

	/** Close any readers or writers. */
	public void close() {
		try {
			if (reader != null)
				reader.close();
			if (writer != null)
				writer.close();
		} catch (IOException e) {
			System.out.println("ERROR: could not close "+filename);
			System.exit(0);
		}
	}
}
