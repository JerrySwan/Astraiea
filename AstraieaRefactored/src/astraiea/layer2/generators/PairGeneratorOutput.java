package astraiea.layer2.generators;


/**	FIXME Refactoring 27/11 - new class. Just encapsulates a GeneratorOutput that is a double boolean pair.
 * 
 * @author Geoffrey Neumann
 *
 */
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
