package astraiea.test.layer2.generators;

import java.util.Random;

import astraiea.layer2.generators.Generator;
import astraiea.layer2.generators.simpleGenerators.PairGeneratorOutput;

/**The Example generator for just vanilla (double, boolean pair valued) generator 
 * rather than a timeseries or generator with multiple artefacts used in the JUnit tests.
 * 
 * @author Geoffrey Neumann
 *
 */
public class ExampleDatapointGenerator implements Generator<PairGeneratorOutput> {

	private double bias;
	private double gap;

	/**
	 * 
	 * @param bias with what probability this class should generate higher values
	 * @param gap by how much, on average, the higher values 
	 * generated from this class should be higher than the lower ones
	 */
	public ExampleDatapointGenerator(double bias, double gap){
		this.bias = bias;
		this.gap = gap;
	}
	
	@Override
	public PairGeneratorOutput generate(Random random) {
		double val = random.nextGaussian();
		if(random.nextDouble() > bias)
			val += gap;
		boolean bool = (val > bias);
		return new PairGeneratorOutput(val,bool);
	}


}
