package pacman.game.internal;

import java.util.ArrayList;
import java.util.HashMap;

import pacman.utils.DataFile;

public class ReadMazeFromFile {
	
	
	
	public static void main(String[] args) {
		
		String filename = "data/mazes/openSmall1.csv";
		DataFile file = new DataFile(filename);
		ArrayList<int[]> maze = new ArrayList<int[]>();
		HashMap<Integer,Integer> nodeIndicesMap = new HashMap<Integer,Integer>();
		//add first data line
		String line = file.nextLine();
		String [] header = line.split(",");
		
		
		//read maze structure to array
		int numNodes = 0;
		int rowIndex = 0;
		while(file.hasNextLine())
		{
			line = file.nextLine();
			String [] values = line.split(",");
			int [] info = new int[values.length];
			for (int i=0;i<values.length;i++)
			{
				info[i]=Integer.parseInt(values[i]);
				if (info[i]!=-1)
				{
					nodeIndicesMap.put(rowIndex*(values.length)+i, numNodes);
					numNodes++;
				}
			}
			maze.add(info);
			rowIndex++;
		}
		
		file.close();
		
		//for maze construction
		
		ArrayList<String[]> mazeStructure = new ArrayList<String[]>();
		int nodeIndex = 0;
		int pillIndex = 0;
		int powerPillIndex = 0;
		int junctionCounter = 0;
		int currRow = 0;
		int currCol = 0;
		String[] currNode;
		String[] mazeRow;
		int numColumns ;
		for (int i=0;i<maze.size();i++)
		{
			
			numColumns = maze.get(i).length;
			for (int j = 0; j<numColumns;j++)
			{
				if (maze.get(i)[j]!=-1)
				{
					currNode = new String[9];
					currNode[0] = Integer.toString(nodeIndex); //index
					currNode[1] = Integer.toString(currRow); //x coordiante
					currNode[2] = Integer.toString(j); //y coordinate
					
					int neighborCount = 0;
					//neighbors
					if (i-1>=0) //there is potentially up neighbor
					{
						if (maze.get(i-1)[j]==-1)
						{
							currNode[3]="-1";
						}
						else
						{
							currNode[3]=Integer.toString(nodeIndicesMap.get((i-1)*numColumns+j));
							neighborCount++;
						}
					}
					else
					{
						if (maze.get(maze.size()-1)[j]==-1)
							currNode[3]="-1";
						else
						{
							currNode[3]=Integer.toString(nodeIndicesMap.get((maze.size()-1)*numColumns+j));
							neighborCount++;
						}
					}
					if (j+1<maze.get(i).length)//potentially right neighbor
					{
						if (maze.get(i)[j+1]==-1)
						{
							currNode[4]="-1";
						}
						else
						{
							currNode[4]=Integer.toString(nodeIndicesMap.get(i*numColumns+j+1));
							neighborCount++;
						}
					}
					else
					{
						if (maze.get(i)[0]==-1)
							currNode[4]="-1";
						else
						{
							currNode[4]=Integer.toString(nodeIndicesMap.get(i*numColumns+0));
							neighborCount++;
						}
					}
					if (i+1<maze.size()) //there is potentially down neighbor
					{
						if (maze.get(i+1)[j]==-1)
						{
							currNode[5]="-1";
						}
						else
						{
							currNode[5]=Integer.toString(nodeIndicesMap.get((i+1)*numColumns+j));;
							neighborCount++;
						}
					}
					else
					{
						if (maze.get(0)[j]==-1)
							currNode[5]="-1";
						else
						{
							currNode[5]=Integer.toString(nodeIndicesMap.get((0)*numColumns+j));;
							neighborCount++;
						}
					}
					if (j-1>=0)//potentially left neighbor
					{
						if (maze.get(i)[j-1]==-1)
						{
							currNode[6]="-1";
						}
						else
						{
							currNode[6]=Integer.toString(nodeIndicesMap.get(i*numColumns+j-1));;
							neighborCount++;
						}
					}
					else
					{
						if (maze.get(i)[maze.get(i).length-1]==-1)
							currNode[6]="-1";
						else
						{
							currNode[6]=Integer.toString(nodeIndicesMap.get(i*numColumns+maze.get(i).length-1));;
							neighborCount++;
						}
					}
					if (maze.get(i)[j]==1)
					{//regular pill
						currNode[7]=Integer.toString(pillIndex);
						pillIndex++;
					}
					else
						currNode[7]="-1";
					if (maze.get(i)[j]==2)
					{//power pill
						currNode[8]=Integer.toString(powerPillIndex);
						powerPillIndex++;
					}
					else
						currNode[8]="-1";
					
					//save this row
					
					currCol++;
					if (neighborCount>2)
						junctionCounter++;
					mazeStructure.add(currNode);
					nodeIndex++;
				}
			}
			currRow++;
			
			
				
//			System.out.println(vec);
		}
		
		header[4] = Integer.toString(nodeIndex);
		header[5] = Integer.toString(pillIndex);
		header[6] = Integer.toString(powerPillIndex);
		header[7] = Integer.toString(junctionCounter);
		
		DataFile file2 = new DataFile("data/mazes/openSmall1_fixed.txt");
		file2.clear();
		for (int i = 0;i<header.length;i++)
		{
			file2.append(header[i]+"\t");
		}
		file2.append("\n");
		for (String[] node:mazeStructure)
		{
			for (int i = 0;i<node.length;i++)
			{
				file2.append(node[i]+"\t");
			}
			file2.append("\n");
		}
		file2.close();
		
	}
	
	

	
	
	

}
