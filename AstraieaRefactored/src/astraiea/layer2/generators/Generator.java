package astraiea.layer2.generators;

import java.util.List;
import java.util.Random;

/**
 * Superclass for any generator.
 * @author Geoffrey Neumann, Jerry Swan 
 */

public interface Generator< T extends GeneratorOutput> {

	T generate( Random random );

}

// End ///////////////////////////////////////////////////////////////

