package astraiea.layer2.generators.artefacts;

import astraiea.layer2.generators.GeneratorOutput;
import astraiea.layer2.generators.timeseries.TimeseriesGeneratorOutput;

/**An ArtefactGenerator where the output of each artefact is a TimeSeries.
 * 
 * @author Geoffrey Neumann
 *
 * @param <T>
 */
public interface ArtefactTimeseriesGenerator<T extends GeneratorOutput> extends ArtefactGenerator<TimeseriesGeneratorOutput<T>> {

}
