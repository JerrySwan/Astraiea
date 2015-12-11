package astraiea.layer2.generators.multipleArtefacts;

import astraiea.layer2.generators.GeneratorOutput;
import astraiea.layer2.generators.timeseries.Timeseries;

/**An ArtefactGenerator where the output of each artefact is a TimeSeries.
 * 
 * @author Geoffrey Neumann
 *
 * @param <T>
 */
public interface ArtefactTimeseriesGenerator<T extends GeneratorOutput> extends ArtefactGenerator<Timeseries<T>> {

}
