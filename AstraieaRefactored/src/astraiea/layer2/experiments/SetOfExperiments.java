package astraiea.layer2.experiments;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import astraiea.layer2.generators.Generator;
import astraiea.layer2.generators.GeneratorOutput;

/**
 * FIXME Refactoring 27/11 - new class. Runs repetitions of an experiment.
 * 
 * @author Geoffrey Neumann
 *
 */
public abstract class SetOfExperiments<T extends GeneratorOutput> {
	
	protected final List<Generator<T>> gens = new ArrayList<Generator<T>>();
	private final String name;
	
	///////////////////////////////
	
	/**
	 * Run the same generator repeatedly. FIXME: JS - this runs an experiment only once.
	 * 
	 * @param gen single generator
	 * @param name
	 */
	protected SetOfExperiments(Generator<T> gen, String name){
//		ListIterator<? extends Generator<T>> gensIter = gens.listIterator();
//		while(gensIter.hasNext())
//			this.gens.add(gensIter.next());
		gens.add(gen);
		this.name = name;
	}
	
	/**
	 * Each repetition runs different related experiment.
	 * 
	 * @param gens list of generators
	 * @param name
	 */
	protected SetOfExperiments(List<? extends Generator<T>> gens, String name){
		ListIterator<? extends Generator<T>> gensIter = gens.listIterator();
		while(gensIter.hasNext())
			this.gens.add(gensIter.next());		
		this.name = name;
	}
	
	public String getName() { return name; }
	public abstract List<T> run(int num, long[] seeds);
}

// End ///////////////////////////////////////////////////////////////
