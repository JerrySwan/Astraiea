package astraiea.layer1.varghaDelaney;

import astraiea.layer2.generators.GeneratorOutput;
import astraiea.util.Ordering;

/**An implementation of the Modified Vargha Delaney test that simply discounts all values that are above or below a specified threshold.
 * 
 * @author Geoffrey Neumann
 *
 */

public class ThresholdVDMod extends VDmod {
	/**value which data has to be greater than or equal to to be considered valid*/
	private double thresholdUp = Double.NEGATIVE_INFINITY;
	/**value which data has to be less than or equal to be considered valid*/
	private double thresholdDown = Double.POSITIVE_INFINITY;

	/**Constructor if setting only thresholdUp or thresholdDown.
	 * 
	 * @param thresh value to set chosen threshold to
	 * @param up if true setting thresholdUp, else setting thresholdDown
	 */
	public ThresholdVDMod(double thresh, boolean up){
		if(up)
			thresholdUp = thresh;
		else
			thresholdDown = thresh;
	}
	
	/**Constructor if setting both thresholdUp and thresholdDown.
	 * 
	 * @param thresholdUp
	 * @param thresholdDown
	 */
	public ThresholdVDMod(double thresholdUp, double thresholdDown){
		this.thresholdUp = thresholdUp;
		this.thresholdDown = thresholdDown;
	}
	
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

}
