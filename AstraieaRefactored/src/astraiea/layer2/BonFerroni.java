package astraiea.layer2;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import astraiea.Result;

public class BonFerroni implements MultiTestAdjustment {

	@Override
	public List<ResultSet> adjust(List<ResultSet> results){
		double num = results.size();
		ListIterator<ResultSet> resIter = results.listIterator();
		List<ResultSet> newResults = new ArrayList<ResultSet>();
		while(resIter.hasNext()){
			ResultSet res = resIter.next();
			double adjustedPVal = res.getRes().getPValue() * num;
			newResults.add(res.pValueAdjustedCopy(adjustedPVal));
		}
		return newResults;
	}

}
