package astraiea.layer2.multipleExperiments;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import org.apache.commons.math3.util.Pair;
import astraiea.layer2.MultiTestAdjustment;
import astraiea.layer2.ResultSet;
import astraiea.layer2.generators.Generator;
import astraiea.layer2.generators.GeneratorOutput;

/**The set of generators being compared and information about how they should be compared.
 * 
 * @author Geoffrey Neumann
 *
 * @param <T>
 */
public class SetOfComparisons<T extends GeneratorOutput> {
	
	/**the set of generators being compared*/
	private final List<SetOfExperiments<T>> experiments;
	/**an adjustment, e.g. bon ferroni or hochberg, to correct for the problem of multiple tests*/
	private final MultiTestAdjustment adjust;
	/**if every generator is compared against just one of the generators then this provides the index within gens of that one generator.
	 * -1 if every generator is being compared against every other generator*/
	private final SetOfExperiments<T> mainGen;
	
	/**For just comparing two generations.
	 * 
	 * @param genA
	 * @param genB
	 */
	public SetOfComparisons(SetOfExperiments<T> expA, SetOfExperiments<T> expB){
		 experiments = new ArrayList<SetOfExperiments<T>>();
		 experiments.add(expA);
		 experiments.add(expB);
		 mainGen = null;
		 adjust = null;
	}

	/**For comparing multiple generators, each against every other one.
	 * 
	 * @param g list of generators
	 */
	public SetOfComparisons(List<SetOfExperiments<T>> exps){
		experiments = exps;
		adjust = null;
		mainGen = null;
	}

	/**For comparing multiple generators, each against just one.
	 * 
	 * @param g list of generators
	 * @param mainGen index of the generator that each is compared against
	 */
	public SetOfComparisons(List<SetOfExperiments<T>> exps, SetOfExperiments<T> mainGen){
		experiments = exps;
		adjust = null;
		this.mainGen = mainGen;
		checkMainGen(mainGen);
	}

	/**For comparing multiple generators, each against every other one, with an adjustment.
	 * 
	 * @param g list of generators
	 * @param adjust
	 */
	public SetOfComparisons(List<SetOfExperiments<T>> exps, MultiTestAdjustment adjust){
		assert(exps.size() > 2);//need more than two generators to apply multi test adjustments
		experiments = exps;
		this.adjust = adjust;
		mainGen = null;
	}
	
	/**For comparing multiple generators, each against just one, with an adjustment.
	 * 
	 * @param g list of generators
	 * @param mainGen index of the generator that each is compared against
	 * @param adjust
	 */
	public SetOfComparisons(List<SetOfExperiments<T>> exps, SetOfExperiments<T> mainGen, MultiTestAdjustment adjust){
		assert(exps.size() > 2);
		
		experiments = exps;
		this.adjust = adjust;
		this.mainGen = mainGen;
		checkMainGen(mainGen);
	}

	private void checkMainGen(SetOfExperiments<T> mainGen) {
		if(!experiments.contains(mainGen))
			throw new IllegalArgumentException("The main generator was not found in the list of generators.");		
	}

	
	
	/**Carry out the adjustment specified by 'adjust'.
	 * 
	 * @param results
	 * @return
	 */
	public List<ResultSet> multipleTestAdjustment(List<ResultSet> results){
		if(adjust == null)
			return results;
		return adjust.adjust(results);
	}

	/**Gets a set of pairs of indexes representing every comparison which should be carried out.
	 * 
	 * @return
	 */
	public List<Pair<SetOfExperiments<T>, SetOfExperiments<T>>> getTestPairs() {
		List<Pair<SetOfExperiments<T>, SetOfExperiments<T>>> pairs = new ArrayList<Pair<SetOfExperiments<T>, SetOfExperiments<T>>>();
		if(mainGen != null){//compare one algorithm against each of the alternatives
			ListIterator<SetOfExperiments<T>> expsIter = experiments.listIterator();
			while(expsIter.hasNext()){
				SetOfExperiments<T> exp = expsIter.next();
				if(exp != mainGen)
					pairs.add(new Pair<SetOfExperiments<T>, SetOfExperiments<T>>(mainGen, exp));
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

	public List<SetOfExperiments<T>> getAllGenerators() {
		return experiments;
	}

	public int getIndexOf(SetOfExperiments<T> exp) {
		int ind = experiments.indexOf(exp);
		if(ind == -1)
			throw new IllegalArgumentException("Attempted to get index of SetOfExperiments which does not exist.");
		return ind;
	}

}
