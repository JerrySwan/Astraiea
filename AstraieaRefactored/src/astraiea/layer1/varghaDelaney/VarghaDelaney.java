package astraiea.layer1.varghaDelaney;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import astraiea.layer2.generators.GeneratorOutput;
import astraiea.util.Ordering;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

//FIXME Refactoring 27/11 - in all methods replaced lists of doubles with lists of GeneratorOutput

/**
 * Vargha Delaney effect-size test.
 * 
 * @author Geoffrey Neumann
 *
 */
public final class VarghaDelaney {
	
	//FIXME Should probably be passed in but the number of parameters passed to Layer1 is getting excessive. Will explain.
	private static final int numCISamples = 1000;

	/**Interpret the effect size
	 * 
	 * @param effectSize
	 * @return
	 */
	public static Ordering getOrder(double effectSize){
		if(effectSize > 0.5)
			return Ordering.GREATER;
		else if(effectSize == 0.5)
			return Ordering.EQUAL;
		else
			return Ordering.LOWER;	
	}

	///////////////////////////////
	
	/**
	 * Return the effect size of two datasets, dataA and dataB.
	 * 
	 * @param results1
	 * @param results2
	 * @param paired 
	 * @param vdmod 
	 * @return
	 */
	public static<T extends GeneratorOutput> double evaluate(List<T> results1, List<T> results2, boolean paired, VDmod vdmod) {
		int lenA = results1.size();
		int lenB = results2.size();
		double[] drawsWins = getDrawsWins(results1,results2,paired,vdmod);
		double draws = drawsWins[0];//treatment draws
		double wins = drawsWins[1];//treatment wins
		
		double product = lenA * lenB;
		
		if(paired){
			if(lenA != lenB)
				throw new IllegalArgumentException("The length of the two datasets must be equal");
			
		    return wins + 0.5 * draws;
		}
		else{
			double PSc = wins/product;
		    return PSc + (0.5 * draws/product);
	    
		}
	}
	

	
	/**Counts the number of times treatment obtains a better result than control and
	 * the number of times that the same result is obtained.
	 * 
	 * @param results1
	 * @param results2
	 * @param paired 
	 * @return
	 */
	private static<T extends GeneratorOutput> double[] getDrawsWins(List<T> results1, List<T> results2, boolean paired, VDmod vdmod) {
		int lenT = results1.size();
		int lenC = results2.size();
		double draws = 0;
		double wins = 0;
		for(int t = 0; t < lenT; t++){
			GeneratorOutput tVal = results1.get(t);
			if(paired){
				GeneratorOutput cVal = results2.get(t);
				Ordering comp = compare(tVal,cVal,vdmod);
				if(comp == Ordering.GREATER)
					wins++;
				else if(comp == Ordering.EQUAL)
					draws++;
			}
			else{
				for(int c = 0; c < lenC; c++){
					GeneratorOutput cVal = results2.get(c);
					Ordering comp = compare(tVal,cVal,vdmod);
					if(comp == Ordering.GREATER)
						wins++;
					else if(comp == Ordering.EQUAL)
						draws++;
				}
			}
		}
		return new double[]{draws, wins};
	}

	/**Compares a treatment and a control value
	 * 
	 * @param tVal treatment value
	 * @param cVal control value
	 * @param vdmod modified version of Vargha Delaney comparison used to compare them or "null" if a standard comparison is used
	 * @return
	 */
	private static<T extends GeneratorOutput> Ordering compare(T tVal, T cVal, VDmod vdmod) {
		if(vdmod == null){
			if(tVal.getValue() > cVal.getValue())
				return Ordering.GREATER;
			else if(tVal.getValue() == cVal.getValue())
				return Ordering.EQUAL;
			else
				return Ordering.LOWER;
		}
			
		return  vdmod.compare(tVal,cVal);
	}

	/**Gets the confidence intervals of the effect size using bootstrapping.
	 * 
	 * @param effSize
	 * @param dataA
	 * @param dataB
	 * @param upper upper bound for confidence interval
	 * @param lower lower bound for confidence interval
	 * @param ran
	 * @param vdmod
	 * @param paired 
	 * @return
	 */
	public static<T extends GeneratorOutput> double[] getConfidenceInterval(double effSize, List<T> dataA, List<T> dataB, double upper, double lower, Random ran, boolean paired, VDmod vdmod){
		int lenA = dataA.size();
		int lenB = dataB.size();
		
		//gets random sets of indices - these are points in the original data array which I will pick from in order to get the data I'm using to get confidence intervals
		int[] bootIndiciesA = getBootIndices(lenA, lenA * numCISamples, ran);
		int[] bootIndiciesB = getBootIndices(lenB, lenB * numCISamples, ran);
		
		double[] results = new double[numCISamples + 1];
		results[0] = effSize;
		for(int i =0; i < numCISamples; i++){
			List<T> listA = new ArrayList<T>();
			List<T> listB = new ArrayList<T>();
			for(int i2 =0; i2 < lenA ; i2++){
				//matches the way that indices are used in R, 
				//for n bootstrap repeats, the first test uses indices 0,n * 1,n * 2,... and the second one uses 1,(n * 1) + 1,(n * 2) + 1,..., 
				//the kth test uses k-1,(n * 1) + (k - 1),(n * 2) + (k - 1),...
				//get the elements of our original data at our chosen index and add to listA and listB
				listA.add(dataA.get(bootIndiciesA[(i2 * numCISamples) + i]));
			}
			for(int i2 =0; i2 < lenB ; i2++)
				listB.add(dataB.get(bootIndiciesB[(i2 * numCISamples) + i]));
			
			//make array of indices
			int newLenA = listA.size();
			int newLenB = listB.size();
			List<GeneratorOutput> newDataA = new ArrayList<GeneratorOutput>();
			List<GeneratorOutput> newDataB = new ArrayList<GeneratorOutput>();
			for(int i2 =0; i2 < newLenA; i2++)
				newDataA.add(listA.get(i2));
			for(int i2 =0; i2 < newLenB; i2++)
				newDataB.add(listB.get(i2));
			
			//get the effect size for the newly selected data
			results[i + 1] = evaluate(newDataA, newDataB, paired, vdmod);
		}
		
		double[] ci = new double[2];
		
		//get the confidence intervals as percentiles from the set of new effect sizes
		Percentile perc = new Percentile();
		ci[0] = perc.evaluate(results, lower);
		ci[1] = perc.evaluate(results, upper);
		return ci;
	}
	
	/**	Gets random sets of indices.
	 * @param len the length of the array will be selecting from i.e. number of original data points
	 * @param total number of indices needed in total
	 * @param ran
	 * @return
	 */
	private static int[] getBootIndices(int len, int total,Random ran) {
		int[] out = new int[total];
		for(int i =0; i < total; i++)
			out[i] = ran.nextInt(len);
		return out;
	}
		
}

// End ///////////////////////////////////////////////////////////////
