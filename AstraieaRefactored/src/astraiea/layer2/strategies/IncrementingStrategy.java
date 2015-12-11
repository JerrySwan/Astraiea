package astraiea.layer2.strategies;

import java.util.List;

import astraiea.layer2.generators.GeneratorOutput;

/**Encodes strategies for adding more data if significance is not achieved after the first set
 * 
 * @author Geoffrey Neumann
 *
 */
public abstract class IncrementingStrategy {
	/**initial number of runs*/
	protected int min;
	/**maximum number of runs that might increment to*/
	protected int max;
	/**p value at the end of the sequence of incrementing*/
	protected double finalPVal;
	/**if another p value is needed*/
	protected boolean runPVal;


	public IncrementingStrategy(int min, int max){
		this.min = min;
		this.max = max;
	}
	
	public int getMin() {
		return min;
	}
	
	public int getMax() {
		return max;
	}

	/**Gets the quantity of runs that should be performed for each
	 * generator next increment (possibly 0).
	 * 
	 * @param results1 results from last run (generator 2)
	 * @param results2 results from last run (generator 2)
	 * @param pVal p value from last run
	 * @param threshold below which significance is considered sufficient for stopping
	 * @return
	 */
	public abstract<T extends GeneratorOutput> int[] getNextIncrement(
			List<T> results1,
			List<T> results2, 
			double pVal, 
			double threshold);

	public abstract String report();

	public boolean runPVal() {
		return runPVal;
	}




}
