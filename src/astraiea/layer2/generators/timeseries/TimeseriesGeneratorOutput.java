package astraiea.layer2.generators.timeseries;

import java.util.List;
import java.util.ListIterator;

import astraiea.layer2.generators.GeneratorOutput;

/**
 * FIXME Refactoring 27/11 - new class. Encapsulates a time series. Is a list of GeneratorOutputs.
 * @author Geoffrey Neumann
 *
 * @param <T>
 */

public class TimeseriesGeneratorOutput<T extends GeneratorOutput> extends GeneratorOutput {
	
	private final List<T> list;
	
	///////////////////////////////
	
	public TimeseriesGeneratorOutput(List<T> list){
		this.list = list;
	}

	/**
	 * By default just get the result at the final time interval.
	 */
	
	@Override
	public boolean getPassed(){
		return list.get(list.size() - 1).getPassed();
	}
	
	@Override
	public double getValue(){
		return list.get(list.size() - 1).getValue();
	}

	/**Gets the length of time taken to pass.
	 * 
	 * @return
	 */
	public int getTimeToPass(){
		ListIterator<T> iter = list.listIterator();
		int i = 0;
		while(iter.hasNext()){
			GeneratorOutput next = iter.next();
			if(next.getPassed()){
				return i;
			}
			i++;
		}
		return -1;
	}
	
	/**
	 * Gets time at a given generation/time index
	 */
	
	public boolean getIntermediatePassed(int index){
		return list.get(index).getPassed();
	}
	
	@Override
	public String toString(){
		ListIterator<T> iter = list.listIterator();
		String out = "";
		while(iter.hasNext()){
			out = out + iter.next().toString() + ",";
		}
		return out;
	}
}

// End ///////////////////////////////////////////////////////////////
