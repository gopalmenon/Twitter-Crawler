package controller;

public class RestartQueueEntry {

	private int levelNumber;
	private long twitterId;
	
	public RestartQueueEntry(int levelNumber, long twitterId) {
		this.levelNumber = levelNumber;
		this.twitterId = twitterId;
	}
	
	public RestartQueueEntry(String savedRestartQueueEntry) {
		String[] savedEntryParts = savedRestartQueueEntry.split(",");
		try {
		this.levelNumber = Integer.parseInt(savedEntryParts[0].trim());
		this.twitterId = Long.parseLong(savedEntryParts[1].trim());
		} catch (NumberFormatException e) {
			System.err.println("NumberFormatException thrown while trying to parse " + savedRestartQueueEntry);
		}
	}
	

	public int getLevelNumber() {
		return levelNumber;
	}

	public long getTwitterId() {
		return twitterId;
	}
	
	@Override 
	public String toString() {
		StringBuffer returnValue = new StringBuffer();
		returnValue.append(this.levelNumber).append(", ").append(this.twitterId);
		return returnValue.toString();
	}
	
}
