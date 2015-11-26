package astraiea.layer2.strategies;

import java.util.List;

import org.apache.commons.math3.util.Pair;

import astraiea.layer2.generators.GeneratorOutput;
import astraiea.util.Distribution;

/**Encodes strategies for adding more data if significance is not achieved after the first set
 * 
 * @author Geoffrey Neumann
 *
 */
public abstract class IncrementingStrategy {
	protected int min;
	protected int max;
	protected double finalPVal;
	protected boolean runPVal;

	
	public IncrementingStrategy(int min, int max){
		this.min = min;
		this.max = max;
	}
	
	public int getMinRuns() {
		return min;
	}
	
	public int getMaxRuns() {
		return max;
	}

	public abstract<T extends GeneratorOutput> int[] getNextIncrement(List<T> results1,
			List<T> results2, double pVal, double threshold);

	public abstract String report();

	public boolean runPVal() {
		return runPVal;
	}




}
