package astraiea.layer2.generators.multipleArtefacts;

import java.util.List;
import java.util.ListIterator;
import astraiea.layer1.Layer1;
import astraiea.layer2.generators.GeneratorOutput;
import astraiea.util.DataUtil;

/**	FIXME Refactoring 27/11 - new class. Encapsulates the output produced by 
 * running multiple repeats on one artefact in problems which involve multiple artefacts.
 * Ensures compliance with Hitchhikers advice for multiple artefact problems
 * such as taking the average of these runs and performing a large number of repeats.
 * 
 * @author Geoffrey Neumann
 *
 */
public class MultipleArtefactOutput<T extends GeneratorOutput> extends GeneratorOutput {

	private static final int recommendedListSize = 1000;
	private final List<T> list;
	
	/**Initialised with a list of GeneratorOutput's where each GeneratorOutput
	 * is the output of running one test on this artefact.
	 * 
	 * @param list
	 */
	public MultipleArtefactOutput(List<T> list){
		if(list.size() < recommendedListSize) //recommended number of repeats for Hitchhikers
			Layer1.LOGGER.warning("Artefact is only repeated " + list.size() + " times. "
					+ "It is recommended that artefacts are repeated at least " + recommendedListSize + " times."); 
		this.list = list;
	}
	
	/**Passed is defined as passed > 50% though this is just an obvious solution - not from hitchhikers
	 * and more a placeholder than anything.
	 * 
	 */
	@Override
	public boolean getPassed() {
		ListIterator<T> iter = list.listIterator();
		int total = 0;
		while(iter.hasNext()){
			if(iter.next().getPassed())
				total ++;
		}
		return total > list.size()/ 2;
	}

	/**When a single value is requested it is the median of all runs, 
	 * ("average" recommended in Hitchhikers and median used for robustness).
	 * 
	 */
	@Override
	public double getValue() {
		ListIterator<T> iter = list.listIterator();
		double[] listArr = new double[list.size()];
		int i =0;
		while(iter.hasNext()){
			listArr[i] = iter.next().getValue();
			i++;
		}
		return DataUtil.getMedian(listArr);
	}
	
	/**
	 * 
	 * @return number of repeats
	 */
	public int getRepeats(){
		return list.size();
	}

}