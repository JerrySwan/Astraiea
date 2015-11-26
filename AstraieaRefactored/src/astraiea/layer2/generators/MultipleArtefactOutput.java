package astraiea.layer2.generators;

import java.util.List;
import java.util.ListIterator;

import astraiea.layer1.Layer1;
import astraiea.layer2.Layer2;
import astraiea.util.DataUtil;

//A list of outputs with added instructions to get median and warn if too small
public class MultipleArtefactOutput<T extends GeneratorOutput> extends GeneratorOutput {

	private static final int recommendedListSize = 1000;
	private final List<T> list;
	
	public MultipleArtefactOutput(List<T> list){
		if(list.size() < recommendedListSize)
			Layer1.LOGGER.warning("Artefact is only repeated " + list.size() + " times. "
					+ "It is recommended that artefacts are repeated at least " + recommendedListSize + " times."); 
		this.list = list;
	}
	
	@Override
	//FIXME - should this be implemented?
	public boolean getPassed() {
		ListIterator<T> iter = list.listIterator();
		int total = 0;
		while(iter.hasNext()){
			if(iter.next().getPassed())
				total ++;
		}
		return total > list.size()/ 2;
	}

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
	
	public int getRepeats(){
		return list.size();
	}

}
