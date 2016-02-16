package astraiea.layer2.generators;

import java.util.List;
import java.util.Random;

/**FIXME Refactoring 27/11 - "extends GeneratorOutput" added.

 * Superclass for any generator.
 * @author Geoffrey Neumann, Jerry Swan 
 */

public interface Generator< T extends GeneratorOutput > {

	T generate( Random random );

}

// End ///////////////////////////////////////////////////////////////

