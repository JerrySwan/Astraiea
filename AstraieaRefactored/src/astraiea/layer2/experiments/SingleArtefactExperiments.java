package astraiea.layer2.experiments;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import astraiea.layer2.generators.Generator;
import astraiea.layer2.generators.GeneratorOutput;
import astraiea.util.MersenneTwister;

/**FIXME Refactoring 27/11 - new class. Runs repetitions of running one generator with only one artefact.
 * 
 * @author Geoffrey Neumann
 *
 */
public class SingleArtefactExperiments<T extends GeneratorOutput> extends SetOfExperiments<T> {

	public SingleArtefactExperiments(Generator<T> gen, String name){
		super(gen,name);
	}
	
	@Override
	public List<T> run(int num, long[] seeds){
		assert(num == seeds.length);
		
		List<T> results = new ArrayList<T>();
		//run the only one generator multiple times
		for(int i =0; i < num; i++){
			results.add(gens.get(0).generate(new MersenneTwister(seeds[i])));
		}
		
		return results;
	}


}