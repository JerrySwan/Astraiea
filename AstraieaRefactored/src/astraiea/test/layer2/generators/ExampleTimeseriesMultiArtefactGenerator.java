package astraiea.test.layer2.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import org.apache.commons.math3.util.Pair;

import astraiea.layer2.generators.Generator;
import astraiea.layer2.generators.Timeseries;
import astraiea.layer2.generators.simpleGenerators.PairGeneratorOutput;
import astraiea.util.MersenneTwister;

/**The Example used in the JUnit tests.
 * 
 * @author Geoffrey Neumann
 *
 */
public class ExampleTimeseriesMultiArtefactGenerator implements Generator<Timeseries<PairGeneratorOutput>>{

	private double bias;
	private double gap;
	private int duration;
	private int artefactNum;

	/**
	 * 
	 * @param bias with what probability this class should generate higher values
	 * @param gap by how much, on average, the higher values 
	 * generated from this class should be higher than the lower ones
	 * @param duration for how many generations this generator should run for
	 * @param artefactNum 
	 */
	public ExampleTimeseriesMultiArtefactGenerator(double bias, double gap, int duration, int artefactNum){
		this.bias = bias;
		this.gap = gap;
		this.duration = duration;
		this.artefactNum = artefactNum;
	}
	
	@Override
	public Timeseries<PairGeneratorOutput> generate(Random random) {
		List<PairGeneratorOutput> results = new ArrayList<PairGeneratorOutput>();
		for(int i =0; i < duration; i++){
			double val = random.nextGaussian();
			if(random.nextDouble() > bias)
				val += gap;
			//so that value increases during the course of the time series, 
			//but with it's mean around the value generated when not time series, i.e. random.nextGaussian()(+gap)
			val *= (((double)i/(double)duration)  * 2) + artefactNum;
			boolean bool = (val > bias);
			results.add(new PairGeneratorOutput(val,bool));
		}
		return new Timeseries<PairGeneratorOutput>(results);
	}

	
}
