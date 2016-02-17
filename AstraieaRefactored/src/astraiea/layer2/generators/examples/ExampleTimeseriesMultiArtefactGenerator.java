package astraiea.layer2.generators.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import org.apache.commons.math3.util.Pair;

import astraiea.layer2.generators.Generator;
import astraiea.layer2.generators.PairGeneratorOutput;
import astraiea.layer2.generators.artefacts.ArtefactTimeseriesGenerator;
import astraiea.layer2.generators.artefacts.MultipleArtefactOutput;
import astraiea.layer2.generators.timeseries.TimeseriesGeneratorOutput;
import astraiea.util.MersenneTwister;

/**
 * The Example generator using multiple artefacts in which each artefact returns a time series
 * used in the JUnit tests.
 * 
 * @author Geoffrey Neumann
 *
 */

public class ExampleTimeseriesMultiArtefactGenerator 
implements ArtefactTimeseriesGenerator<PairGeneratorOutput> {

	private double bias;
	private double gap;
	private int duration;
	private int index;
	private int numRepeats;

	/**
	 * @param bias with what probability this class should generate higher values
	 * @param gap by how much, on average, the higher values 
	 * generated from this class should be higher than the lower ones
	 * @param duration for how many generations this generator should run for
	 * @param artefactNum 
	 */
	public ExampleTimeseriesMultiArtefactGenerator(double bias, double gap, int numRepeats, int index, int duration){
		this.bias = bias;
		this.gap = gap;
		this.numRepeats = numRepeats;
		this.duration = duration;
		this.index = index;
	}
	
	@Override
	public MultipleArtefactOutput<TimeseriesGeneratorOutput<PairGeneratorOutput>> generate(Random random) {
		List<TimeseriesGeneratorOutput<PairGeneratorOutput>> results = new ArrayList<>();
		for(int r = 0; r < numRepeats; r++){
			List<PairGeneratorOutput> results2 = new ArrayList<PairGeneratorOutput>();
			for(int i =0; i < duration; i++){
				double val = random.nextGaussian();
				//so that value increases during the course of the time series, 
				//but with it's mean around the value generated when not time series, i.e. random.nextGaussian()(+gap)
				val *= ((i/(double)duration)  * 2) + index + r;
				if(random.nextDouble() > bias)
					val += gap;
				
				boolean bool = (val > bias);
				results2.add(new PairGeneratorOutput(val,bool));
			}
			results.add(new TimeseriesGeneratorOutput<PairGeneratorOutput>(results2));
		}
		return new MultipleArtefactOutput<TimeseriesGeneratorOutput<PairGeneratorOutput>>(results);
	}
}

// End ///////////////////////////////////////////////////////////////

