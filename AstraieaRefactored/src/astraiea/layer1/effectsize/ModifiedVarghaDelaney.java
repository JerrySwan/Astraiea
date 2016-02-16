package astraiea.layer1.effectsize;

import astraiea.Ordering;
import astraiea.layer2.generators.GeneratorOutput;

/**
 * Implementation of the Modified Vargha Delaney from .... . 
 * This modifies the comparison between individual results that is part of the Vargha Delaney process to ensure that this 
 * comparison is problem specific and excludes irrelevant differences. This class should be extended for your problem.
 * 
 * @author Geoffrey Neumann
 *
 */

public abstract class ModifiedVarghaDelaney {

	//FIXME Refactoring 27/11 - now with GeneratorOutput allows more complex comparisons.
	/**
	 * Will be called every time a result from one dataset is compared to one from another dataset 
	 * as part of the Vargha Delaney comparison. Compares the two results to indicate which one is greater. 
	 * Overwrite this method for a problem specific comparison.
	 * 
	 * @param tVal first result
	 * @param cVal second result
	 * @return GREATER if first result is greater, EQUAL if both results are equal and LOWER if first result is lower
	 */
	public abstract Ordering compare(GeneratorOutput tVal, GeneratorOutput cVal);
	
	/**The standard comparison whereby a result is "greater" simply if it has a higher value,
	 * "lower" if it has a lower value and "equal" only if both values are exactly the same.
	 * 
	 * @param tVal
	 * @param cVal
	 * @return
	 */
	public Ordering standardCompare(GeneratorOutput tVal, GeneratorOutput cVal){
		if(tVal.getValue() > cVal.getValue())
			return Ordering.GREATER;
		else if(tVal.getValue() == cVal.getValue())
			return Ordering.EQUAL;
		else 
			return Ordering.LOWER;
	}

	/**
	 * 
	 * @return textual description of this modification for user output
	 */
	public abstract String describe();
}

// End ///////////////////////////////////////////////////////////////

