package astraiea.test.layer2.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import astraiea.layer2.generators.Generator;
import astraiea.layer2.generators.GeneratorOutput;
import astraiea.layer2.generators.PairGeneratorOutput;
import astraiea.layer2.generators.artefacts.ArtefactGenerator;
import astraiea.layer2.generators.artefacts.MultipleArtefactOutput;

/**The Example multiple artefact generator used in the JUnit tests.
 * 
 * @author Geoffrey Neumann
 *
 */
public class ExampleDatapointMultiArtefactGenerator implements ArtefactGenerator<PairGeneratorOutput> {

	private final double bias;
	private final double gap;
	private final int numRepeats;
	private final int index;
	
	/**
	 * @param bias with what probability this class should generate higher values
	 * @param gap by how much, on average, the higher values 
	 * @param numRepeats number of repeated runs should be performed with this artefact 
	 * @param index the number of this artefact within the set of artefacts
	 */
	public ExampleDatapointMultiArtefactGenerator(double bias, double gap, int numRepeats, int index){
		this.bias = bias;
		this.gap = gap;
		this.numRepeats = numRepeats;
		this.index = index;
	}
	
	@Override
	public MultipleArtefactOutput<PairGeneratorOutput> generate(Random random) {
		List<PairGeneratorOutput> out = new ArrayList<PairGeneratorOutput>();
		for(int i = 0; i < numRepeats; i++){
			double val = random.nextGaussian();
			if(random.nextDouble() > bias)
				val += gap + index;
			boolean bool = (val > bias);
			out.add(new PairGeneratorOutput(val,bool));
		}
		return new MultipleArtefactOutput<PairGeneratorOutput>(out);
	}


}

// End ///////////////////////////////////////////////////////////////

