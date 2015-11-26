package astraiea.output;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Logger;

import astraiea.Result;
import astraiea.layer2.strategies.CensoringStrategy;

public class Report {

	
	
	public static Logger LOGGER = Logger.getLogger( Report.class.getName() );

	/**
	 * Sets up the LOGGER for this class using a default configuration.
	 * 
	 * @param filename file where output of logger should be stored.
	 * @throws SecurityException
	 * @throws IOException
	 */
	public static void setupLaTeXLoggers(String filename) throws SecurityException, IOException {		
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
			int artefactRepeats) {
		
		if(initialRuns < 10)
			LOGGER.warning("The number of runs ($n$) is low, at only " + initialRuns 
					+ " To ensure reliability of results the number of runs should be at least 10");
		LOGGER.info( "Statistical testing was carried out as follows: \n" );
		LOGGER.info( LaTeXLogFormatter.itemizeFormat( describeOptions(significanceThreshold, brunnerMunzel, paired, 
				initialRuns, maxRuns, cens, timeseries, artefactRepeats) ) );
		if(cens.complexStrategy() || maxRuns > initialRuns) //if there will be a table then add this line to introduce it
			LOGGER.info("A complete list of p value tests carried out is shown in table~\\ref{p value tests}, with $n$ corresponding to the number of samples in each data set.\n");
		
		//start table for intermediate results
		if(cens.complexStrategy())//start table with additional notes column for complex censoring strategies
			LOGGER.info(LaTeXLogFormatter.startTable(new String[]{"n","Notes","P-Value"}, "P Value Tests", "p value tests"));
		else if(maxRuns > initialRuns)
			LOGGER.info(LaTeXLogFormatter.startTable(new String[]{"n","P-Value"}, "P Value Tests", "p value tests"));
	}

	public void printPostTestOutput(boolean incremented, 
			int runsSoFar, 
			Result res, 
			CensoringStrategy cens, 
			String dataAName, 
			String dataBName, 
			int censCounter) {
		if(cens.complexStrategy() || incremented){
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
			int artefactRepeats) {

		if(artefactRepeats > 0){ 
			LOGGER.info("This data was obtained from runs on multiple artefacts. Each artefact is treated as a single run "
					+ "for the purpose of statistical comparison and so, the experimental description below, "
					+ " the number of runs refers to the number of artefacts. "
					+ "For each artefact " + artefactRepeats + " repeated tests were carried out and the median of these "
					+ "results was used as the result for that artefact for the purposes of subsequent statistical testing."
					);
		}
		
		List< String > result = new ArrayList< String >();
		
		
		if(brunnerMunzel && cens.isCensoring())
			LOGGER.warning("The Brunner Munzel test has been requested. "
					+ "This is not relevant for dichotomous tests and will only be used if a non dichotomous test is carried out.");
		if(brunnerMunzel && paired)
			LOGGER.warning("Both a Brunner Munzel test and paired data has been specified. "
					+ "Brunner Munzel is not a paired test and so the request to use it has been ignored.");

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
				if(cens.hasMoreSteps(0)){//are more censoring strategies
					censBuf.append("\n\nThis data is censored, that is to say the point at which results are obtained is the "
							+ "arbitrary point at which the test stopped running. "
							+ "This may not accurately reflect the true difference between the two datasets. "
							+ "For this reason, additional statistical tests were carried out if "
							+ "this initial test did not show significance. Additional tests where carried out in this order:\n");
					List<String> censItems = new ArrayList<String>();
					int i =0;
					while(cens.hasMoreSteps(i)){
						if(cens.usingTimesToPass(i)){
							censItems.add((i + 1) + ": Tests were run using a non dichotomous P Value test "
									+ "in which the length of time taken to reach a successful result is compared"
									+ "(\"Time to Pass\" test).");
							useNoncensoredInCensoredTests = true;
						}
						else{
							censItems.add(i + ": Tests were run using an artificial censoring point at point " + 
							cens.getCensoringPoint(i) + " in the time series.");
							
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
		}
		return result;
	}
	
}
