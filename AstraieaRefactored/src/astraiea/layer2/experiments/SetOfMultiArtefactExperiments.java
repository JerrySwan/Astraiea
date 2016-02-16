package astraiea.layer2.experiments;

import java.util.ArrayList;
import java.util.List;

import astraiea.layer2.generators.Generator;
import astraiea.layer2.generators.GeneratorOutput;
import astraiea.layer2.generators.artefacts.ArtefactGenerator;
import astraiea.layer2.generators.artefacts.MultipleArtefactOutput;
import astraiea.util.MersenneTwister;

/**FIXME Refactoring 27/11 - new class. Runs repetitions of running one generator on multiple artefacts.
 * 
 * @author Geoffrey Neumann
 *
 */
public class SetOfMultiArtefactExperiments<T extends GeneratorOutput> extends SetOfExperiments<MultipleArtefactOutput<T>> {
	
	public SetOfMultiArtefactExperiments(List<? extends ArtefactGenerator<T>> gens, String name){
		super(gens,name);
	}

	@Override
	public List<MultipleArtefactOutput<T>> run(int num, long[] seeds){
		assert(num == seeds.length);
	
		List<MultipleArtefactOutput<T>> results = new ArrayList<MultipleArtefactOutput<T>>();
		
		//run each generator once. 
		//any repeats within an artefact will be handled by client code in the Generator subclass.
		//see MultipleArtefactOutput
		for(int i =0; i < num; i++){
			results.add(gens.get(i).generate(new MersenneTwister(seeds[i])));
		}
		
		return results;
	}

}
