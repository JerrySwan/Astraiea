package astraiea.layer2.generators;


public abstract class GeneratorOutput {
	
	public abstract boolean getPassed();
	
	public abstract double getValue();
	
	/**Gets the length of time taken to pass.
	 * 
	 * @return
	 */
	public int getTimeToPass(){
		return 1;
	}
	
	public boolean getIntermediatePassed(int index){
		return getPassed();
	}
	
}
