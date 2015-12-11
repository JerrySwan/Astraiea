package astraiea.layer2;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import astraiea.Result;

/**Implementation of the Bon Ferroni adjustment for multiple comparisons.
 * 
 * @author Geoffrey Neumann
 *
 */
public class BonFerroni implements MultiTestAdjustment {

	@Override
	public List<Result> adjust(List<Result> results){
		double num = results.size();
		ListIterator<Result> resIter = results.listIterator();
		List<Result> newResults = new ArrayList<Result>();
		while(resIter.hasNext()){
			Result res = resIter.next();
			double adjustedPVal = res.getPValue() * num;
			newResults.add(res.pValueAdjustedCopy(adjustedPVal));
		}
		return newResults;
	}

	@Override
	public String getName() {
		return "Bon Ferroni";
	}

	
}
