package astraiea.layer2.generators;

/**FIXME Refactoring 27/11 - new class. Output from Generator
 * 
 * @author Geoffrey Neumann
 *
 */
public abstract class GeneratorOutput {
	
	public abstract boolean getPassed();
	
	public abstract double getValue();
	
	/**Gets the length of time taken to pass though by default this is just 1.
	 * 
	 * @return
	 */
	public int getTimeToPass(){
		return 1;
	}
	
	/**Get any pass result before the test has finished, by default it makes no difference.
	 * 
	 * @param index
	 * @return
	 */
	public boolean getIntermediatePassed(int index){
		return getPassed();
	}
	
}
