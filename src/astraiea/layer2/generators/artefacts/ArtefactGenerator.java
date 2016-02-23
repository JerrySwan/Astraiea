package astraiea.layer2.generators.artefacts;

import astraiea.layer2.generators.Generator;
import astraiea.layer2.generators.GeneratorOutput;

/**Generator which generates a set MultipleArtefactOutputs.
 * 
 * @author Geoffrey Neumann
 *
 * @param <T>
 */
public interface ArtefactGenerator<T extends GeneratorOutput> extends Generator<MultipleArtefactOutput<T>> {

}
