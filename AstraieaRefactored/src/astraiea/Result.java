package astraiea;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import astraiea.layer2.generators.Generator;
import astraiea.util.Ordering;

/** 
 * Stores the results of carrying out the significance and the p value tests.
 * @author Geoffrey Neumann
 */

public class Result {
	
	/** effect size */
	private final double effectSize; // = Double.NaN;
	/**p value significance*/
	private final double pValue;
	/**whether dataA is greater, lower or equal to dataB*/
	private final Ordering order;
	/**whether the difference is significant*/
	private final boolean significant;
	/**threshold below which a p value is significant*/
	private final double threshold;
	private double[] cis;

	///////////////////////////////

	public Result(double effectSize, double pValue, boolean significant, double threshold, Ordering order, double[] cis) {
		this.effectSize = effectSize;
		this.pValue = pValue;
		this.threshold = threshold;
		this.significant = significant;
		this.order = order;
		this.cis = cis;
	}
	
	public Result(double effectSize, double pValue, boolean significant, double threshold, Ordering order) {
		this.effectSize = effectSize;
		this.pValue = pValue;
		this.threshold = threshold;
		this.significant = significant;
		this.order = order;	
	}

	
	/////////////////////////



	public boolean isSignificant() { return significant; }
	public double getPValue() { return pValue; }
	public Ordering getOrder() { return order; }
	public double getEffectSize(){ return effectSize; }

	///////////////////////////////
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString( this, ToStringStyle.MULTI_LINE_STYLE );
	}
	
	/**Produces printed output describing the results.
	 * 
	 * @param run the result object storing effect size, significance etc.
	 * @param data1Name output name of first dataset
	 * @param data2Name output name of second dataset
	 * @return
	 */
	public static List< String > 
	describe(Result run, String data1Name, String data2Name ) {
		
		List< String > result = new ArrayList< String >();
		
		if( !run.significant ) {
			result.add( "data is not significant (p value of " + run.pValue + ", threshold of " + run.threshold + ")" );
		}
		else {
			// effect size has only been calculated if difference is significant
			result.add( "difference is significant (p value of " + run.pValue + ", threshold of " + run.threshold + ")" );
			String ciString = "";
			double[] cis = run.getCIs();
			if(cis != null)
				ciString = ", confidence intervals (obtained by bootstrapping)= " + cis[0] + " - " + cis[1];
			result.add( "the effect size is " + run.effectSize + ciString);
			if( run.order == Ordering.LOWER )
				result.add( data1Name + " is lower than " + data2Name  );
			else if(run.order == Ordering.GREATER)
				result.add( data1Name + " is greater than " + data2Name );
			else
				result.add( data1Name + " is the same as " + data2Name );
		}
		return result;
	}

	public Result pValueAdjustedCopy(double newPVal) {
		boolean significant = newPVal < threshold;
		double effectSize = Double.NaN;
		if(significant)
			effectSize = this.effectSize;
		return new Result(effectSize, newPVal, significant, threshold, order, cis);
	}

	public double[] getCIs() {
		return cis;
	}

}

// End ///////////////////////////////////////////////////////////////
