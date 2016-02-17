package astraiea.test.layer2;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.math3.util.Pair;
import org.junit.Test;

import astraiea.Result;
import astraiea.layer1.Layer1;
import astraiea.layer2.Layer2;
import astraiea.layer2.experiments.SetOfComparisons;
import astraiea.layer2.experiments.SingleArtefactExperiments;
import astraiea.layer2.generators.PairGeneratorOutput;
import astraiea.layer2.generators.examples.ExampleDatapointGenerator;
import astraiea.layer2.generators.examples.ExampleTimeseriesGenerator;
import astraiea.layer2.generators.timeseries.TimeseriesGeneratorOutput;
import astraiea.layer2.strategies.CensoringStrategy;
import astraiea.layer2.strategies.NoIncrementing;
import astraiea.layer2.strategies.SimpleIncrementing;
import astraiea.outputformat.Report;
import astraiea.util.MersenneTwister;

/**
 * JUnit tests for all layer 2 configurations involving multiple artefacts or 
 * comparison between more than two generators. 
 * Carried out by comparing the outcome of invoking layer 2
 * with the outcome of independently generating data and directly invoking layer 1 
 * (which is known to be correct because it is tested in Layer1JUnit class).
 * If retesting this class Layer1JUnit should also be retested. 
 * Layer2Junit assumes that Layer1Junit is correct as it uses Layer1 as an oracle.
 * 
 * @author Geoffrey Neumann
 *
 */
public class TestLayer2 {

	/**Test 1 = Runs tests with the data point generator, with and without paired data, censoring and brunner munzel.
	 */
	@Test
	public void DatapointGenerators(){
		//output
		try {
			Report.setupLaTeXLoggers("reports/layer2/report1.tex");
		} catch (IOException e) {
			e.printStackTrace();
		}		
		
		//////////////the actual tests///////////////////////
		
			//setup generators with a significant difference
		ExampleDatapointGenerator gen1 = new ExampleDatapointGenerator(0.9, 1);
		ExampleDatapointGenerator gen2 = new ExampleDatapointGenerator(0.1, 1);
			//setup generators with a non significant difference
		ExampleDatapointGenerator gen3 = new ExampleDatapointGenerator(0.4, 1);
		ExampleDatapointGenerator gen4 = new ExampleDatapointGenerator(0.5, 1);
		
		//setup sets of experiments
		SingleArtefactExperiments<PairGeneratorOutput> gen1Exp = new SingleArtefactExperiments<PairGeneratorOutput>(gen1, "test1");
		SingleArtefactExperiments<PairGeneratorOutput> gen2Exp = new SingleArtefactExperiments<PairGeneratorOutput>(gen2, "test2");
		SingleArtefactExperiments<PairGeneratorOutput> gen3Exp = new SingleArtefactExperiments<PairGeneratorOutput>(gen3, "test3");
		SingleArtefactExperiments<PairGeneratorOutput> gen4Exp = new SingleArtefactExperiments<PairGeneratorOutput>(gen4, "test4");

		SetOfComparisons<PairGeneratorOutput> gen1gen2Comp = new SetOfComparisons<PairGeneratorOutput>(gen1Exp,gen2Exp);
		SetOfComparisons<PairGeneratorOutput> gen3gen4Comp = new SetOfComparisons<PairGeneratorOutput>(gen3Exp,gen4Exp);

			//check the two way comparison of generators works
			
			Result res = Layer2.run(gen1gen2Comp, 0.05, false, false, new NoIncrementing(30), new MersenneTwister(0)).get(0);
			Result res2 = Layer2.run(gen1gen2Comp, 0.05, true, false, new NoIncrementing(30), new MersenneTwister(1)).get(0);//brunner munzel
			Result res3 = Layer2.run(gen1gen2Comp, 0.05, false, true, new NoIncrementing(30), new MersenneTwister(2)).get(0);//paired
			Result res4 = Layer2.runCensored(gen1gen2Comp, 0.05, false, new NoIncrementing(30), new MersenneTwister(3)).get(0);//censored and unpaired
			Result res5 = Layer2.runCensored(gen1gen2Comp, 0.05, true, new NoIncrementing(30), new MersenneTwister(4)).get(0);//censored and paired
			Result res6 = Layer2.run(gen3gen4Comp, 0.05, false, false, new NoIncrementing(30), new MersenneTwister(5)).get(0);//not significant

			//////////////separately create the expected results to compare against///////////////////////////////////////
		
			List<Pair<Double, Boolean>> artificialResults1= artificiallyCreateDataPoints(new MersenneTwister(0),0.9,1,30,false,false);
			List<Pair<Double, Boolean>> artificialResults2= artificiallyCreateDataPoints(new MersenneTwister(1),0.9,1,30,false,false);//brunner munzel
			List<Pair<Double, Boolean>> artificialResults3= artificiallyCreateDataPoints(new MersenneTwister(2),0.9,1,30,true,false);//paired
			List<Pair<Double, Boolean>> artificialResults4= artificiallyCreateDataPoints(new MersenneTwister(3),0.9,1,30,false,false);//censored unpaired
			List<Pair<Double, Boolean>> artificialResults5= artificiallyCreateDataPoints(new MersenneTwister(4),0.9,1,30,true,false);//censored paired
			List<Pair<Double, Boolean>> artificialResults6= artificiallyCreateDataPoints(new MersenneTwister(5),0.4,1,30,false,false);//not significant

			List<Pair<Double, Boolean>> artificialResults1b= artificiallyCreateDataPoints(new MersenneTwister(0),0.1,1,30,false,true);//as above
			List<Pair<Double, Boolean>> artificialResults2b= artificiallyCreateDataPoints(new MersenneTwister(1),0.1,1,30,false,true);
			List<Pair<Double, Boolean>> artificialResults3b= artificiallyCreateDataPoints(new MersenneTwister(2),0.1,1,30,true,false);//2nd=false because in paired both use same seeds
			List<Pair<Double, Boolean>> artificialResults4b= artificiallyCreateDataPoints(new MersenneTwister(3),0.1,1,30,false,true);
			List<Pair<Double, Boolean>> artificialResults5b= artificiallyCreateDataPoints(new MersenneTwister(4),0.1,1,30,true,false);
			List<Pair<Double, Boolean>> artificialResults6b= artificiallyCreateDataPoints(new MersenneTwister(5),0.5,1,30,false,true);
			
			
			//use layer1 to compare artificial data (mimic the comparison that should have been made in layer 2)
			Result resTest = Layer1.comparePair(artificialResults1, artificialResults1b, 0.05, false, false, false, "dataA", "dataB", null, null);
			Result resTest2 = Layer1.comparePair(artificialResults2, artificialResults2b, 0.05, true, false, false, "dataA", "dataB", null, null);
			Result resTest3 = Layer1.comparePair(artificialResults3, artificialResults3b, 0.05, false, true, false, "dataA", "dataB", null, null);
			Result resTest4 = Layer1.comparePair(artificialResults4, artificialResults4b, 0.05, false, false, true, "dataA", "dataB", null, null);
			Result resTest5 = Layer1.comparePair(artificialResults5, artificialResults5b, 0.05, false, true, true, "dataA", "dataB", null, null);
			Result resTest6 = Layer1.comparePair(artificialResults6, artificialResults6b, 0.05, false, false, false, "dataA", "dataB", null, null);
			/////////////////////////////////////

			///compare results from layer 1 and the artificial data with those from layer2 
			//(compare p value, effect size, whether is significant and order using JUnit tests)
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", resTest.getPValue(), res.getPValue(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (effect size)", resTest.getEffectSize(), res.getEffectSize(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (order)", resTest.getOrder(), res.getOrder());
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (significance)", resTest.isSignificant(), res.isSignificant());
			
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", resTest2.getPValue(), res2.getPValue(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (effect size)", resTest2.getEffectSize(), res2.getEffectSize(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (order)", resTest2.getOrder(), res2.getOrder());
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (significance)", resTest2.isSignificant(), res2.isSignificant());

			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", resTest3.getPValue(), res3.getPValue(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (effect size)", resTest3.getEffectSize(), res3.getEffectSize(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (order)", resTest3.getOrder(), res3.getOrder());
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (significance)", resTest3.isSignificant(), res3.isSignificant());

			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", resTest4.getPValue(), res4.getPValue(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (effect size)", resTest4.getEffectSize(), res4.getEffectSize(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (order)", resTest4.getOrder(), res4.getOrder());
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (significance)", resTest4.isSignificant(), res4.isSignificant());
	
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", resTest5.getPValue(), res5.getPValue(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (effect size)", resTest5.getEffectSize(), res5.getEffectSize(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (order)", resTest5.getOrder(), res5.getOrder());
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (significance)", resTest5.isSignificant(), res5.isSignificant());
	
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", resTest6.getPValue(), res6.getPValue(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (effect size)", resTest6.getEffectSize(), res6.getEffectSize(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (order)", resTest6.getOrder(), res6.getOrder());
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (significance)", resTest6.isSignificant(), res6.isSignificant());
	}

	/**Randomly creates a set of data points through the same method as used in ExampleDatapointGenerator.
	 * 
	 * @param ran the random number generator
	 * @param bias with what probability should generate higher values
	 * @param gap by how much, on average, the higher values 
	 * generated should be higher than the lower ones
	 * @param n the number of results to generate 
	 * @param secondSeeds whether the seeds used should be the second one generated, fourth, sixth etc.
	 * @return
	 */
	private List<Pair<Double, Boolean>> artificiallyCreateDataPoints(MersenneTwister ran, double bias, double gap, int n, boolean paired, boolean secondSeeds) {
		long[] seeds = new long[n];
		if(secondSeeds && !paired)
			ran.nextLong();
		for(int i =0; i < n; i++){
			seeds[i] = ran.nextLong();
			if(!paired)
				ran.nextLong();
		}
		List<Pair<Double, Boolean>> points = new ArrayList<Pair<Double, Boolean>>();
		for(int i =0; i < n; i++)
			points.add(this.artificiallyCreateDatapoint(new MersenneTwister(seeds[i]), bias, gap));
		
		return points;
	}
	
	/**Randomly create a single data point. 
	 * Called from createDataPoints() and the parameters are as described in that method.
	 * 
	 * @param random
	 * @param bias
	 * @param gap
	 * @param reRandomize
	 * @return
	 */
	private Pair<Double, Boolean> artificiallyCreateDatapoint(
			MersenneTwister random, double bias, double gap) {
		double val = random.nextGaussian();
		if(random.nextDouble() > bias)//for higher values
			val += gap;
		boolean bool = (val > bias);
		return new Pair<Double, Boolean>(val,bool);
	}
	
	/**Randomly create an array of data points representing a time series, using the same method as in ExampleTimeseriesGenerator.
	 * The parameters are the same as in that method and the data point generator.
	 * 
	 * @param random
	 * @param bias
	 * @param gap
	 * @param duration
	 * @param reRandomize
	 * @return
	 */
	private List<Pair<Double, Boolean>> artificiallyCreateTimeseries(
			MersenneTwister random, double bias, int gap, int duration) {
		List<Pair<Double, Boolean>> results = new ArrayList<Pair<Double, Boolean>>();
		for(int i =0; i < duration; i++){
			double val = random.nextGaussian();
			if(random.nextDouble() > bias)//higher values
				val += gap;
			//so that value increases during the course of the time series 
			val *= (((double)i/(double)duration)  * 2);
			boolean bool = (val > bias);
			results.add(new Pair<Double, Boolean>(val,bool));
		}
		return results;
	}

	/**Test 2 = Run tests with the time series generator. Without censoring or incrementing.
	 */
	@Test
	public void TimeseriesGenerators(){
			//output
			try {
				Report.setupLaTeXLoggers("reports/layer2/report2.tex");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//////////////the actual tests///////////////////////

			//get results from Layer2
			ExampleTimeseriesGenerator gen1 = new ExampleTimeseriesGenerator(0.9, 5,100);//the first data set is higher (0.9)
			ExampleTimeseriesGenerator gen2 = new ExampleTimeseriesGenerator(0.1, 5,100);//the second data set is lower (0.1)
			MersenneTwister ran = new MersenneTwister(0);
			
			//setup sets of experiments
			SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>> gen1Exp = new SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>>(gen1, "test1");
			SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>> gen2Exp = new SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>>(gen2, "test2");

			SetOfComparisons<TimeseriesGeneratorOutput<PairGeneratorOutput>> gen1gen2Comp = new SetOfComparisons<TimeseriesGeneratorOutput<PairGeneratorOutput>>(gen1Exp,gen2Exp);

			Result res = Layer2.run(gen1gen2Comp, 0.05, false, false, new NoIncrementing(30), ran).get(0);
			
			//////////////////separately create the expected results to compare against/////////////////////////////////////
			
			//higher first dataset
			List<List<Pair<Double, Boolean>>> artificialResults1= new ArrayList<List<Pair<Double, Boolean>>>();
			//lower second
			List<List<Pair<Double, Boolean>>> artificialResults2= new ArrayList<List<Pair<Double, Boolean>>>();
			MersenneTwister ranTest = new MersenneTwister(0);
			long [] seeds = new long [ 60 ];
			for( int i=0; i<60; ++i )//create seeds
				seeds[ i ] = ranTest.nextLong();
			for(int i =0; i < 60; i+= 2){//create data
				//higher first dataset
				artificialResults1.add(artificiallyCreateTimeseries(new MersenneTwister(seeds[i]), 0.9, 5, 100));
				//lower second
				artificialResults2.add(artificiallyCreateTimeseries(new MersenneTwister(seeds[i + 1]), 0.1, 5, 100));
			}
		
			//get the final result from each time series and place it into a set of final results
			//for first data set
			List<Double> artificialResults1FinalGen = new ArrayList<Double>();
			ListIterator<List<Pair<Double, Boolean>>> iter = artificialResults1.listIterator();
			while(iter.hasNext()){
				List<Pair<Double, Boolean>> next = iter.next();
				artificialResults1FinalGen.add(next.get(next.size() - 1).getFirst());
			}
			//for second
			List<Double> artificialResults2FinalGen = new ArrayList<Double>();
			iter = artificialResults2.listIterator();
			while(iter.hasNext()){
				List<Pair<Double, Boolean>> next = iter.next();
				artificialResults2FinalGen.add(next.get(next.size() - 1).getFirst());
			}
			//get results from Layer1
			Result resTest = Layer1.compare(artificialResults1FinalGen, artificialResults2FinalGen, 0.05, false, null);
			///////////////compare significance etc. for results Layer2 generates vs Layer1//////////
			
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", resTest.getPValue(), res.getPValue(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (effect size)", resTest.getEffectSize(), res.getEffectSize(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (order)", resTest.getOrder(), res.getOrder());
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (significance)", resTest.isSignificant(), res.isSignificant());
	}
	
	
	/**Test 3 = Run tests with the time series generator. With censoring and with no incrementing.
	 * 
	 */
	@Test
	public void TimeseriesGeneratorsCensoring(){
			try {
				Report.setupLaTeXLoggers("reports/layer2/report3.tex");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//////////////the actual tests///////////////////////

			ExampleTimeseriesGenerator gen1 = new ExampleTimeseriesGenerator(0.9, 5,100);
			ExampleTimeseriesGenerator gen2 = new ExampleTimeseriesGenerator(0.1, 5,100);
			
			//setup sets of experiments
			SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>> gen1Exp = new SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>>(gen1, "test1");
			SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>> gen2Exp = new SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>>(gen2, "test2");

			SetOfComparisons<TimeseriesGeneratorOutput<PairGeneratorOutput>> gen1gen2Comp = new SetOfComparisons<TimeseriesGeneratorOutput<PairGeneratorOutput>>(gen1Exp,gen2Exp);

			MersenneTwister ran = new MersenneTwister(0);
			Result res = Layer2.run(gen1gen2Comp, 0.05, false, false, new CensoringStrategy(true), new NoIncrementing(30), ran).get(0);
			
			////////////////separately create the expected results to compare against///////////////////////////////////////////////////////////
			
			List<List<Pair<Double, Boolean>>> artificialResults1= new ArrayList<List<Pair<Double, Boolean>>>();
			List<List<Pair<Double, Boolean>>> artificialResults2= new ArrayList<List<Pair<Double, Boolean>>>();
			MersenneTwister ranTest = new MersenneTwister(0);
			
			long [] seeds = new long [ 60 ];
			for( int i=0; i<60; ++i )
				seeds[ i ] = ranTest.nextLong();
			for(int i =0; i < 60; i+= 2){
				artificialResults1.add(artificiallyCreateTimeseries(new MersenneTwister(seeds[i]), 0.9, 5, 100));
				artificialResults2.add(artificiallyCreateTimeseries(new MersenneTwister(seeds[i+1]), 0.1, 5, 100));
			}
		
			//only difference from uncensored method TimeseriesGenerators,
			//collect boolean data from last generation instead of double
			List<Boolean> artificialResults1FinalGen = new ArrayList<Boolean>();
			ListIterator<List<Pair<Double, Boolean>>> iter = artificialResults1.listIterator();
			while(iter.hasNext()){
				List<Pair<Double, Boolean>> next = iter.next();
				artificialResults1FinalGen.add(next.get(next.size() - 1).getSecond());
			}
			
			List<Boolean> artificialResults2FinalGen = new ArrayList<Boolean>();
			iter = artificialResults2.listIterator();
			while(iter.hasNext()){
				List<Pair<Double, Boolean>> next = iter.next();
				artificialResults2FinalGen.add(next.get(next.size() - 1).getSecond());
			}
			
			Result resTest = Layer1.compareCensored(artificialResults1FinalGen, artificialResults2FinalGen, 0.05, false);
			
			////////////////////compare the two sets of results - p value, effect size etc./////////////////////////
			
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", resTest.getPValue(), res.getPValue(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (effect size)", resTest.getEffectSize(), res.getEffectSize(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (order)", resTest.getOrder(), res.getOrder());
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (significance)", resTest.isSignificant(), res.isSignificant());
	}
	
	
	/**Test 4a = Run tests with the time series generator. With censoring and with no incrementing.	
	 * Use a complex censoring strategy this time, 
	 * in which an additional test using data from point 50 is used if the initial final point test doesn't achieve significance.
	 */
	@Test
	public void TimeseriesCensoringAdditionalCensorPoints(){
		try {
			Report.setupLaTeXLoggers("reports/layer2/report4a.tex");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//////////////the actual tests///////////////////////

		ExampleTimeseriesGenerator gen1 = new ExampleTimeseriesGenerator(0.6, 5,100);
		ExampleTimeseriesGenerator gen2 = new ExampleTimeseriesGenerator(0.5, 5,100);
		
		//setup sets of experiments
		SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>> gen1Exp = new SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>>(gen1, "test1");
		SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>> gen2Exp = new SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>>(gen2, "test2");

		SetOfComparisons<TimeseriesGeneratorOutput<PairGeneratorOutput>> gen1gen2Comp = new SetOfComparisons<TimeseriesGeneratorOutput<PairGeneratorOutput>>(gen1Exp,gen2Exp);
		
		MersenneTwister ran = new MersenneTwister(0);
		
		Result res = Layer2.run(gen1gen2Comp, 0.05, false, false, new CensoringStrategy(new int[]{50},false), new NoIncrementing(30), ran).get(0);
		
		/////////////separately create the expected results to compare against///////////////////////////////////////////////////
		
		MersenneTwister ranTest = new MersenneTwister(0);
		long [] seeds = new long [ 60 ];
		for( int i=0; i<60; ++i )
			seeds[ i ] = ranTest.nextLong();
		
		//data from final point/generation for initial test
		List<Boolean> artificialResults1FinalGen = new ArrayList<Boolean>();
		List<Boolean> artificialResults2FinalGen = new ArrayList<Boolean>();
		//data from point/generation 50 
		List<Boolean> artificialResults1Gen50 = new ArrayList<Boolean>();
		List<Boolean> artificialResults2Gen50 = new ArrayList<Boolean>();
		for(int i =0; i < 60; i+=2){
			List<Pair<Double, Boolean>> timeseries1 = artificiallyCreateTimeseries(new MersenneTwister(seeds[i]), 0.6, 5, 100);
			List<Pair<Double, Boolean>> timeseries2 = artificiallyCreateTimeseries(new MersenneTwister(seeds[i+1]), 0.5, 5, 100);
			
			artificialResults1FinalGen.add(timeseries1.get(99).getSecond());
			artificialResults2FinalGen.add(timeseries2.get(99).getSecond());
			artificialResults1Gen50.add(timeseries1.get(50).getSecond());
			artificialResults2Gen50.add(timeseries2.get(50).getSecond());
		}
		
		Result resTest = Layer1.compareCensored(artificialResults1FinalGen, artificialResults2FinalGen, 0.05, false);//compare censored at final generation
		//move onto point 50 test only if initial test did not result in a significant result
		if(!resTest.isSignificant()){ 
			resTest = Layer1.compareCensored(artificialResults1Gen50, artificialResults2Gen50, 0.05, false);//compare censored at generation 50 - intermediate artificial censoring point
		}
		
		///////////////////compare significance etc.//////////
		
		assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", resTest.getPValue(), res.getPValue(), 0);
		assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (effect size)", resTest.getEffectSize(), res.getEffectSize(), 0);
		assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (order)", resTest.getOrder(), res.getOrder());
		assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (significance)", resTest.isSignificant(), res.isSignificant());
	}
	
	/**Test 4b = Run tests with the time series generator. With censoring and with no incrementing.	
	 * Use a complex censoring strategy this time, 
	 * in which an additional test using the time that timeseries's take to reach a passing grade
	 * is compared using a regular (non censored) p value test.
	 * 
	 */
	@Test
	public void TimeseriesCensoringLengthOfTime(){
		try {
			Report.setupLaTeXLoggers("reports/layer2/report4b.tex");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//////////////the actual tests///////////////////////

		ExampleTimeseriesGenerator gen1 = new ExampleTimeseriesGenerator(0.6, 5,100);
		ExampleTimeseriesGenerator gen2 = new ExampleTimeseriesGenerator(0.5, 5,100);
		
		//setup sets of experiments
		SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>> gen1Exp = new SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>>(gen1, "test1");
		SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>> gen2Exp = new SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>>(gen2, "test2");

		SetOfComparisons<TimeseriesGeneratorOutput<PairGeneratorOutput>> gen1gen2Comp = new SetOfComparisons<TimeseriesGeneratorOutput<PairGeneratorOutput>>(gen1Exp,gen2Exp);

		//standard test
		Result res = Layer2.run(gen1gen2Comp, 0.05, false, false, new CensoringStrategy(true, true), new NoIncrementing(30), new MersenneTwister(0)).get(0);//testing that the non dichotomous test uses brunner munzel
		//run this test also with brunner munzel set as the non censored test should allow this setting,
		//even though the censored test doesn't
		Result res2 = Layer2.run(gen1gen2Comp, 0.05, true, false, new CensoringStrategy(true, true), new NoIncrementing(30), new MersenneTwister(0)).get(0);//testing that the non dichotomous test uses brunner munzel

		
		//////////////separately create the expected results to compare against///////////////////////////////////////////////
		
		MersenneTwister ranTest = new MersenneTwister(0);
		long [] seeds = new long [ 60 ];
		for( int i=0; i<60; ++i )
			seeds[ i ] = ranTest.nextLong();
		
		List<Boolean> artificialResults1Censor = new ArrayList<Boolean>();
		List<Boolean> artificialResults2Censor = new ArrayList<Boolean>();
		List<Double> artificialResults1Times = new ArrayList<Double>();//datasets for time to pass
		List<Double> artificialResults2Times = new ArrayList<Double>();
		for(int i =0; i < 60; i+= 2){
			List<Pair<Double, Boolean>> timeseries1 = artificiallyCreateTimeseries(new MersenneTwister(seeds[i]), 0.6, 5, 100);
			List<Pair<Double, Boolean>> timeseries2 = artificiallyCreateTimeseries(new MersenneTwister(seeds[i+1]), 0.5, 5, 100);
			//get regular, censored results
			artificialResults1Censor.add(timeseries1.get(99).getSecond());
			artificialResults2Censor.add(timeseries2.get(99).getSecond());
			//get the the length of time based results
			double timeToPass1 = getTimeToPass(timeseries1);
			double timeToPass2 = getTimeToPass(timeseries2);
			if(timeToPass1 > -1)//if has at least reached pass at some point in the run
				artificialResults1Times.add(timeToPass1);//add time to pass to dataset
			if(timeToPass2 > -1)
				artificialResults2Times.add(timeToPass2);		
		}
		
		//initial test with Layer1
		Result resTest = Layer1.compareCensored(artificialResults1Censor, artificialResults2Censor, 0.05, false);//compare censored at final generation
		Result resTest2 = null;
		//time based test with Layer1 if needed
		if(!resTest.isSignificant()){
			resTest2 = Layer1.compare(artificialResults1Times, artificialResults2Times, 0.05, true, null);//compare non censored - time to reach a pass - using Brunner Munzel
		}
		//now do time based test with layer 1 and brunner munzel set 
		if(!resTest.isSignificant()){
			resTest = Layer1.compare(artificialResults1Times, artificialResults2Times, 0.05, false, null);//compare non censored - time to reach a pass 
		}
		
		/////////////compare significance etc.//////////////////
		
		assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", resTest.getPValue(), res.getPValue(), 0);
		assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (effect size)", resTest.getEffectSize(), res.getEffectSize(), 0);
		assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (order)", resTest.getOrder(), res.getOrder());
		assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (significance)", resTest.isSignificant(), res.isSignificant());
	
		//comparison using Brunner Munzel
		if(resTest2 != null){//if the second test was done at all 
			//(if significance was obtained in the first test then this isn't relevant)
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", resTest2.getPValue(), res2.getPValue(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (effect size)", resTest2.getEffectSize(), res2.getEffectSize(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (order)", resTest2.getOrder(), res2.getOrder());
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (significance)", resTest2.isSignificant(), res2.isSignificant());
		}
	}
	
	/**Test 5 = Run tests with the time series generator. Without censoring and with incrementing.	
	 * Starting with 20 runs, incrementing until 100 runs has passed or signficance is reached.
	 * 
	 */
	@Test
	public void TimeseriesGeneratorsIncrementing(){
			try {
				Report.setupLaTeXLoggers("reports/layer2/report5.tex");
			} catch (IOException e) {
				e.printStackTrace();
			}
		
			//////////////the actual tests///////////////////////

			
			ExampleTimeseriesGenerator gen1 = new ExampleTimeseriesGenerator(0.6, 5,100);
			ExampleTimeseriesGenerator gen2 = new ExampleTimeseriesGenerator(0.4, 5,100);
			
			//setup sets of experiments
			SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>> gen1Exp = new SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>>(gen1, "test1");
			SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>> gen2Exp = new SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>>(gen2, "test2");

			SetOfComparisons<TimeseriesGeneratorOutput<PairGeneratorOutput>> gen1gen2Comp = new SetOfComparisons<TimeseriesGeneratorOutput<PairGeneratorOutput>>(gen1Exp,gen2Exp);

			
			MersenneTwister ran = new MersenneTwister(0);
			Result res = Layer2.run(gen1gen2Comp, 0.05, false, false, new SimpleIncrementing(20,100), ran).get(0);
			
			
			/////////////////separately create the expected results to compare against/////////////////////////////////////////
			MersenneTwister ranTest = new MersenneTwister(0);
			long [] seeds = new long [ 200 ];
			for( int i=0; i<200; ++i )
				seeds[ i ] = ranTest.nextLong();
			
			//generate first 20		
			List<Double> artificialResults1FinalGen = new ArrayList<Double>();
			List<Double> artificialResults2FinalGen = new ArrayList<Double>();
			for(int i =0; i < 40; i+=2){
				artificialResults1FinalGen.add(artificiallyCreateTimeseries(new MersenneTwister(seeds[i]), 0.6, 5, 100).get(99).getFirst());
				artificialResults2FinalGen.add(artificiallyCreateTimeseries(new MersenneTwister(seeds[i+1]), 0.4, 5, 100).get(99).getFirst());
			}
			//compare using first 20
			Result resTest = Layer1.compare(artificialResults1FinalGen, artificialResults2FinalGen, 0.05, false, null);
			
			//generate further results
			int i = 40;
			while(resTest == null || !resTest.isSignificant() && i < 200){
				for(int i2 = 0; i2 < 40; i2+= 2){
					artificialResults1FinalGen.add(artificiallyCreateTimeseries(new MersenneTwister(seeds[i + i2]), 0.6, 5, 100).get(99).getFirst());
					artificialResults2FinalGen.add(artificiallyCreateTimeseries(new MersenneTwister(seeds[i + i2 +1]), 0.4, 5, 100).get(99).getFirst());	
				}
				//compare additional result each time
				resTest = Layer1.compare(artificialResults1FinalGen, artificialResults2FinalGen, 0.05, false, null);
				i+=40;
			}

			///////////////////////compare significance etc.////////////////////////////////////////////
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", resTest.getPValue(), res.getPValue(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (effect size)", resTest.getEffectSize(), res.getEffectSize(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (order)", resTest.getOrder(), res.getOrder());
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (significance)", resTest.isSignificant(), res.isSignificant());
	}
	
	/**Test 6 = Run tests with the time series generator. With censoring and with incrementing.	
	 * Starting with 20 runs, incrementing until 100 runs has passed or significance is reached. 
	 */
	@Test
	public void TimeseriesGeneratorsIncrementingAndCensoring(){
		try {
			Report.setupLaTeXLoggers("reports/layer2/report6.tex");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//////////////the actual tests///////////////////////
		
		ExampleTimeseriesGenerator gen1 = new ExampleTimeseriesGenerator(0.6, 5,100);
		ExampleTimeseriesGenerator gen2 = new ExampleTimeseriesGenerator(0.4, 5,100);
		
		//setup sets of experiments
		SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>> gen1Exp = new SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>>(gen1, "test1");
		SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>> gen2Exp = new SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>>(gen2, "test2");

		SetOfComparisons<TimeseriesGeneratorOutput<PairGeneratorOutput>> gen1gen2Comp = new SetOfComparisons<TimeseriesGeneratorOutput<PairGeneratorOutput>>(gen1Exp,gen2Exp);

		
		MersenneTwister ran = new MersenneTwister(0);
		Result res = Layer2.run(gen1gen2Comp, 0.05, false, false, new CensoringStrategy(true), new SimpleIncrementing(20,100), ran).get(0);
		
		////////////////separately create the expected results to compare against////////////
		MersenneTwister ranTest = new MersenneTwister(0);
		
		long [] seeds = new long [ 200 ];
		for( int i=0; i<200; i++ )
			seeds[ i ] = ranTest.nextLong();
		
		//initial set			
		List<Boolean> artificialResults1FinalGen = new ArrayList<Boolean>();
		List<Boolean> artificialResults2FinalGen = new ArrayList<Boolean>();

		for(int i =0; i < 40; i+=2){
			artificialResults1FinalGen.add(artificiallyCreateTimeseries(new MersenneTwister(seeds[i]), 0.6, 5, 100).get(99).getSecond());
			artificialResults2FinalGen.add(artificiallyCreateTimeseries(new MersenneTwister(seeds[i+1]), 0.4, 5, 100).get(99).getSecond());
		}
		
		//additional increments
		Result resTest = Layer1.compareCensored(artificialResults1FinalGen, artificialResults2FinalGen, 0.05, false);
		int i = 40;
		while(resTest == null || !resTest.isSignificant() && i < 200){
			for(int i2 = 0; i2 < 40; i2+=2){
				artificialResults1FinalGen.add(artificiallyCreateTimeseries(new MersenneTwister(seeds[i + i2]), 0.6, 5, 100).get(99).getSecond());
				artificialResults2FinalGen.add(artificiallyCreateTimeseries(new MersenneTwister(seeds[i + i2 + 1]), 0.4, 5, 100).get(99).getSecond());	
			}
			resTest = Layer1.compareCensored(artificialResults1FinalGen, artificialResults2FinalGen, 0.05, false);
			i+= 40;
		}
		
		///////////////compare significance etc./////////////////////////
		assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", resTest.getPValue(), res.getPValue(), 0);
		assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (effect size)", resTest.getEffectSize(), res.getEffectSize(), 0);
		assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (order)", resTest.getOrder(), res.getOrder());
		assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (significance)", resTest.isSignificant(), res.isSignificant());
	}

	/**Determines how many time intervals have passed in a dichotomous data set before 
	 * a passing (true) result is first seen.
	 * 
	 * @param timeseries the data set
	 * @return
	 */
	private Double getTimeToPass(List<Pair<Double, Boolean>> timeseries) {
		int i= 0;
		ListIterator<Pair<Double, Boolean>> iter = timeseries.listIterator();
		while(iter.hasNext()){
			if(iter.next().getSecond())
				return (double) i;
			i++;
		}
		return -1.0;
	}

	/**Test 1 = Runs tests with the data point generator, with and without paired data, censoring and brunner munzel.
	 */
	@Test
	public void DatapointGeneratorsIncrementing(){
		//output
		try {
			Report.setupLaTeXLoggers("reports/layer2/report7.tex");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		final int initialRuns = 30;
		final int maxRuns = 100;
		
		//////////////tests with a significant difference///////////////////////
			
			//setup generators
			ExampleDatapointGenerator gen1 = new ExampleDatapointGenerator(0.9, 1);
			ExampleDatapointGenerator gen2 = new ExampleDatapointGenerator(0.1, 1);
			
			//setup sets of experiments
			SingleArtefactExperiments<PairGeneratorOutput> gen1Exp = new SingleArtefactExperiments<PairGeneratorOutput>(gen1, "test1");
			SingleArtefactExperiments<PairGeneratorOutput> gen2Exp = new SingleArtefactExperiments<PairGeneratorOutput>(gen2, "test2");

			SetOfComparisons<PairGeneratorOutput> gen1gen2Comp = new SetOfComparisons<PairGeneratorOutput>(gen1Exp,gen2Exp);
			
			//results from layer2
			Result res = Layer2.run(gen1gen2Comp, 0.05, false, false, new SimpleIncrementing(initialRuns, maxRuns), new MersenneTwister(0)).get(0);
	
			//////////////separately create the expected results to compare against///////////////////////////////////////
		
			List<Pair<Double, Boolean>> artificialResults1 = artificiallyCreateDataPoints(new MersenneTwister(0),0.9,1,initialRuns,false,false);
			List<Pair<Double, Boolean>> artificialResults2 = artificiallyCreateDataPoints(new MersenneTwister(0),0.1,1,initialRuns,false,true);

			Result resTest = null;
			boolean finished = false; //significance achieved
			//increment
			int n = initialRuns;
			while(artificialResults1.size() < maxRuns && !finished){
				resTest = Layer1.comparePair(artificialResults1, artificialResults2, 0.05, false, false, false, "dataA", "dataB", null, null);
				if(resTest.getPValue() < 0.05)
					finished = true;
				else{
					n += initialRuns;
					artificialResults1 = artificiallyCreateDataPoints(new MersenneTwister(0),0.9,1,n,false,false);
					artificialResults2 = artificiallyCreateDataPoints(new MersenneTwister(0),0.1,1,n,false,true);
				}
			}
			
			
			///compare results from layer 1 and the artificial data with those from layer2 
			//(compare p value, effect size, whether is significant and order using JUnit tests)
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", resTest.getPValue(), res.getPValue(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (effect size)", resTest.getEffectSize(), res.getEffectSize(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (order)", resTest.getOrder(), res.getOrder());
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (significance)", resTest.isSignificant(), res.isSignificant());

	
	//////////////tests without a significant difference///////////////////////
			
			//setup generators
			gen1 = new ExampleDatapointGenerator(0.6, 1);
			gen2 = new ExampleDatapointGenerator(0.4, 1);
			
			//setup sets of experiments
			gen1Exp = new SingleArtefactExperiments<PairGeneratorOutput>(gen1, "test1");
			gen2Exp = new SingleArtefactExperiments<PairGeneratorOutput>(gen2, "test2");
			gen1gen2Comp = new SetOfComparisons<PairGeneratorOutput>(gen1Exp,gen2Exp);

			res = Layer2.run(gen1gen2Comp, 0.05, false, false, new SimpleIncrementing(initialRuns, maxRuns), new MersenneTwister(1)).get(0);
	
			//////////////separately create the expected results to compare against///////////////////////////////////////
		
			artificialResults1 = artificiallyCreateDataPoints(new MersenneTwister(1),0.6,1,30,false,false);
			artificialResults2 = artificiallyCreateDataPoints(new MersenneTwister(1),0.4,1,30,false,true);


			//values to compare
			resTest = null;
			finished = false; //significance achieved
			//increment
			n = initialRuns;
			
			resTest = Layer1.comparePair(artificialResults1, artificialResults2, 0.05, false, false, false, "dataA", "dataB", null, null);

			while(artificialResults1.size() < maxRuns && !finished){
				if(resTest.getPValue() < 0.05)
					finished = true;
				else{
					n += initialRuns;
					artificialResults1 = artificiallyCreateDataPoints(new MersenneTwister(1),0.6,1,n,false,false);
					artificialResults2 = artificiallyCreateDataPoints(new MersenneTwister(1),0.4,1,n,false,true);
				}				
				
				resTest = Layer1.comparePair(artificialResults1, artificialResults2, 0.05, false, false, false, "dataA", "dataB", null, null);

			}
			
			
			///compare results from layer 1 and the artificial data with those from layer2 
			//(compare p value, effect size, whether is significant and order using JUnit tests)
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", resTest.getPValue(), res.getPValue(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (effect size)", resTest.getEffectSize(), res.getEffectSize(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (order)", resTest.getOrder(), res.getOrder());
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (significance)", resTest.isSignificant(), res.isSignificant());

	}



}

// End ///////////////////////////////////////////////////////////////

