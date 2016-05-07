import pagerank.TransitionProbabilityMatrix;
import twitter4j.TwitterException;


public class TestC {

	public static void main(String[] args) throws TwitterException {
		
		TransitionProbabilityMatrix transitionProbabilityMatrix = new TransitionProbabilityMatrix("/Users/gopalmenon/Desktop/followers", 0.14, 10);
		System.out.println("TransitionProbabilityMatrix has elements " + transitionProbabilityMatrix.getNumberOfTwittersIdsInFollowerGraph());

	}

}
