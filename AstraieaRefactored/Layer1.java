package astraiea.layer1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Logger;

import org.apache.commons.math3.util.Pair;

import com.google.common.primitives.Booleans;
import com.google.common.primitives.Doubles;

import astraiea.Result;
import astraiea.layer1.varghaDelaney.VDmod;
import astraiea.layer1.varghaDelaney.VarghaDelaney;
import astraiea.layer2.generators.GeneratorOutput;
import astraiea.output.LaTeXLogFormatter;
import astraiea.util.Ordering;

/**
 * Carries out the comparison of two samples. Computes statistical significance and effect size.
 * 
 * @author Geoffrey Neumann, Jerry Swan
 * 
 */
public final class Layer1 {

	private static final String resultIntroPhrase = "Statistical testing was carried out as follows: ";
	public static Logger LOGGER = Logger.getLogger( Layer1.class.getName() );

	///////////////////////////////USER INTERFACE METHODS///////////////////////////////

	/**Compares two sets of unpaired, non dichotomous results (dataA and dataB).
	 * 
	 * @param dataA
	 * @param dataB
	 * @param significanceThreshold
	 * @param brunnerMunzel
	 * @param ran
	 * @return
	 */
	public static Result compare( List< Double > dataA, List< Double > dataB, 
			double significanceThreshold, 
			boolean brunnerMunzel, Random ran ) {
		
		double [] dA = Doubles.toArray( dataA );
		double [] dB = Doubles.toArray( dataB );		
		
		return compareImpl( dA, dB, 
				significanceThreshold, 
				brunnerMunzel, 
				false, "dataA", 
				"dataB", ran,null);
	}
	
	/**Compares two sets of unpaired, non dichotomous results (dataA and dataB).
	 * 
	 * @param dataA
	 * @param dataB
	 * @param significanceThreshold
	 * @param brunnerMunzel
	 * @param ran
	 * @param vdmod allows modification of the comparison function used in Vargha Delaney
	 * @return
	 */
	public static Result compare( List< Double > dataA, 
			List< Double > dataB, 
			double significanceThreshold, 
			boolean brunnerMunzel, 
			Random ran, 
			VDmod vdmod ) {
		
		double [] dA = Doubles.toArray( dataA );
		double [] dB = Doubles.toArray( dataB );		
		
		return compareImpl( dA, dB, 
				significanceThreshold, 
				brunnerMunzel, 
				false, "dataA", 
				"dataB", ran,vdmod);
	}

	/**Compares two sets of unpaired, non dichotomous results (dataA and dataB).
	 * 
	 * @param dataA
	 * @param dataB
	 * @param significanceThreshold
	 * @param brunnerMunzel
	 * @param ran
	 * @return
	 */
	public static Result compare( double[] dataA, double[] dataB, 
			double significanceThreshold, 
			boolean brunnerMunzel,Random ran) {		
		
		return compareImpl( dataA, dataB, 
				significanceThreshold, 
				brunnerMunzel, 
				false, "dataA", "dataB", ran, null);
	}
	
	/**Compares two sets of unpaired, non dichotomous results (dataA and dataB).
	 * 
	 * @param dataA
	 * @param dataB
	 * @param significanceThreshold
	 * @param brunnerMunzel
	 * @param ran
	 * @param vdmod allows modification of the comparison function used in Vargha Delaney
	 * @return
	 */
	public static Result compare( double[] dataA, double[] dataB, 
			double significanceThreshold, 
			boolean brunnerMunzel,Random ran, VDmod vdmod ) {		
		
		return compareImpl( dataA, dataB, 
				significanceThreshold, 
				brunnerMunzel, 
				false, "dataA", "dataB", ran, vdmod);
	}
	
	/**Compares two sets of unpaired, non dichotomous results (dataA and dataB).
	 * 
	 * @param dataA
	 * @param dataB
	 * @param significanceThreshold
	 * @param brunnerMunzel
	 * @param ran
	 * @param dataAName user given name for dataA 
	 * @param dataBName user given name for dataB
	 * @param vdmod allows modification of the comparison function used in Vargha Delaney
	 * @return
	 */
	public static Result compare( List< Double > dataA, List< Double > dataB, 
			double significanceThreshold, 
			boolean brunnerMunzel, Random ran, String dataAName, String dataBName, VDmod vdmod) {
		
		double [] dA = Doubles.toArray( dataA );
		double [] dB = Doubles.toArray( dataB );		
		
		return compareImpl( dA, dB, 
				significanceThreshold, 
				brunnerMunzel, 
				false, dataAName, dataBName, ran, vdmod);
	}
	
	/**Compares two sets of unpaired, non dichotomous results (dataA and dataB).
	 * 
	 * @param dataA
	 * @param dataB
	 * @param significanceThreshold
	 * @param brunnerMunzel
	 * @param ran
	 * @param dataAName user given name for dataA 
	 * @param dataBName user given name for dataB
	 * @return
	 */
	public static Result compare( List< Double > dataA, List< Double > dataB, 
			double significanceThreshold, 
			boolean brunnerMunzel, Random ran, String dataAName, String dataBName) {
		
		double [] dA = Doubles.toArray( dataA );
		double [] dB = Doubles.toArray( dataB );		
		
		return compareImpl( dA, dB, 
				significanceThreshold, 
				brunnerMunzel, 
				false, dataAName, dataBName, ran, null);
	}

	/**Compares two sets of unpaired, non dichotomous results (dataA and dataB).
	 * 
	 * @param dataA
	 * @param dataB
	 * @param significanceThreshold
	 * @param brunnerMunzel
	 * @param ran
	 * @param dataAName user given name for dataA 
	 * @param dataBName user given name for dataB
	 * @param vdmod allows modification of the comparison function used in Vargha Delaney
	 * @return
	 */
	public static Result compare( double[] dataA, double[] dataB, 
			double significanceThreshold, 
			boolean brunnerMunzel,Random ran, String dataAName, String dataBName, VDmod vdmod) {		
		
		return compareImpl( dataA, dataB, 
				significanceThreshold, 
				brunnerMunzel, 
				false, dataAName, dataBName, ran, vdmod);
	}
	
	/**Compares two sets of unpaired, non dichotomous results (dataA and dataB).
	 * 
	 * @param dataA
	 * @param dataB
	 * @param significanceThreshold
	 * @param brunnerMunzel
	 * @param ran
	 * @param dataAName user given name for dataA 
	 * @param dataBName user given name for dataB
	 * @return
	 */
	public static Result compare( double[] dataA, double[] dataB, 
			double significanceThreshold, 
			boolean brunnerMunzel,Random ran, String dataAName, String dataBName) {		
		
		return compareImpl( dataA, dataB, 
				significanceThreshold, 
				brunnerMunzel, 
				false, dataAName, dataBName, ran, null);
	}
	
	/**	Compares two sets of paired, non dichotomous results (dataA and dataB).
	 * 
	 * @param dataA
	 * @param dataB
	 * @param significanceThreshold
	 * @return
	 */
	public static Result comparePaired( double[] dataA, double[] dataB, 
			double significanceThreshold) {
		return compareImpl( dataA, dataB, 
				significanceThreshold, 
				false, 
				true, "dataA", "dataB", null,null);
	}
	
	/**	Compares two sets of paired, non dichotomous results (dataA and dataB).
	 * 
	 * @param dataA
	 * @param dataB
	 * @param significanceThreshold
	 * @return
	 */
	public static Result comparePaired( List< Double > dataA, List< Double > dataB, 
			double significanceThreshold) {
		
		double [] dA = Doubles.toArray( dataA );
		double [] dB = Doubles.toArray( dataB );
		
		return compareImpl( dA, dB, 
				significanceThreshold, 
				false, 
				true, "dataA", "dataB", null,null);
	}

	/**	Compares two sets of paired, non dichotomous results (dataA and dataB).
	 * 
	 * @param dataA
	 * @param dataB
	 * @param significanceThreshold
	 * @param dataAName user given name for dataA 
	 * @param dataBName user given name for dataB
	 * @return
	 */
	public static Result comparePaired( double[] dataA, double[] dataB, 
			double significanceThreshold, String dataAName, String dataBName) {
		return compareImpl( dataA, dataB, 
				significanceThreshold, 
				false, 
				true, dataAName, dataBName, null,null);
	}
	
	/**	Compares two sets of paired, non dichotomous results (dataA and dataB).
	 * 
	 * @param dataA
	 * @param dataB
	 * @param significanceThreshold
	 * @param dataAName user given name for dataA 
	 * @param dataBName user given name for dataB
	 * @return
	 */
	public static Result comparePaired( List< Double > dataA, List< Double > dataB, 
			double significanceThreshold,String dataAName, String dataBName) {
		
		double [] dA = Doubles.toArray( dataA );
		double [] dB = Doubles.toArray( dataB );
		
		return compareImpl( dA, dB, 
				significanceThreshold, 
				false, 
				true, dataAName, dataBName, null,null);
	}
	
	
	/**	Compares two sets of paired, non dichotomous results (dataA and dataB).
	 * 
	 * @param dataA
	 * @param dataB
	 * @param significanceThreshold
	 * @param vdmod 
	 * @return
	 */
	public static Result comparePaired( double[] dataA, double[] dataB, 
			double significanceThreshold, VDmod vdmod) {
		return compareImpl( dataA, dataB, 
				significanceThreshold, 
				false, 
				true, "dataA", "dataB", null,vdmod);
	}
	
	/**	Compares two sets of paired, non dichotomous results (dataA and dataB).
	 * 
	 * @param dataA
	 * @param dataB
	 * @param significanceThreshold
	 * @param vdmod 
	 * @return
	 */
	public static Result comparePaired( List< Double > dataA, List< Double > dataB, 
			double significanceThreshold, VDmod vdmod) {
		
		double [] dA = Doubles.toArray( dataA );
		double [] dB = Doubles.toArray( dataB );
		
		return compareImpl( dA, dB, 
				significanceThreshold, 
				false, 
				true, "dataA", "dataB", null,vdmod);
	}

	/**	Compares two sets of paired, non dichotomous results (dataA and dataB).
	 * 
	 * @param dataA
	 * @param dataB
	 * @param significanceThreshold
	 * @param dataAName user given name for dataA 
	 * @param dataBName user given name for dataB
	 * @param vdmod 
	 * @return
	 */
	public static Result comparePaired( double[] dataA, double[] dataB, 
			double significanceThreshold, String dataAName, String dataBName, VDmod vdmod) {
		return compareImpl( dataA, dataB, 
				significanceThreshold, 
				false, 
				true, dataAName, dataBName, null,vdmod);
	}
	
	/**	Compares two sets of paired, non dichotomous results (dataA and dataB).
	 * 
	 * @param dataA
	 * @param dataB
	 * @param significanceThreshold
	 * @param dataAName user given name for dataA 
	 * @param dataBName user given name for dataB
	 * @param vdmod 
	 * @return
	 */
	public static Result comparePaired( List< Double > dataA, List< Double > dataB, 
			double significanceThreshold,String dataAName, String dataBName, VDmod vdmod) {
		
		double [] dA = Doubles.toArray( dataA );
		double [] dB = Doubles.toArray( dataB );
		
		return compareImpl( dA, dB, 
				significanceThreshold, 
				false, 
				true, dataAName, dataBName, null,vdmod);
	}
	
	/**	Compares two sets of paired, dichotomous results (dataA and dataB).
	 * 
	 * @param dataA
	 * @param dataB
	 * @param significanceThreshold
	 * @param paired
	 * @return
	 */
	public static Result compareCensored( List< Boolean > dataA, List< Boolean > dataB, 
			double significanceThreshold, boolean paired) {
		
		boolean [] dA = Booleans.toArray( dataA );
		boolean [] dB = Booleans.toArray( dataB );		
		
		return compareCensoredImpl( dA, dB, 
				significanceThreshold, paired,
				"dataA", "dataB");
	}

	/**	Compares two sets of paired, dichotomous results (dataA and dataB).
	 * 
	 * @param dataA
	 * @param dataB
	 * @param significanceThreshold
	 * @param paired
	 * @param dataAName user given name for dataA 
	 * @param dataBName user given name for dataB
	 * @return
	 */
	public static Result compareCensored( List< Boolean > dataA, List< Boolean > dataB, 
			double significanceThreshold, boolean paired,
			String dataAName, String dataBName ) {
		
		boolean [] dA = Booleans.toArray( dataA );
		boolean [] dB = Booleans.toArray( dataB );
		
		return compareCensoredImpl( dA, dB, 
				significanceThreshold, paired,
				dataAName, dataBName);
	}

	/**	Compares two sets of paired, dichotomous results (dataA and dataB).
	 * 
	 * @param dataA
	 * @param dataB
	 * @param significanceThreshold
	 * @param paired 
	 * @return
	 */
	public static Result compareCensored( boolean[] dataA, boolean[] dataB, 
			double significanceThreshold, boolean paired 
			) {		
		
		return compareCensoredImpl( dataA, dataB, 
				significanceThreshold, paired,
				"dataA", "dataB");
	}

	/**	Compares two sets of paired, dichotomous results (dataA and dataB).
	 * 
	 * @param dataA
	 * @param dataB
	 * @param significanceThreshold
	 * @param paired
	 * @param dataAName user given name for dataA 
	 * @param dataBName user given name for dataB
	 * @return
	 */
	public static Result compareCensored( boolean[] dataA, boolean[] dataB, 
			double significanceThreshold, 
			String dataAName, String dataBName, boolean paired ) {
		return compareCensoredImpl( dataA, dataB, 
				significanceThreshold, paired,
				dataAName, dataBName);
	}
	
	/**	Compares two sets of paired, dichotomous results (dataA and dataB), with a threshold.
	 * 
	 * @param dataA
	 * @param dataB
	 * @param censorThreshold threshold such that data above it or equal to it is said to have "passed"
	 * and given a "true" boolean value, else "false".
	 * @param significanceThreshold
	 * @param paired 
	 * @return
	 */
	public static Result compareCensored( List< Double > dataA, List< Double > dataB, double censorThreshold, 
			double significanceThreshold, boolean paired 
			) {
		
		boolean [] dA = new boolean[dataA.size()];
		boolean [] dB = new boolean[dataB.size()];
		
		//convert non dichotomous to dichotomous using threshold
		ListIterator<Double> iter = dataA.listIterator();
		int i =0;
		while(iter.hasNext())
			dA[i++] = iter.next() >= censorThreshold;
		iter = dataB.listIterator();
		i =0;
		while(iter.hasNext())
			dB[i++] = iter.next() >= censorThreshold;	
			
		//compare
		return compareCensoredImpl( dA, dB, 
				significanceThreshold, paired, 
				"dataA", "dataB");
	}

	/**	Compares two sets of paired, dichotomous results (dataA and dataB), with a threshold.
	 * 
	 * @param dataA
	 * @param dataB
	 * @param censorThreshold threshold such that data above it or equal to it is said to have "passed"
	 * and given a "true" boolean value, else "false".
	 * @param significanceThreshold
	 * @param dataAName user given name for dataA 
	 * @param dataBName user given name for dataB
	 * @param paired 
	 * @return
	 */
	public static Result compareCensored( List< Double > dataA, List< Double > dataB, double censorThreshold, 
			double significanceThreshold, 
			String dataAName, String dataBName, boolean paired ) {
		
		boolean [] dA = new boolean[dataA.size()];
		boolean [] dB = new boolean[dataB.size()];
		ListIterator<Double> iter = dataA.listIterator();
		int i =0;
		while(iter.hasNext())
			dA[i++] = iter.next() >= censorThreshold;
		iter = dataB.listIterator();
		i =0;
		while(iter.hasNext())
			dB[i++] = iter.next() >= censorThreshold;		
		return compareCensoredImpl( dA, dB, 
				significanceThreshold, paired,
				dataAName, dataBName);
	}

	/**	Compares two sets of paired, dichotomous results (dataA and dataB), with a threshold.
	 * 
	 * @param dataA
	 * @param dataB
	 * @param censorThreshold threshold such that data above it or equal to it is said to have "passed"
	 * and given a "true" boolean value, else "false".
	 * @param significanceThreshold
	 * @param paired 
	 * @return
	 */
	public static Result compareCensored( double[] dataA, double[] dataB, double censorThreshold, 
			double significanceThreshold, boolean paired 
			) {		
		
		boolean [] dA = new boolean[dataA.length];
		boolean [] dB = new boolean[dataB.length];
		for(int i =0; i < dA.length; i++)
			dA[i] = dataA[i] >= censorThreshold;
		for(int i =0; i < dB.length; i++)
			dB[i] = dataB[i] >= censorThreshold;
		
		return compareCensoredImpl( dA, dB, 
				significanceThreshold, paired,
				"dataA", "dataB");
	}


	/**	Compares two sets of paired, dichotomous results (dataA and dataB), with a threshold.
	 * 
	 * @param dataA
	 * @param dataB
	 * @param censorThreshold threshold such that data above it or equal to it is said to have "passed"
	 * and given a "true" boolean value, else "false".
	 * @param significanceThreshold
	 * @param dataAName user given name for dataA 
	 * @param dataBName user given name for dataB
	 * @param paired 
	 * @return
	 */
	public static Result compareCensored( 
			double[] dataA, 
			double[] dataB, 
			double censorThreshold,
			double significanceThreshold, 
			String dataAName, 
			String dataBName, 
			boolean paired ) {
		boolean [] dA = new boolean[dataA.length];
		boolean [] dB = new boolean[dataB.length];
		for(int i =0; i < dA.length; i++)
			dA[i] = dataA[i] >= censorThreshold;
		for(int i =0; i < dB.length; i++)
			dB[i] = dataB[i] >= censorThreshold;
		
		return compareCensoredImpl( dA, dB, 
				significanceThreshold, paired,
				dataAName, dataBName);
	}
	
	/** Compares two sets of results stored in the paired format produced by layer2.
	 * In this format a single data element has both a 
	 * double (non dichotomous part) and a boolean (dichotomous) part.
	 * This method converts data from this format into a simple 'List(boolean)' or 'List(Double)' as appropriate
	 * and then calls the main compare method to actually do the comparison.
	 * 
	 * 
	 * @param dataA
	 * @param dataB
	 * @param significanceThreshold
	 * @param brunnerMunzel
	 * @param paired
	 * @param censored if true will convert into boolean list, otherwise into double list
	 * @param dataAName user given name for dataA 
	 * @param dataBName user given name for dataB
	 * @param ran 
	 * @param vdmod allows modification of the comparison function used in Vargha Delaney
	 * @return
	 */
	public static Result comparePair(List<Pair<Double, Boolean>> dataA,
			List<Pair<Double, Boolean>> dataB, double significanceThreshold,
			boolean brunnerMunzel, boolean paired, boolean censored,
			String dataAName, String dataBName, Random ran, VDmod vdmod) {
		
			//convert to two dichotomous arrays of booleans
			if(censored){
				boolean[] dA = new boolean[dataA.size()];
				ListIterator<Pair<Double, Boolean>> iter = dataA.listIterator();
				int i =0;
				while(iter.hasNext()){
					dA[i] = iter.next().getSecond();
					i++;
				}
				
				boolean[] dB = new boolean[dataB.size()];
				iter = dataB.listIterator();
				i =0;
				while(iter.hasNext()){
					dB[i] = iter.next().getSecond();
					i++;
				}
				
				
				return Layer1.compareCensoredImpl(dA, dB, significanceThreshold, paired, dataAName, dataBName);
			}
			else{			//convert to two arrays of doubles

				double[] dA = new double[dataA.size()];
				ListIterator<Pair<Double, Boolean>> iter = dataA.listIterator();
				int i =0;
				while(iter.hasNext()){
					dA[i] = iter.next().getFirst();
					i++;
				}
				
				double[] dB = new double[dataB.size()];
				iter = dataB.listIterator();
				i =0;
				while(iter.hasNext()){
					dB[i] = iter.next().getFirst();
					i++;
				}
				return Layer1.compareImpl(dA, dB, significanceThreshold, brunnerMunzel, paired, dataAName, dataBName, ran, vdmod);
			}
	}
	
	/**Compares an array of data with a single data point
	 * 
	 * @param dataA array of data
	 * @param dataB single data point
	 * @param significanceThreshold
	 * @return
	 */
	public static Result compareOneSample( double[] dataA, double dataB, 
			double significanceThreshold) {		
		
		return compareOneSampleImpl( dataA, dataB, 
				significanceThreshold, "data stochastic", "data deterministic");
	}
	
	/**Compares an array of data with a single data point
	 * 
	 * @param dataA array of data
	 * @param dataB single data point
	 * @param significanceThreshold
	 * @return
	 */
	public static Result compareOneSample( List<Double> dataA, double dataB, 
			double significanceThreshold) {		
		return compareOneSampleImpl( Doubles.toArray( dataA ), dataB, 
				significanceThreshold, "data stochastic", "data deterministic");
	}

	/**Compares an array of data with a single data point
	 * 
	 * @param dataA array of data
	 * @param dataB single data point
	 * @param nameA user given name for dataA 
	 * @param nameB user given name for dataB
	 * @param significanceThreshold
	 * @return
	 */
	public static Result compareOneSample( double[] dataA, double dataB, 
			double significanceThreshold, String nameA, String nameB) {		
		
		return compareOneSampleImpl( dataA, dataB, 
				significanceThreshold, nameA, nameB);
	}
	
	/**Compares an array of data with a single data point
	 * 
	 * @param dataA array of data
	 * @param dataB single data point
	 * @param nameA user given name for dataA 
	 * @param nameB user given name for dataB
	 * @param significanceThreshold
	 * @return
	 */
	public static Result compareOneSample( List<Double> dataA, double dataB, 
			double significanceThreshold, String nameA, String nameB) {		
		return compareOneSampleImpl( Doubles.toArray( dataA ), dataB, 
				significanceThreshold, nameA, nameB);
	}

	///////////////////////IMPLEMENTATIONS//////////////////////
	
	/**The implementation for comparing dichotomous, censored data.
	 * 
	 * @param dataA first data set
	 * @param dataB second data set
	 * @param significanceThreshold threshold above which data is considered to be significant
	 * @param paired if the data is paired
	 * @param dataAName name of first data set for user output
	 * @param dataBName name of second data set for user output
	 * @return object holding the effect size and significance results
	 */
	private static Result compareCensoredImpl(
			boolean[] dataA, 
			boolean[] dataB,
			double significanceThreshold,
			boolean paired,
			String dataAName, 
			String dataBName) {
		
		
		double pValue;
		Ordering order = null;
		boolean significant;
		double effectSize = Double.NaN;
		
		
		//output the settings
		LOGGER.info( resultIntroPhrase );
		LOGGER.info( LaTeXLogFormatter.itemizeFormat( describeOptions(significanceThreshold, false, paired, dataA.length, dataB.length, dataAName, dataBName,true ) ) );
		if(paired){
			if(dataA.length != dataB.length)
				throw new IllegalArgumentException("Unequal number of samples. For paired tests the number of samples in both datasets must be equal.");			
			//convert data sets into a set of 4 variables defining how much passed and failed in data sets A and B
			int passBoth = 0, passAonly = 0, passBonly = 0, failBoth = 0;
			for(int i= 0; i < dataA.length; i++){
				if(dataA[i] && dataB[i]) passBoth++;
				else if(dataA[i] && !dataB[i]) passAonly++;
				else if(!dataA[i] && dataB[i]) passBonly++;
				else failBoth++;
			}
			LOGGER.fine( "Running McNemar Test");			
			pValue = McNemar.evaluate(passBoth, failBoth, passAonly, passBonly);
			if(significant = pValue < significanceThreshold){//effect size test if data significant
				LOGGER.fine( "Running Matched Odds Ratio Test");
				effectSize = OddsRatio.evaluateMatched(passAonly, passBonly);
				order = OddsRatio.getOrder(effectSize);
			}
		}
		else{
			//convert data sets into a set of 4 variables defining how much passed and failed in data sets A and B
			int passA = 0, failA = 0, passB = 0, failB = 0;
			for(int i= 0; i < dataA.length; i++){
				if(dataA[i]) passA++;
				else failA++;
			}
			for(int i= 0; i < dataB.length; i++){
				if(dataB[i]) passB++;
				else failB++;
			}
			LOGGER.fine( "Running Fisher Test");			
			pValue = Fisher.evaluate(passA, failA, passB, failB);
			if(significant = pValue < significanceThreshold){//effect size test if data significant
				LOGGER.fine( "Running Odds Ratio Test");
				effectSize = OddsRatio.evaluate(passA, failA, passB, failB);
				order = OddsRatio.getOrder(effectSize);
			}
		}
		
		//store results
		Result res = new Result(effectSize,pValue,significant, significanceThreshold, order);
		//record results
		LOGGER.info("Results:\n");
		LOGGER.info( LaTeXLogFormatter.itemizeFormat( Result.describe(res, dataAName, dataBName ) ) );		
		return res;

	}
	
	/**The implementation for comparing non censored data.
	 * 
	 * @param dataA first data set
	 * @param dataB second data set
	 * @param significanceThreshold threshold above which data is considered to be significant
	 * @param brunnerMunzel if the brunner munzel test is to be used
	 * @param paired if the data is paired
	 * @param dataAName name of first data set for user output
	 * @param dataBName name of second data set for user output
	 * @param ran random number generator for use in calculating confidence intervals
	 * @param vdmod allows modification of the comparison function used in Vargha Delaney
	 * @return object holding the effect size and significance results
	 */
	private static Result compareImpl( double[] dataA, 
			double[] dataB, 
			double significanceThreshold, 
			boolean brunnerMunzel, 
			boolean paired, 
			String dataAName, 
			String dataBName, 
			Random ran, 
			VDmod vdmod) {
		
		double pValue = getPVal(dataA,dataB,brunnerMunzel,paired,significanceThreshold,dataAName,dataBName);
		boolean significant = false;
		double effectSize = Double.NaN;
		Ordering order = null;
		double[] cis = null;
		if(pValue < significanceThreshold){
			significant = true;
			effectSize = gEffectSize(dataA, dataB, paired, vdmod);
			order = VarghaDelaney.getOrder(effectSize);
			if(!paired && ran != null)//get confidence interval with upper and lower bounds based on the significance threshold used for other tests
				cis = VarghaDelaney.getConfidenceInterval(effectSize, dataA, dataB, 100 - ((100 * significanceThreshold)/2), ((100 * significanceThreshold)/2), ran, paired, vdmod);

		}
		Result res = null;
		res = new Result(effectSize,pValue,significant, significanceThreshold, order, cis);

		LOGGER.info("Results:\n");
		LOGGER.info( LaTeXLogFormatter.itemizeFormat( Result.describe(res, dataAName, dataBName ) ) );		
		return res;
	}
	
	private static double gEffectSize(double[] dataA, double[] dataB, boolean paired, VDmod vdmod) {
		LOGGER.fine( "Running Vargha Delaney Test");
		double effectSize = VarghaDelaney.evaluate(dataA,dataB, paired, vdmod);
		return effectSize;
	}
	
	private static<T extends GeneratorOutput<?>> double gEffectSize(List<T> dataA, List<T> dataB, boolean paired, VDmod vdmod) {
		LOGGER.fine( "Running Vargha Delaney Test");
		double effectSize = VarghaDelaney.evaluate(dataA,dataB, paired, vdmod);
		return effectSize;
	}

	private static double getPVal(double[] dataA, double[] dataB, boolean brunnerMunzel, boolean paired, double significanceThreshold, String dataAName, String dataBName) {
		double pValue;
		LOGGER.info( "Statistical testing was carried out as follows: " );
		LOGGER.info( LaTeXLogFormatter.itemizeFormat( describeOptions(significanceThreshold, brunnerMunzel, paired, dataA.length, dataB.length, dataAName, dataBName,false ) ) );
		
		if(paired){
			LOGGER.fine( "Running Wilcoxon Signed Rank Test");
			pValue = WilcoxonSignedRank.evaluate(dataA, dataB);					
		}
		else{
			if(brunnerMunzel){
				LOGGER.fine( "Running Brunner Munzel Test");
				pValue = BrunnerMunzel.evaluate(dataA, dataB);
			}
			else{
				LOGGER.fine( "Running Mann Whitney U Test");
				pValue = MannWhitney.evaluate(dataA, dataB);
			}
		}
		return pValue;
	}

	/**The implementation for carrying out a one sampled comparison where dataB is a single data point.
	 * 
	 * @param dataA array of data
	 * @param dataB single data point
	 * @param significanceThreshold
	 * @param dataAName user given name for dataA
	 * @param dataBName user given name for dataB
	 * @return
	 */
	private static Result compareOneSampleImpl( double[] dataA, double dataB, 
			double significanceThreshold, String dataAName, String dataBName ) {
		LOGGER.info( "Statistical testing was carried out as follows: " );
		LOGGER.info( LaTeXLogFormatter.itemizeFormat( describeOptions(significanceThreshold, false, false, dataA.length, 1, dataAName, dataBName,false ) ) );
		
		LOGGER.info(resultIntroPhrase);
		LOGGER.fine("Running The 1 Sample Wilcoxon Test");
		double pVal = MannWhitney.evaluate(dataA, dataB);
		//FIXME - what effect size test should be used? Place holders for effect size and order for now 
		Result res = new Result(0, pVal, pVal < significanceThreshold, 0, null);
		LOGGER.info("Results:\n");
		LOGGER.info( LaTeXLogFormatter.itemizeFormat( Result.describe(res, dataAName, dataBName ) ) );		

		return res;
	}

	
	///////////////////////////////OUTPUT /////////////////////////////////////
	
	/**
	 * Output all the options associated with Layer1.
	 * 
	 * @param significanceThreshold threshold below which a p value is judged to be significant
	 * @param brunnerMunzel if the brunner munzel test is used
	 * @param paired if the data is paired
	 * @param n1 number of samples in the first data set
	 * @param n2 number of samples in the second data set
	 * @param censored if the data is censored, dichotomous
	 */
	public static List< String > 
	describeOptions( double significanceThreshold,boolean brunnerMunzel, boolean paired, int n1, int n2, String dataAName, String dataBName, boolean censored ) {
		
		List< String > result = new ArrayList< String >();
		
		if( censored) {
			if(paired)
				result.add( "The data is dichotomous and paired and so the McNemar test of statistical significance~\\cite{Gibbons2011} and the Matched Odds Ratio test of effect size are used." );
			else
				result.add( "The data is dichotomous and unpaired and so the Fisher test of statistical significance and the Odds Ratio test of effect size are used." );
		}
		else if( paired ) {
			result.add( "The data is \\textit{paired}, i.e. the data sets are related, "
					+ "and so the Wilcoxon Signed Rank test for significance will be used." );
		}
		else if(brunnerMunzel) {
			 
			result.add( "The Brunner Munzel P Value significance test was used followed by the Vargha Delaney effect size test. "
					+ "Brunner Munzel is used in place of the  Wilcoxon/ Mann Whitney U test recommended in Arcuri's paper~\\cite{Arcuri2012} "
					+ "because the Brunner Munzel test is tolerant of heteroscedastic data whereas the "
					+ "Wilcoxon/ Mann Whitney U test is not~\\cite{Brunner2000}." );
		}
		else {
			result.add( "Using the default testing configuration: Wilcoxon/ Mann Whitney U Statistical Tests and Vargha Delaney Effect size tests." );
		}
		
		if( n1 == n2 )
			result.add( "These results were obtained with " + n1 + " samples in each dataset" );
		else
			result.add( "These results were obtained with " + n1 + " runs of metaheuristic " + dataAName + " and " + n2 + " runs of metaheuristic " + dataBName );
		
		return result;
	}
	
	/**Sets up the LOGGER for this class using a default configuration.
	 * 
	 * @param filename file where output of logger should be stored.
	 * @throws SecurityException
	 * @throws IOException
	 */
	public static void setupLatexLoggers(String filename) throws SecurityException, IOException {		
		Formatter formatter = new LaTeXLogFormatter();		
		Handler fileHandler = new FileHandler( filename);
		fileHandler.setFormatter(formatter);
		LOGGER.addHandler( fileHandler );
	}

	public static <T extends GeneratorOutput<?>> Result compare(
			List<T> dataA, 
			List<T> dataB, 
			double significanceThreshold,
			boolean brunnerMunzel, 
			boolean paired, 
			String dataAName, 
			String dataBName, 
			Random ran,
			VDmod vdmod) {
		int size1 = dataA.size();
		int size2 = dataB.size();
		double[] dataAArr = new double[size1];
		double[] dataBArr = new double[size2];
		ListIterator<T> iter1 = dataA.listIterator();
		ListIterator<T> iter2 = dataB.listIterator();
		int i =0;
		while(iter1.hasNext()){
			dataAArr[i] = iter1.next().getValue();
			i++;
		}
		i =0;
		while(iter2.hasNext()){
			dataBArr[i] = iter2.next().getValue();
			i++;
		}
		

		double pValue = getPVal(dataAArr,dataBArr,brunnerMunzel,paired,significanceThreshold,dataAName,dataBName);
		boolean significant = false;
		double effectSize = Double.NaN;
		Ordering order = null;
		double[] cis = null;
		if(pValue < significanceThreshold){
			significant = true;
			effectSize = gEffectSize(dataA, dataB, paired, vdmod);
			order = VarghaDelaney.getOrder(effectSize);
			if(!paired && ran != null)//get confidence interval with upper and lower bounds based on the significance threshold used for other tests
				cis = VarghaDelaney.getConfidenceInterval(effectSize, dataA, dataB, 100 - ((100 * significanceThreshold)/2), ((100 * significanceThreshold)/2), ran, paired, vdmod);

		}
		Result res = null;
		res = new Result(effectSize,pValue,significant, significanceThreshold, order, cis);

		LOGGER.info("Results:\n");
		LOGGER.info( LaTeXLogFormatter.itemizeFormat( Result.describe(res, dataAName, dataBName ) ) );		
		return res;
	}	

}

// End ///////////////////////////////////////////////////////////////

