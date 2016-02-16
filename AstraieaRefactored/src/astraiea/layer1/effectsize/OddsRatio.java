package astraiea.layer1.effectsize;

import astraiea.Ordering;

/**
 * Odds Ratio effect-size test.
 * 
 * @author Geoffrey Neumann
 *
 */

public final class OddsRatio {
	
	/** an arbitrary constant, 0.5 was the example given in Arcuri2012 */
	public static final double ARCURI_CONSTANT = 0.5;
	
	///////////////////////////////

	public static Ordering getOrder(double effectSize){
		if(Double.isNaN(effectSize))
			return null;
		if(effectSize > 1)
			return Ordering.GREATER;
		else if(effectSize == 1)
			return Ordering.EQUAL;
		else
			return Ordering.LOWER;	
	}
	
	/**
	 * Return the odds ratio for two datasets.
	 * 
	 * @param totalOnA number of passes for dataA
	 * @param totalOffA number of passes for dataB
	 * @param totalOnB number of failures for dataA
	 * @param totalOffB number of failures for dataB
	 * @return
	 */
	public static double evaluate(int totalOnA, int totalOffA, int totalOnB, int totalOffB ) {
		// implementation taken from Arcuri2012
		final int a = totalOnA;
		final int b = totalOnB;
		final int n = totalOnA + totalOffA;
		final int m = totalOnB + totalOffB;
		return ((a + ARCURI_CONSTANT) / (n + ARCURI_CONSTANT - a)) / ((b + ARCURI_CONSTANT) / (m + ARCURI_CONSTANT - b));
	}
	
	/**Return the matched odds ration, used when the two datasets are paired.
	 * 
	 * @param onAoffB number of pairs in which the sample from dataA passes and the sample from dataB fails
	 * @param onBoffA number of pairs in which the sample from dataB passes and the sample from dataA fails
	 * @return
	 */
	public static double evaluateMatched(int onAoffB, int onBoffA){
		return (onAoffB + ARCURI_CONSTANT)/(onBoffA + ARCURI_CONSTANT);
	}
}

// End ///////////////////////////////////////////////////////////////

