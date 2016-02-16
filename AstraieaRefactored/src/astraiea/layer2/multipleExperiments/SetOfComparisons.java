package astraiea.layer2.multipleExperiments;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import org.apache.commons.math3.util.Pair;

import astraiea.Result;
import astraiea.layer2.MultiTestAdjustment;
import astraiea.layer2.generators.Generator;
import astraiea.layer2.generators.GeneratorOutput;

/**
 * The set of generators being compared and information about how they should be compared.
 * 
 * @author Geoffrey Neumann
 *
 * @param <T>
 */

public class SetOfComparisons<T extends GeneratorOutput> {
	
	/**the set of generators being compared, wrapped in "experiments" to enable them to run multiple times*/
	private final List<SetOfExperiments<T>> experiments;
	/**an adjustment, e.g. bon ferroni or hochberg, to correct for the problem of multiple tests*/
	private final MultiTestAdjustment adjust;
	/**if every generator is compared against just one of the generators then this provides the index within experiments of that one generator.
	 * null if every generator is being compared against every other generator*/
	private final SetOfExperiments<T> mainExp;
	
	/**
	 * For just comparing two generators.
	 * 
	 * @param genA
	 * @param genB
	 */
	public SetOfComparisons(SetOfExperiments<T> expA, SetOfExperiments<T> expB){
		if(expA.getName().equals(expB.getName()))
			throw new IllegalArgumentException("Duplicate generator names.");

		
		 experiments = new ArrayList<SetOfExperiments<T>>();
		 experiments.add(expA);
		 experiments.add(expB);
		 mainExp = null;
		 adjust = null;
	}

	/**
	 * For comparing multiple generators, each against every other one.
	 * 
	 * @param g list of generators
	 */
	public SetOfComparisons(List<SetOfExperiments<T>> exps){
		if(containsDuplicates(exps))
			throw new IllegalArgumentException("Duplicate generator names.");

		experiments = exps;
		adjust = null;
		mainExp = null;
	}

	/**
	 * For comparing multiple generators, each against just one.
	 * 
	 * @param g list of generators
	 * @param mainExp index of the generator that each is compared against
	 */
	public SetOfComparisons(List<SetOfExperiments<T>> exps, SetOfExperiments<T> mainExp){
		if(containsDuplicates(exps))
			throw new IllegalArgumentException("Duplicate generator names.");

		
		experiments = exps;
		adjust = null;
		this.mainExp = mainExp;
		checkMainExp(mainExp);
	}

	/**
	 * For comparing multiple generators, each against every other one, with an adjustment.
	 * 
	 * @param g list of generators
	 * @param adjust
	 */
	public SetOfComparisons(List<SetOfExperiments<T>> exps, MultiTestAdjustment adjust){
		if(exps.size() <= 2)
			throw new IllegalArgumentException("Number of generators must be at least 2 for multiple test adjustments.");
		if(containsDuplicates(exps))
			throw new IllegalArgumentException("Duplicate generator names.");

		experiments = exps;
		this.adjust = adjust;
		mainExp = null;
	}
	
	/**
	 * For comparing multiple generators, each against just one, with an adjustment.
	 * 
	 * @param g list of generators
	 * @param mainGen index of the generator that each is compared against
	 * @param adjust
	 */
	public SetOfComparisons(List<SetOfExperiments<T>> exps, SetOfExperiments<T> mainGen, MultiTestAdjustment adjust){
		if(exps.size() <= 2)
			throw new IllegalArgumentException("Number of generators must be at least 2 for multiple test adjustments.");
		if(containsDuplicates(exps))
			throw new IllegalArgumentException("Duplicate generator names.");
		experiments = exps;
		this.adjust = adjust;
		this.mainExp = mainGen;
		checkMainExp(mainGen);
	}

	/**
	 * Checks that there are not two or experiments in a set with the same name
	 * 
	 * @param exps
	 * @return true if there are duplicates
	 */
	private boolean containsDuplicates(List<SetOfExperiments<T>> exps) {
		ListIterator<SetOfExperiments<T>> expIter = exps.listIterator();
		int i =0;
		while(expIter.hasNext()){
			i++;
			SetOfExperiments<T> next = expIter.next();
			ListIterator<SetOfExperiments<T>> expIter2 = exps.listIterator(i);
			while(expIter2.hasNext()){
				if(next.getName().equals(expIter2.next().getName()))
					return true;
			}
		}
		return false;
	}

	/**
	 * Checks that the experiment specified to be "main" is actually in the set of experiments.
	 * Throws exception if not.
	 * 
	 * @param mainGen 
	 */
	private void checkMainExp(SetOfExperiments<T> mainGen) {
		if(!experiments.contains(mainGen))
			throw new IllegalArgumentException("The main generator was not found in the list of generators.");		
	}

	
	
	/**
	 * Carry out the adjustment specified by 'adjust'.
	 * 
	 * @param results
	 * @return
	 */
	public List<Result> multipleTestAdjustment(List<Result> results){
		if(adjust == null)
			return results;
		return adjust.adjust(results);
	}

	/**
	 * Gets a set of pairs of experiments covering every comparison which should be carried out.
	 * Either all against all or all against mainExp.
	 * 
	 * @return
	 */
	public List<Pair<SetOfExperiments<T>, SetOfExperiments<T>>> getTestPairs() {
		List<Pair<SetOfExperiments<T>, SetOfExperiments<T>>> pairs = new ArrayList<Pair<SetOfExperiments<T>, SetOfExperiments<T>>>();
		if(mainExp != null){//compare one algorithm against each of the alternatives
			ListIterator<SetOfExperiments<T>> expsIter = experiments.listIterator();
			while(expsIter.hasNext()){
				SetOfExperiments<T> exp = expsIter.next();
				if(exp != mainExp)
					pairs.add(new Pair<SetOfExperiments<T>, SetOfExperiments<T>>(mainExp, exp));
			}
		}
		else{//compare every 2 way comparison
			for(int i =0; i < experiments.size(); i++){
				for(int i2 = i + 1; i2 < experiments.size(); i2++){
					pairs.add(new Pair<SetOfExperiments<T>, SetOfExperiments<T>>(experiments.get(i),experiments.get(i2)));
				}
			}
		}
		return pairs;
	}

	public List<SetOfExperiments<T>> getExperiments() { return experiments;	}

	/**
	 * Gets the index of a set of experiments (generator) within the complete set of generators.
	 * 
	 * @param exp
	 * @return
	 */
	public int getIndexOf(SetOfExperiments<T> exp) {
		int ind = experiments.indexOf(exp);
		if(ind == -1)
			throw new IllegalArgumentException("Attempted to get index of SetOfExperiments which does not exist.");
		return ind;
	}

	public MultiTestAdjustment getAdjust() { return adjust;	}
}

// End ///////////////////////////////////////////////////////////////
