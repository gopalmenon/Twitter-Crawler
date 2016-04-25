package pagerank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import file.io.FileWriter;

public class TransitionProbabilityMatrix {
	
	private List<Long> twitterIds;
	private Map<MatrixElement, Double> matrix;
	
	/**
	 * Create the transition probability matrix based on twitter follower files present in folder
	 * @param followersFolder
	 */
	public TransitionProbabilityMatrix(String followersFolder) {
		
		createSetOfUniqueTwitterIdsInFollowerGraph(followersFolder);
		createTransitionMatrixEntries(followersFolder);
		normalizeTransitionProbabilityMatrixRows();
		
	}
	
	private void createSetOfUniqueTwitterIdsInFollowerGraph(String followersFolder) {
		
		//Set to hold twitter ids
		TreeSet<Long> twitterIds = new TreeSet<Long>();
		
		FileWriter fileOperations = new FileWriter();
		
		//Get list of files containing twitter followers
		String[] folderContents = fileOperations.getFolderContents(followersFolder);
		if (folderContents != null && folderContents.length > 0) {
			List<String> followersIds = null;
			for (String fileName : folderContents) {
				if (fileOperations.fileExists(getFullFilePath(followersFolder, fileName))) {
					try {
						//Add friend id to master list
						twitterIds.add(Long.valueOf(extractTwitterIdFromFileName(fileName)));
						followersIds = fileOperations.getFileContents(getFullFilePath(followersFolder, fileName));
						//Add followers to master list
						for (String twitterFollower : followersIds) {
							twitterIds.add(Long.valueOf(extractTwitterIdFromFileName(twitterFollower)));
						}
					} catch (NumberFormatException e) {
						System.err.println("NumberFormatException thrown while trying to extract Twitter ID from String");
						e.printStackTrace();
						continue;
					} catch (IOException e) {
						System.err.println("IOException thrown while reading contents of file " + followersFolder + "/" + fileName);
						e.printStackTrace();
						continue;
					}
				}
			}
		}
		
		this.twitterIds = new ArrayList<Long>(twitterIds);

	}
	
	private String extractTwitterIdFromFileName(String filename) {
		
		//Filename will consist of numeric twitter id followed by ".txt" extension
		String[] filenameComponents = filename.trim().split("\\.");
		return filenameComponents[0];
		
	}
	
	private String getFullFilePath(String foldername, String filename) {
		return foldername + "/" + filename;
	}
	
	private String getFilenameFromTwitterId(Long twitterId) {
		return twitterId.toString() + ".txt";
	}
	
	/**
	 * Create transition probability matrix entries by going column by column. For each twitter id in master list, get the list of followers.
	 * Create entries with value 1.0 for every follower of a twitter id.
	 * @param followersFolder
	 */
	private void createTransitionMatrixEntries(String followersFolder) {
		
		this.matrix = new HashMap<MatrixElement, Double>();
		
		FileWriter fileOperations = new FileWriter();
		boolean listOfFollowersExists = false;
		String fileName = null;
		List<String> followerIds = null;
		//Loop through twitter ids in master list
		for (Long twitterId : this.twitterIds) {
			
			//Get list of followers
			fileName = getFullFilePath(followersFolder, getFilenameFromTwitterId(twitterId));
			listOfFollowersExists = fileOperations.fileExists(fileName);
			if (listOfFollowersExists) {
				try {
					followerIds = fileOperations.getFileContents(fileName);
					//Create transition matrix entries for column corresponding to twitter id
					for (String followerId : followerIds) {
						createFollowerLink(twitterId, Long.valueOf(followerId));
					}
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				} catch (NumberFormatException e) {
					e.printStackTrace();
					continue;
				}
			}
			
			
		}
		
		
	}
	
	private void createFollowerLink(Long twitterId, Long followerId) {
		
		int columnNumber = this.twitterIds.indexOf(twitterId);
		int rowNumber = this.twitterIds.indexOf(followerId);
		
		//Create the link with a value of 1. This will be normalized later.
		this.matrix.put(new MatrixElement(rowNumber, columnNumber, getNumberOfTwittersIdsInFollowerGraph()), 1.0);
		
	}
	
	/**
	 * For every row in transition matrix, divide each present entry by total number of entries to create transition probabilities
	 */
	private void normalizeTransitionProbabilityMatrixRows() {
		
		int numberOfTwittersIdsInFollowerGraph = getNumberOfTwittersIdsInFollowerGraph(), numberOfElementsInRow = 0;
		for (int twitterIdRowIndex = 0; twitterIdRowIndex < numberOfTwittersIdsInFollowerGraph; ++twitterIdRowIndex) {
			numberOfElementsInRow = 0;
			for (int twitterIdColumnIndex = 0; twitterIdColumnIndex < numberOfTwittersIdsInFollowerGraph; ++twitterIdColumnIndex) {
				if (this.matrix.containsKey(new MatrixElement(twitterIdRowIndex, twitterIdColumnIndex, numberOfTwittersIdsInFollowerGraph))) {
					++numberOfElementsInRow;
				}
			}
			for (int twitterIdColumnIndex = 0; twitterIdColumnIndex < numberOfTwittersIdsInFollowerGraph; ++twitterIdColumnIndex) {
				if (this.matrix.containsKey(new MatrixElement(twitterIdRowIndex, twitterIdColumnIndex, numberOfTwittersIdsInFollowerGraph))) {
					this.matrix.put(new MatrixElement(twitterIdRowIndex, twitterIdColumnIndex, numberOfTwittersIdsInFollowerGraph), Double.valueOf(1/numberOfElementsInRow));
				}
			}
		}
		
		
	}
	
	public int getNumberOfTwittersIdsInFollowerGraph() {
		return this.twitterIds.size();
	}
	
}
