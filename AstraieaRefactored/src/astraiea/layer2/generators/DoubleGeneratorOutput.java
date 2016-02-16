package astraiea.layer2.generators;


/**	FIXME Refactoring 27/11 - new class. Just encapsulates a GeneratorOutput 
 * that is just a double and implicitly considered to always pass.
 * 
 * @author Geoffrey Neumann
 *
 */
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
