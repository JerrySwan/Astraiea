package astraiea.layer2;

import org.apache.commons.math3.util.Pair;

import astraiea.Result;
import astraiea.layer2.generators.Generator;

public class ResultSet {
	private final String gen1;
	private final String gen2;
	private final Result res;

	public ResultSet(String gen1, String gen2, Result res){
		this.gen1 = gen1;
		this.gen2 = gen2;
		this.res = res;
	}

	public Result getRes() {
		return res;
	}
	
	//TODO - Result.toString()
	@Override
	public String toString(){
		return "Result of comparing " + gen1 + " with " + gen2 + ":" + res.toString();
	}

	public boolean isaComparisonOf(String g1,
			String g2) {
		return ((g1.equals(gen1) && g2.equals(gen2)) || (g1.equals(gen2) && g2.equals(gen1)));
	}


	public ResultSet pValueAdjustedCopy(double adjustedPVal) {
		return new ResultSet(gen1,gen2,res.pValueAdjustedCopy(adjustedPVal));
	}
	
	
}

