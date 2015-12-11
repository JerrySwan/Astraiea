package astraiea.layer2.strategies;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import astraiea.layer2.generators.GeneratorOutput;

/** 
 * Stores information about what approach should be taken if censored tests are used
 * but the first one does not return significance.
 * FIXME Refactoring 27/11 - Now non stateful. 
 * Censoring is a multi step process that has to separately execute and return each step as the outcome of 
 * other parts of Astraiea (tests in layer1, incrementing etc.) determines whether the next step is carried out.
 * This was previously done with a stateful counter. Now the counter is a parameter each step.
 * 
 * @author Geoffrey Neumann
 *
 */
public class CensoringStrategy {

	private final boolean censoring;
	/**after the initial test at the final generation this stores which time point censoring should take place at*/
	private final int[] points;
	/**if true then after the initial test and the additional censoring points in "points" have been exhausted,
	 * will attempt at test using the lengths of time taken to reach a passing grade as a set of integers*/
	private final boolean switching;
	/**when 'counter' equals 'maxCounter' there are no more tests*/
	private final int maxCounter;

	/**Constructor for just using a single censored test at the final generation/time point.
	 * 
	 */
	public CensoringStrategy(boolean censoring){
		points = new int[0];
		switching = false;
		maxCounter = 0;
		this.censoring = censoring;
	}

	/**Constructor for using additional censoring points other than the final time point.
	 * 
	 * @param points the addition points
	 * @param switching whether the switching technique should be used too
	 */
	public CensoringStrategy(int[] points, boolean switching) {
		this.points = points;
		this.switching = switching;
		maxCounter = points.length + (switching ? 1 : 0);
		this.censoring = true;
	}
	
	/**Constructor which possibly sets the switching technique without additional points.
	 * 
	 * @param switching
	 */
	public CensoringStrategy(boolean switching, boolean censoring) {
		this.points = new int[0];
		this.switching = switching;
		maxCounter = switching ? 1 : 0;
		this.censoring = censoring;
	}
	
	/**Returns if censoring is used at all. 
	 * Will always return true in this class as the subclass 'NoCensoring' is used for no censoring.
	 * 
	 * @return
	 */
	public boolean isCensoring(){
		return censoring;
	}

	/**Returns if there are more censoring strategies to try.
	 * 
	 * @return
	 */
	public boolean hasMoreSteps(int counter) {
		return counter <= maxCounter;
	}

	/**Gets whether the current strategy is to switch to the non censored test.
	 * 
	 * @return
	 */
	public boolean usingTimesToPass(int counter) {
		return counter == maxCounter && switching;
	}


	/**Returns a textual description of strategy which has just been used.
	 * @return
	 */
	public String describeLastStrategy(int lastCounter) {
		if(!isCensoring() || !complexStrategy())
			return "";
		else{
			if(lastCounter == 0)
				return "The initial censoring strategy was used (using a dichotomous test on the results at the end of the time series). ";
			if(usingTimesToPass(lastCounter))
				return "The time to pass strategy for censored data was used "
						+ "(using a non dichotomous p value test on the length of time that each successful run took to reach a passing value). ";
			else
				return "An intermediate, artificial censoring point was used of " + getCensoringPoint(lastCounter) + ".";
		}
	}

	/**Returns true if more strategies than just the initial final point in the test strategy will be used.
	 * 
	 * @return
	 */
	public boolean complexStrategy() {
		return points.length > 0 || switching;
	}

	/**Returns how long a test takes to achieve a passing result.
	 * 
	 * @param results
	 * @return
	 */
	public<T extends GeneratorOutput> double[] getTimesToPass(List<T> results) {
		ArrayList<Integer> times = new ArrayList<Integer>();
		ListIterator<T> resultsIter = results.listIterator();
		
		int count =0;
		while(resultsIter.hasNext()){
			int timeToPass = resultsIter.next().getTimeToPass();
			if(timeToPass != -1){
				times.add(timeToPass);
				count++;
			}
		}
		
		double[] timesArr = new double[count];
		for(int i =0 ; i < count; i++){
			timesArr[i] = times.get(i);
		}
		return timesArr;
	}


	/**Returns either the final result if this is first (or default) censoring or an intermediate result
	 * (from an intermediate interval in a time series)
	 * if relevant and required by subsequent steps.
	 * 
	 * @param results
	 * @param counter
	 * @return
	 */
	public<T extends GeneratorOutput> boolean[] censor(List<T> results, int counter) {
		boolean[] resArr = new boolean[results.size()];
		ListIterator<T> resultsIter = results.listIterator();
		
		int censorPoint = counter == 0 ? -1 : points[counter - 1];
		int i =0;
		while(resultsIter.hasNext()){
			if(counter == 0)
				resArr[i] = resultsIter.next().getPassed();
			else
				resArr[i] = resultsIter.next().getIntermediatePassed(censorPoint);
			i++;
		}
		return resArr;
	}

	/**Returns which interval in the time series was used for this step
	 * (or -1 if not relevant or just default final interval used)
	 * 
	 * @param counter the step
	 * @return
	 */
	public int getCensoringPoint(int counter) {
		if(counter == 0)
			return -1;
		return points[counter - 1];
	}
}