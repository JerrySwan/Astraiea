package astraiea.layer1.pvalue;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.StatUtils;

import astraiea.util.RankCalculator;

/**
 * Brunner-Munzel p-value test.
 * 
 * @author Geoffrey Neumann
 *
 */

public final class BrunnerMunzel {
	
	/**
	 * Return the p-value of two datasets
	 * 
	 * @param dataA 
	 * @param dataB
	 * @return
	 */
	
	public static double evaluate(double[] dataA, double[] dataB) {

		RankCalculator ranks = new RankCalculator(dataA,dataB);
		
		double [] rankBothA = ranks.getCombinedRanks(0);
		double [] rankBothB = ranks.getCombinedRanks(1);
		double [] rankAloneA = ranks.getSeparateRanks(0);
		double [] rankAloneB= ranks.getSeparateRanks(1);
		
		// copied from https://github.com/cran/lawstat/blob/master/R/brunner.munzel.test.R
		final double m1 = StatUtils.mean( rankBothA );
		final double m2 = StatUtils.mean( rankBothB );
		final double n1 = dataA.length;
		final double n2 = dataB.length;
				
		double v1 = 0;
		for( int i=0; i<n1; ++i )
			v1 += Math.pow((rankBothA[i] - rankAloneA[i] - m1 + (n1 + 1)/2),2);
		v1 /= n1 - 1;

		double v2 = 0;
		for( int i =0; i<n2; ++i )
			v2 += Math.pow((rankBothB[i] - rankAloneB[i] - m2 + (n2 + 1)/2),2);
		v2 /= n2 - 1;

		final double statistic = n1 * n2 * (m2 - m1)/(n1 + n2)/Math.sqrt(n1 * v1 + n2 * v2);
		final double dfbm = Math.pow((n1 * v1 + n2 * v2),2)/(Math.pow((n1 * v1),2)/(n1 - 1) + 
				Math.pow((n2 * v2),2)/(n2 - 1));
		
		TDistribution tdis = new TDistribution(dfbm);
		final double pValue = 2 * Math.min(
			tdis.cumulativeProbability(Math.abs(statistic)), 
			(1 - tdis.cumulativeProbability(Math.abs(statistic))) );
		return pValue;
	}

}

// End ///////////////////////////////////////////////////////////////
