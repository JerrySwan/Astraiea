package astraiea.layer2.generators.timeseries;

import astraiea.layer2.generators.Generator;
import astraiea.layer2.generators.GeneratorOutput;

/**Generator for generating a set of Timeseries's.
 * 
 * @author Geoffrey Neumann
 *
 * @param <T>
 */
public interface TimeseriesGenerator<T extends GeneratorOutput> extends Generator<TimeseriesGeneratorOutput<T>> {

}
