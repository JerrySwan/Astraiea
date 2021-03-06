package astraiea;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Logger;

import astraiea.layer1.Layer1;
import astraiea.layer1.effectsize.ModifiedVarghaDelaney;
import astraiea.layer2.MultiTestAdjustment;
import astraiea.layer2.experiments.SetOfComparisons;
import astraiea.layer2.generators.GeneratorOutput;
import astraiea.layer2.strategies.CensoringStrategy;

/**
 * FIXME Refactoring 27/11 - new class. Just a minor change - separates out printing output to log file from
 * layer 2 as it was getting too complicated when stored just in layer2.
 * 
 * @author Geoffrey Neumann
 *
 */

public class Report {

	public static Logger LOGGER = Logger.getLogger( Report.class.getName() );

	/**
	 * Sets up the LOGGER for this class using a default configuration.
	 * 
	 * @param filename file where output of logger should be stored.
	 * @throws IOException
	 */
	public static void setupLaTeXLoggers(String filename) throws IOException {		
		Formatter formatter = new LaTeXLogFormatter();		
		Handler fileHandler = new FileHandler( filename);
		fileHandler.setFormatter(formatter);
		LOGGER.addHandler( fileHandler );
	}
	
	/**
	 * Prints a description to the statistical process carried out above the results.
	 * @param significanceThreshold the threshold above which p values are considered to be significant
	 * @param brunnerMunzel if the brunner munzel test is being used
	 * @param paired if the data sets are paired
	 * @param initialRuns number of initial repeats
	 * @param maxRuns number of repeats to increment up to
	 * @param cens censoring strategies
	 * @param artefactRepeats 
	 * @param timeSeries whether the data is of the time series, instead of the dataset variety
	 * @param artefactRepeats number of times each artefact was run
	 */
	public void printPreTestOutput(
			double significanceThreshold, 
			boolean brunnerMunzel, 
			boolean paired,
			int initialRuns,  
			int maxRuns, 
			CensoringStrategy cens, 
			boolean timeseries, 
			int artefactRepeats,
			SetOfComparisons<? extends GeneratorOutput> gens,
			ModifiedVarghaDelaney vdmod) {
		
		if(initialRuns < 10)
			warning("The number of runs ($n$) is low, at only " + initialRuns 
					+ " To ensure reliability of results the number of runs should be at least 10");
		LOGGER.info( "\n\nStatistical testing was carried out as follows: \n" );
		LOGGER.info( LaTeXLogFormatter.itemizeFormat( describeOptions(significanceThreshold, brunnerMunzel, paired, 
				initialRuns, maxRuns, cens, timeseries, artefactRepeats, gens, vdmod) ) );
	} 

	public void printPostTestOutput(int minRuns, 
			int maxRuns,
			int runsSoFar, 
			Result res, 
			CensoringStrategy cens, 
			String dataAName, 
			String dataBName, 
			int censCounter) {

		if(cens.complexStrategy() || maxRuns > minRuns){
			LOGGER.info(LaTeXLogFormatter.endTable());
			LOGGER.info( "\nThe final test, and the test on which effect size was calculated, "
					+ "was carried out using an $n$ of " + runsSoFar + ". " + 
					cens.describeLastStrategy(censCounter) + "\n");
		}
		LOGGER.info("\nThe final results from comparing " + dataAName + " and " + dataBName + " are as follows:\n");
		LOGGER.info( LaTeXLogFormatter.itemizeFormat( Result.describe(res,dataAName, dataBName ) ) );		
	}
	
	/**
	 * Returns output describing each option set.
	 * 
	 * @param significanceThreshold the threshold above which p values are considered to be significant
	 * @param brunnerMunzel if the brunner munzel test is being used
	 * @param paired if the data sets are paired
	 * @param initialRuns number of initial repeats
	 * @param maxRuns number of repeats to increment up to
	 * @param cens censoring strategies
	 * @param timeSeries whether the data is of the time series, instead of the dataset variety
	 * @param artefactRepeats number of times each artefact was run
	 * @param vdmod 
	 * @param gens 
	 * @return
	 */
	private List<String> describeOptions(
			double significanceThreshold,
			boolean brunnerMunzel, 
			boolean paired, 
			int initialRuns,
			int maxRuns, 
			CensoringStrategy cens, 
			boolean timeSeries, 
			int artefactRepeats, SetOfComparisons<? extends GeneratorOutput> gens, ModifiedVarghaDelaney vdmod) {

		if(artefactRepeats > 1){ 
			LOGGER.info("This data was obtained from runs on multiple artefacts. Each artefact is treated as a single run "
					+ "for the purpose of statistical comparison and so, the experimental description below, "
					+ " the number of runs refers to the number of artefacts. "
					+ "For each artefact " + artefactRepeats + " repeated tests were carried out and the median of these "
					+ "results was used as the result for that artefact for the purposes of subsequent statistical testing."
					);
		}
		
		List< String > result = new ArrayList< String >();

		if(maxRuns > initialRuns){
			result.add("Using the process of incrementing the number of runs until the difference is statistically significant. "
					+ "Initially " + initialRuns + " experiments were run. "
					+ "If sigificance is not obtained from these experiments then additional experiments are run until either significance is obtained or a maximum of " 
					+ maxRuns + " experiments have been run. "
					+ "When this technique is used effect size testing is especially important as a number of samples sufficient to show a "
					+ "statistically significant difference is likely to be reached even if the difference is too small to be useful. "
					+ "Note that this strategy involves multiple P value calculations as $n$ increases. "
					+ "These have not been adjusted and issues related to multiple testing should be taken into account when interpreting these results.\n");
		}
		else{
			result.add("All experiments were repeated " + initialRuns + " times ($n$=" + initialRuns + ")");
		}
		
		boolean useNoncensoredInCensoredTests = false;
		if(cens.isCensoring()){//censoring
			StringBuffer censBuf = new StringBuffer();
			if(paired){
				censBuf.append( "The data is dichotomous and paired and so "
						+ "the McNemar test of statistical significance~\\cite{Gibbons2011} was used followed by the Matched Odds Ratio test of effect size.");
			}
			else
				censBuf.append( "The data is dichotomous and paired and so "
						+ "the Fisher test of statistical significance was used followed by the Odds Ratio test of effect size.");

			if(timeSeries){
					censBuf.append("These tests compare boolean values, denoting a pass or a fail, "
							+ "from the final point in the result time series. " );//initial standard censoring strategy
				if(cens.complexStrategy()){//are more censoring strategies
					censBuf.append("\n\nThis data is censored, that is to say the point at which results are obtained is the "
							+ "arbitrary point at which the test stopped running. "
							+ "This may not accurately reflect the true difference between the two datasets. "
							+ "For this reason, additional statistical tests were carried out if "
							+ "this initial test did not show significance. Additional tests where carried out in this order:\n");
					List<String> censItems = new ArrayList<String>();
					int i =0;
					while(cens.hasMoreSteps(i)){
						if(cens.usingTimesToPass(i)){
							censItems.add(i + ": Tests were run using a non dichotomous P Value test "
									+ "in which the length of time taken to reach a successful result is compared"
									+ "(\"Time to Pass\" test).");
							useNoncensoredInCensoredTests = true;
						}
						else{
							censItems.add(i + ": Tests were run using a censoring point at " + 
							(cens.getCensoringPoint(i) == -1 ? "the final point": "point " + cens.getCensoringPoint(i)) + " in the time series.");
							
						}
						i++;
					}
					censBuf.append(LaTeXLogFormatter.itemizeFormat(censItems));
					censBuf.append("Note that using these strategies involves multiple P value calculations on the same data. "
							+ "This should be taken into account when interpreting these results.\n");
				}
			}
			result.add(censBuf.toString());
		}
		
		//print settings only relevant to non censored tests or if the integer 
		//based test time to pass strategy is used as a censoring strategy 
		if(useNoncensoredInCensoredTests){
			result.add("The points below only apply to the non dichotomous time based test:");
		}
		if(!cens.isCensoring() || useNoncensoredInCensoredTests){
			if( paired ) {
				result.add( "The data is paired. A paired version of the Wilcoxon/Mann-Whitney U test for significance will be used." );
			}
			else if(brunnerMunzel) {
				result.add( "The Brunner Munzel P Value Significance Test was used. "
						+ "Brunner Munzel is used in place of the  Wilcoxon/ Mann Whitney U test recommended in Arcuri's paper~\\cite{Arcuri2012} "
						+ "because the Brunner Munzel test is tolerant of heteroscedastic data whereas the "
						+ "Wilcoxon/ Mann Whitney U test is not~\\cite{Brunner2000}." );
			}
			else {
				result.add( "The Wilcoxon/ Mann Whitney U Significance Test was used." );
			}
			result.add("The Vargha Delaney Effect Size Test was used. "
					+ "Effect size testing is essential in addition to significance testing as it demonstrates the magnitude of the difference between two samples. "
					+ "With a large enough number of experiments (large enough $n$), the results of two different generating techniques are likely to be different to a statistically significant extent. "
					+ "Effect size testing is needed to show that this difference is useful.");
			if(vdmod != null)
				result.add("According to the principles of Transformed Vargha Delaney~\\cite{Neumann2015}, "
						+ "Vargha Delaney results are adjusted as follows:\n " + vdmod.describe());
		}
		
		MultiTestAdjustment adjust = gens.getAdjust();
		if(adjust != null)
			result.add("As there are multiple comparisons, p values are adjusted using the " + adjust.getName() + " adjustment.");

		
		return result;
	}

	public static void addToTable(String[] strings) {
		LOGGER.info(LaTeXLogFormatter.tabulateItems(strings));
		
	}

	public void endTable() {
		LOGGER.info(LaTeXLogFormatter.endTable());
		
	}

	public void printPrePairOutput(String gen1Name, String gen2Name, CensoringStrategy cens, int initialRuns, int maxRuns) {
		if(cens.complexStrategy() || maxRuns > initialRuns){ //if there will be a table then add this line to introduce it
			LOGGER.info("A complete list of p values for generators " + gen1Name + " and " + gen2Name + " is shown in table~\\ref{p value tests}, with $n$ corresponding to the number of samples in each data set.\n");
			LOGGER.info(LaTeXLogFormatter.startTable(new String[]{"n for " + gen1Name, "n for " + gen2Name, "Notes", "P-Value"}, "P Value Tests", "p value tests"));
		}
	}

	public static void warning(String warning) {
		LOGGER.warning(warning + "\n");
		Layer1.LOGGER.warning(warning + "\n");
	}
}

// End ///////////////////////////////////////////////////////////////

