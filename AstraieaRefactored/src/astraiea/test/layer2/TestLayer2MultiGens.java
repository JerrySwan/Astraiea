package astraiea.test.layer2;
//TODO - the description in each assert
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.math3.util.Pair;
import org.junit.Test;

import astraiea.Result;
import astraiea.layer1.Layer1;
import astraiea.layer2.BonFerroni;
import astraiea.layer2.Layer2;
import astraiea.layer2.ResultSet;
import astraiea.layer2.generators.Generator;
import astraiea.layer2.generators.MultipleArtefactOutput;
import astraiea.layer2.generators.Timeseries;
import astraiea.layer2.generators.simpleGenerators.PairGeneratorOutput;
import astraiea.layer2.multipleExperiments.SetOf1ArtefactExperiments;
import astraiea.layer2.multipleExperiments.SetOfComparisons;
import astraiea.layer2.multipleExperiments.SetOfExperiments;
import astraiea.layer2.multipleExperiments.SetOfMultiArtefactExperiments;
import astraiea.layer2.strategies.CensoringStrategy;
import astraiea.layer2.strategies.NoIncrementing;
import astraiea.layer2.strategies.SimpleIncrementing;
import astraiea.output.Report;
import astraiea.test.layer2.generators.ExampleDatapointGenerator;
import astraiea.test.layer2.generators.ExampleDatapointMultiArtefactGenerator;
import astraiea.test.layer2.generators.ExampleTimeseriesGenerator;
import astraiea.util.MersenneTwister;

/**
 * JUnit tests for all layer 2 configurations. Carried out by comparing the outcome of carrying out experimentation through layer 2
 * with the outcome of independently generating data and carrying out experimentation directly through layer 1 
 * (which is known to be correct because it is tested in Layer1JUnit class).
 * If retesting this class Layer1JUnit should also be retested. 
 * Layer2Junit assumes that Layer1Junit is correct as it uses Layer1 as an oracle.
 * 
 * @author Geoffrey Neumann
 *
 */

public class TestLayer2MultiGens {

	/**Test 1 = Runs tests with the data point and the time series generators with multiple artefacts.
	 */
	@Test
	public void MultipleArtefacts(){
		//output
		try {
			Report.setupLaTeXLoggers("reports/layer2/multiple/report1.tex");
		} catch (IOException e) {
			e.printStackTrace();
		}		
		
		//////////////the actual tests///////////////////////
						
			//make the multiple artefacts
			int numArtefacts = 100;
			List<Generator<MultipleArtefactOutput<PairGeneratorOutput>>> gens1D = new ArrayList<Generator<MultipleArtefactOutput<PairGeneratorOutput>>>();
			List<Generator<MultipleArtefactOutput<PairGeneratorOutput>>> gens2D = new ArrayList<Generator<MultipleArtefactOutput<PairGeneratorOutput>>>();
			for(int i =0; i < numArtefacts; i++){
				//setup generators with a significant difference
				gens1D.add(new ExampleDatapointMultiArtefactGenerator(0.9, 1, 100, i));
				gens2D.add(new ExampleDatapointMultiArtefactGenerator(0.1, 1, 100, i));
			}
			
			//setup sets of experiments
			SetOfExperiments<MultipleArtefactOutput<PairGeneratorOutput>> gen1Exp = new SetOfMultiArtefactExperiments<PairGeneratorOutput>(gens1D);
			SetOfExperiments<MultipleArtefactOutput<PairGeneratorOutput>> gen2Exp = new SetOfMultiArtefactExperiments<PairGeneratorOutput>(gens2D);
			
			SetOfComparisons gen1gen2Comp = new SetOfComparisons(gen1Exp,gen2Exp);

			Result resD1 = Layer2.run(gen1gen2Comp, 0.05, false, new NoIncrementing(30), 50, new MersenneTwister(0)).get(0).getRes();
			Result resD2 = Layer2.run(gen1gen2Comp, 0.05, false, new SimpleIncrementing(20, 100), 50, new MersenneTwister(1)).get(0).getRes();

			Result resT1 = Layer2.run(gen1gen2Comp, 0.05, false, new NoIncrementing(30), 50, new MersenneTwister(2)).get(0).getRes();
			Result resT2 = Layer2.run(new TimeseriesGeneratorSet(gen1T, gen2T), 0.05, false, new SimpleIncrementing(20, 100), 50, new MersenneTwister(3)).get(0).getRes();

			MersenneTwister ran = new MersenneTwister(0);
			ArrayList<Double> medians = new ArrayList<Double>();
			ArrayList<Double> mediansB = new ArrayList<Double>();
			long[] seeds = new long[1500];
			for(int i =0; i < 1500; i++)
				seeds[i] = ran.nextLong();
			
			int a = 0;
			List<Pair<Double, Boolean>> artificialResults = new ArrayList<Pair<Double, Boolean>>();
			List<Pair<Double, Boolean>> artificialResultsB = new ArrayList<Pair<Double, Boolean>>();
			
			for(int i =0; i < 1500; i++){
				MersenneTwister random = new MersenneTwister(seeds[i]);
				double val = random.nextGaussian();
				if(random.nextDouble() > 0.9)//for higher values
					val ++;
				boolean bool = (val > 0.9);
				artificialResults.add(new Pair<Double, Boolean>(val + a,bool));
				
				random = new MersenneTwister(seeds[i]);
				val = random.nextGaussian();
				if(random.nextDouble() > 0.1)//for higher values
					val ++;
				bool = (val > 0.1);
				artificialResultsB.add(new Pair<Double, Boolean>(val + a,bool));
				
				if((i + 1) % 50 == 0){//moving onto next artefact
					double[] tempRes = new double[50];
					for(int i2 = 0; i2 < 50; i2++){
						tempRes[i2] = artificialResults.get(i2 + (a * 50)).getFirst();
					}
					Arrays.sort(tempRes);
					medians.add(tempRes[24] + ((tempRes[25] - tempRes[24])/2));
					
					tempRes = new double[50];
					for(int i2 = 0; i2 < 50; i2++){
						tempRes[i2] = artificialResultsB.get(i2 + (a * 50)).getFirst();
					}
					Arrays.sort(tempRes);
					mediansB.add(tempRes[24] + ((tempRes[25] - tempRes[24])/2));
					a++;
				}
			}

			
			///////////TIME SERIES//////////////////////////////
			
			MersenneTwister ran2 = new MersenneTwister(2);
			ArrayList<Double> mediansT = new ArrayList<Double>();
			ArrayList<Double> mediansTB = new ArrayList<Double>();
			long[] seeds2 = new long[1500];
			for(int i =0; i < 1500; i++){
				seeds2[i] = ran2.nextLong();
			}				
			List<Double> artificialResultsT = new ArrayList<Double>();
			List<Double> artificialResultsTB = new ArrayList<Double>();
			a = 0;
			for(int i =0; i < 1500; i++){//for each artefact
				double[] tempRes = new double[50];
				double[] tempResB = new double[50];
				
				artificialResultsT.add(artificiallyCreateTimeseries(new MersenneTwister(seeds2[i]),0.9,5,100) + (int)(i / 50));
				artificialResultsTB.add(artificiallyCreateTimeseries(new MersenneTwister(seeds2[i]),0.1,5,100) + (int)(i / 50));

				
				if((i + 1) % 50 == 0){
					tempRes = new double[50];
					tempResB = new double[50];
					for(int i2 = 0; i2 < 50; i2++){
						tempRes[i2] = artificialResultsT.get(i2 + (a * 50));
						tempResB[i2] = artificialResultsTB.get(i2 + (a * 50));
					}
					Arrays.sort(tempRes);
					mediansT.add(tempRes[24] + ((tempRes[25] - tempRes[24])/2));
					Arrays.sort(tempResB);
					mediansTB.add(tempResB[24] + ((tempResB[25] - tempResB[24])/2));
					a++;
				}
			}

			
			//use layer1 to compare artificial data (mimic the comparison that should have been made in layer 2)
			Result resTest = Layer1.comparePaired(medians, mediansB, 0.05);
			
			//Result resTest2 = Layer1.comparePaired(mediansIncr, mediansIncrB, 0.05);
			Result resTest3 = Layer1.comparePaired(mediansT, mediansTB, 0.05);
			//Result resTest4 = Layer1.compare(mediansT20, mediansT20B, 0.05, false, true); TODO - incrementing

			//TODO - sort out incrementing after we have sequential sampling of implemented
			
			/////////////////////////////////////

			///compare results from layer 1 and the artificial data with those from layer2 
			//(compare p value, effect size, whether is significant and order using JUnit tests)
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", resTest.getPValue(), resD1.getPValue(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (effect size)", resTest.getEffectSize(), resD1.getEffectSize(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (order)", resTest.getOrder(), resD1.getOrder());
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (significance)", resTest.isSignificant(), resD1.isSignificant());
			
//			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", resTest2.getPValue(), resD2.getPValue(), 0);
//			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (effect size)", resTest2.getEffectSize(), resD2.getEffectSize(), 0);
//			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (order)", resTest2.getOrder(), resD2.getOrder());
//			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (significance)", resTest2.isSignificant(), resD2.isSignificant());
			
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", resTest3.getPValue(), resT1.getPValue(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (effect size)", resTest3.getEffectSize(), resT1.getEffectSize(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (order)", resTest3.getOrder(), resT1.getOrder());
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (significance)", resTest3.isSignificant(), resT1.isSignificant());
	}




	
	/**Randomly create an array of data points representing a time series, using the same method as in ExampleTimeseriesGenerator.
	 * The parameters are the same as in that method and the data point generator.
	 * 
	 * @param random
	 * @param bias
	 * @param gap
	 * @param duration
	 * @return
	 */
	private double artificiallyCreateTimeseries(
			MersenneTwister random, double bias, int gap, int duration) {
		double finalResult = 0;
		for(int i =0; i < duration; i++){
			double val = random.nextGaussian();
			if(random.nextDouble() > bias)//higher values
				val += gap;
			//so that value increases during the course of the time series 
			val *= (((double)i/(double)duration)  * 2);
			finalResult = val;
		}
		return finalResult;
	}

	/**Test 2 = Run tests with the time series generator. Without censoring or incrementing.
	 */
	@Test
	public void MultipleGenerators(){
			//output
			try {
				Report.setupLaTeXLoggers("reports/layer2/report2.tex");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//////////////the actual tests///////////////////////

			
			//get results from Layer2
			TimeseriesGenerator gen1 = new ExampleTimeseriesGenerator(0, 5,100);
			TimeseriesGenerator gen2 = new ExampleTimeseriesGenerator(0.2, 5,100);
			TimeseriesGenerator gen3 = new ExampleTimeseriesGenerator(0.4, 5,100);
			TimeseriesGenerator gen4 = new ExampleTimeseriesGenerator(0.6, 5,100);
			TimeseriesGenerator gen5 = new ExampleTimeseriesGenerator(0.8, 5,100);
			
			DatapointGenerator genD1 = new ExampleDatapointGenerator(0, 5);
			DatapointGenerator genD2 = new ExampleDatapointGenerator(0.2, 5);
			DatapointGenerator genD3 = new ExampleDatapointGenerator(0.4, 5);
			DatapointGenerator genD4 = new ExampleDatapointGenerator(0.6, 5);
			DatapointGenerator genD5 = new ExampleDatapointGenerator(0.8, 5);
			
			MersenneTwister ran = new MersenneTwister(0);
			List<TimeseriesGenerator> gens = new ArrayList<TimeseriesGenerator>();
			gens.add(gen1);
			gens.add(gen2);
			gens.add(gen3);
			gens.add(gen4);
			gens.add(gen5);

			
			List<DatapointGenerator> gensD = new ArrayList<DatapointGenerator>();
			gensD.add(genD1);
			gensD.add(genD2);
			gensD.add(genD3);
			gensD.add(genD4);
			gensD.add(genD5);

			//TIMESERIES
			//comparing each with each
			List<ResultSet> results = Layer2.run(new TimeseriesGeneratorSet(gens), 0.05, false, false, new NoIncrementing(30), new MersenneTwister(0));
			//comparing everything with one (gen1)
			List<ResultSet> results2 = Layer2.run(new TimeseriesGeneratorSet(gens,gen1), 0.05, false, false, new NoIncrementing(30), new MersenneTwister(1));//with main generator
			
			//DATAPOINT
			List<ResultSet> resultsD = Layer2.run(new DatapointGeneratorSet(gensD), 0.05, false, false, new NoIncrementing(30), new MersenneTwister(2));
			List<ResultSet> results2D = Layer2.run(new DatapointGeneratorSet(gensD,genD1), 0.05, false, false, new NoIncrementing(30), new MersenneTwister(3));//with main generator

			
			//////////////////separately create the expected results to compare against/////////////////////////////////////
			
			//Expected results for time series////////////////////////////////////
			List<Double> artificialResults1= new ArrayList<Double>();
			List<Double> artificialResults2= new ArrayList<Double>();
			List<Double> artificialResults3= new ArrayList<Double>();
			List<Double> artificialResults4= new ArrayList<Double>();
			List<Double> artificialResults5= new ArrayList<Double>();

			MersenneTwister ranTest = new MersenneTwister(0);
			long [] seeds = new long [ 30 ];
			long [] seeds1 = new long [ 30 ];
			long [] seeds2 = new long [ 30 ];
			long [] seeds3 = new long [ 30 ];
			long [] seeds4 = new long [ 30 ];

			for( int i=0; i<30; ++i ){//create seeds
				seeds[ i ] = ranTest.nextLong();
				seeds1[ i ] = ranTest.nextLong();
				seeds2[ i ] = ranTest.nextLong();
				seeds3[ i ] = ranTest.nextLong();
				seeds4[ i ] = ranTest.nextLong();
			}
			
			for(int i =0; i < 30; i++){//create data
				artificialResults1.add(artificiallyCreateTimeseries(new MersenneTwister(seeds[i]), 0, 5, 100));
				artificialResults2.add(artificiallyCreateTimeseries(new MersenneTwister(seeds1[i]), 0.2, 5, 100));
				artificialResults3.add(artificiallyCreateTimeseries(new MersenneTwister(seeds2[i]), 0.4, 5, 100));
				artificialResults4.add(artificiallyCreateTimeseries(new MersenneTwister(seeds3[i]), 0.6, 5, 100));
				artificialResults5.add(artificiallyCreateTimeseries(new MersenneTwister(seeds4[i]), 0.8, 5, 100));

			}

			Result resTest12 = Layer1.compare(artificialResults1, artificialResults2, 0.05, false, null);			
			Result resTest13 = Layer1.compare(artificialResults1, artificialResults3, 0.05, false, null);
			Result resTest14 = Layer1.compare(artificialResults1, artificialResults4, 0.05, false, null);
			Result resTest15 = Layer1.compare(artificialResults1, artificialResults5, 0.05, false, null);
			Result resTest23 = Layer1.compare(artificialResults2, artificialResults3, 0.05, false, null);
			Result resTest24 = Layer1.compare(artificialResults2, artificialResults4, 0.05, false, null);
			Result resTest25 = Layer1.compare(artificialResults2, artificialResults5, 0.05, false, null);
			Result resTest34 = Layer1.compare(artificialResults3, artificialResults4, 0.05, false, null);
			Result resTest35 = Layer1.compare(artificialResults3, artificialResults5, 0.05, false, null);
			Result resTest45 = Layer1.compare(artificialResults4, artificialResults5, 0.05, false, null);
			
			artificialResults1.clear();
			artificialResults2.clear();
			artificialResults3.clear();
			artificialResults4.clear();
			artificialResults5.clear();

			ranTest = new MersenneTwister(1);
			seeds = new long [ 30 ];
			seeds1 = new long [ 30 ];
			seeds2 = new long [ 30 ];
			seeds3 = new long [ 30 ];
			seeds4 = new long [ 30 ];

			for( int i=0; i<30; ++i ){//create seeds
				seeds[ i ] = ranTest.nextLong();
				seeds1[ i ] = ranTest.nextLong();
				seeds2[ i ] = ranTest.nextLong();
				seeds3[ i ] = ranTest.nextLong();
				seeds4[ i ] = ranTest.nextLong();
			}
			
			for(int i =0; i < 30; i++){//create data
				artificialResults1.add(artificiallyCreateTimeseries(new MersenneTwister(seeds[i]), 0, 5, 100));
				artificialResults2.add(artificiallyCreateTimeseries(new MersenneTwister(seeds1[i]), 0.2, 5, 100));
				artificialResults3.add(artificiallyCreateTimeseries(new MersenneTwister(seeds2[i]), 0.4, 5, 100));
				artificialResults4.add(artificiallyCreateTimeseries(new MersenneTwister(seeds3[i]), 0.6, 5, 100));
				artificialResults5.add(artificiallyCreateTimeseries(new MersenneTwister(seeds4[i]), 0.8, 5, 100));

			}
			
			Result resTest2_12 = Layer1.compare(artificialResults1, artificialResults2, 0.05, false, null);			
			Result resTest2_13 = Layer1.compare(artificialResults1, artificialResults3, 0.05, false, null);
			Result resTest2_14 = Layer1.compare(artificialResults1, artificialResults4, 0.05, false, null);
			Result resTest2_15 = Layer1.compare(artificialResults1, artificialResults5, 0.05, false, null);
			
			
			//And for data point///////////////////////////////
			List<Pair<Double,Boolean>> artificialResults1D = new ArrayList<Pair<Double,Boolean>>();
			List<Pair<Double,Boolean>> artificialResults2D = new ArrayList<Pair<Double,Boolean>>();
			List<Pair<Double,Boolean>> artificialResults3D = new ArrayList<Pair<Double,Boolean>>();
			List<Pair<Double,Boolean>> artificialResults4D = new ArrayList<Pair<Double,Boolean>>();
			List<Pair<Double,Boolean>> artificialResults5D = new ArrayList<Pair<Double,Boolean>>();

			ranTest = new MersenneTwister(2);
			seeds = new long [ 30 ];
			seeds1 = new long [ 30 ];
			seeds2 = new long [ 30 ];
			seeds3 = new long [ 30 ];
			seeds4 = new long [ 30 ];

			for( int i=0; i<30; ++i ){//create seeds
				seeds[ i ] = ranTest.nextLong();
				seeds1[ i ] = ranTest.nextLong();
				seeds2[ i ] = ranTest.nextLong();
				seeds3[ i ] = ranTest.nextLong();
				seeds4[ i ] = ranTest.nextLong();
			}
			
			for(int i =0; i < 30; i++){//create data
				
				
				
				artificialResults1D.add(artificiallyCreateDatapoint(new MersenneTwister(seeds[i]), 0, 5));
				artificialResults2D.add(artificiallyCreateDatapoint(new MersenneTwister(seeds1[i]), 0.2, 5));
				artificialResults3D.add(artificiallyCreateDatapoint(new MersenneTwister(seeds2[i]), 0.4, 5));
				artificialResults4D.add(artificiallyCreateDatapoint(new MersenneTwister(seeds3[i]), 0.6, 5));
				artificialResults5D.add(artificiallyCreateDatapoint(new MersenneTwister(seeds4[i]), 0.8, 5));

			}
			
 			Result resTest12D = Layer1.comparePair(artificialResults1D, artificialResults2D, 0.05, false, false, false, "dataA", "dataB", null, null);
 			Result resTest13D = Layer1.comparePair(artificialResults1D, artificialResults3D, 0.05, false, false, false, "dataA", "dataB", null, null);
			Result resTest14D = Layer1.comparePair(artificialResults1D, artificialResults4D, 0.05, false, false, false, "dataA", "dataB", null, null);
			Result resTest15D = Layer1.comparePair(artificialResults1D, artificialResults5D, 0.05, false, false, false, "dataA", "dataB", null, null);
			Result resTest23D = Layer1.comparePair(artificialResults2D, artificialResults3D, 0.05, false, false, false, "dataA", "dataB", null, null);
			Result resTest24D = Layer1.comparePair(artificialResults2D, artificialResults4D, 0.05, false, false, false, "dataA", "dataB", null, null);
			Result resTest25D = Layer1.comparePair(artificialResults2D, artificialResults5D, 0.05, false, false, false, "dataA", "dataB", null, null);
			Result resTest34D = Layer1.comparePair(artificialResults3D, artificialResults4D, 0.05, false, false, false, "dataA", "dataB", null, null);
			Result resTest35D = Layer1.comparePair(artificialResults3D, artificialResults5D, 0.05, false, false, false, "dataA", "dataB", null, null);
			Result resTest45D = Layer1.comparePair(artificialResults4D, artificialResults5D, 0.05, false, false, false, "dataA", "dataB", null, null);

			
				
			artificialResults1D.clear();
			artificialResults2D.clear();
			artificialResults3D.clear();
			artificialResults4D.clear();
			artificialResults5D.clear();

			ranTest = new MersenneTwister(3);
			seeds = new long [ 30 ];
			seeds1 = new long [ 30 ];
			seeds2 = new long [ 30 ];
			seeds3 = new long [ 30 ];
			seeds4 = new long [ 30 ];

			for( int i=0; i<30; ++i ){//create seeds
				seeds[ i ] = ranTest.nextLong();
				seeds1[ i ] = ranTest.nextLong();
				seeds2[ i ] = ranTest.nextLong();
				seeds3[ i ] = ranTest.nextLong();
				seeds4[ i ] = ranTest.nextLong();
			}
			
			for(int i =0; i < 30; i++){//create data
				artificialResults1D.add(artificiallyCreateDatapoint(new MersenneTwister(seeds[i]), 0, 5));
				artificialResults2D.add(artificiallyCreateDatapoint(new MersenneTwister(seeds1[i]), 0.2, 5));
				artificialResults3D.add(artificiallyCreateDatapoint(new MersenneTwister(seeds2[i]), 0.4, 5));
				artificialResults4D.add(artificiallyCreateDatapoint(new MersenneTwister(seeds3[i]), 0.6, 5));
				artificialResults5D.add(artificiallyCreateDatapoint(new MersenneTwister(seeds4[i]), 0.8, 5));

			}
			
			Result resTest2_12D = Layer1.comparePair(artificialResults1D, artificialResults2D, 0.05, false, false, false, "dataA", "dataB", null, null);
			Result resTest2_13D = Layer1.comparePair(artificialResults1D, artificialResults3D, 0.05, false, false, false, "dataA", "dataB", null, null);
			Result resTest2_14D = Layer1.comparePair(artificialResults1D, artificialResults4D, 0.05, false, false, false, "dataA", "dataB", null, null);
			Result resTest2_15D = Layer1.comparePair(artificialResults1D, artificialResults5D, 0.05, false, false, false, "dataA", "dataB", null, null);
			
			boolean[] passedTests = new boolean[10];
			boolean[] passedTests2 = new boolean[4];

			Arrays.fill(passedTests, false);
			Arrays.fill(passedTests2, false);

			
			//Tests with time series////////////////////////////////////////////////////////////////////////
			assertTrue("size of results array should be 10 (one for each pair)",results.size() == 10);
			assertTrue("size of results array should be 10 (one for each pair)",results2.size() == 4);
			ListIterator<ResultSet> resIter = results.listIterator();
			ResultSet res;

	
			while(resIter.hasNext()){
				res = resIter.next();
				if(res.isaComparisonOf("data1","data2")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), resTest12.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getEffectSize(), resTest12.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getOrder() == resTest12.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == resTest12.isSignificant());
					passedTests[0] = true;
				}
				else if(res.isaComparisonOf("data1","data3")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), resTest13.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getEffectSize(), resTest13.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getOrder() == resTest13.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == resTest13.isSignificant());
					passedTests[1] = true;
				}
				else if(res.isaComparisonOf("data1","data4")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), resTest14.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getEffectSize(), resTest14.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getOrder() == resTest14.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == resTest14.isSignificant());
					passedTests[2] = true;
				}
				else if(res.isaComparisonOf("data1","data5")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), resTest15.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getEffectSize(), resTest15.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getOrder() == resTest15.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == resTest15.isSignificant());
					passedTests[3] = true;
				}
				else if(res.isaComparisonOf("data2","data3")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), resTest23.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getEffectSize(), resTest23.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getOrder() == resTest23.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == resTest23.isSignificant());
					passedTests[4] = true;
				}
				else if(res.isaComparisonOf("data2","data4")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), resTest24.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getEffectSize(), resTest24.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getOrder() == resTest24.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == resTest24.isSignificant());
					passedTests[5] = true;
				}
				else if(res.isaComparisonOf("data2","data5")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), resTest25.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getEffectSize(), resTest25.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getOrder() == resTest25.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == resTest25.isSignificant());
					passedTests[6] = true;
				}
				else if(res.isaComparisonOf("data3","data4")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), resTest34.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getEffectSize(), resTest34.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getOrder() == resTest34.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == resTest34.isSignificant());
					passedTests[7] = true;
				}
				else if(res.isaComparisonOf("data3","data5")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), resTest35.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getEffectSize(), resTest35.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getOrder() == resTest35.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == resTest35.isSignificant());
					passedTests[8] = true;
				}
				else if(res.isaComparisonOf("data4","data5")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), resTest45.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getEffectSize(), resTest45.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getOrder() == resTest45.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == resTest45.isSignificant());
					passedTests[9] = true;
				}
					
			}
			
			//main generator
			
			resIter = results2.listIterator();
			while(resIter.hasNext()){
				res = resIter.next();
				if(res.isaComparisonOf("data1","data4")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), resTest2_14.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getEffectSize(), resTest2_14.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getOrder() == resTest2_14.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == resTest2_14.isSignificant());
					passedTests2[0] = true;
				}
				else if(res.isaComparisonOf("data1","data2")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), resTest2_12.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getEffectSize(), resTest2_12.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getOrder() == resTest2_12.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == resTest2_12.isSignificant());
					passedTests2[1] = true;
				}
				else if(res.isaComparisonOf("data1","data3")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), resTest2_13.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getEffectSize(), resTest2_13.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getOrder() == resTest2_13.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == resTest2_13.isSignificant());
					passedTests2[2] = true;
				}
				else if(res.isaComparisonOf("data1","data5")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), resTest2_15.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getEffectSize(), resTest2_15.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getOrder() == resTest2_15.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == resTest2_15.isSignificant());
					passedTests2[3] = true;
				}
					
			}
		
			//check all have passed
			assertTrue("all tests should have passed", !arrayContains(passedTests,false));
			
			//check all have passed for the main technique test
			assertTrue("all tests should have passed", !arrayContains(passedTests2,false));
			
			//Tests with data point////////////////////////////////////////////////////////////////////////
			assertTrue("size of results array should be 10 (one for each pair)",resultsD.size() == 10);
			assertTrue("size of results array should be 10 (one for each pair)",results2D.size() == 4);
			
			Arrays.fill(passedTests, false);
			Arrays.fill(passedTests2, false);

			
			resIter = resultsD.listIterator();
			while(resIter.hasNext()){
				res = resIter.next();
				if(res.isaComparisonOf("data1","data2")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), resTest12D.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getEffectSize(), resTest12D.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getOrder() == resTest12D.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == resTest12D.isSignificant());
					passedTests[0] = true;
				}
				else if(res.isaComparisonOf("data1","data3")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), resTest13D.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getEffectSize(), resTest13D.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getOrder() == resTest13D.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == resTest13D.isSignificant());
					passedTests[1] = true;
				}
				else if(res.isaComparisonOf("data1","data4")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), resTest14D.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getEffectSize(), resTest14D.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getOrder() == resTest14D.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == resTest14D.isSignificant());
					passedTests[2] = true;
				}
				else if(res.isaComparisonOf("data1","data5")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), resTest15D.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getEffectSize(), resTest15D.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getOrder() == resTest15D.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == resTest15D.isSignificant());
					passedTests[3] = true;
				}
				else if(res.isaComparisonOf("data2","data3")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), resTest23D.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getEffectSize(), resTest23D.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getOrder() == resTest23D.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == resTest23D.isSignificant());
					passedTests[4] = true;
				}
				else if(res.isaComparisonOf("data2","data4")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), resTest24D.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getEffectSize(), resTest24D.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getOrder() == resTest24D.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == resTest24D.isSignificant());
					passedTests[5] = true;
				}
				else if(res.isaComparisonOf("data2","data5")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), resTest25D.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getEffectSize(), resTest25D.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getOrder() == resTest25D.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == resTest25D.isSignificant());
					passedTests[6] = true;
				}
				else if(res.isaComparisonOf("data3","data4")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), resTest34D.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getEffectSize(), resTest34D.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getOrder() == resTest34D.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == resTest34D.isSignificant());
					passedTests[7] = true;
				}
				else if(res.isaComparisonOf("data3","data5")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), resTest35D.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getEffectSize(), resTest35D.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getOrder() == resTest35D.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == resTest35D.isSignificant());
					passedTests[8] = true;
				}
				else if(res.isaComparisonOf("data4","data5")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), resTest45D.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getEffectSize(), resTest45D.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getOrder() == resTest45D.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == resTest45D.isSignificant());
					passedTests[9] = true;
				}
					
			}
			
			//main generator
			
			resIter = results2D.listIterator();
			while(resIter.hasNext()){
				res = resIter.next();
				if(res.isaComparisonOf("data1","data4")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), resTest2_14D.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getEffectSize(), resTest2_14D.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getOrder() == resTest2_14D.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == resTest2_14D.isSignificant());
					passedTests2[0] = true;
				}
				else if(res.isaComparisonOf("data1","data2")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), resTest2_12D.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getEffectSize(), resTest2_12D.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getOrder() == resTest2_12D.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == resTest2_12D.isSignificant());
					passedTests2[1] = true;
				}
				else if(res.isaComparisonOf("data1","data3")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), resTest2_13D.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getEffectSize(), resTest2_13D.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getOrder() == resTest2_13D.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == resTest2_13D.isSignificant());
					passedTests2[2] = true;
				}
				else if(res.isaComparisonOf("data1","data5")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), resTest2_15D.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getEffectSize(), resTest2_15D.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getOrder() == resTest2_15D.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == resTest2_15D.isSignificant());
					passedTests2[3] = true;
				}
					
			}
		
			//check all have passed
			assertTrue("all tests should have passed", !arrayContains(passedTests,false));
			
			//check all have passed for the main technique test
			assertTrue("all tests should have passed", !arrayContains(passedTests2,false));


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

	/**Test 3 = Bon Ferroni.
	 */
	@Test
	public void MultipleGeneratorsBonferroni(){
			//output
			try {
				Report.setupLaTeXLoggers("reports/layer2/report2.tex");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//////////////the actual tests///////////////////////
			
			//get results from Layer2
			TimeseriesGenerator gen1 = new ExampleTimeseriesGenerator(0, 5,100);
			TimeseriesGenerator gen2 = new ExampleTimeseriesGenerator(0.2, 5,100);
			TimeseriesGenerator gen3 = new ExampleTimeseriesGenerator(0.4, 5,100);
			TimeseriesGenerator gen4 = new ExampleTimeseriesGenerator(0.6, 5,100);
			TimeseriesGenerator gen5 = new ExampleTimeseriesGenerator(0.8, 5,100);
			
			DatapointGenerator genD1 = new ExampleDatapointGenerator(0, 5);
			DatapointGenerator genD2 = new ExampleDatapointGenerator(0.2, 5);
			DatapointGenerator genD3 = new ExampleDatapointGenerator(0.4, 5);
			DatapointGenerator genD4 = new ExampleDatapointGenerator(0.6, 5);
			DatapointGenerator genD5 = new ExampleDatapointGenerator(0.8, 5);
			
			List<TimeseriesGenerator> gens = new ArrayList<TimeseriesGenerator>();
			gens.add(gen1);
			gens.add(gen2);
			gens.add(gen3);
			gens.add(gen4);
			gens.add(gen5);

			List<DatapointGenerator> gensD = new ArrayList<DatapointGenerator>();
			gensD.add(genD1);
			gensD.add(genD2);
			gensD.add(genD3);
			gensD.add(genD4);
			gensD.add(genD5);

			List<ResultSet> results = Layer2.run(new TimeseriesGeneratorSet(gens, new BonFerroni()), 0.05, false, false, new NoIncrementing(30), new MersenneTwister(0));
			List<ResultSet> results2 = Layer2.run(new TimeseriesGeneratorSet(gens,gen1, new BonFerroni()), 0.05, false, false, new NoIncrementing(30), new MersenneTwister(1));

			List<ResultSet> resultsD = Layer2.run(new DatapointGeneratorSet(gensD, new BonFerroni()), 0.05, false, false, new NoIncrementing(30), new MersenneTwister(2));
			List<ResultSet> results2D = Layer2.run(new DatapointGeneratorSet(gensD,genD1, new BonFerroni()), 0.05, false, false, new NoIncrementing(30), new MersenneTwister(3));

			//////////////////separately create the expected results to compare against/////////////////////////////////////
			
			/////////////////Time series///////////////////////////////////////
			List<Double> artificialResults1= new ArrayList<Double>();
			List<Double> artificialResults2= new ArrayList<Double>();
			List<Double> artificialResults3= new ArrayList<Double>();
			List<Double> artificialResults4= new ArrayList<Double>();
			List<Double> artificialResults5= new ArrayList<Double>();

			MersenneTwister ranTest = new MersenneTwister(0);
			long [] seeds = new long [ 30 ];
			long [] seeds1 = new long [ 30 ];
			long [] seeds2 = new long [ 30 ];
			long [] seeds3 = new long [ 30 ];
			long [] seeds4 = new long [ 30 ];

			for( int i=0; i<30; ++i ){//create seeds
				seeds[ i ] = ranTest.nextLong();
				seeds1[ i ] = ranTest.nextLong();
				seeds2[ i ] = ranTest.nextLong();
				seeds3[ i ] = ranTest.nextLong();
				seeds4[ i ] = ranTest.nextLong();
			}
			
			for(int i =0; i < 30; i++){//create data
				artificialResults1.add(artificiallyCreateTimeseries(new MersenneTwister(seeds[i]), 0, 5, 100));
				artificialResults2.add(artificiallyCreateTimeseries(new MersenneTwister(seeds1[i]), 0.2, 5, 100));
				artificialResults3.add(artificiallyCreateTimeseries(new MersenneTwister(seeds2[i]), 0.4, 5, 100));
				artificialResults4.add(artificiallyCreateTimeseries(new MersenneTwister(seeds3[i]), 0.6, 5, 100));
				artificialResults5.add(artificiallyCreateTimeseries(new MersenneTwister(seeds4[i]), 0.8, 5, 100));

			}

			Result resTest12 = Layer1.compare(artificialResults1, artificialResults2, 0.05, false, null);			
			Result resTest13 = Layer1.compare(artificialResults1, artificialResults3, 0.05, false, null);
			Result resTest14 = Layer1.compare(artificialResults1, artificialResults4, 0.05, false, null);
			Result resTest15 = Layer1.compare(artificialResults1, artificialResults5, 0.05, false, null);
			Result resTest23 = Layer1.compare(artificialResults2, artificialResults3, 0.05, false, null);
			Result resTest24 = Layer1.compare(artificialResults2, artificialResults4, 0.05, false, null);
			Result resTest25 = Layer1.compare(artificialResults2, artificialResults5, 0.05, false, null);
			Result resTest34 = Layer1.compare(artificialResults3, artificialResults4, 0.05, false, null);
			Result resTest35 = Layer1.compare(artificialResults3, artificialResults5, 0.05, false, null);
			Result resTest45 = Layer1.compare(artificialResults4, artificialResults5, 0.05, false, null);
			
			//time series 2 - all comparisons
			artificialResults1.clear();
			artificialResults2.clear();
			artificialResults3.clear();
			artificialResults4.clear();
			artificialResults5.clear();

			ranTest = new MersenneTwister(1);
			seeds = new long [ 30 ];
			seeds1 = new long [ 30 ];
			seeds2 = new long [ 30 ];
			seeds3 = new long [ 30 ];
			seeds4 = new long [ 30 ];

			for( int i=0; i<30; ++i ){//create seeds
				seeds[ i ] = ranTest.nextLong();
				seeds1[ i ] = ranTest.nextLong();
				seeds2[ i ] = ranTest.nextLong();
				seeds3[ i ] = ranTest.nextLong();
				seeds4[ i ] = ranTest.nextLong();
			}
			
			for(int i =0; i < 30; i++){//create data
				artificialResults1.add(artificiallyCreateTimeseries(new MersenneTwister(seeds[i]), 0, 5, 100));
				artificialResults2.add(artificiallyCreateTimeseries(new MersenneTwister(seeds1[i]), 0.2, 5, 100));
				artificialResults3.add(artificiallyCreateTimeseries(new MersenneTwister(seeds2[i]), 0.4, 5, 100));
				artificialResults4.add(artificiallyCreateTimeseries(new MersenneTwister(seeds3[i]), 0.6, 5, 100));
				artificialResults5.add(artificiallyCreateTimeseries(new MersenneTwister(seeds4[i]), 0.8, 5, 100));

			}

			Result resTest12_2 = Layer1.compare(artificialResults1, artificialResults2, 0.05, false, null);			
			Result resTest13_2 = Layer1.compare(artificialResults1, artificialResults3, 0.05, false, null);
			Result resTest14_2 = Layer1.compare(artificialResults1, artificialResults4, 0.05, false, null);
			Result resTest15_2 = Layer1.compare(artificialResults1, artificialResults5, 0.05, false, null);
			
			/////////////////Data point///////////////////////////////////////
			List<Pair<Double,Boolean>> artificialResults1D = new ArrayList<Pair<Double,Boolean>>();
			List<Pair<Double,Boolean>> artificialResults2D = new ArrayList<Pair<Double,Boolean>>();
			List<Pair<Double,Boolean>> artificialResults3D = new ArrayList<Pair<Double,Boolean>>();
			List<Pair<Double,Boolean>> artificialResults4D = new ArrayList<Pair<Double,Boolean>>();
			List<Pair<Double,Boolean>> artificialResults5D = new ArrayList<Pair<Double,Boolean>>();

			ranTest = new MersenneTwister(2);
			seeds = new long [ 30 ];
			seeds1 = new long [ 30 ];
			seeds2 = new long [ 30 ];
			seeds3 = new long [ 30 ];
			seeds4 = new long [ 30 ];

			for( int i=0; i<30; ++i ){//create seeds
				seeds[ i ] = ranTest.nextLong();
				seeds1[ i ] = ranTest.nextLong();
				seeds2[ i ] = ranTest.nextLong();
				seeds3[ i ] = ranTest.nextLong();
				seeds4[ i ] = ranTest.nextLong();
			}
			
			for(int i =0; i < 30; i++){//create data
				artificialResults1D.add(artificiallyCreateDatapoint(new MersenneTwister(seeds[i]), 0, 5));
				artificialResults2D.add(artificiallyCreateDatapoint(new MersenneTwister(seeds1[i]), 0.2, 5));
				artificialResults3D.add(artificiallyCreateDatapoint(new MersenneTwister(seeds2[i]), 0.4, 5));
				artificialResults4D.add(artificiallyCreateDatapoint(new MersenneTwister(seeds3[i]), 0.6, 5));
				artificialResults5D.add(artificiallyCreateDatapoint(new MersenneTwister(seeds4[i]), 0.8, 5));

			}

			Result resTest12D = Layer1.comparePair(artificialResults1D, artificialResults2D, 0.05, false, false, false, "dataA", "dataB", null, null);			
			Result resTest13D = Layer1.comparePair(artificialResults1D, artificialResults3D, 0.05, false, false, false, "dataA", "dataB", null, null);
			Result resTest14D = Layer1.comparePair(artificialResults1D, artificialResults4D, 0.05, false, false, false, "dataA", "dataB", null, null);
			Result resTest15D = Layer1.comparePair(artificialResults1D, artificialResults5D, 0.05, false, false, false, "dataA", "dataB", null, null);
			Result resTest23D = Layer1.comparePair(artificialResults2D, artificialResults3D, 0.05, false, false, false, "dataA", "dataB", null, null);
			Result resTest24D = Layer1.comparePair(artificialResults2D, artificialResults4D, 0.05, false, false, false, "dataA", "dataB", null, null);
			Result resTest25D = Layer1.comparePair(artificialResults2D, artificialResults5D, 0.05, false, false, false, "dataA", "dataB", null, null);
			Result resTest34D = Layer1.comparePair(artificialResults3D, artificialResults4D, 0.05, false, false, false, "dataA", "dataB", null, null);
			Result resTest35D = Layer1.comparePair(artificialResults3D, artificialResults5D, 0.05, false, false, false, "dataA", "dataB", null, null);
			Result resTest45D = Layer1.comparePair(artificialResults4D, artificialResults5D, 0.05, false, false, false, "dataA", "dataB", null, null);
			
			
						
			artificialResults1D.clear();
			artificialResults2D.clear();
			artificialResults3D.clear();
			artificialResults4D.clear();
			artificialResults5D.clear();

			ranTest = new MersenneTwister(3);
			seeds = new long [ 30 ];
			seeds1 = new long [ 30 ];
			seeds2 = new long [ 30 ];
			seeds3 = new long [ 30 ];
			seeds4 = new long [ 30 ];

			for( int i=0; i<30; ++i ){//create seeds
				seeds[ i ] = ranTest.nextLong();
				seeds1[ i ] = ranTest.nextLong();
				seeds2[ i ] = ranTest.nextLong();
				seeds3[ i ] = ranTest.nextLong();
				seeds4[ i ] = ranTest.nextLong();
			}
			
			for(int i =0; i < 30; i++){//create data
				artificialResults1D.add(artificiallyCreateDatapoint(new MersenneTwister(seeds[i]), 0, 5));
				artificialResults2D.add(artificiallyCreateDatapoint(new MersenneTwister(seeds1[i]), 0.2, 5));
				artificialResults3D.add(artificiallyCreateDatapoint(new MersenneTwister(seeds2[i]), 0.4, 5));
				artificialResults4D.add(artificiallyCreateDatapoint(new MersenneTwister(seeds3[i]), 0.6, 5));
				artificialResults5D.add(artificiallyCreateDatapoint(new MersenneTwister(seeds4[i]), 0.8, 5));
			}

			Result resTest12D_2 = Layer1.comparePair(artificialResults1D, artificialResults2D, 0.05, false, false, false, "dataA", "dataB", null, null);		
			Result resTest13D_2 = Layer1.comparePair(artificialResults1D, artificialResults3D, 0.05, false, false, false, "dataA", "dataB", null, null);
			Result resTest14D_2 = Layer1.comparePair(artificialResults1D, artificialResults4D, 0.05, false, false, false, "dataA", "dataB", null, null);
			Result resTest15D_2 = Layer1.comparePair(artificialResults1D, artificialResults5D, 0.05, false, false, false, "dataA", "dataB", null, null);

			
			//The tests
			
			
			//Time series///////////////////////////////////////////
			boolean[] passedTests = new boolean[10];
			boolean[] passedTests2 = new boolean[4];
			
			Arrays.fill(passedTests, false);
			Arrays.fill(passedTests2, false);


			ListIterator<ResultSet> resIter = results.listIterator();
			ResultSet res;
			
			assertTrue("size of results array should be 10 (one for each pair)",results.size() == 10);
			assertTrue("size of results array should be 10 (one for each pair)",results2.size() == 4);
			
			
			while(resIter.hasNext()){
				res = resIter.next();
				if(res.isaComparisonOf("data1","data2")){
					double expectedPVal = resTest12.getPValue() * 10;
					boolean expectedSig = expectedPVal <= 0.05;
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), expectedPVal, 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == expectedSig);
					passedTests[0] = true;
				}
				else if(res.isaComparisonOf("data1","data3")){
					double expectedPVal = resTest13.getPValue() * 10;
					boolean expectedSig = expectedPVal <= 0.05;
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), expectedPVal, 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == expectedSig);
					passedTests[1] = true;
				}
				else if(res.isaComparisonOf("data1","data4")){
					double expectedPVal = resTest14.getPValue() * 10;
					boolean expectedSig = expectedPVal <= 0.05;
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), expectedPVal, 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == expectedSig);
					passedTests[2] = true;
				}
				else if(res.isaComparisonOf("data1","data5")){
					double expectedPVal = resTest15.getPValue() * 10;
					boolean expectedSig = expectedPVal <= 0.05;
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), expectedPVal, 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == expectedSig);
					passedTests[3] = true;
				}		
				else if(res.isaComparisonOf("data2","data3")){
					double expectedPVal = resTest23.getPValue() * 10;
					boolean expectedSig = expectedPVal <= 0.05;
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), expectedPVal, 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == expectedSig);
					passedTests[4] = true;
				}
				else if(res.isaComparisonOf("data2","data4")){
					double expectedPVal = resTest24.getPValue() * 10;
					boolean expectedSig = expectedPVal <= 0.05;
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), expectedPVal, 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == expectedSig);
					passedTests[5] = true;
				}
				else if(res.isaComparisonOf("data2","data5")){
					double expectedPVal = resTest25.getPValue() * 10;
					boolean expectedSig = expectedPVal <= 0.05;
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), expectedPVal, 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == expectedSig);
					passedTests[6] = true;
				}
				else if(res.isaComparisonOf("data3","data4")){
					double expectedPVal = resTest34.getPValue() * 10;
					boolean expectedSig = expectedPVal <= 0.05;
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), expectedPVal, 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == expectedSig);
					passedTests[7] = true;
				}
				else if(res.isaComparisonOf("data3","data5")){
					double expectedPVal = resTest35.getPValue() * 10;
					boolean expectedSig = expectedPVal <= 0.05;
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), expectedPVal, 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == expectedSig);
					passedTests[8] = true;
				}
				else if(res.isaComparisonOf("data4","data5")){
					double expectedPVal = resTest45.getPValue() * 10;
					boolean expectedSig = expectedPVal <= 0.05;
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), expectedPVal, 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == expectedSig);
					passedTests[9] = true;
				}
					
			}
			
			resIter = results2.listIterator();
			while(resIter.hasNext()){
				res = resIter.next();
				if(res.isaComparisonOf("data1","data2")){
					double expectedPVal = resTest12_2.getPValue() * 4;
					boolean expectedSig = expectedPVal <= 0.05;
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), expectedPVal, 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == expectedSig);
					passedTests2[0] = true;
				}
				else if(res.isaComparisonOf("data1","data3")){
					double expectedPVal = resTest13_2.getPValue() * 4;
					boolean expectedSig = expectedPVal <= 0.05;
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), expectedPVal, 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == expectedSig);
					passedTests2[1] = true;
				}
				else if(res.isaComparisonOf("data1","data4")){
					double expectedPVal = resTest14_2.getPValue() * 4;
					boolean expectedSig = expectedPVal <= 0.05;
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), expectedPVal, 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == expectedSig);
					passedTests2[2] = true;
				}
				else if(res.isaComparisonOf("data1","data5")){
					double expectedPVal = resTest15_2.getPValue() * 4;
					boolean expectedSig = expectedPVal <= 0.05;
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), expectedPVal, 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == expectedSig);
					passedTests2[3] = true;
				}
					
			}
			
			//check all have passed for Bonferroni
			assertTrue("all tests should have passed", !arrayContains(passedTests,false));

			//check all have passed for main test and Bonferroni
			assertTrue("all tests should have passed", !arrayContains(passedTests2,false));


			//Data point///////////////////////////////////////////			
			Arrays.fill(passedTests, false);
			Arrays.fill(passedTests2, false);


			resIter = resultsD.listIterator();
			
			assertTrue("size of results array should be 10 (one for each pair)",resultsD.size() == 10);
			assertTrue("size of results array should be 10 (one for each pair)",results2D.size() == 4);
			
			
			while(resIter.hasNext()){
				res = resIter.next();
				if(res.isaComparisonOf("data1","data2")){
					double expectedPVal = resTest12D.getPValue() * 10;
					boolean expectedSig = expectedPVal <= 0.05;
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), expectedPVal, 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == expectedSig);
					passedTests[0] = true;
				}
				else if(res.isaComparisonOf("data1","data3")){
					double expectedPVal = resTest13D.getPValue() * 10;
					boolean expectedSig = expectedPVal <= 0.05;
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), expectedPVal, 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == expectedSig);
					passedTests[1] = true;
				}
				else if(res.isaComparisonOf("data1","data4")){
					double expectedPVal = resTest14D.getPValue() * 10;
					boolean expectedSig = expectedPVal <= 0.05;
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), expectedPVal, 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == expectedSig);
					passedTests[2] = true;
				}
				else if(res.isaComparisonOf("data1","data5")){
					double expectedPVal = resTest15D.getPValue() * 10;
					boolean expectedSig = expectedPVal <= 0.05;
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), expectedPVal, 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == expectedSig);
					passedTests[3] = true;
				}		
				else if(res.isaComparisonOf("data2","data3")){
					double expectedPVal = resTest23D.getPValue() * 10;
					boolean expectedSig = expectedPVal <= 0.05;
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), expectedPVal, 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == expectedSig);
					passedTests[4] = true;
				}
				else if(res.isaComparisonOf("data2","data4")){
					double expectedPVal = resTest24D.getPValue() * 10;
					boolean expectedSig = expectedPVal <= 0.05;
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), expectedPVal, 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == expectedSig);
					passedTests[5] = true;
				}
				else if(res.isaComparisonOf("data2","data5")){
					double expectedPVal = resTest25D.getPValue() * 10;
					boolean expectedSig = expectedPVal <= 0.05;
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), expectedPVal, 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == expectedSig);
					passedTests[6] = true;
				}
				else if(res.isaComparisonOf("data3","data4")){
					double expectedPVal = resTest34D.getPValue() * 10;
					boolean expectedSig = expectedPVal <= 0.05;
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), expectedPVal, 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == expectedSig);
					passedTests[7] = true;
				}
				else if(res.isaComparisonOf("data3","data5")){
					double expectedPVal = resTest35D.getPValue() * 10;
					boolean expectedSig = expectedPVal <= 0.05;
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), expectedPVal, 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == expectedSig);
					passedTests[8] = true;
				}
				else if(res.isaComparisonOf("data4","data5")){
					double expectedPVal = resTest45D.getPValue() * 10;
					boolean expectedSig = expectedPVal <= 0.05;
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), expectedPVal, 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == expectedSig);
					passedTests[9] = true;
				}
					
			}
			
			resIter = results2D.listIterator();
			while(resIter.hasNext()){
				res = resIter.next();
				if(res.isaComparisonOf("data1","data2")){
					double expectedPVal = resTest12D_2.getPValue() * 4;
					boolean expectedSig = expectedPVal <= 0.05;
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), expectedPVal, 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == expectedSig);
					passedTests2[0] = true;
				}
				else if(res.isaComparisonOf("data1","data3")){
					double expectedPVal = resTest13D_2.getPValue() * 4;
					boolean expectedSig = expectedPVal <= 0.05;
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), expectedPVal, 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == expectedSig);
					passedTests2[1] = true;
				}
				else if(res.isaComparisonOf("data1","data4")){
					double expectedPVal = resTest14D_2.getPValue() * 4;
					boolean expectedSig = expectedPVal <= 0.05;
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), expectedPVal, 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == expectedSig);
					passedTests2[2] = true;
				}
				else if(res.isaComparisonOf("data1","data5")){
					double expectedPVal = resTest15D_2.getPValue() * 4;
					boolean expectedSig = expectedPVal <= 0.05;
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().getPValue(), expectedPVal, 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getRes().isSignificant() == expectedSig);
					passedTests2[3] = true;
				}
					
			}
			
			//check all have passed for Bonferroni
			assertTrue("all tests should have passed", !arrayContains(passedTests,false));

			//check all have passed for main test and Bonferroni
			assertTrue("all tests should have passed", !arrayContains(passedTests2,false));

	}
	
	private static boolean arrayContains(boolean[] arr, boolean b) {
		for(int i =0; i < arr.length; i++){
			if(arr[i] == b)
				return true;
		}
		return false;
	}

	
	/**Test 4b = Run tests with the time series generator. With censoring and with no incrementing.	
	 * Use a complex censoring strategy this time, 
	 * in which an additional test using the time that time series's take to reach a passing grade
	 * is compared using a regular (non censored) p value test.
	 * 
	 */
	@Test
	public void MultipleGeneratorsAndArtefacts(){
		
	}
	



}

// End ///////////////////////////////////////////////////////////////

