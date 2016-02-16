package astraiea.layer2.strategies;

import java.util.List;

import org.apache.commons.math3.util.Pair;

import astraiea.layer2.generators.GeneratorOutput;

/**
 * Test performs a fixed number of runs (no incrementing).
 * @author Geoffrey Neumann
 *
 */
public class NoIncrementing extends IncrementingStrategy {

	public NoIncrementing(int num) {
		super(num,num);//min and max are the same
	}

	@Override
	public String report() {
		return "Statistic: " + finalPVal;
	}

	@Override
	public<T extends GeneratorOutput> int[] getNextIncrement(List<T> results1, List<T> results2, double pVal,
			double threshold) {
		return new int[]{0,0};
	}
}

// End ///////////////////////////////////////////////////////////////

