package pagerank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import file.io.FileWriter;

public class TransitionProbabilityMatrix {
	
	public static final double DEFAULT_TELEPORTATION_RATE = 0.1;
	public static final double MINIMUM_TELEPORTATION_RATE = 0.0;
	public static final double MAXIMUM_TELEPORTATION_RATE = 1.0;
	public static final double VECTOR_SIMILARITY_TARGET = 0.9999;
	public static final int MAXIMUM_RANDOM_WALK_COUNT = 75;
	
	private List<Long> twitterIds;
	private Map<MatrixElement, Double> matrix;
	private Map<Integer, Integer> rowCounts;
	private double teleportationRate;
	private double teleportationMatrixEntry;
	private double[] probabilityDistributionVector;
	
	/**
	 * Create the transition probability matrix based on twitter follower files present in folder
	 * @param followersFolder
	 */
	public TransitionProbabilityMatrix(String followersFolder) {
		
		this(followersFolder, DEFAULT_TELEPORTATION_RATE);
		
	}
	
	public TransitionProbabilityMatrix(String followersFolder, double teleportationRate) {
		
		if (teleportationRate > MINIMUM_TELEPORTATION_RATE && teleportationRate < MAXIMUM_TELEPORTATION_RATE) {
			this.teleportationRate = teleportationRate;
		} else {
			System.err.println("Teleportation rate must be between " + MINIMUM_TELEPORTATION_RATE + " and " + MAXIMUM_TELEPORTATION_RATE + ". Value of " + teleportationRate + " is not valid.");
			this.teleportationRate = DEFAULT_TELEPORTATION_RATE;
		}
		
		createSetOfUniqueTwitterIdsInFollowerGraph(followersFolder);
		createTransitionMatrixEntries(followersFolder);
		normalizeTransitionProbabilityMatrixRows();
		doRandomWalkOnFollowerGraph();
		
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
		
		createTeleportationMatrixEntry();

	}
	
	/**
	 * The teleportation probability will be maintained outside the transition matrix so as to keep the matrix sparse
	 */
	private void createTeleportationMatrixEntry() {
		this.teleportationMatrixEntry = this.teleportationRate / getNumberOfTwittersIdsInFollowerGraph();
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
		this.rowCounts = new HashMap<Integer, Integer>();
		
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
	
	/**
	 * For each twitter id with a list of followers, create a follower link by setting the value to 1 for the column corresponding to the id being followed
	 * @param twitterId
	 * @param followerId
	 */
	private void createFollowerLink(Long twitterId, Long followerId) {
		
		int columnNumber = this.twitterIds.indexOf(twitterId);
		int rowNumber = this.twitterIds.indexOf(followerId);
		
		//Create the link with a value of 1. This will be normalized later.
		this.matrix.put(new MatrixElement(rowNumber, columnNumber, getNumberOfTwittersIdsInFollowerGraph()), 1.0);
		incrementRowCount(rowNumber);
		System.out.println("Created follower link for column " + columnNumber + " and row " + rowNumber);
	}
	
	/**
	 * Keep track of the number of follower links in a matrix row. Will be used later as a denominator to compute probability.
	 * @param rowNumber
	 */
	private void incrementRowCount(int rowNumber) {
		
		if (this.rowCounts.containsKey(Integer.valueOf(rowNumber))) {
			this.rowCounts.put(Integer.valueOf(rowNumber), Integer.valueOf(this.rowCounts.get(Integer.valueOf(rowNumber)).intValue() + 1));
		} else {
			this.rowCounts.put(Integer.valueOf(rowNumber), Integer.valueOf(1));
		}
		
		
	}
	
	/**
	 * For every row in transition matrix, divide each present entry by total number of entries to create transition probabilities
	 */
	private void normalizeTransitionProbabilityMatrixRows() {
		
		int numberOfTwittersIdsInFollowerGraph = getNumberOfTwittersIdsInFollowerGraph(), numberOfElementsInRow = 0;
		for (int twitterIdRowIndex = 0; twitterIdRowIndex < numberOfTwittersIdsInFollowerGraph; ++twitterIdRowIndex) {
			if (this.rowCounts.containsKey(Integer.valueOf(twitterIdRowIndex))) {
				numberOfElementsInRow = this.rowCounts.get(Integer.valueOf(twitterIdRowIndex)).intValue();
			} else {
				numberOfElementsInRow = 0;
			}
			for (int twitterIdColumnIndex = 0; twitterIdColumnIndex < numberOfTwittersIdsInFollowerGraph; ++twitterIdColumnIndex) {
				if (this.matrix.containsKey(new MatrixElement(twitterIdRowIndex, twitterIdColumnIndex, numberOfTwittersIdsInFollowerGraph))) {
					this.matrix.put(new MatrixElement(twitterIdRowIndex, twitterIdColumnIndex, numberOfTwittersIdsInFollowerGraph), numberOfElementsInRow != 0 ? Double.valueOf((1 - this.teleportationRate)/numberOfElementsInRow) : 0.0);
					System.out.println("Normalized transition matrix for row " + twitterIdRowIndex + " and column " + twitterIdColumnIndex);
				}
			}
		}
		
		
	}
	
	/**
	 * Keep doing a random follower graph walk till the probability vector reaches a stead state or maximum iterations have been reached.
	 */
	private void doRandomWalkOnFollowerGraph() {
		
		//Create page rank vector and start at the first node 
		this.probabilityDistributionVector = new double[getNumberOfTwittersIdsInFollowerGraph()];
		this.probabilityDistributionVector[0] = 1.0;
		
		TransitionProbabilityMatrix.PageRankHelper pageRankHelper = this.new PageRankHelper();
		while (pageRankHelper.getVectorSimilarity() < VECTOR_SIMILARITY_TARGET && pageRankHelper.getRandownWalkCount() < MAXIMUM_RANDOM_WALK_COUNT) {
			pageRankHelper = doOneRandomWalkStep(pageRankHelper);
		}
		
	}
	
	/**
	 * Do one random walk step and return the updated walk count and vector similarity measure between old and updated probability vector. 
	 * These will be used to decide whether to terminate the random walk or not.
	 * 
	 * @param pageRankHelper
	 * @return
	 */
	private TransitionProbabilityMatrix.PageRankHelper doOneRandomWalkStep(TransitionProbabilityMatrix.PageRankHelper pageRankHelper) {
	
		double[] newProbabilityVector= new double[getNumberOfTwittersIdsInFollowerGraph()];;
		int numberOfTwittersIdsInFollowerGraph = getNumberOfTwittersIdsInFollowerGraph();
		
		for (int probabilityVectorComponentIndex = 0; probabilityVectorComponentIndex < numberOfTwittersIdsInFollowerGraph; ++probabilityVectorComponentIndex) {
			
			multiplyWithTransitionMatrixColumn(this.probabilityDistributionVector, newProbabilityVector, probabilityVectorComponentIndex);
			
		}
		
		pageRankHelper.updateVectorSimilarity(this.probabilityDistributionVector, newProbabilityVector);
		pageRankHelper.incrementRandownWalkCount();
		this.probabilityDistributionVector = newProbabilityVector;

		return pageRankHelper;
		
	}
	
	/**
	 * Multiple the probability vector with a column of the transition probability vector.
	 * 
	 * @param oldProbabilityVector
	 * @param newProbabilityVector
	 * @param transitionMatrixColumnNumber
	 */
	private void multiplyWithTransitionMatrixColumn(double[] oldProbabilityVector, double[] newProbabilityVector, int transitionMatrixColumnNumber) {
		
		int transitionMatrixRows = getNumberOfTwittersIdsInFollowerGraph();
		for (int transitionMatrixRowIndex = 0; transitionMatrixRowIndex < transitionMatrixRows; ++transitionMatrixRowIndex) {
			
			newProbabilityVector[transitionMatrixColumnNumber] += oldProbabilityVector[transitionMatrixRowIndex] * (this.teleportationMatrixEntry +
																 (this.matrix.containsKey(new MatrixElement(transitionMatrixRowIndex, transitionMatrixColumnNumber, transitionMatrixRows)) ? 
																  this.matrix.get(new MatrixElement(transitionMatrixRowIndex, transitionMatrixColumnNumber, transitionMatrixRows)).doubleValue() : 
																  0.0));
		}
		
		
		
	}
	
	/**
	 * Helper class for controlling PageRank iterations.
	 *
	 */
	class PageRankHelper {
		
		private double vectorSimilarity;
		private int randownWalkCount;
		
		public PageRankHelper() {
			this.vectorSimilarity = 0.0;
			this.randownWalkCount = 0;
		}
		
		public double getVectorSimilarity() {
			return vectorSimilarity;
		}
		public void setVectorSimilarity(double vectorSimilarity) {
			this.vectorSimilarity = vectorSimilarity;
		}
		public int getRandownWalkCount() {
			return randownWalkCount;
		}
		public void setRandownWalkCount(int randownWalkCount) {
			this.randownWalkCount = randownWalkCount;
		}
		public void incrementRandownWalkCount() {
			++this.randownWalkCount;
		}
		public void updateVectorSimilarity(double[] oldVector, double[] newVector) {
			
			this.vectorSimilarity = getDotProduct(oldVector, newVector) / (getVectorMagnitude(oldVector) * getVectorMagnitude(newVector));
		}
		
		private double getDotProduct(double[] oldVector, double[] newVector) {
			
			double dotProduct = 0.0;
			int oldVectorLength = oldVector.length;
			
			if (oldVectorLength == newVector.length) {
				
				for (int vectorComponentIndex = 0; vectorComponentIndex < oldVectorLength; ++vectorComponentIndex) {
					dotProduct += oldVector[vectorComponentIndex] * newVector[vectorComponentIndex];
				}
				
			}
			
			return dotProduct;
			
		}
		
		
		private double getVectorMagnitude(double[] vector) {
			
			int vectorLength = vector.length;
			double vectorMagnitude = 0.0;
			
			for (int vectorComponentIndex = 0; vectorComponentIndex < vectorLength; ++vectorComponentIndex) {
				vectorMagnitude += Math.pow(vector[vectorComponentIndex], 2.0);
			}
			
			return Math.sqrt(vectorMagnitude);
			
		}
	}
	
	public int getNumberOfTwittersIdsInFollowerGraph() {
		return this.twitterIds.size();
	}
	
}
