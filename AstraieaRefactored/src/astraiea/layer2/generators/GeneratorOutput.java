package astraiea.layer2.generators;

/**
 * FIXME Refactoring 27/11 - new class. Output from Generator
 * 
 * @author Geoffrey Neumann
 *
 */

public abstract class GeneratorOutput {
	
	/**
	 * 
	 * @return
	 */
	public abstract boolean getPassed();
	
	public abstract double getValue();
	
	/**Gets the length of time taken to pass, 
	 * which is 1 unless this is a timeseries.
	 * Returns -1 if never passes.
	 * 
	 * @return
	 */
	public int getTimeToPass(){
		return getPassed() ? 1 : -1;
	}
	
	/**Get the pass or fail value for an intermediate point in time before the 
	 * end of the test (if this information is available (e.g. for a timeseries),
	 * otherwise just returns the same value as getPassed() ).
	 * 
	 * @param index
	 * @return
	 */
	public boolean getIntermediatePassed(int index){
		return getPassed();
	}
	
}
