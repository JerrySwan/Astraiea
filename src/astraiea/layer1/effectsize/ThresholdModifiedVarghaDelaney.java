package astraiea.layer1.effectsize;

import astraiea.Ordering;
import astraiea.layer2.generators.GeneratorOutput;

/**
 * An implementation of the Modified Vargha-Delaney test 
 * that simply discounts all values that are above or below a specified threshold.
 * 
 * @author Geoffrey Neumann
 *
 */

public class ThresholdModifiedVarghaDelaney extends ModifiedVarghaDelaney {
	
	/** value which data has to be greater than or equal to to be considered valid */
	private double thresholdUp = Double.NEGATIVE_INFINITY;
	/** value which data has to be less than or equal to be considered valid */
	private double thresholdDown = Double.POSITIVE_INFINITY;
	
	///////////////////////////////

	/**
	 * Use either thresholdUp or thresholdDown.
	 * 
	 * @param thresh value to set chosen threshold to
	 * @param up if true setting thresholdUp, else setting thresholdDown
	 */
	public ThresholdModifiedVarghaDelaney(double thresh, boolean up){
		if(up)
			thresholdUp = thresh;
		else
			thresholdDown = thresh;
	}
	
	/**
	 * Use both thresholdUp and thresholdDown.
	 * 
	 * @param thresholdUp
	 * @param thresholdDown
	 */
	public ThresholdModifiedVarghaDelaney(double thresholdUp, double thresholdDown){
		this.thresholdUp = thresholdUp;
		this.thresholdDown = thresholdDown;
	}
	
	//FIXME Refactoring 27/11 - now with GeneratorOutput
	@Override
	public Ordering compare(GeneratorOutput tVal, GeneratorOutput cVal) {
		double tDouble = tVal.getValue();
		double cDouble = cVal.getValue();
		
		if((tDouble < thresholdUp && cDouble < thresholdUp) || (tDouble > thresholdDown && cDouble > thresholdDown))
			return Ordering.EQUAL;
		else if(tDouble < thresholdUp || cDouble > thresholdDown)
			return Ordering.LOWER;
		else if(tDouble > thresholdDown || cDouble < thresholdUp)
			return Ordering.GREATER;
		else return standardCompare(tVal,cVal);
	}

	@Override
	public String describe() {
		return (!Double.isInfinite(thresholdUp) ? 
			"\nAll results below " + thresholdUp + " are converted to 0." :
				"") + 
			(!Double.isInfinite(thresholdDown) ? 
			"\nAll results above " + thresholdDown + " are converted to infinity." :
				"");
	}
}

// End ///////////////////////////////////////////////////////////////
