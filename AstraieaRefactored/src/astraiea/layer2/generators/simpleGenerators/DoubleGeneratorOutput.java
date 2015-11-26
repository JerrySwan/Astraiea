package astraiea.layer2.generators.simpleGenerators;

import astraiea.layer2.generators.GeneratorOutput;

public class DoubleGeneratorOutput extends GeneratorOutput {

	private double val;

	public DoubleGeneratorOutput(double val){
		this.val = val;
	}
	
	@Override
	public boolean getPassed() {
		return true;
	}

	@Override
	public double getValue() {
		return val;
	}

}
