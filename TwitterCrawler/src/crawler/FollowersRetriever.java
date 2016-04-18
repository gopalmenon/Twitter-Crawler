package crawler;

import java.util.ArrayList;
import java.util.List;

import twitter4j.*;

public class FollowersRetriever {
	
	public static final long BEGINNING_CURSOR = -1;
	public static final long NO_MORE_RESULTS = 0;

	private long twitterId;
	
	public FollowersRetriever(long twitterId) {
		this.twitterId = twitterId;
	}
	
	public List<Long> getFollowers() throws TwitterException {
		
		Twitter twitter = new TwitterFactory().getInstance();
		twitter.verifyCredentials();
		long nextCursor = BEGINNING_CURSOR;
		IDs followerIds = null;
		int secondsToSleep = 0;
		List<Long> followerIdsList = new ArrayList<Long>();
		
		do {
			
			//Get the list of followers and add to the list
			followerIds = twitter.getFollowersIDs(this.twitterId, nextCursor);
			addIdsToFollowersList(followerIdsList, followerIds.getIDs());
			
			//Wait for a while if retrieval rate limit has been exceeded
			if (followerIds.getRateLimitStatus().getRemaining() == 0) {
				secondsToSleep = followerIds.getRateLimitStatus().getSecondsUntilReset() + 1;
				System.out.println("Sleeping for " + secondsToSleep + " seconds while retrieving followers for Twitter ID " + this.twitterId);
				try {
					Thread.sleep(secondsToSleep * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			//Get the next cursor to be used for retrieval of followers
			nextCursor = followerIds.getNextCursor();
			
		} while (nextCursor != NO_MORE_RESULTS);
		
		return followerIdsList;
		
	}
	
	
	/** Add contents of an array to a list
	 * @param followerIdsList
	 * @param newFollowers
	 */
	private void addIdsToFollowersList(List<Long> followerIdsList, long[] newFollowers) {
		
		for (long follower : newFollowers) {
			followerIdsList.add(Long.valueOf(follower));
		}

	}
		
}
