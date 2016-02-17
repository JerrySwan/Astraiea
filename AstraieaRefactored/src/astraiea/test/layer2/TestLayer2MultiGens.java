package astraiea.test.layer2;
//TODO The description in each assert
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import org.apache.commons.math3.util.Pair;
import org.junit.Test;

import astraiea.Result;
import astraiea.layer1.Layer1;
import astraiea.layer2.BonFerroni;
import astraiea.layer2.Layer2;
import astraiea.layer2.experiments.SetOfComparisons;
import astraiea.layer2.experiments.SetOfExperiments;
import astraiea.layer2.experiments.SetOfMultiArtefactExperiments;
import astraiea.layer2.experiments.SingleArtefactExperiments;
import astraiea.layer2.generators.Generator;
import astraiea.layer2.generators.GeneratorOutput;
import astraiea.layer2.generators.PairGeneratorOutput;
import astraiea.layer2.generators.artefacts.ArtefactGenerator;
import astraiea.layer2.generators.artefacts.ArtefactTimeseriesGenerator;
import astraiea.layer2.generators.artefacts.MultipleArtefactOutput;
import astraiea.layer2.generators.examples.ExampleDatapointGenerator;
import astraiea.layer2.generators.examples.ExampleDatapointMultiArtefactGenerator;
import astraiea.layer2.generators.examples.ExampleTimeseriesGenerator;
import astraiea.layer2.generators.examples.ExampleTimeseriesMultiArtefactGenerator;
import astraiea.layer2.generators.timeseries.TimeseriesGeneratorOutput;
import astraiea.layer2.generators.timeseries.TimeseriesGenerator;
import astraiea.layer2.strategies.CensoringStrategy;
import astraiea.layer2.strategies.IncrementingStrategy;
import astraiea.layer2.strategies.NoIncrementing;
import astraiea.layer2.strategies.SimpleIncrementing;
import astraiea.outputformat.Report;
import astraiea.util.DataUtil;
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
		
		//////////////DATA POINT///////////////////////
						
			//make the multiple artefacts
			int numArtefacts = 30; //maximum number of artefacts
			int numRepeats = 100; //number of repeats of each artefact
			int duration = 100; //duration for when timeseries is used
			
			List<ArtefactGenerator<PairGeneratorOutput>> gens1D = new ArrayList<ArtefactGenerator<PairGeneratorOutput>>();
			List<ArtefactGenerator<PairGeneratorOutput>> gens2D = new ArrayList<ArtefactGenerator<PairGeneratorOutput>>();
			for(int i =0; i < numArtefacts; i++){
				//setup generators with a significant difference
				gens1D.add(new ExampleDatapointMultiArtefactGenerator(0.9, 1, numRepeats, i));
				gens2D.add(new ExampleDatapointMultiArtefactGenerator(0.1, 1, numRepeats, i));
			}
			
			//setup sets of experiments
			SetOfExperiments<MultipleArtefactOutput<PairGeneratorOutput>> gen1Exp = new SetOfMultiArtefactExperiments<PairGeneratorOutput>(gens1D, "test1");
			SetOfExperiments<MultipleArtefactOutput<PairGeneratorOutput>> gen2Exp = new SetOfMultiArtefactExperiments<PairGeneratorOutput>(gens2D, "test2");
			SetOfComparisons<MultipleArtefactOutput<PairGeneratorOutput>> gen1gen2Comp = new SetOfComparisons<MultipleArtefactOutput<PairGeneratorOutput>>(gen1Exp,gen2Exp);

			//run
			Result resD = Layer2.runArtefacts(gen1gen2Comp,0.05,new NoIncrementing(30),new MersenneTwister(0),false).get(0);

			//artificially create results to compare against
			MersenneTwister ran = new MersenneTwister(0);
			long[] seeds = new long[numArtefacts];
			for(int i =0; i < numArtefacts; i++)
				seeds[i] = ran.nextLong();
			
			double[] medians1 = new double[numArtefacts];
			double[] medians2 = new double[numArtefacts];

			for(int i =0; i < numArtefacts; i++){
				MersenneTwister random = new MersenneTwister(seeds[i]);

				//repeat each artefact in the same manner as in the automatically ran tests above
				//(same code as in ExampleDatapointMultiArtefactGenerator)
				double[] vals1 = new double[numRepeats];
				double[] vals2 = new double[numRepeats];

				for(int r = 0; r < numRepeats; r++){
					double val = random.nextGaussian();
					double ranVal = random.nextDouble();
					if(ranVal > 0.9) //bias for gen1
						vals1[r] = val + 1 + r;
					
					if(ranVal > 0.1)//bias for gen2
						vals2[r] = val + 1 + r;
				}
				medians1[i] = DataUtil.getMedian(vals1);

				medians2[i] = DataUtil.getMedian(vals2);

			}
			Result resTestD = Layer1.comparePaired(medians1, medians2, 0.05);
			
			///////////TIME SERIES//////////////////////////////
			
			List<ArtefactTimeseriesGenerator<PairGeneratorOutput>> gens1T = new ArrayList<ArtefactTimeseriesGenerator<PairGeneratorOutput>>();
			List<ArtefactTimeseriesGenerator<PairGeneratorOutput>> gens2T = new ArrayList<ArtefactTimeseriesGenerator<PairGeneratorOutput>>();
			for(int i =0; i < numArtefacts; i++){
				//setup generators with a significant difference
				gens1T.add(new ExampleTimeseriesMultiArtefactGenerator(0.9, 1, numRepeats, i, 100));
				gens2T.add(new ExampleTimeseriesMultiArtefactGenerator(0.1, 1, numRepeats, i, 100));
			}
			
			//setup sets of experiments
			SetOfExperiments<MultipleArtefactOutput<TimeseriesGeneratorOutput<PairGeneratorOutput>>> gen1ExpT = new SetOfMultiArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>>(gens1T, "test1");
			SetOfExperiments<MultipleArtefactOutput<TimeseriesGeneratorOutput<PairGeneratorOutput>>> gen2ExpT = new SetOfMultiArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>>(gens2T, "test2");
			SetOfComparisons<MultipleArtefactOutput<TimeseriesGeneratorOutput<PairGeneratorOutput>>> gen1gen2CompT = new SetOfComparisons<MultipleArtefactOutput<TimeseriesGeneratorOutput<PairGeneratorOutput>>>(gen1ExpT,gen2ExpT);

			//run
			Result resT = Layer2.runArtefacts(gen1gen2CompT,0.05,new NoIncrementing(30),new MersenneTwister(0),false).get(0);

			//artificially create results to compare against

			medians1 = new double[numArtefacts];
			medians2 = new double[numArtefacts];

			for(int i =0; i < numArtefacts; i++){
				MersenneTwister random = new MersenneTwister(seeds[i]);

				//repeat each artefact in the same manner as in the automatically ran tests above
				//(same code as in ExampleDatapointMultiArtefactGenerator)
				double[] vals1 = new double[numRepeats];
				double[] vals2 = new double[numRepeats];

				for(int r = 0; r < numRepeats; r++){
					
					double val1 = 0;
					double val2 = 0;
					for(int t =0; t < duration; t++){
						double val = random.nextGaussian();
						double ranVal = random.nextDouble();
						val *= (((double)t/(double)duration)  * 2) + i + r;
						
						val1 = val;
						val2 = val;
						if(ranVal > 0.9) //bias for gen1
							val1 ++;
						
						if(ranVal > 0.1)//bias for gen2
							val2 ++;
					}
					//val1 and val2 will be the result from the last generation
					vals1[r] = val1;
					vals2[r] = val2;

					//the output of the last generation
					
					
				}
				medians1[i] = DataUtil.getMedian(vals1);

				medians2[i] = DataUtil.getMedian(vals2);

			}
			Result resTestT = Layer1.comparePaired(medians1, medians2, 0.05);
			
			/////////////////////////////////////

			///compare results from layer 1 and the artificial data with those from layer2 
			//(compare p value, effect size, whether is significant and order using JUnit tests)
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", resTestD.getPValue(), resD.getPValue(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (effect size)", resTestD.getEffectSize(), resD.getEffectSize(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (order)", resTestD.getOrder(), resD.getOrder());
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (significance)", resTestD.isSignificant(), resD.isSignificant());
			
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", resTestT.getPValue(), resT.getPValue(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (effect size)", resTestT.getEffectSize(), resT.getEffectSize(), 0);
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (order)", resTestT.getOrder(), resT.getOrder());
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (significance)", resTestT.isSignificant(), resT.isSignificant());
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
				Report.setupLaTeXLoggers("reports/layer2/multiple/report2.tex");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//////////////the actual tests///////////////////////

			
			//get results from Layer2
			TimeseriesGenerator<PairGeneratorOutput> gen1 = new ExampleTimeseriesGenerator(0, 5,100);
			TimeseriesGenerator<PairGeneratorOutput> gen2 = new ExampleTimeseriesGenerator(0.2, 5,100);
			TimeseriesGenerator<PairGeneratorOutput> gen3 = new ExampleTimeseriesGenerator(0.4, 5,100);
			TimeseriesGenerator<PairGeneratorOutput> gen4 = new ExampleTimeseriesGenerator(0.6, 5,100);
			TimeseriesGenerator<PairGeneratorOutput> gen5 = new ExampleTimeseriesGenerator(0.8, 5,100);
			
			Generator<PairGeneratorOutput> genD1 = new ExampleDatapointGenerator(0, 5);
			Generator<PairGeneratorOutput> genD2 = new ExampleDatapointGenerator(0.2, 5);
			Generator<PairGeneratorOutput> genD3 = new ExampleDatapointGenerator(0.4, 5);
			Generator<PairGeneratorOutput> genD4 = new ExampleDatapointGenerator(0.6, 5);
			Generator<PairGeneratorOutput> genD5 = new ExampleDatapointGenerator(0.8, 5);
			
			//set up sets of experiments
			List<TimeseriesGenerator<PairGeneratorOutput>> gens = new ArrayList<TimeseriesGenerator<PairGeneratorOutput>>();
			gens.add(gen1);
			gens.add(gen2);
			gens.add(gen3);
			gens.add(gen4);
			gens.add(gen5);
			SetOfExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>> gen1ExpT = new SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>>(gen1, "data1");
			SetOfExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>> gen2ExpT = new SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>>(gen2, "data2");
			SetOfExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>> gen3ExpT = new SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>>(gen3, "data3");
			SetOfExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>> gen4ExpT = new SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>>(gen4, "data4");
			SetOfExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>> gen5ExpT = new SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>>(gen5, "data5");

			List<SetOfExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>>> setsOfExperimentsT = new ArrayList<SetOfExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>>>();
			setsOfExperimentsT.add(gen1ExpT);
			setsOfExperimentsT.add(gen2ExpT);
			setsOfExperimentsT.add(gen3ExpT);
			setsOfExperimentsT.add(gen4ExpT);
			setsOfExperimentsT.add(gen5ExpT);
			
			SetOfComparisons<TimeseriesGeneratorOutput<PairGeneratorOutput>> gensCompT1 = new SetOfComparisons<TimeseriesGeneratorOutput<PairGeneratorOutput>>(setsOfExperimentsT);
			SetOfComparisons<TimeseriesGeneratorOutput<PairGeneratorOutput>> gensCompT2 = new SetOfComparisons<TimeseriesGeneratorOutput<PairGeneratorOutput>>(setsOfExperimentsT,gen1ExpT);
			
			List<Generator<PairGeneratorOutput>> gensD = new ArrayList<Generator<PairGeneratorOutput>>();
			gensD.add(genD1);
			gensD.add(genD2);
			gensD.add(genD3);
			gensD.add(genD4);
			gensD.add(genD5);
			SetOfExperiments<PairGeneratorOutput> gen1Exp = new SingleArtefactExperiments<PairGeneratorOutput>(genD1, "data1");
			SetOfExperiments<PairGeneratorOutput> gen2Exp = new SingleArtefactExperiments<PairGeneratorOutput>(genD2, "data2");
			SetOfExperiments<PairGeneratorOutput> gen3Exp = new SingleArtefactExperiments<PairGeneratorOutput>(genD3, "data3");
			SetOfExperiments<PairGeneratorOutput> gen4Exp = new SingleArtefactExperiments<PairGeneratorOutput>(genD4, "data4");
			SetOfExperiments<PairGeneratorOutput> gen5Exp = new SingleArtefactExperiments<PairGeneratorOutput>(genD5, "data5");
			
			List<SetOfExperiments<PairGeneratorOutput>> setsOfExperimentsD = new ArrayList<SetOfExperiments<PairGeneratorOutput>>();
			setsOfExperimentsD.add(gen1Exp);
			setsOfExperimentsD.add(gen2Exp);
			setsOfExperimentsD.add(gen3Exp);
			setsOfExperimentsD.add(gen4Exp);
			setsOfExperimentsD.add(gen5Exp);
			
			SetOfComparisons<PairGeneratorOutput> gensCompD1 = new SetOfComparisons<PairGeneratorOutput>(setsOfExperimentsD);
			SetOfComparisons<PairGeneratorOutput> gensCompD2 = new SetOfComparisons<PairGeneratorOutput>(setsOfExperimentsD,gen1Exp);

			//the layer2 tests
			
			//TIMESERIES
			//comparing each with each
			List<Result> results = Layer2.run(gensCompT1, 0.05, false, false, new NoIncrementing(30), new MersenneTwister(0));
			//comparing everything with one (gen1)
			List<Result> results2 = Layer2.run(gensCompT2, 0.05, false, false, new NoIncrementing(30), new MersenneTwister(1));//with main generator
			
			//DATAPOINT
			List<Result> resultsD = Layer2.run(gensCompD1, 0.05, false, false, new NoIncrementing(30), new MersenneTwister(2));
			List<Result> results2D = Layer2.run(gensCompD2, 0.05, false, false, new NoIncrementing(30), new MersenneTwister(3));//with main generator

			
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
			
 			Result resTest12D = Layer1.comparePair(artificialResults1D, artificialResults2D, 0.05, false, false, false, "data1", "data2", null, null);
 			Result resTest13D = Layer1.comparePair(artificialResults1D, artificialResults3D, 0.05, false, false, false, "data1", "data3", null, null);
			Result resTest14D = Layer1.comparePair(artificialResults1D, artificialResults4D, 0.05, false, false, false, "data1", "data4", null, null);
			Result resTest15D = Layer1.comparePair(artificialResults1D, artificialResults5D, 0.05, false, false, false, "data1", "data5", null, null);
			Result resTest23D = Layer1.comparePair(artificialResults2D, artificialResults3D, 0.05, false, false, false, "data2", "data3", null, null);
			Result resTest24D = Layer1.comparePair(artificialResults2D, artificialResults4D, 0.05, false, false, false, "data2", "data4", null, null);
			Result resTest25D = Layer1.comparePair(artificialResults2D, artificialResults5D, 0.05, false, false, false, "data2", "data5", null, null);
			Result resTest34D = Layer1.comparePair(artificialResults3D, artificialResults4D, 0.05, false, false, false, "data3", "data4", null, null);
			Result resTest35D = Layer1.comparePair(artificialResults3D, artificialResults5D, 0.05, false, false, false, "data3", "data5", null, null);
			Result resTest45D = Layer1.comparePair(artificialResults4D, artificialResults5D, 0.05, false, false, false, "data4", "data5", null, null);

			
				
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
			
			Result resTest2_12D = Layer1.comparePair(artificialResults1D, artificialResults2D, 0.05, false, false, false, "data1", "data2", null, null);
			Result resTest2_13D = Layer1.comparePair(artificialResults1D, artificialResults3D, 0.05, false, false, false, "data1", "data3", null, null);
			Result resTest2_14D = Layer1.comparePair(artificialResults1D, artificialResults4D, 0.05, false, false, false, "data1", "data4", null, null);
			Result resTest2_15D = Layer1.comparePair(artificialResults1D, artificialResults5D, 0.05, false, false, false, "data1", "data5", null, null);
			
			boolean[] passedTests = new boolean[10];
			boolean[] passedTests2 = new boolean[4];

			Arrays.fill(passedTests, false);
			Arrays.fill(passedTests2, false);

			
			//JUnit Tests with time series////////////////////////////////////////////////////////////////////////
			
			//check that correct number of comparisons were carried
			assertTrue("size of results array should be 10 (one for each pair)",results.size() == 10);
			assertTrue("size of results array should be 4 (one for each pair all compared against one)",results2.size() == 4);
			ListIterator<Result> resIter = results.listIterator();
			Result res;

			//check each comparison is correct
			
			//all vs all
			while(resIter.hasNext()){
				res = resIter.next();
				if(res.isaComparisonOf("data1","data2")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), resTest12.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getEffectSize(), resTest12.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getOrder() == resTest12.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == resTest12.isSignificant());
					passedTests[0] = true;
				}
				else if(res.isaComparisonOf("data1","data3")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), resTest13.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getEffectSize(), resTest13.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getOrder() == resTest13.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == resTest13.isSignificant());
					passedTests[1] = true;
				}
				else if(res.isaComparisonOf("data1","data4")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), resTest14.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getEffectSize(), resTest14.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getOrder() == resTest14.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == resTest14.isSignificant());
					passedTests[2] = true;
				}
				else if(res.isaComparisonOf("data1","data5")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), resTest15.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getEffectSize(), resTest15.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getOrder() == resTest15.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == resTest15.isSignificant());
					passedTests[3] = true;
				}
				else if(res.isaComparisonOf("data2","data3")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), resTest23.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getEffectSize(), resTest23.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getOrder() == resTest23.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == resTest23.isSignificant());
					passedTests[4] = true;
				}
				else if(res.isaComparisonOf("data2","data4")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), resTest24.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getEffectSize(), resTest24.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getOrder() == resTest24.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == resTest24.isSignificant());
					passedTests[5] = true;
				}
				else if(res.isaComparisonOf("data2","data5")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), resTest25.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getEffectSize(), resTest25.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getOrder() == resTest25.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == resTest25.isSignificant());
					passedTests[6] = true;
				}
				else if(res.isaComparisonOf("data3","data4")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), resTest34.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getEffectSize(), resTest34.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getOrder() == resTest34.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == resTest34.isSignificant());
					passedTests[7] = true;
				}
				else if(res.isaComparisonOf("data3","data5")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), resTest35.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getEffectSize(), resTest35.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getOrder() == resTest35.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == resTest35.isSignificant());
					passedTests[8] = true;
				}
				else if(res.isaComparisonOf("data4","data5")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), resTest45.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getEffectSize(), resTest45.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getOrder() == resTest45.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == resTest45.isSignificant());
					passedTests[9] = true;
				}
					
			}
			
			//all vs main
			resIter = results2.listIterator();
			while(resIter.hasNext()){
				res = resIter.next();
				if(res.isaComparisonOf("data1","data4")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), resTest2_14.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getEffectSize(), resTest2_14.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getOrder() == resTest2_14.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == resTest2_14.isSignificant());
					passedTests2[0] = true;
				}
				else if(res.isaComparisonOf("data1","data2")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), resTest2_12.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getEffectSize(), resTest2_12.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getOrder() == resTest2_12.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == resTest2_12.isSignificant());
					passedTests2[1] = true;
				}
				else if(res.isaComparisonOf("data1","data3")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), resTest2_13.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getEffectSize(), resTest2_13.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getOrder() == resTest2_13.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == resTest2_13.isSignificant());
					passedTests2[2] = true;
				}
				else if(res.isaComparisonOf("data1","data5")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), resTest2_15.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getEffectSize(), resTest2_15.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getOrder() == resTest2_15.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == resTest2_15.isSignificant());
					passedTests2[3] = true;
				}
					
			}
		
			//check all have passed for all vs all
			assertTrue("all tests should have passed", !arrayContains(passedTests,false));
			
			//check all have passed for all vs main
			assertTrue("all tests should have passed", !arrayContains(passedTests2,false));
			
			//Tests with data point////////////////////////////////////////////////////////////////////////
			assertTrue("size of results array should be 10 (one for each pair)",resultsD.size() == 10);
			assertTrue("size of results array should be 4 (all compared against one)",results2D.size() == 4);
			
			Arrays.fill(passedTests, false);
			Arrays.fill(passedTests2, false);

			
			resIter = resultsD.listIterator();
			while(resIter.hasNext()){
				res = resIter.next();
				if(res.isaComparisonOf("data1","data2")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), resTest12D.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getEffectSize(), resTest12D.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getOrder() == resTest12D.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == resTest12D.isSignificant());
					passedTests[0] = true;
				}
				else if(res.isaComparisonOf("data1","data3")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), resTest13D.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getEffectSize(), resTest13D.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getOrder() == resTest13D.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == resTest13D.isSignificant());
					passedTests[1] = true;
				}
				else if(res.isaComparisonOf("data1","data4")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), resTest14D.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getEffectSize(), resTest14D.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getOrder() == resTest14D.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == resTest14D.isSignificant());
					passedTests[2] = true;
				}
				else if(res.isaComparisonOf("data1","data5")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), resTest15D.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getEffectSize(), resTest15D.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getOrder() == resTest15D.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == resTest15D.isSignificant());
					passedTests[3] = true;
				}
				else if(res.isaComparisonOf("data2","data3")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), resTest23D.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getEffectSize(), resTest23D.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getOrder() == resTest23D.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == resTest23D.isSignificant());
					passedTests[4] = true;
				}
				else if(res.isaComparisonOf("data2","data4")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), resTest24D.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getEffectSize(), resTest24D.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getOrder() == resTest24D.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == resTest24D.isSignificant());
					passedTests[5] = true;
				}
				else if(res.isaComparisonOf("data2","data5")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), resTest25D.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getEffectSize(), resTest25D.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getOrder() == resTest25D.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == resTest25D.isSignificant());
					passedTests[6] = true;
				}
				else if(res.isaComparisonOf("data3","data4")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), resTest34D.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getEffectSize(), resTest34D.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getOrder() == resTest34D.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == resTest34D.isSignificant());
					passedTests[7] = true;
				}
				else if(res.isaComparisonOf("data3","data5")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), resTest35D.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getEffectSize(), resTest35D.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getOrder() == resTest35D.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == resTest35D.isSignificant());
					passedTests[8] = true;
				}
				else if(res.isaComparisonOf("data4","data5")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), resTest45D.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getEffectSize(), resTest45D.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getOrder() == resTest45D.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == resTest45D.isSignificant());
					passedTests[9] = true;
				}
					
			}
			
			resIter = results2D.listIterator();
			while(resIter.hasNext()){
				res = resIter.next();
				if(res.isaComparisonOf("data1","data4")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), resTest2_14D.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getEffectSize(), resTest2_14D.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getOrder() == resTest2_14D.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == resTest2_14D.isSignificant());
					passedTests2[0] = true;
				}
				else if(res.isaComparisonOf("data1","data2")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), resTest2_12D.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getEffectSize(), resTest2_12D.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getOrder() == resTest2_12D.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == resTest2_12D.isSignificant());
					passedTests2[1] = true;
				}
				else if(res.isaComparisonOf("data1","data3")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), resTest2_13D.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getEffectSize(), resTest2_13D.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getOrder() == resTest2_13D.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == resTest2_13D.isSignificant());
					passedTests2[2] = true;
				}
				else if(res.isaComparisonOf("data1","data5")){
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), resTest2_15D.getPValue(), 0);
					assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getEffectSize(), resTest2_15D.getEffectSize(), 0);
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getOrder() == resTest2_15D.getOrder());
					assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == resTest2_15D.isSignificant());
					passedTests2[3] = true;
				}
					
			}
			
			//check all have passed
			assertTrue("all tests should have passed", !arrayContains(passedTests,false));
						
			//check all have passed for main test
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
			Report.setupLaTeXLoggers("reports/layer2/multiple/report3.tex");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//////////////the actual tests///////////////////////

		
		//get results from Layer2
		TimeseriesGenerator<PairGeneratorOutput> gen1 = new ExampleTimeseriesGenerator(0, 5,100);
		TimeseriesGenerator<PairGeneratorOutput> gen2 = new ExampleTimeseriesGenerator(0.2, 5,100);
		TimeseriesGenerator<PairGeneratorOutput> gen3 = new ExampleTimeseriesGenerator(0.4, 5,100);
		TimeseriesGenerator<PairGeneratorOutput> gen4 = new ExampleTimeseriesGenerator(0.6, 5,100);
		TimeseriesGenerator<PairGeneratorOutput> gen5 = new ExampleTimeseriesGenerator(0.8, 5,100);
		
		Generator<PairGeneratorOutput> genD1 = new ExampleDatapointGenerator(0, 5);
		Generator<PairGeneratorOutput> genD2 = new ExampleDatapointGenerator(0.2, 5);
		Generator<PairGeneratorOutput> genD3 = new ExampleDatapointGenerator(0.4, 5);
		Generator<PairGeneratorOutput> genD4 = new ExampleDatapointGenerator(0.6, 5);
		Generator<PairGeneratorOutput> genD5 = new ExampleDatapointGenerator(0.8, 5);
		
		List<TimeseriesGenerator<PairGeneratorOutput>> gens = new ArrayList<TimeseriesGenerator<PairGeneratorOutput>>();
		gens.add(gen1);
		gens.add(gen2);
		gens.add(gen3);
		gens.add(gen4);
		gens.add(gen5);
		SetOfExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>> gen1ExpT = new SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>>(gen1, "data1");
		SetOfExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>> gen2ExpT = new SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>>(gen2, "data2");
		SetOfExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>> gen3ExpT = new SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>>(gen3, "data3");
		SetOfExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>> gen4ExpT = new SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>>(gen4, "data4");
		SetOfExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>> gen5ExpT = new SingleArtefactExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>>(gen5, "data5");

		List<SetOfExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>>> setsOfExperimentsT = new ArrayList<SetOfExperiments<TimeseriesGeneratorOutput<PairGeneratorOutput>>>();
		setsOfExperimentsT.add(gen1ExpT);
		setsOfExperimentsT.add(gen2ExpT);
		setsOfExperimentsT.add(gen3ExpT);
		setsOfExperimentsT.add(gen4ExpT);
		setsOfExperimentsT.add(gen5ExpT);
		
		SetOfComparisons<TimeseriesGeneratorOutput<PairGeneratorOutput>> gensCompT1 = new SetOfComparisons<TimeseriesGeneratorOutput<PairGeneratorOutput>>(setsOfExperimentsT, new BonFerroni());
		SetOfComparisons<TimeseriesGeneratorOutput<PairGeneratorOutput>> gensCompT2 = new SetOfComparisons<TimeseriesGeneratorOutput<PairGeneratorOutput>>(setsOfExperimentsT,gen1ExpT, new BonFerroni());
		
		List<Generator<PairGeneratorOutput>> gensD = new ArrayList<Generator<PairGeneratorOutput>>();
		gensD.add(genD1);
		gensD.add(genD2);
		gensD.add(genD3);
		gensD.add(genD4);
		gensD.add(genD5);
		SetOfExperiments<PairGeneratorOutput> gen1Exp = new SingleArtefactExperiments<PairGeneratorOutput>(genD1, "data1");
		SetOfExperiments<PairGeneratorOutput> gen2Exp = new SingleArtefactExperiments<PairGeneratorOutput>(genD2, "data2");
		SetOfExperiments<PairGeneratorOutput> gen3Exp = new SingleArtefactExperiments<PairGeneratorOutput>(genD3, "data3");
		SetOfExperiments<PairGeneratorOutput> gen4Exp = new SingleArtefactExperiments<PairGeneratorOutput>(genD4, "data4");
		SetOfExperiments<PairGeneratorOutput> gen5Exp = new SingleArtefactExperiments<PairGeneratorOutput>(genD5, "data5");
		
		List<SetOfExperiments<PairGeneratorOutput>> setsOfExperimentsD = new ArrayList<SetOfExperiments<PairGeneratorOutput>>();
		setsOfExperimentsD.add(gen1Exp);
		setsOfExperimentsD.add(gen2Exp);
		setsOfExperimentsD.add(gen3Exp);
		setsOfExperimentsD.add(gen4Exp);
		setsOfExperimentsD.add(gen5Exp);
		
		SetOfComparisons<PairGeneratorOutput> gensCompD1 = new SetOfComparisons<PairGeneratorOutput>(setsOfExperimentsD, new BonFerroni());
		SetOfComparisons<PairGeneratorOutput> gensCompD2 = new SetOfComparisons<PairGeneratorOutput>(setsOfExperimentsD,gen1Exp, new BonFerroni());

		
		
		//TIMESERIES
		//comparing each with each
		List<Result> results = Layer2.run(gensCompT1, 0.05, false, false, new NoIncrementing(30), new MersenneTwister(0));
		//comparing everything with one (gen1)
		List<Result> results2 = Layer2.run(gensCompT2, 0.05, false, false, new NoIncrementing(30), new MersenneTwister(1));//with main generator
		
		//DATAPOINT
		List<Result> resultsD = Layer2.run(gensCompD1, 0.05, false, false, new NoIncrementing(30), new MersenneTwister(2));
		List<Result> results2D = Layer2.run(gensCompD2, 0.05, false, false, new NoIncrementing(30), new MersenneTwister(3));//with main generator

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
		
		Result resTest12D = Layer1.comparePair(artificialResults1D, artificialResults2D, 0.05, false, false, false, "data1", "data2", null, null);
		Result resTest13D = Layer1.comparePair(artificialResults1D, artificialResults3D, 0.05, false, false, false, "data1", "data3", null, null);
		Result resTest14D = Layer1.comparePair(artificialResults1D, artificialResults4D, 0.05, false, false, false, "data1", "data4", null, null);
		Result resTest15D = Layer1.comparePair(artificialResults1D, artificialResults5D, 0.05, false, false, false, "data1", "data5", null, null);
		Result resTest23D = Layer1.comparePair(artificialResults2D, artificialResults3D, 0.05, false, false, false, "data2", "data3", null, null);
		Result resTest24D = Layer1.comparePair(artificialResults2D, artificialResults4D, 0.05, false, false, false, "data2", "data4", null, null);
		Result resTest25D = Layer1.comparePair(artificialResults2D, artificialResults5D, 0.05, false, false, false, "data2", "data5", null, null);
		Result resTest34D = Layer1.comparePair(artificialResults3D, artificialResults4D, 0.05, false, false, false, "data3", "data4", null, null);
		Result resTest35D = Layer1.comparePair(artificialResults3D, artificialResults5D, 0.05, false, false, false, "data3", "data5", null, null);
		Result resTest45D = Layer1.comparePair(artificialResults4D, artificialResults5D, 0.05, false, false, false, "data4", "data5", null, null);

		
			
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
		
		Result resTest2_12D = Layer1.comparePair(artificialResults1D, artificialResults2D, 0.05, false, false, false, "data1", "data2", null, null);
		Result resTest2_13D = Layer1.comparePair(artificialResults1D, artificialResults3D, 0.05, false, false, false, "data1", "data3", null, null);
		Result resTest2_14D = Layer1.comparePair(artificialResults1D, artificialResults4D, 0.05, false, false, false, "data1", "data4", null, null);
		Result resTest2_15D = Layer1.comparePair(artificialResults1D, artificialResults5D, 0.05, false, false, false, "data1", "data5", null, null);
		
		boolean[] passedTests = new boolean[10];
		boolean[] passedTests2 = new boolean[4];

		Arrays.fill(passedTests, false);
		Arrays.fill(passedTests2, false);

		
		//Tests with time series////////////////////////////////////////////////////////////////////////
		
		//check correct number of comparisons carried out
		assertTrue("size of results array should be 10 (one for each pair - all vs all)",results.size() == 10);
		assertTrue("size of results array should be 4 (one for each pair - all vs main)",results2.size() == 4);
		ListIterator<Result> resIter = results.listIterator();
		Result res;

		//check every comparison
		//all vs all
		while(resIter.hasNext()){
		res = resIter.next();
		if(res.isaComparisonOf("data1","data2")){
			//carry out bon ferroni adjustment
			double expectedPVal = resTest12.getPValue() * 10;
			boolean expectedSig = expectedPVal <= 0.05;
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), expectedPVal, 0);
			assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == expectedSig);
			passedTests[0] = true;
		}
		else if(res.isaComparisonOf("data1","data3")){
			double expectedPVal = resTest13.getPValue() * 10;
			boolean expectedSig = expectedPVal <= 0.05;
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), expectedPVal, 0);
			assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == expectedSig);
			passedTests[1] = true;
		}
		else if(res.isaComparisonOf("data1","data4")){
			double expectedPVal = resTest14.getPValue() * 10;
			boolean expectedSig = expectedPVal <= 0.05;
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), expectedPVal, 0);
			assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == expectedSig);
			passedTests[2] = true;
		}
		else if(res.isaComparisonOf("data1","data5")){
			double expectedPVal = resTest15.getPValue() * 10;
			boolean expectedSig = expectedPVal <= 0.05;
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), expectedPVal, 0);
			assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == expectedSig);
			passedTests[3] = true;
		}		
		else if(res.isaComparisonOf("data2","data3")){
			double expectedPVal = resTest23.getPValue() * 10;
			boolean expectedSig = expectedPVal <= 0.05;
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), expectedPVal, 0);
			assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == expectedSig);
			passedTests[4] = true;
		}
		else if(res.isaComparisonOf("data2","data4")){
			double expectedPVal = resTest24.getPValue() * 10;
			boolean expectedSig = expectedPVal <= 0.05;
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), expectedPVal, 0);
			assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == expectedSig);
			passedTests[5] = true;
		}
		else if(res.isaComparisonOf("data2","data5")){
			double expectedPVal = resTest25.getPValue() * 10;
			boolean expectedSig = expectedPVal <= 0.05;
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), expectedPVal, 0);
			assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == expectedSig);
			passedTests[6] = true;
		}
		else if(res.isaComparisonOf("data3","data4")){
			double expectedPVal = resTest34.getPValue() * 10;
			boolean expectedSig = expectedPVal <= 0.05;
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), expectedPVal, 0);
			assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == expectedSig);
			passedTests[7] = true;
		}
		else if(res.isaComparisonOf("data3","data5")){
			double expectedPVal = resTest35.getPValue() * 10;
			boolean expectedSig = expectedPVal <= 0.05;
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), expectedPVal, 0);
			assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == expectedSig);
			passedTests[8] = true;
		}
		else if(res.isaComparisonOf("data4","data5")){
			double expectedPVal = resTest45.getPValue() * 10;
			boolean expectedSig = expectedPVal <= 0.05;
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), expectedPVal, 0);
			assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == expectedSig);
			passedTests[9] = true;
		}
			
	}
	
	//all vs main
	resIter = results2.listIterator();
	while(resIter.hasNext()){
		res = resIter.next();
		if(res.isaComparisonOf("data1","data2")){
			double expectedPVal = resTest2_12.getPValue() * 4;
			boolean expectedSig = expectedPVal <= 0.05;
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), expectedPVal, 0);
			assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == expectedSig);
			passedTests2[0] = true;
		}
		else if(res.isaComparisonOf("data1","data3")){
			double expectedPVal = resTest2_13.getPValue() * 4;
			boolean expectedSig = expectedPVal <= 0.05;
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), expectedPVal, 0);
			assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == expectedSig);
			passedTests2[1] = true;
		}
		else if(res.isaComparisonOf("data1","data4")){
			double expectedPVal = resTest2_14.getPValue() * 4;
			boolean expectedSig = expectedPVal <= 0.05;
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), expectedPVal, 0);
			assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == expectedSig);
			passedTests2[2] = true;
		}
		else if(res.isaComparisonOf("data1","data5")){
			double expectedPVal = resTest2_15.getPValue() * 4;
			boolean expectedSig = expectedPVal <= 0.05;
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), expectedPVal, 0);
			assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == expectedSig);
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
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), expectedPVal, 0);
			assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == expectedSig);
			passedTests[0] = true;
		}
		else if(res.isaComparisonOf("data1","data3")){
			double expectedPVal = resTest13D.getPValue() * 10;
			boolean expectedSig = expectedPVal <= 0.05;
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), expectedPVal, 0);
			assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == expectedSig);
			passedTests[1] = true;
		}
		else if(res.isaComparisonOf("data1","data4")){
			double expectedPVal = resTest14D.getPValue() * 10;
			boolean expectedSig = expectedPVal <= 0.05;
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), expectedPVal, 0);
			assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == expectedSig);
			passedTests[2] = true;
		}
		else if(res.isaComparisonOf("data1","data5")){
			double expectedPVal = resTest15D.getPValue() * 10;
			boolean expectedSig = expectedPVal <= 0.05;
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), expectedPVal, 0);
			assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == expectedSig);
			passedTests[3] = true;
		}		
		else if(res.isaComparisonOf("data2","data3")){
			double expectedPVal = resTest23D.getPValue() * 10;
			boolean expectedSig = expectedPVal <= 0.05;
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), expectedPVal, 0);
			assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == expectedSig);
			passedTests[4] = true;
		}
		else if(res.isaComparisonOf("data2","data4")){
			double expectedPVal = resTest24D.getPValue() * 10;
			boolean expectedSig = expectedPVal <= 0.05;
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), expectedPVal, 0);
			assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == expectedSig);
			passedTests[5] = true;
		}
		else if(res.isaComparisonOf("data2","data5")){
			double expectedPVal = resTest25D.getPValue() * 10;
			boolean expectedSig = expectedPVal <= 0.05;
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), expectedPVal, 0);
			assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == expectedSig);
			passedTests[6] = true;
		}
		else if(res.isaComparisonOf("data3","data4")){
			double expectedPVal = resTest34D.getPValue() * 10;
			boolean expectedSig = expectedPVal <= 0.05;
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), expectedPVal, 0);
			assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == expectedSig);
			passedTests[7] = true;
		}
		else if(res.isaComparisonOf("data3","data5")){
			double expectedPVal = resTest35D.getPValue() * 10;
			boolean expectedSig = expectedPVal <= 0.05;
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), expectedPVal, 0);
			assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == expectedSig);
			passedTests[8] = true;
		}
		else if(res.isaComparisonOf("data4","data5")){
			double expectedPVal = resTest45D.getPValue() * 10;
			boolean expectedSig = expectedPVal <= 0.05;
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), expectedPVal, 0);
			assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == expectedSig);
			passedTests[9] = true;
		}
			
	}
	
	resIter = results2D.listIterator();
	while(resIter.hasNext()){
		res = resIter.next();
		if(res.isaComparisonOf("data1","data2")){
			double expectedPVal = resTest2_12D.getPValue() * 4;
			boolean expectedSig = expectedPVal <= 0.05;
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), expectedPVal, 0);
			assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == expectedSig);
			passedTests2[0] = true;
		}
		else if(res.isaComparisonOf("data1","data3")){
			double expectedPVal = resTest2_13D.getPValue() * 4;
			boolean expectedSig = expectedPVal <= 0.05;
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), expectedPVal, 0);
			assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == expectedSig);
			passedTests2[1] = true;
		}
		else if(res.isaComparisonOf("data1","data4")){
			double expectedPVal = resTest2_14D.getPValue() * 4;
			boolean expectedSig = expectedPVal <= 0.05;
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), expectedPVal, 0);
			assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == expectedSig);
			passedTests2[2] = true;
		}
		else if(res.isaComparisonOf("data1","data5")){
			double expectedPVal = resTest2_15D.getPValue() * 4;
			boolean expectedSig = expectedPVal <= 0.05;
			assertEquals("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.getPValue(), expectedPVal, 0);
			assertTrue("test result obtained directly from Layer 1 must match result obtained via Layer 2 (pValue)", res.isSignificant() == expectedSig);
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
	



}

// End ///////////////////////////////////////////////////////////////

