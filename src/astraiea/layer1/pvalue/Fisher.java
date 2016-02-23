package astraiea.layer1.pvalue;

import edu.northwestern.at.utils.math.statistics.*;

/**
 * Fisher p-value test.
 * 
 * @author Geoffrey Neumann
 *
 */
public final class Fisher {

	/**
	 * Return the p value for two datasets.
	 * 
	 * @param totalOnA number of passes for dataA
	 * @param totalOffA number of passes for dataB
	 * @param totalOnB number of failures for dataA
	 * @param totalOffB number of failures for dataB
	 * @return
	 */
	public static double evaluate(int totalOnA, int totalOffA, int totalOnB, int totalOffB) {
		return FishersExactTest.fishersExactTest(totalOnA, totalOffA, totalOnB, totalOffB)[0];
	}

}

// End ///////////////////////////////////////////////////////////////

