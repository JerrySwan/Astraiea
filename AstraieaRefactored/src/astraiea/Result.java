package astraiea;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/** 
 * Stores the results of carrying out the significance and the p value tests.
 * @author Geoffrey Neumann
 */

public class Result {
	
	private final double effectSize;
	private final double pValue;
	private final Ordering order; 		// whether dataA is greater, lower or equal to dataB
	private final boolean significant; 	// whether the difference is significant
	private final double threshold;		// threshold below which a p value is significant*/
	private double[] cis;
	private final String name1;
	private final String name2;

	///////////////////////////////

	public Result(String name1, String name2, double effectSize, 
			double pValue, boolean significant, 
			double threshold, Ordering order, double[] cis) {
		this.name1 = name1;
		this.name2 = name2;
		this.effectSize = effectSize;
		this.pValue = pValue;
		this.threshold = threshold;
		this.significant = significant;
		this.order = order;
		this.cis = cis;
	}
	
	public Result(String name1, String name2, double effectSize, 
			double pValue, boolean significant, 
			double threshold, Ordering order) {
		this.name1 = name1;
		this.name2 = name2;
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
	
	/**
	 * Produces printed output describing the results.
	 * 
	 * @param run the result object storing effect size, significance etc.
	 * @param data1Name output name of first dataset
	 * @param data2Name output name of second dataset
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
			
			if(!Double.isNaN(run.effectSize)){
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
		}
		return result;
	}

	public Result pValueAdjustedCopy(double newPVal) {
		boolean significant = newPVal < threshold;
		double effectSize = Double.NaN;
		if(significant)
			effectSize = this.effectSize;
		return new Result(name1, name2, effectSize, newPVal, significant, threshold, order, cis);
	}

	public double[] getCIs() { return cis; }

	public boolean isaComparisonOf(String name1, String name2) {
		return (this.name1.equals(name1) && this.name2.equals(name2)) || 
				(this.name1.equals(name2) && this.name2.equals(name1));
	}
}

// End ///////////////////////////////////////////////////////////////
