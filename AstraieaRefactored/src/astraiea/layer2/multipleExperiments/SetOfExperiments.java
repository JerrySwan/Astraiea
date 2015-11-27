package astraiea.layer2.multipleExperiments;

import java.util.ArrayList;
import java.util.List;
import astraiea.layer2.generators.Generator;
import astraiea.layer2.generators.GeneratorOutput;

/**FIXME Refactoring 27/11 - new class. Encapsulates repetitions on running one generator
 * 
 * @author Geoffrey Neumann
 *
 */
public abstract class SetOfExperiments<T extends GeneratorOutput> {
	protected final List<Generator<T>> gens;
	private final String name;
	
	protected SetOfExperiments(Generator<T> gen){
		gens = new ArrayList<Generator<T>>();
		gens.add(gen);
		name = null;
	}
	
	protected SetOfExperiments(List<Generator<T>> gens){
		this.gens = gens;
		name = null;
	}
	
	protected SetOfExperiments(Generator<T> gen, String name){
		gens = new ArrayList<Generator<T>>();
		gens.add(gen);
		this.name = name;
	}
	
	protected SetOfExperiments(List<Generator<T>> gens, String name){
		this.gens = gens;
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	
	public abstract List<T> run(int num, long[] seeds);
	

}
