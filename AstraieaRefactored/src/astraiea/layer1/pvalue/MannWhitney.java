package astraiea.layer1.pvalue;

import java.util.List;

import jsc.onesample.WilcoxonTest;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

import astraiea.outputformat.Report;
import astraiea.util.DataUtil;
import astraiea.util.RankCalculator;

/**
 * Wilcoxon Rank Sum/Mann-Whitney U test.
 * 
 * @author Geoffrey Neumann
 *
 */

public final class MannWhitney {

	/**
	 * Return the p value of two datasets, dataA and dataB
	 * 
	 * @param dataA
	 * @param dataB
	 * @return
	 */
	public static double evaluate( double[] dataA, double[] dataB ) {
		//carry out an exact test only if there are no ties or zeros because this is the behaviour of the R method that is our oracle whereas it is not the default behaviour of the java method.
		//also conforming to the default behavior of r, carry out an exact test only if n < 50, important because wilcoxonSignedRankTest can become very slow otherwise.
		if(DataUtil.hasTiesOrZeros(dataA,dataB) || dataA.length > 30){
			// out.printWarning("Because the data contains ties or zeros, a non exact version of the Mann Whitney U test was used");
			Report.warning( "Since the data contains ties or zeros, a non exact version of the Mann Whitney U test was used" );
			return nonExactTest(dataA, dataB);
		}
		else {
			return new MannWhitneyUTest().mannWhitneyUTest(dataA, dataB);
		}
	}

	/**
	 * Runs a non-exact version of this test for when the data contains ties or zeroes.
	 * 
	 * @param dataA
	 * @param dataB
	 * @return
	 */
	private static double nonExactTest(double[] dataA, double[] dataB) {
		//copied from the non exact method R uses for this test in https://svn.r-project.org/R/trunk/src/library/stats/R/wilcox.test.R
		RankCalculator rankCalc = new RankCalculator(dataA,dataB);
		double[] ranks = rankCalc.getCombinedRanks(0);//get combined ranks for both datasets
		double statistic = 0;
		int nA = dataA.length;
		int nB = dataB.length;
		for( int i =0; i < nA; ++i  )
			statistic += ranks[i]; 

		statistic -= nA * (nA + 1)/2;
		List<Integer> ties = rankCalc.getTies();
		// ListIterator<Integer> tiesIter = ties.listIterator();
		double z =  statistic - nA * nB / 2;
		double tiesTotal = 0;
		for( Integer val : ties ){
			// double val = tiesIter.next();
			tiesTotal += (Math.pow(val, 3) - val);
		}
		
		final double sigma = Math.sqrt( (nA * nB / 12) * ((nA + nB + 1) - tiesTotal / ((nA + nB) * (nA + nB - 1))));
		if( z > 0 )//continuity correction
			z -= 0.5;
		else
			z += 0.5;
		
		z /= sigma;
		
        // get the p-value. 
		// Code taken from org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest
        NormalDistribution standardNormal = new NormalDistribution(0, 1); 
        final double pval = 2*Math.min(standardNormal.cumulativeProbability(z),standardNormal.cumulativeProbability(-z));
		return pval;
	}

	/**
	 * Return the p value from comparing a dataset with a single value.
	 * 
	 * @param dataA array of results
	 * @param dataDet single result
	 * @return
	 */
	public static double evaluate(double[] dataA, double dataDet) {
		WilcoxonTest wilcox = new WilcoxonTest(dataA, dataDet);
		return wilcox.approxSP();
	}
}

// End ///////////////////////////////////////////////////////////////
