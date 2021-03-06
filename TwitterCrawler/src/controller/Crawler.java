package controller;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayDeque;
import java.util.List;

import crawler.FollowersRetriever;
import file.io.FileWriter;
import twitter4j.TwitterException;

public class Crawler {

	public static final int DEFAULT_CRAWL_TO_LEVEL = 1;
	public static final int INSUFFICIENT_AUTHORITY_TO_PROFILE = 401;
	public static final int PROFILE_DOES_NOT_EXIST = 404;
	public static final int SLEEP_TIME_IN_SECONDS = 15 * 60;
	public static final int NAP_TIME_IN_SECONDS = 1 * 60;
	public static final String FOLLOWERS_FOLDER = "followers/";
	public static final String FRIENDS_FOLDER = "friends/";
	
	
	public static void main(String[] args) {
		Crawler crawler = new Crawler();
		crawler.startCrawling(args);
	}
	
	private void startCrawling(String[] args) {
		
		int crawlToLevel = DEFAULT_CRAWL_TO_LEVEL;
		if (args.length > 0 && args[0].trim().length() > 0) {
			try {
				crawlToLevel = Integer.parseInt(args[0].trim());
				System.out.println("Crawl to level " + crawlToLevel);
			} catch (NumberFormatException e) {
				System.err.println("Could not parse input parameter " + args[0]);
			}
		}
		
		boolean crawlSuccess = false;
		RestartController restartController = RestartController.getInstance();
		while (!crawlSuccess) {
			ArrayDeque<RestartQueueEntry> startingSet = restartController.getStartingSet();
			crawlSuccess = crawlTwitterFollowers(startingSet, crawlToLevel);
		}

	}
	
	private boolean crawlTwitterFollowers(ArrayDeque<RestartQueueEntry> startingSet, int crawlToLevel) {
		
		boolean crawlSuccess = true;
		String followersListFileName = null;
		
		while (!startingSet.isEmpty()) {
			
			RestartQueueEntry restartQueueEntry = startingSet.remove();
			if (restartQueueEntry.getLevelNumber() >= crawlToLevel) {
				System.out.println("Target crawl level is " + crawlToLevel + " and level reached is " + restartQueueEntry.getLevelNumber() + ". Crawl process terminated.");
				break;
			}
			
			FileWriter fileOperations = new FileWriter();
			followersListFileName = FOLLOWERS_FOLDER + restartQueueEntry.getTwitterId() + ".txt";
			if (!fileOperations.fileExists(followersListFileName)) {
				try {
					FollowersRetriever followersRetriever = new FollowersRetriever(restartQueueEntry.getTwitterId());
					List<Long> followersList = followersRetriever.getFollowers();
					//Save the list of followers
					fileOperations.saveTwitterIdsList(followersListFileName, followersList);
					putFollowersOnQueue(startingSet, followersList, restartQueueEntry.getLevelNumber() + 1);
				} catch (TwitterException e) {
					System.err.println("TwitterException thrown while finding followers for ID " + restartQueueEntry.getTwitterId() + " at level " + restartQueueEntry.getLevelNumber());
					RestartController restartController = RestartController.getInstance();
					//If exception is thrown due to insufficient authority to twitter profile, skip it
					if (e.getStatusCode() == INSUFFICIENT_AUTHORITY_TO_PROFILE || e.getStatusCode() == PROFILE_DOES_NOT_EXIST) {
						System.err.println("Not authorized to ID " + restartQueueEntry.getTwitterId() + " at level " + restartQueueEntry.getLevelNumber() + ". Twitter ID skipped");
						System.err.println("Going to sleep for " + NAP_TIME_IN_SECONDS + " seconds.");
						try {
							restartController.sleepAtLeast(NAP_TIME_IN_SECONDS *1000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					} else {
						startingSet.addFirst(restartQueueEntry);
						System.err.println("Going to sleep for " + SLEEP_TIME_IN_SECONDS + " seconds.");
						try {
							restartController.sleepAtLeast(SLEEP_TIME_IN_SECONDS * 1000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
					restartController.saveCurrentState(startingSet);
					crawlSuccess = false;
					break;
				}  catch (FileNotFoundException | UnsupportedEncodingException e) {
					System.err.println("Exception thrown while saving followers for ID " + restartQueueEntry.getTwitterId() + " at level " + restartQueueEntry.getLevelNumber());
				} 
			}
		}
		
		return crawlSuccess;
		
	}
	
	private void putFollowersOnQueue(ArrayDeque<RestartQueueEntry> queue, List<Long> followersList, int level) {
		for (long twitterId : followersList) {
			queue.add(new RestartQueueEntry(level, twitterId));
		}
		
	}

}
