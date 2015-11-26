package astraiea.layer1;

import jsc.contingencytables.ContingencyTable2x2;
import jsc.contingencytables.McNemarTest;

/**
 * McNemar p-value test.
 * 
 * @author Geoffrey Neumann
 *
 */
public final class McNemar {

	/**Get the p value.
	 * 
	 * @param passBoth
	 * @param failBoth
	 * @param passAonly
	 * @param passBonly
	 * @return
	 */
	public static double evaluate(int passBoth, int failBoth, int passAonly, int passBonly) {
		ContingencyTable2x2 tbl = new ContingencyTable2x2(passBoth,passAonly,passBonly,failBoth);
		//DataTable tbl = new DataTable(new double[][]{{passBoth,passAonly},{passBonly,failBoth}});
		McNemarTest test = new McNemarTest(tbl);
		return test.getSP();
	}

}

// End ///////////////////////////////////////////////////////////////

