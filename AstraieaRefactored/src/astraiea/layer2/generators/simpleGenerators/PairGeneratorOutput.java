package astraiea.layer2.generators.simpleGenerators;

import astraiea.layer2.generators.GeneratorOutput;

public class PairGeneratorOutput extends GeneratorOutput {
	private double val;
	private boolean passed;

	public PairGeneratorOutput(double val, boolean passed){
		this.val = val;
		this.passed = passed;
	}
	
	@Override
	public boolean getPassed() {
		return passed;
	}

	@Override
	public double getValue() {
		return val;
	}
	
	@Override
	public String toString() {
		return "(" + val + "," + passed + ")";
	}

}
