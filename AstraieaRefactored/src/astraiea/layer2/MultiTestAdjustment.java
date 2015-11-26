package astraiea.layer2;

import java.util.List;

import astraiea.Result;

/**For adjustments such as Hochberg or Bon Ferroni that make results fair when multiple comparisons are performed.
 * 
 * @author Geoffrey Neumann
 *
 */
public interface MultiTestAdjustment {
	
	public List<ResultSet> adjust(List<ResultSet> results);
}
