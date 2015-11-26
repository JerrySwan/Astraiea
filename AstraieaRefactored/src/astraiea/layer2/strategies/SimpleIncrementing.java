package astraiea.layer2.strategies;

import java.util.List;

import org.apache.commons.math3.util.Pair;

import astraiea.layer2.generators.GeneratorOutput;

/**
 * FIXME This is just a temporary method until SPRT can be implemented and so hasn't been thoroughly documented
 * n runs are performed, if significance isn't achieved then n more runs are performed.
 * It stops when significance is achieved, either after a bon ferroni correction has been performed or just un adjusted.
 * It's safe but weak to stop after a bon ferroni correction and if this isn't used intermediate results are printed anyway so,
 * as recommended by Arcuri and Briand, all the information is given.
 * 
 * @author Geoffrey Neumann
 *
 */
//

public class SimpleIncrementing extends IncrementingStrategy {

	private boolean stopOnBonFerroni;
	private double originalPVal;
	private int numSets;

	public SimpleIncrementing(int min, int max, boolean stopOnBonFerroni) {
		super(min, max);
		this.stopOnBonFerroni = stopOnBonFerroni;
	}
	
	public SimpleIncrementing(int min, int max) {
		super(min, max);
		this.stopOnBonFerroni = false;
	}

	@Override
	public <T extends GeneratorOutput> int[] getNextIncrement(List<T> results1,
			List<T> results2, double pVal, double threshold) {
		
		if(results1.size() != results2.size())
			throw new IllegalArgumentException("Both datasets must be the same size.");
		if(results1.size() % min != 0)
			throw new IllegalArgumentException("Something went wrong. The datasets are the wrong size for the incrementing method");
		numSets = results1.size() / min;
		
		finalPVal = pVal * numSets; //Bon Ferroni adjustment
		originalPVal = pVal;
		runPVal = true;
		if((stopOnBonFerroni && finalPVal < threshold) || (!stopOnBonFerroni && originalPVal < threshold))
			return new int[]{0,0}; //won
		if(results1.size() + min > max) 
			return new int[]{0,0}; //ran out of time
		return new int[]{min,min};
	}

	@Override
	public String report() {
		return "Statistic adjusted with Bon Ferroni (With " + numSets + " sets): " + finalPVal + "\n Unadjusted Statistic: " + originalPVal;
	}

}
