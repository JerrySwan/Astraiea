package astraiea.layer2.multipleExperiments;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import astraiea.layer2.generators.Generator;
import astraiea.layer2.generators.GeneratorOutput;
import astraiea.layer2.generators.MultipleArtefactOutput;
import astraiea.util.MersenneTwister;

/**Encapsulates repetitions on running one generator on multiple artefacts.
 * 
 * @author Geoffrey Neumann
 *
 */
public class SetOfMultiArtefactExperiments<T extends GeneratorOutput> extends SetOfExperiments<MultipleArtefactOutput<T>> {

	public SetOfMultiArtefactExperiments(List<Generator<MultipleArtefactOutput<T>>> gens) {
		super(gens);
	}
	
	public SetOfMultiArtefactExperiments(List<Generator<MultipleArtefactOutput<T>>> gens, String name){
		super(gens,name);
	}

	@Override
	public List<MultipleArtefactOutput<T>> run(int num, long[] seeds){
		assert(num == seeds.length);
	
		List<MultipleArtefactOutput<T>> results = new ArrayList<MultipleArtefactOutput<T>>();
		
		//run each generator once
		for(int i =0; i < num; i++){
			results.add(gens.get(i).generate(new MersenneTwister(seeds[i])));
		}
		
		return results;
	}

}
