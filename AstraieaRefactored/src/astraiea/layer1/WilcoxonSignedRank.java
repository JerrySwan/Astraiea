package astraiea.layer1;

import java.util.List;

import javanpst.data.structures.dataTable.DataTable;
import javanpst.tests.oneSample.wilcoxonTest.WilcoxonTest;

import org.apache.commons.math3.distribution.NormalDistribution;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import astraiea.util.DataUtil;
import astraiea.util.RankCalculator;

/**
 * Wilcoxon signed rank Test
 * 
 * @author Geoffrey Neumann
 *
 */

public final class WilcoxonSignedRank{

	public static double evaluate(double[] dataA, double[] dataB ) {
		if( dataA.length != dataB.length)
			throw new IllegalArgumentException("Unequal number of samples. For paired tests the number of samples in both datasets must be equal.");
		
		int n = dataA.length;
		double[] dataSub = new double[n];
		for( int i=0; i<n; ++i )
			dataSub[i] = dataA[i] - dataB[i];
		
		// Carry out an exact test only if there are no ties or zeros 
		// because this is the behaviour of the R method that is our Oracle 
		// whereas it is not the default behaviour of the Apache Commons method.
		// Aalso conforming to the default behaviour of R, carry out an exact test 
		// only if n < 50. Important because wilcoxonSignedRankTest can 
		// become very slow otherwise.
		

		//return x2;
		
		if( DataUtil.hasTiesOrZeros(dataSub) || n > 30 ) {
			Layer1.LOGGER.warning( "Since the data contains ties or zeros, "
					+ "a non exact version of the Wilcoxon Signed Rank test was used" );
			return nonExactTest(dataSub);
		}
		else{
			return exactTest(dataA, dataB);
		}
	}

	private static double exactTest(double[] dataA, double[] dataB) {
		//convert data into n row, 2 column table where each row is a pair of values, one from each data set
		double[][] tblData = new double[dataA.length][2];
		for(int i =0; i < tblData.length; i++){
			tblData[i][0] = dataA[i];
			tblData[i][1] = dataB[i];
		}
		DataTable tbl = new DataTable(tblData);
		WilcoxonTest wil = new WilcoxonTest(tbl);
		
		//get p value
		wil.doTest();
		return wil.getExactDoublePValue();
	}

	/**
	 * Equivalent to R non exact test
	 * 
	 * @param dataSub an array that is dataA - dataB
	 * @return
	 */
	private static double nonExactTest(double[] dataSub) {
		
		// copied from from R - https://svn.r-project.org/R/trunk/src/library/stats/R/wilcox.test.R
		dataSub = DataUtil.removeZeros(dataSub);
		
		final int n = dataSub.length;
		double[] dataSubAbs = new double[n];
		for( int i=0; i<n; ++i )
			dataSubAbs[i] = Math.abs(dataSub[i]);
		
		// rank the absolute version of the dataA - dataB array on its own
		RankCalculator rankCalc = new RankCalculator(dataSubAbs);
		double [] ranks = rankCalc.getRanks();
		double totalR = 0;
		
		// add to dataList all values to establish which are > 0		
		// List<Double> dataList = new ArrayList<Double>();
		Multiset<Double> dataList = HashMultiset.create();		
		for( int i=0; i<n; ++i )
			dataList.add(dataSub[i]);
		
		for( int i=0; i<n; ++i ) { 
			//add the ranks to totalR of all positive values
			final double val = dataSubAbs[i];
			if( val != 0 )
				totalR += ranks[i];
			if( dataList.contains( -val ) ){
				// Don't add the rank (i.e. subtract the rank that we've just added) 
				// if it is associated with a negative value
				totalR -= ranks[i];
				// dataList.remove(new Double(-val));
				dataList.remove( -val );				
			}
		}
		
		// Add up ties
		List<Integer> ties = rankCalc.getTies();
		// ListIterator<Integer> tiesIter = ties.listIterator();
		double tiesTotal = 0;
		// while( tiesIter.hasNext() ){
		for( Integer val : ties ){		
			// double val = tiesIter.next();
			tiesTotal += (Math.pow(val, 3) - val);
		}
		
        final double sigma = Math.sqrt(n * (n + 1) * (2 * n + 1) / 24 - tiesTotal / 48);
        double z = totalR - n * (n + 1)/4.0;
        // continuity correction - assuming two sided
        if(z > 0)
        	z -= 0.5;
        else
        	z += 0.5;
        z /= sigma;
        
        // get the p-value. Code taken from: 
        // org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest
        NormalDistribution standardNormal = new NormalDistribution(0, 1); 
        return 2*Math.min(standardNormal.cumulativeProbability(z),
        	standardNormal.cumulativeProbability(-z));
	}
}

// End ///////////////////////////////////////////////////////////////

