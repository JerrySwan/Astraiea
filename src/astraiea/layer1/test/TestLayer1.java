package astraiea.layer1.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import astraiea.Ordering;
import astraiea.Result;
import astraiea.layer1.Layer1;
import astraiea.layer1.effectsize.ThresholdModifiedVarghaDelaney;
import astraiea.layer2.generators.DoubleGeneratorOutput;
import astraiea.layer2.generators.GeneratorOutput;
import astraiea.util.MersenneTwister;

/**
 * Unit tests to confirm that, for each type of p value (significance) or effect size test implemented in Astraiea, 
 * it produces results that match those produced by R. 
 * In all cases results are expected to be as close as possible to R, 
 * though how close this is depends on the test - as stated in the description above each method. 
 * In all cases results match at least to within 10^-3.
 * 
 * Unit tests are also used to confirm that the Result.isSignificant() and Result.getOrder() fields are correctly set, giving a binary 
 * statement whether the result is isSignificant() and which of the two data samples are greater respectively.
 * 
 * @author Geoffrey Neumann
 *
 */

public final class TestLayer1 {
	 	
	/**
	 * Confirms that p values when the Wilcoxon/Mann Whitney U test (default p value test) 
	 * is used match those generated from R with a margin of error no more than 10^-3
	 * @throws IOException 
	 * @throws SecurityException 
	 */
	@Test
	public void wilcoxon() throws IOException {
		//generate data for testing
		double[][] data = generateData(10, 10, 20, 20, false);//default - data[0] is higher
		double[][] dataN50 = generateData(10, 10, 50, 50, false);//larger datasets
		double[][] dataN2050 = generateData(10, 10, 20, 50, false);//unequal datasets
		double[][] dataD0 = generateData(0, 10, 20, 20, false);//difference of 0 (data is the same)
		double[][] dataDneg10 = generateData(-10, 10, 20, 20, false);//data[0] is lower
		double[][] dataV100 = generateData(100, 100, 20, 20, false);//data with more variation
		double[][] dataContinuous = generateData(10, 10, 20, 20, true);//continuous data
	
		//set up logger
		Layer1.setupLatexLoggers("reports/layer1/reportWilcoxon.tex");
		
		///////////////////////////
		
		//random for confidence interval
		MersenneTwister ran = new MersenneTwister(12345);
		long[] seeds = new long[7];
		for(int i =0; i < 7; i++)
			seeds[i] = ran.nextLong();
		
		// executes layer 1 comparing the datasets in 'data' with brunner munzel, pairing and censoring turned off
		Result res = Layer1.compare(data[0], data[1], 0.05, false, new MersenneTwister(seeds[0]) , "testdata1", "testdata2");
		
		// unit testing
		double expectedP = 2.681993082216759E-4;
		assertEquals("p value must be accurately using Wilcoxon, using R function wilcox.test(dataA, dataB) as an oracle", expectedP, res.getPValue(),0.001);
		assertTrue("significance must be true", res.isSignificant());
			
		// as above - for different data sets
		res = Layer1.compare(dataN50[0], dataN50[1], 0.05, false, new MersenneTwister(seeds[1]));
		expectedP = 1.1453687069155925E-7;
		assertEquals("p value must be accurately using Wilcoxon, using R function wilcox.test(dataA, dataB) as an oracle", expectedP, res.getPValue(),0.001);
		assertTrue("significance must be true", res.isSignificant());
		
		res = Layer1.compare(dataN2050[0],dataN2050[1], 0.05, false, new MersenneTwister(seeds[2]));
		expectedP = 2.086e-05;
		assertEquals("p value must be accurately using Wilcoxon, using R function wilcox.test(dataA, dataB) as an oracle", expectedP, res.getPValue(),0.001);
		assertTrue("significance must be true", res.isSignificant());
		
		res = Layer1.compare(dataD0[0],dataD0[1], 0.05, false, new MersenneTwister(seeds[3]));
		expectedP = 0.8922025897776891;
		assertEquals("p value must be accurately using Wilcoxon, using R function wilcox.test(dataA, dataB) as an oracle",expectedP, res.getPValue(),0.001);
		assertFalse("significance must be false", res.isSignificant());
		
		res = Layer1.compare(dataDneg10[0],dataDneg10[1], 0.05, false, new MersenneTwister(seeds[4]));
		expectedP = 0.0010437986512413352;
		assertEquals("p value must be accurately using Wilcoxon, using R function wilcox.test(dataA, dataB) as an oracle", expectedP, res.getPValue(),0.001);
		assertTrue("significance must be true", res.isSignificant());
		
		res = Layer1.compare(dataV100[0],dataV100[1], 0.05, false, new MersenneTwister(seeds[5]));
		expectedP = 2.468953705088296E-4;
		assertEquals("p value must be accurately using Wilcoxon, using R function wilcox.test(dataA, dataB) as an oracle", expectedP, res.getPValue(),0.001);
		assertTrue("significance must be true", res.isSignificant());
		
		res = Layer1.compare(dataContinuous[0],dataContinuous[1], 0.05, false, new MersenneTwister(seeds[6]));
		expectedP = 1.1921029960397374E-4;
		assertEquals("p value must be accurately using Wilcoxon, using R function wilcox.test(dataA, dataB) as an oracle", expectedP, res.getPValue(),0.001);
		assertTrue("significance must be true", res.isSignificant());
	}
	
	/**
	 * Confirms that p values when the Wilcoxon/Mann Whitney U test (default p value test) 
	 * is used match those generated from R with a margin of error no more than 10^-3
	 * @throws IOException 
	 * @throws SecurityException 
	 */
	@Test
	public void modifiedVarghaDelaney() throws IOException {
		//generate data for testing
		
		MersenneTwister ran = new MersenneTwister(12345);
		int noisenum = 30;
		int validnum = 10;
		double[] data1Noise = new double[noisenum];
		double[] data2Noise = new double[noisenum];
		for(int i =0; i < noisenum; i++){
			data1Noise[i] = ran.nextDouble() + 1;
			data2Noise[i] = ran.nextDouble();
		}
		
		double[] data1Valid = new double[validnum];
		double[] data2Valid = new double[validnum];
		for(int i =0; i < validnum; i++){
			data1Valid[i] = (ran.nextDouble() * 10);
			data2Valid[i] = (ran.nextDouble() * 10) + 10;
		}
		
		double[] data1Combined = combineAtRandom(data1Noise,data1Valid,ran);
		double[] data2Combined = combineAtRandom(data2Noise,data2Valid,ran);
	
		//convert the combined data to a List<GeneratorOutput> format for the VD class
		List<GeneratorOutput> data1CombList = new ArrayList<GeneratorOutput>();
		List<GeneratorOutput> data2CombList = new ArrayList<GeneratorOutput>();
		for(int i =0; i< data1Combined.length; i++){
			data1CombList.add(new DoubleGeneratorOutput(data1Combined[i]));
			data2CombList.add(new DoubleGeneratorOutput(data2Combined[i]));

		}
	
		//set up logger
		Layer1.setupLatexLoggers("reports/layer1/reportWilcoxon.tex");
				
		Result res = Layer1.compare(data1Combined, data2Combined, 0.05, false, ran, new ThresholdModifiedVarghaDelaney(2,true));

		//remove all the values under 2 (which should have been ignored by the ThresholdVDMod)
		data1CombList.clear();
		data2CombList.clear();
		for(int i =0; i < noisenum + validnum; i++){
			if(data1Combined[i] < 2)
				data1Combined[i] = 0;
			data1CombList.add(new DoubleGeneratorOutput(data1Combined[i]));
			if(data2Combined[i] < 2)
				data2Combined[i] = 0;
			data2CombList.add(new DoubleGeneratorOutput(data2Combined[i]));

		}
		
		//get what the result should be (given that we've just manually removed all values which the VDmod should ignore)
		double expEffSize = astraiea.layer1.effectsize.VarghaDelaney.evaluate(data1CombList, data2CombList, false, null);
		Ordering expOrder = astraiea.layer1.effectsize.VarghaDelaney.getOrder(expEffSize);
		
		// unit testing
		assertEquals("Effect size obtained using VDmod should match that obtained by manually transforming data", expEffSize, res.getEffectSize(), 0);
		assertEquals("Effect size order obtained using VDmod should match that obtained by manually transforming data", expOrder, res.getOrder());
	}
	
	/**
	 * Combines 2 arrays in a random order
	 * 
	 * @param arr1 
	 * @param arr2
	 * @return
	 */
	private double[] combineAtRandom(double[] arr1, double[] arr2, MersenneTwister ran) {
		int combinedLen = arr1.length + arr2.length;
		
		double[] combinedArr = new double[combinedLen];
		ArrayList<Integer> indexes  = new ArrayList<Integer>();
		for(int i =0; i < combinedLen; i++){
			indexes.add(i);
		}
		
		int i =0;
		while(!indexes.isEmpty()){
			int ranval = indexes.remove(ran.nextInt(indexes.size()));
			if(ranval >= arr1.length)
				combinedArr[i] = arr2[ranval - arr1.length];
			else
				combinedArr[i] = arr1[ranval];
			i++;
		}
		
		return combinedArr;
	}

	/**
	 * Confirms that p values when a comparison is made against deterministic data 
	 * (using the Mann Whitney test with deterministic data)
	 *  match those generated from R with a margin of error no more than 10^-3
	 * @throws IOException 
	 * @throws SecurityException 
	 */
	@Test
	public void wilcoxonOneSample() throws IOException {
		Random ran = new Random(0);
		
		
		//generate data for testing
		double[] data = generateSingleData(0, 10, 20, false, new Random(ran.nextLong()));
		double[] dataN50 = generateSingleData(0, 10, 50, false, new Random(ran.nextLong()));//larger dataset
		double[] dataO10 = generateSingleData(10, 10, 20, false, new Random(ran.nextLong()));//higher offset
		double[] dataV100 = generateSingleData(0, 100, 20, false, new Random(ran.nextLong()));//larger variation
		double[] dataContinuous = generateSingleData(0, 10, 20, true, new Random(ran.nextLong()));//continuous data

		Layer1.setupLatexLoggers("reports/layer1/reportWilcoxon.tex");

		
		///////////////////////////
		
		// executes layer 1 comparing the datasets in 'data' with brunner munzel, pairing and censoring turned off
		Result res = Layer1.compareOneSample(data, 5, 0.05, "testdata1", "testdata2");
		double expectedP = 0.1695;
		assertEquals("p value must be accurately using Wilcoxon one sample test, using R function wilcox.test(dataA, mu=5) as an oracle", expectedP, res.getPValue(),0.001);
		assertFalse("significance must be false", res.isSignificant());
			
		// as above - for different data sets
		res = Layer1.compareOneSample(data, 15, 0.05);
		expectedP = 0.0002522;
		assertEquals("p value must be accurately using Wilcoxon one sample test, using R function wilcox.test(dataA, mu=15) as an oracle", expectedP, res.getPValue(),0.001);
		assertTrue("significance must be true", res.isSignificant());
		
		res = Layer1.compareOneSample(data, -5, 0.05);
		expectedP = 0.02498;
		assertTrue("significance must be true", res.isSignificant());
		
		res = Layer1.compareOneSample(dataN50,5, 0.05);
		expectedP = 0.0003074;
		assertEquals("p value must be accurately using Wilcoxon one sample test, using R function wilcox.test(dataA, mu=5) as an oracle", expectedP, res.getPValue(),0.001);
		assertTrue("significance must be true", res.isSignificant());
		
		res = Layer1.compareOneSample(dataN50,15, 0.05);
		expectedP = 1.4e-09;
		assertEquals("p value must be accurately using Wilcoxon one sample test, using R function wilcox.test(dataA, mu=15) as an oracle",expectedP, res.getPValue(),0.001);
		assertTrue("significance must be true", res.isSignificant());
		
		res = Layer1.compareOneSample(dataN50,-5, 0.05);
		expectedP = 2.541e-05;
		assertEquals("p value must be accurately using Wilcoxon one sample test, using R function wilcox.test(dataA, mu=-5) as an oracle", expectedP, res.getPValue(),0.001);
		assertTrue("significance must be true", res.isSignificant());
		
		res = Layer1.compareOneSample(dataO10,15, 0.05);
		expectedP = 0.05564;
		assertEquals("p value must be accurately using Wilcoxon one sample test, using R function wilcox.test(dataA, mu=15) as an oracle", expectedP, res.getPValue(),0.001);
		assertFalse("significance must be false", res.isSignificant());
		
		res = Layer1.compareOneSample(dataO10,25, 0.05);
		expectedP = 0.0001424;
		assertEquals("p value must be accurately using Wilcoxon one sample test, using R function wilcox.test(dataA, mu=25) as an oracle", expectedP, res.getPValue(),0.001);
		assertTrue("significance must be true", res.isSignificant());
		
		res = Layer1.compareOneSample(dataO10,5, 0.05);
		expectedP = 0.008918;
		assertEquals("p value must be accurately using Wilcoxon one sample test, using R function wilcox.test(dataA, mu=5) as an oracle", expectedP, res.getPValue(),0.001);
		assertTrue("significance must be true", res.isSignificant());
		
		res = Layer1.compareOneSample(dataV100,50, 0.05);
		expectedP = 0.6676;
		assertEquals("p value must be accurately using Wilcoxon one sample test, using R function wilcox.test(dataA, mu=50) as an oracle", expectedP, res.getPValue(),0.001);
		assertFalse("significance must be false", res.isSignificant());
	
		res = Layer1.compareOneSample(dataV100,150, 0.05);
		expectedP = 0.0004178;
		assertEquals("p value must be accurately using Wilcoxon one sample test, using R function wilcox.test(dataA, mu=150) as an oracle", expectedP, res.getPValue(),0.001);
		assertTrue("significance must be true", res.isSignificant());
		
		res = Layer1.compareOneSample(dataV100,-50, 0.05);
		expectedP = 0.001239;
		assertEquals("p value must be accurately using Wilcoxon one sample test, using R function wilcox.test(dataA, mu=-50) as an oracle", expectedP, res.getPValue(),0.001);
		assertTrue("significance must be true", res.isSignificant());

		res = Layer1.compareOneSample(dataContinuous,5, 0.05);
		expectedP = 0.06372;
		assertEquals("p value must be accurately using Wilcoxon one sample test, using R function wilcox.test(dataA, mu=5) as an oracle", expectedP, res.getPValue(),0.001);
		assertFalse("significance must be false", res.isSignificant());

		res = Layer1.compareOneSample(dataContinuous,15, 0.05);
		expectedP = 1.907e-06;
		assertEquals("p value must be accurately using Wilcoxon one sample test, using R function wilcox.test(dataA, mu=15) as an oracle", expectedP, res.getPValue(),0.001);
		assertTrue("significance must be true", res.isSignificant());

		res = Layer1.compareOneSample(dataContinuous,-5, 0.05);
		expectedP = 0.08969;
		assertEquals("p value must be accurately using Wilcoxon one sample test, using R function wilcox.test(dataA, mu=-5) as an oracle", expectedP, res.getPValue(),0.001);
		assertFalse("significance must be false", res.isSignificant());
	}

	private double[] generateSingleData(int offset, int var, int n, boolean continuous, Random ran) {
		double[] data = new double[n];
		
		for(int i =0; i < n; i++){
			if(continuous)
				data[i] = ran.nextGaussian() * var + offset;
			else
				data[i] = Math.round(ran.nextGaussian() * var) + offset;
		}
		return data;
	}

	/**
	 * Confirms that p values when the Brunner Munzel test (optional p value test) is used match those 
	 * generated from R with a margin of error no more than 10^-9
	 * @throws IOException 
	 * @throws SecurityException 
	 */
	
	@Test
	public void brunnerMunzel() throws IOException {
		//as in wilcoxon()
		double[][] data = generateData(10, 10, 20, 20, false);
		double[][] dataN50 = generateData(10, 10, 50, 50, false);
		double[][] dataN2050 = generateData(10, 10, 20, 50, false);
		double[][] dataD0 = generateData(0, 10, 20, 20, false);
		double[][] dataDneg10 = generateData(-10, 10, 20, 20, false);
		double[][] dataV100 = generateData(100, 100, 20, 20, false);
		double[][] dataContinuous = generateData(10, 10, 20, 20, true);

		Layer1.setupLatexLoggers("reports/layer1/reportBrunnerMunzel.tex");
		// comparison, unlike in wilcoxon(), includes brunner munzel
		
		//random for confidence interval
		MersenneTwister ran = new MersenneTwister(12345);
		long[] seeds = new long[7];
		for(int i =0; i < 7; i++)
			seeds[i] = ran.nextLong();
		
		double expectedP = 3.4037320961743944E-6;
		Result res = Layer1.compare(data[0],data[1], 0.05,true, new MersenneTwister(seeds[0]));
		assertEquals("p value must be accurately using Brunner Munzel, using R function lawstat::brunner.munzel.test as an oracle", expectedP, res.getPValue(),0.000000001);
		assertTrue("significance must be true", res.isSignificant());
		
		expectedP = 4.57537785436557E-10;
		res = Layer1.compare(dataN50[0],dataN50[1], 0.05,true, new MersenneTwister(seeds[1]));
		assertEquals("p value must be accurately using Brunner Munzel, using R function lawstat::brunner.munzel.test as an oracle", expectedP, res.getPValue(),0.000000001);
		assertTrue("significance must be true", res.isSignificant());
		
		expectedP = 1.726e-08;
		res = Layer1.compare(dataN2050[0],dataN2050[1], 0.05,true, new MersenneTwister(seeds[2]));
		assertEquals("p value must be accurately using Brunner Munzel, using R function lawstat::brunner.munzel.test as an oracle", expectedP, res.getPValue(),0.000000001);
		assertTrue("significance must be true", res.isSignificant());
		
		expectedP = 0.8863669200867559;
		res = Layer1.compare(dataD0[0],dataD0[1], 0.05,true, new MersenneTwister(seeds[3]));
		assertEquals("p value must be accurately using Brunner Munzel, using R function lawstat::brunner.munzel.test as an oracle", expectedP, res.getPValue(),0.000000001);
		assertFalse("significance must be false", res.isSignificant());
		
		expectedP = 1.9752616227086683E-4;
		res = Layer1.compare(dataDneg10[0],dataDneg10[1],0.05,true, new MersenneTwister(seeds[4]));
		assertEquals("p value must be accurately using Brunner Munzel, using R function lawstat::brunner.munzel.test as an oracle", expectedP, res.getPValue(),0.000000001);
		assertTrue("significance must be true", res.isSignificant());
		
		expectedP = 2.7495843510916274E-6;
		res = Layer1.compare(dataV100[0],dataV100[1], 0.05,true, new MersenneTwister(seeds[5]));
		assertEquals("p value must be accurately using Brunner Munzel, using R function lawstat::brunner.munzel.test as an oracle", expectedP, res.getPValue(),0.000000001);
		assertTrue("significance must be true", res.isSignificant());
		
		expectedP = 2.7495843510916274E-6;
		res = Layer1.compare(dataContinuous[0],dataContinuous[1],0.05,true, new MersenneTwister(seeds[6]));
		assertEquals("p value must be accurately using Brunner Munzel, using R function lawstat::brunner.munzel.test as an oracle", expectedP, res.getPValue(),0.000000001);
		assertTrue("significance must be true", res.isSignificant());
	}

	/**
	 * Confirms that p values when the Wilcoxon Signed Rank test (p value test used for paired data) is used 
	 * match those generated from R with a margin of error no more than 10^-4
	 * @throws IOException 
	 * @throws SecurityException 
	 */
	
	@Test
	public void pairedWilcoxon() throws IOException {
		double[][] data = generateData(10, 10, 20, 20, false);
		double[][] dataN50 = generateData(10, 10, 50, 50, false);
		double[][] dataD0 = generateData(0, 10, 20, 20, false);
		double[][] dataDneg10 = generateData(-10, 10, 20, 20, false);
		double[][] dataV100 = generateData(100, 100, 20, 20, false);
		double[][] dataContinuous = generateData(10, 10, 20, 20, true);


		Layer1.setupLatexLoggers("reports/layer1/reportPairedWilcoxon.tex");
		
		Result res = Layer1.comparePaired(data[0],data[1],0.05);
		double expectedP = 0.0014009823240127002;
		assertEquals("p value must be accurately using Paired Wilcoxon, using R function wilcox.test(dataA, dataB, paired=TRUE) as an oracle", expectedP, res.getPValue(),0.0001);
		assertTrue("significance must be true", res.isSignificant());
		
		res = Layer1.comparePaired(dataN50[0],dataN50[1],0.05);
		expectedP = 2.226416138413922E-6;
		assertEquals("p value must be accurately using Paired Wilcoxon, using R function wilcox.test(dataA, dataB, paired=TRUE) as an oracle", expectedP, res.getPValue(),0.0001);
		assertTrue("significance must be true", res.isSignificant());
		
		res = Layer1.comparePaired(dataD0[0],dataD0[1],0.05);
		expectedP = 0.9826066677679626;
		assertEquals("p value must be accurately using Paired Wilcoxon, using R function wilcox.test(dataA, dataB, paired=TRUE) as an oracle", expectedP, res.getPValue(),0.0001);
		assertFalse("significance must be false", res.isSignificant());
		
		res = Layer1.comparePaired(dataDneg10[0],dataDneg10[1],0.05);
		expectedP = 0.0028016980879210897;
		assertEquals("p value must be accurately using Paired Wilcoxon, using R function wilcox.test(dataA, dataB, paired=TRUE) as an oracle", expectedP, res.getPValue(),0.0001);
		assertTrue("significance must be true", res.isSignificant());
		
		res = Layer1.comparePaired(dataV100[0],dataV100[1], 0.05);
		expectedP = 0.001321961308240755;
		assertEquals("p value must be accurately using Paired Wilcoxon, using R function wilcox.test(dataA, dataB, paired=TRUE) as an oracle", expectedP, res.getPValue(),0.0001);
		assertTrue("significance must be true", res.isSignificant());
		
		res = Layer1.comparePaired(dataContinuous[0],dataContinuous[1],0.05);
		expectedP = 4.825592041015625E-4;
		assertEquals("p value must be accurately using Paired Wilcoxon, using R function wilcox.test(dataA, dataB, paired=TRUE) as an oracle", expectedP, res.getPValue(),0.0001);
		assertTrue("significance must be true", res.isSignificant());
	}
		
	/**
	 * Confirms that the effect sizes when the Vargha Delaney test is used 
	 * match those generated from R with a margin of error no more than 10^-5
	 * @throws IOException 
	 * @throws SecurityException 
	 */
	@Test
	public void varghaDelaney() throws IOException {
		double[][] data = generateData(10, 10, 20, 20, false);
		double[][] dataN50 = generateData(10, 10, 50, 50, false);
		double[][] dataN2050 = generateData(10, 10, 20, 50, false);
		double[][] dataD0 = generateData(0, 10, 20, 20, false);
		double[][] dataDneg10 = generateData(-10, 10, 20, 20, false);
		double[][] dataV100 = generateData(100, 100, 20, 20, false);
		double[][] dataContinuous = generateData(10, 10, 20, 20, true);

		Layer1.setupLatexLoggers("reports/layer1/VarghaDelaney.tex");
		
		//random for confidence interval
		MersenneTwister ran = new MersenneTwister(12345);
		long[] seeds = new long[8];
		for(int i =0; i < 8; i++)
			seeds[i] = ran.nextLong();

		Result res = Layer1.compare(data[0],data[1],0.05,false, new MersenneTwister(seeds[0]));
		double expectedP = 0.8375;
		assertEquals("effect size must be accurately using Vargha Delaney, using R function effsize::VD.A as an oracle", expectedP, res.getEffectSize(),0.00001);
		assertTrue("dataA must be greater than dataB, according to the 'ord' field in 'res'", res.getOrder() == Ordering.GREATER);
	
		res = Layer1.compare(dataN50[0],dataN50[1],0.05,false,new MersenneTwister(seeds[1]));
		expectedP = 0.8076;
		assertEquals("effect size must be accurately using Vargha Delaney, using R function effsize::VD.A as an oracle", expectedP, res.getEffectSize(),0.00001);
		assertTrue("dataA must be greater than dataB, according to the 'ord' field in 'res'", res.getOrder() == Ordering.GREATER);
		
		res = Layer1.compare(dataD0[0],dataD0[1],0.05,false,new MersenneTwister(seeds[2]));
		assertTrue("effect size must have not been calculated because the p value test will not have returned significance (see any of the three significance test methods Wilcoxon(), WilcoxonPaired() and BrunnerMunzel() - all return non isSignificant() for this test)", Double.isNaN(res.getEffectSize()));
			
		res = Layer1.compare(dataN2050[0],dataN2050[1],0.05,false,new MersenneTwister(seeds[3]));
		expectedP = 0.8275;
		assertEquals("effect size must be accurately using Vargha Delaney, using R function effsize::VD.A as an oracle", expectedP, res.getEffectSize(),0.00001);
		assertTrue("dataA must be greater than dataB, according to the 'ord' field in 'res'", res.getOrder() == Ordering.GREATER);
		
		res = Layer1.compare(dataD0[0],dataD0[1],0.05,false, new MersenneTwister(seeds[4]));
		assertTrue("effect size must have not been calculated because the p value test will not have returned significance (see any of the three significance test methods Wilcoxon(), WilcoxonPaired() and BrunnerMunzel() - all return non isSignificant() for this test)", Double.isNaN(res.getEffectSize()));

		res = Layer1.compare(dataDneg10[0],dataDneg10[1],0.05,false, new MersenneTwister(seeds[5]));
		expectedP = 0.19625;
		assertEquals("effect size must be accurately using Vargha Delaney, using R function effsize::VD.A as an oracle", expectedP, res.getEffectSize(),0.00001);
		assertTrue("dataA must be less than dataB, according to the 'ord' field in 'res'", res.getOrder() == Ordering.LOWER);

		res = Layer1.compare(dataV100[0],dataV100[1],0.05,false, new MersenneTwister(seeds[6]));
		expectedP = 0.84;
		assertEquals("effect size must be accurately using Vargha Delaney, using R function effsize::VD.A as an oracle", expectedP, res.getEffectSize(),0.00001);
		assertTrue("dataA must be greater than dataB, according to the 'ord' field in 'res'", res.getOrder() == Ordering.GREATER);
		
		res = Layer1.compare(dataContinuous[0],dataContinuous[1],0.05,false, new MersenneTwister(seeds[7]) );
		expectedP = 0.84;
		assertEquals("effect size must be accurately using Vargha Delaney, using R function effsize::VD.A as an oracle", expectedP, res.getEffectSize(),0.00001);
		assertTrue("dataA must be greater than dataB, according to the 'ord' field in 'res'", res.getOrder() == Ordering.GREATER);
	}
	
	
	/**
	 * TODO description in asserts
	 * Confirms that the effect sizes when the Vargha Delaney test is used 
	 * match those generated from R with a margin of error no more than 10^-5
	 * 4 decimal places appears to be the greatest precision that R offers for this function and so, 
	 * for comparison, my results from Astraiea are rounded to 4 decimal places
	 * @throws IOException 
	 * @throws SecurityException 
	 */
	
	@Test
	public void VDconfidenceIntervals() throws IOException {
		double[][] data = generateData(10, 10, 20, 20, false);
		double[][] dataN50 = generateData(10, 10, 50, 50, false);
		double[][] dataN2050 = generateData(10, 10, 20, 50, false);
		double[][] dataDneg10 = generateData(-10, 10, 20, 20, false);
		double[][] dataV100 = generateData(100, 100, 20, 20, false);
		double[][] dataContinuous = generateData(10, 10, 20, 20, true);

		Layer1.setupLatexLoggers("reports/layer1/VarghaDelaney.tex");
		
		//random for confidence interval
		MersenneTwister ran = new MersenneTwister(12345);
		long[] seeds = new long[8];
		for(int i =0; i < 8; i++)
			seeds[i] = ran.nextLong();

		Result res = Layer1.compare(data[0],data[1],0.05,false, new MersenneTwister(seeds[0]));
		double[] expectedCIs = new double[]{0.6938,  0.94};
		double[] actualCIs = res.getCIs();
		assertEquals("confidence intervals must accurately match those obtained by R using bootstraping (R function boot::boot) as an oracle", expectedCIs[0],actualCIs[0], 0.0001 );
		assertEquals("confidence intervals must accurately match those obtained by R using bootstraping (R function boot::boot) as an oracle", expectedCIs[1],actualCIs[1],0.0001);

		
		res = Layer1.compare(dataN50[0],dataN50[1],0.05,false,new MersenneTwister(seeds[1]));
		expectedCIs = new double[]{0.7200,  0.8888};
		actualCIs = res.getCIs();
		assertEquals("confidence intervals must accurately match those obtained by R using bootstraping (R function boot::boot) as an oracle", expectedCIs[0],actualCIs[0], 0.0001);
		assertEquals("confidence intervals must accurately match those obtained by R using bootstraping (R function boot::boot) as an oracle", expectedCIs[1],actualCIs[1], 0.0001);
			
		res = Layer1.compare(dataN2050[0],dataN2050[1],0.05,false,new MersenneTwister(seeds[3]));
		expectedCIs = new double[]{0.7281,  0.9195};
		actualCIs = res.getCIs();
		assertEquals("confidence intervals must accurately match those obtained by R using bootstraping (R function boot::boot) as an oracle", expectedCIs[0],actualCIs[0], 0.0001);
		assertEquals("confidence intervals must accurately match those obtained by R using bootstraping (R function boot::boot) as an oracle", expectedCIs[1],actualCIs[1], 0.0001);

		res = Layer1.compare(dataDneg10[0],dataDneg10[1],0.05,false, new MersenneTwister(seeds[5]));
		expectedCIs = new double[]{0.0650,  0.3561};
		actualCIs = res.getCIs();
		assertEquals("confidence intervals must accurately match those obtained by R using bootstraping (R function boot::boot) as an oracle", expectedCIs[0],actualCIs[0], 0.0001);
		assertEquals("confidence intervals must accurately match those obtained by R using bootstraping (R function boot::boot) as an oracle", expectedCIs[1],actualCIs[1], 0.0002);

		res = Layer1.compare(dataV100[0],dataV100[1],0.05,false, new MersenneTwister(seeds[6]));
		expectedCIs = new double[]{0.6850,  0.9425};
		actualCIs = res.getCIs();
		assertEquals("confidence intervals must accurately match those obtained by R using bootstraping (R function boot::boot) as an oracle", expectedCIs[0],actualCIs[0], 0.0001);
		assertEquals("confidence intervals must accurately match those obtained by R using bootstraping (R function boot::boot) as an oracle", expectedCIs[1],actualCIs[1], 0.0001);
		
		res = Layer1.compare(dataContinuous[0],dataContinuous[1],0.05,false, new MersenneTwister(seeds[7]) );
		expectedCIs = new double[]{0.7125,  0.9375};
		actualCIs = res.getCIs();
		assertEquals("confidence intervals must accurately match those obtained by R using bootstraping (R function boot::boot) as an oracle", expectedCIs[0],actualCIs[0], 0.0001);
		assertEquals("confidence intervals must accurately match those obtained by R using bootstraping (R function boot::boot) as an oracle", expectedCIs[1],actualCIs[1], 0.0001);
	}

	/**
	 * Generate random data.
	 * 
	 * @param diff difference between the two samples
	 * @param var amount of variance in the datasets
	 * @param nA size of dataA
	 * @param nB size of dataB
	 * @param continuous whether data is continuous
	 * @return array in which [0] = dataA, [1] = dataB
	 */
	
	private double[][] generateData(int diff, int var, int nA, int nB, boolean continuous){
		double[] dataA = new double[nA];
		double[] dataB = new double[nB];
		Random ran = new Random(0);
		
		int n = Math.max(nA, nB);
		for(int i =0; i < n; i++){
			if(continuous){
				if(i < nA)
					dataA[i] = ran.nextGaussian() * var + diff;
				if(i < nB)
					dataB[i] = ran.nextGaussian() * var;
			}
			else{
				if(i < nA)
					dataA[i] = Math.round(ran.nextGaussian() * var) + diff;
				if(i < nB)
					dataB[i] = Math.round(ran.nextGaussian() * var);
			}
		}
		
		System.out.println("dataA = " + Arrays.toString(dataA));
		System.out.println("dataB = " + Arrays.toString(dataB));
		double[][] allData = new double[2][];
		allData[0] = dataA;
		allData[1] = dataB;
		return allData;
	}
	
	/**
	 * Generate data for censored/dichotomous tests.
	 * 
	 * @param propGrtrA proportion of samples in dataA that passes (greater than threshold)
	 * @param propGrtrB proportion of samples in dataB that passes (greater than threshold)
	 * @param propEqA proportion of samples in dataA that passed (equal to threshold)
	 * @param propEqB proportion of samples in dataB that passed (equal to threshold)
	 * @param censorVal the threshold
	 * @param var by how much the generated values should vary
	 * @param nA number of samples in dataA
	 * @param nB number of samples in dataB
	 * @param continuous whether data is continuous
	 * @param ran
	 * @return
	 */
	private double[][] generateCensoredData(double propGrtrA, double propGrtrB, double propEqA, double propEqB, double censorVal, int var, int nA, int nB, boolean continuous, Random ran){
		int numGrtrA = (int) Math.round(propGrtrA * nA);
		int numGrtrB = (int) Math.round(propGrtrB * nB);
		int numEqA = (int) Math.round(propEqA * nA);
		int numEqB = (int) Math.round(propEqB * nB);
		
		double[] data1 = new double[nA];
		double[] data2 = new double[nB];
		//lists to enable us to select indexes within the datasets without replacement
		ArrayList<Integer> indexesA = new ArrayList<Integer>();
		ArrayList<Integer> indexesB = new ArrayList<Integer>();
		for(int i =0; i < nA; i++){
			indexesA.add(i);
		}
		for(int i =0; i < nB; i++){
			indexesB.add(i);
		}
		
		//randomly assign to indexes within the data arrays, the required number of values greater than the threshold (censorVal)
		for(int i =0; i < numGrtrA; i++){//for data1
			int ind = indexesA.remove(ran.nextInt(indexesA.size()));
			if(continuous)
				data1[ind] = censorVal + 1 + ran.nextDouble() * var;
			else
				data1[ind] = censorVal + 1 + ran.nextInt(var);
		}
		for(int i =0; i < numGrtrB; i++){//for data2
			int ind = indexesB.remove(ran.nextInt(indexesB.size()));
			if(continuous)
				data2[ind] = censorVal + 1 + ran.nextDouble() * var;
			else
				data2[ind] = censorVal + 1 + ran.nextInt(var);
		}
		
		//randomly assign ones equal
		for(int i =0; i < numEqA; i++){
			int ind = indexesA.remove(ran.nextInt(indexesA.size()));
			data1[ind] = censorVal;
		}
		for(int i =0; i < numEqB; i++){
			int ind = indexesB.remove(ran.nextInt(indexesB.size()));
			data2[ind] = censorVal;
		}
		
		//and below - using remaining indexes
		while(!indexesA.isEmpty()){
			int ind = indexesA.remove(ran.nextInt(indexesA.size()));
			if(continuous)
				data1[ind] = censorVal - 1 - ran.nextDouble() * var;
			else
				data1[ind] = censorVal - 1 - ran.nextInt(var);
		}
		while(!indexesB.isEmpty()){
			int ind = indexesB.remove(ran.nextInt(indexesB.size()));
			if(continuous)
				data2[ind] = censorVal - 1 - ran.nextDouble() * var;
			else
				data2[ind] = censorVal - 1 - ran.nextInt(var);
		}
		

		System.out.println("data1 = " + Arrays.toString(data1));
		System.out.println("data2 = " + Arrays.toString(data2));
		double[][] allData = new double[2][];
		allData[0] = data1;
		allData[1] = data2;
		
		//confirm this method creates the correct number above, below, equal the threshold
		int numAboveA = 0, numEqualA = 0;
		int numAboveB = 0, numEqualB = 0;
		
		for(int i =0; i < nA; i++){
			if(data1[i] > censorVal)
				numAboveA++;
			else if(data1[i] == censorVal)
				numEqualA++;
		}
		
		for(int i =0; i < nB; i++){
			if(data2[i] > censorVal)
				numAboveB++;
			else if(data2[i] == censorVal)
				numEqualB++;

		}
		assertEquals("confirming test data",numEqA,numEqualA,0);
		assertEquals("confirming test data",numEqB,numEqualB,0);
		assertEquals("confirming test data",numGrtrA,numAboveA,0);
		assertEquals("confirming test data",numGrtrB,numAboveB,0);

		return allData;
	}

	/**
	 * Ensures correctness for the Odds Ratio effect size test. An equation for this test is given in Arcuri2012. 
	 * As it appears that no R function exists that matches this equation and yet Astraiea is an implementation of this paper, 
	 * a comparison with R cannot be made as it is for the other methods. However, we provide the equation 
	 * that is used in method oddsRatioEquation(double[], double[]) where it can easily be compared
	 * against that found in Arcuri2012 and we confirm through unit testing that the results from the Astraiea 
	 * system matches the results of invoking oddsRatioEquation(double[], double[]). 
	 * @throws IOException 
	 * @throws SecurityException 
	 * 
	 */
	@Test
	public void oddsRatio() throws IOException {
		Random ran = new Random(0);
		
		boolean[][] data = convertToBools(this.generateCensoredData(0.8, 0.2, 0, 0, 10, 10, 20, 20, false, ran),10);//dataA passes more
		boolean[][] data0505 = convertToBools(this.generateCensoredData(0.5, 0.5, 0, 0, 10, 10, 20, 20, false, ran),10);//both pass same amount
		boolean[][] data0208 = convertToBools(this.generateCensoredData(0.2, 0.8, 0, 0, 10, 10, 20, 20, false, ran),10);//dataB passes more
		boolean[][] data050 = convertToBools(this.generateCensoredData(0.5, 0, 0, 0, 10, 10, 20, 20, false, ran),10);//one of the datasets (dataB) has 100% failure rate
		boolean[][] data005 = convertToBools(this.generateCensoredData(0, 0.5, 0, 0, 10, 10, 20, 20, false, ran),10);//dataB has 100% failure rate
		boolean[][] data105 = convertToBools(this.generateCensoredData(1, 0.5, 0, 0, 10, 10, 20, 20, false, ran),10);//100% pass rate
		boolean[][] dataEq01 = convertToBools(this.generateCensoredData(0.5, 0.5, 0.1, 0, 10, 10, 20, 20, false, ran),10);//proportion that pass for dataA includes 10% that are equal, not above the pass rate

		Layer1.setupLatexLoggers("reports/layer1/reportOddsRatio.tex");
		
		double r1 = ran.nextInt(50);//random pass and fail rate - for datasets of size 50
		double r2 = ran.nextInt(50);
		boolean[][] dataRan1 = convertToBools(this.generateCensoredData(r1/50.0, r2/50.0, 0, 0, 10, 10, 50, 50, false, ran),10);
		double r3 = ran.nextInt(50); //another with random pass and fail
		double r4 = ran.nextInt(50);
		boolean[][] dataRan2 = convertToBools(this.generateCensoredData(r3/50.0, r4/50.0, 0, 0, 10, 10, 50, 50, false, ran),10);
		double r5 = ran.nextInt(50);//different pass threshold (0 instead of 10)
		double r6 = ran.nextInt(50);//no reason why this should have any effect as the numbers of passes and fails remain the same. Just sanity checking nothing goes wrong with counting passes/fails
		boolean[][] dataCV0 = convertToBools(this.generateCensoredData(r5/50.0, r6/50.0, 0, 0, 0, 10, 50, 50, false, ran),0);
		double r7 = ran.nextInt(50);
		double r8 = ran.nextInt(50);//continuous data - no reason why this should have any effect as the numbers of passes and fails remain the same. Just sanity checking nothing goes wrong with counting passes/fails
		boolean[][] dataCont = convertToBools(this.generateCensoredData(r7/50.0, r8/50.0, 0, 0, 0, 10, 50, 50, true, ran),0);
		boolean[][] dataUneqN = convertToBools(this.generateCensoredData(0.2, 0.8, 0, 0, 10, 10, 20, 50, false, ran),10); //unequal sized data sets


		Result res = Layer1.compareCensored(data[0],data[1], 0.05, false);
		
		double expectedP = this.calcOddsRatio(16, 20, 4, 20);
		assertEquals("Effect size must be accurately using Odds Ratio", expectedP, res.getEffectSize(),0);
		assertTrue("dataA must be greater than dataB, according to the 'ord' field in 'res'", res.getOrder() == Ordering.GREATER);
		
		res = Layer1.compareCensored(data0505[0],data0505[1],0.05,false);
		assertTrue("Effect size must not be calculated as p value is not isSignificant()", Double.isNaN(res.getEffectSize()));

		res = Layer1.compareCensored(data0208[0],data0208[1],0.05,false);
		expectedP = this.calcOddsRatio(4, 20, 16, 20);
		assertEquals("Effect size must be accurately using Odds Ratio", expectedP, res.getEffectSize(),0);
		assertTrue("dataA must be less than dataB, according to the 'ord' field in 'res'", res.getOrder() == Ordering.LOWER);

		res = Layer1.compareCensored(data050[0],data050[1],0.05,false);
		expectedP = this.calcOddsRatio(10, 20, 0, 20);
		assertEquals("Effect size must be accurately using Odds Ratio", expectedP, res.getEffectSize(),0);
		assertTrue("dataA must be greater than dataB, according to the 'ord' field in 'res'", res.getOrder() == Ordering.GREATER);

		res = Layer1.compareCensored(data005[0],data005[1],0.05,false);
		expectedP = this.calcOddsRatio(0, 20, 10, 20);
		assertEquals("Effect size must be accurately using Odds Ratio", expectedP, res.getEffectSize(),0);
		assertTrue("dataA must be less than dataB, according to the 'ord' field in 'res'", res.getOrder() == Ordering.LOWER);

		res = Layer1.compareCensored(data105[0],data105[1],0.05,false);
		expectedP = this.calcOddsRatio(20, 20, 10, 20);
		assertEquals("Effect size must be accurately using Odds Ratio", expectedP, res.getEffectSize(),0);
		assertTrue("dataA must be greater than dataB, according to the 'ord' field in 'res'", res.getOrder() == Ordering.GREATER);

		res = Layer1.compareCensored(dataEq01[0],dataEq01[1],0.05,false);
		assertTrue("effect size must not be calculated as p value is not isSignificant()", Double.isNaN(res.getEffectSize()));

		res = Layer1.compareCensored(dataRan1[0],dataRan1[1],0.05,false);
		assertTrue("effect size must not be calculated as p value is not isSignificant()", Double.isNaN(res.getEffectSize()));

		res = Layer1.compareCensored(dataRan2[0],dataRan2[1],0.05,false);
		expectedP = this.calcOddsRatio(35,50,46,50);
		assertEquals("Effect size must be accurately using Odds Ratio", expectedP, res.getEffectSize(),0);
		assertTrue("dataA must be less than dataB, according to the 'ord' field in 'res'", res.getOrder() == Ordering.LOWER);

		res = Layer1.compareCensored(dataCV0[0],dataCV0[1],0.05,false);
		expectedP = this.calcOddsRatio(15,50,29,50);
		assertEquals("Effect size must be accurately using Odds Ratio", expectedP, res.getEffectSize(),0);
		assertTrue("dataA must be less than dataB, according to the 'ord' field in 'res'", res.getOrder() == Ordering.LOWER);

		res = Layer1.compareCensored(dataCont[0],dataCont[1],0.05,false);
		expectedP = this.calcOddsRatio(41,50,10,50);
		assertEquals("Effect size must be accurately using Odds Ratio", expectedP, res.getEffectSize(),0);
		assertTrue("dataA must be greater than dataB, according to the 'ord' field in 'res'", res.getOrder() == Ordering.GREATER);

		res = Layer1.compareCensored(dataUneqN[0],dataUneqN[1],0.05,false);
		expectedP = this.calcOddsRatio(4,20,40,50);
		assertEquals("Effect size must be accurately using Odds Ratio", expectedP, res.getEffectSize(),0);
		assertTrue("dataA must be less than dataB, according to the 'ord' field in 'res'", res.getOrder() == Ordering.LOWER);
	}
	
	/**
	 * Converts double data to booleans.
	 *  
	 * @param data input data in doubles
	 * @param thresh threshold for which >=thresh is a pass
	 * @return
	 */
	private boolean[][] convertToBools(double[][] data, int thresh) {
		boolean[][] result = new boolean[2][];
		result[0] = new boolean[data[0].length];
		result[1] = new boolean[data[1].length];

		for(int i =0; i < data[0].length; i++){
			result[0][i] = data[0][i] >= thresh;
		}
		for(int i =0; i < data[1].length; i++){
			result[1][i] = data[1][i] >= thresh;
		}
		return result;
	}

	/**
	 * An exact copy of the odds ratio equation in Arcuri2012, with p set to 0.5 (matching the example value given in that paper).
	 * The parameter names are used to match the paper.
	 * @param a number of positive results in dataA
	 * @param n total number of samples in dataA
	 * @param b number of positive results in dataB
	 * @param m total number of samples in dataB
	 * @return
	 */
	private double calcOddsRatio(int a, int n, int b, int m){
		final double p = 0.5;
		return ((a + p) / (n + p - a)) / ((b + p) / (m + p - b));
	}
	

	
	/**
	 * Confirms that p values when the Fisher test (For censored/dichotomous data)
	 *  is used match those generated from R with a margin of error no more than 10^-5
	 * @throws IOException 
	 * @throws SecurityException 
	 */
	@Test
	public void fisher() throws IOException {
		Layer1.setupLatexLoggers("reports/layer1/reportFisher.tex");

		Random ran = new Random(0);
		//same as odds ratio
		boolean[][] data = convertToBools(this.generateCensoredData(0.8, 0.2, 0, 0, 10, 10, 20, 20, false, ran),10);
		boolean[][] data0505 = convertToBools(this.generateCensoredData(0.5, 0.5, 0, 0, 10, 10, 20, 20, false, ran),10);
		boolean[][] data0208 = convertToBools(this.generateCensoredData(0.2, 0.8, 0, 0, 10, 10, 20, 20, false, ran),10);
		boolean[][] data050 = convertToBools(this.generateCensoredData(0.5, 0, 0, 0, 10, 10, 20, 20, false, ran),10);
		boolean[][] data005 = convertToBools(this.generateCensoredData(0, 0.5, 0, 0, 10, 10, 20, 20, false, ran),10);
		boolean[][] data105 = convertToBools(this.generateCensoredData(1, 0.5, 0, 0, 10, 10, 20, 20, false, ran),10);
		boolean[][] dataEq01 = convertToBools(this.generateCensoredData(0.5, 0.5, 0.1, 0, 10, 10, 20, 20, false, ran),10);
		double r1 = ran.nextInt(50);
		double r2 = ran.nextInt(50);
		boolean[][] dataRan1 = convertToBools(this.generateCensoredData(r1/50.0, r2/50.0, 0, 0, 10, 10, 50, 50, false, ran),10);
		double r3 = ran.nextInt(50);
		double r4 = ran.nextInt(50);
		boolean[][] dataRan2 = convertToBools(this.generateCensoredData(r3/50.0, r4/50.0, 0, 0, 10, 10, 50, 50, false, ran),10);
		double r5 = ran.nextInt(50);
		double r6 = ran.nextInt(50);
		boolean[][] dataCV0 = convertToBools(this.generateCensoredData(r5/50.0, r6/50.0, 0, 0, 0, 10, 50, 50, false, ran),0);
		double r7 = ran.nextInt(50);
		double r8 = ran.nextInt(50);
		boolean[][] dataCont = convertToBools(this.generateCensoredData(r7/50.0, r8/50.0, 0, 0, 0, 10, 50, 50, true, ran),0);
		boolean[][] dataUneqN = convertToBools(this.generateCensoredData(0.2, 0.8, 0, 0, 10, 10, 20, 50, false, ran),10);
		//testing large data sets as Arcuri2012 highlights accuracy issues when n >= 100
		double r9 = ran.nextInt(100);
		double r10 = ran.nextInt(100);
		boolean[][] dataN100 = convertToBools(this.generateCensoredData(r9/100.0, r10/100.0, 0, 0, 10, 10, 100, 100, false, ran),10);
		double r11 = ran.nextInt(200);
		double r12 = ran.nextInt(200);
		boolean[][] dataN200 = convertToBools(this.generateCensoredData(r11/200.0, r12/200.0, 0, 0, 10, 10, 200, 200, false, ran),10);
		
		Result res = Layer1.compareCensored(data[0],data[1],0.05,false);
		double expectedP = 3.599673667865366E-4;
		assertEquals("p value must be accurately using Fisher test, using R function fisher.test as an oracle", expectedP, res.getPValue(),0.00001);
		assertTrue("significance must be true", res.isSignificant());
		
		res = Layer1.compareCensored(data0505[0],data0505[1],0.05,false);
		expectedP = 0.9999999999999999;
		assertEquals("p value must be accurately using Fisher test, using R function fisher.test as an oracle", expectedP, res.getPValue(),0.00001);
		assertFalse("significance must be false", res.isSignificant());
		
		res = Layer1.compareCensored(data0208[0],data0208[1],0.05,false);
		expectedP = 3.599673667865366E-4;
		assertEquals("p value must be accurately using Fisher test, using R function fisher.test as an oracle", expectedP, res.getPValue(),0.00001);
		assertTrue("significance must be true", res.isSignificant());
		
		res = Layer1.compareCensored(data050[0],data050[1],0.05,false);
		expectedP = 4.3591979075850175E-4;
		assertEquals("p value must be accurately using Fisher test, using R function fisher.test as an oracle", expectedP, res.getPValue(),0.00001);
		assertTrue("significance must be true", res.isSignificant());

		res = Layer1.compareCensored(data005[0],data005[1],0.05,false);
		expectedP = 4.3591979075850175E-4;
		assertEquals("p value must be accurately using Fisher test, using R function fisher.test as an oracle", expectedP, res.getPValue(),0.00001);
		assertTrue("significance must be true", res.isSignificant());
		
		res = Layer1.compareCensored(data105[0],data105[1],0.05,false);
		expectedP = 4.3591979075850175E-4;
		assertEquals("p value must be accurately using Fisher test, using R function fisher.test as an oracle", expectedP, res.getPValue(),0.00001);
		assertTrue("significance must be true", res.isSignificant());
		
		res = Layer1.compareCensored(dataEq01[0],dataEq01[1],0.05,false);
		expectedP = 0.7511863074565593;
		assertEquals("p value must be accurately using Fisher test, using R function fisher.test as an oracle", expectedP, res.getPValue(),0.00001);
		assertFalse("significance must be false", res.isSignificant());
		
		res = Layer1.compareCensored(dataRan1[0],dataRan1[1],0.05,false);
		expectedP = 1;
		assertEquals("p value must be accurately using Fisher test, using R function fisher.test as an oracle", expectedP, res.getPValue(),0.00001);
		assertFalse("significance must be false", res.isSignificant());
		
		res = Layer1.compareCensored(dataRan2[0],dataRan2[1],0.05,false);
		expectedP = 0.009488575318861166;
		assertEquals("p value must be accurately using Fisher test, using R function fisher.test as an oracle", expectedP, res.getPValue(),0.00001);
		assertTrue("significance must be true", res.isSignificant());

		res = Layer1.compareCensored(dataCV0[0],dataCV0[1],0.05,false);
		expectedP = 0.008471290461855274;
		assertEquals("p value must be accurately using Fisher test, using R function fisher.test as an oracle", expectedP, res.getPValue(),0.00001);
		assertTrue("significance must be true", res.isSignificant());
		
		res = Layer1.compareCensored(dataCont[0],dataCont[1],0.05,false);
		expectedP = 2.577040463093276E-6;
		assertEquals("p value must be accurately using Fisher test, using R function fisher.test as an oracle", expectedP, res.getPValue(),0.00001);
		assertTrue("significance must be true", res.isSignificant());

		res = Layer1.compareCensored(dataUneqN[0],dataUneqN[1],0.05,false);
		expectedP = 4.719176169198741E-6;
		assertEquals("p value must be accurately using Fisher test, using R function fisher.test as an oracle", expectedP, res.getPValue(),0.00001);
		assertTrue("significance must be true", res.isSignificant());
		
		res = Layer1.compareCensored(dataN100[0],dataN100[1],0.05,false);
		expectedP = 5.808e-06;
		assertEquals("p value must be accurately using Fisher test, using R function fisher.test as an oracle", expectedP, res.getPValue(),0.00001);
		assertTrue("significance must be true", res.isSignificant());
		
		res = Layer1.compareCensored(dataN200[0],dataN200[1],0.05,false);
		expectedP = 0.0006017;
		assertEquals("p value must be accurately using Fisher test, using R function fisher.test as an oracle", expectedP, res.getPValue(),0.00001);
		assertTrue("significance must be true", res.isSignificant());
	}
	
	/**
	 * Confirms that p values when the McNemar test (For censored/dichotomous data which is also paired)
	 *  is used match those generated from R with a margin of error no more than 10^-4.
	 * @throws IOException 
	 * @throws SecurityException 
	 */
	@Test
	public void mcNemar() throws IOException {
		Layer1.setupLatexLoggers("reports/layer1/reportMcNemar.tex");

		Random ran = new Random(0);

		boolean[][] data = convertToBools(this.generateCensoredData(0.8, 0.2, 0, 0, 10, 10, 20, 20, false, ran),10);
		boolean[][] data0208 = convertToBools(this.generateCensoredData(0.2, 0.8, 0, 0, 10, 10, 20, 20, false, ran),10);
		boolean[][] data050 = convertToBools(this.generateCensoredData(0.5, 0, 0, 0, 10, 10, 20, 20, false, ran),10);
		boolean[][] data005 = convertToBools(this.generateCensoredData(0, 0.5, 0, 0, 10, 10, 20, 20, false, ran),10);
		boolean[][] data105 = convertToBools(this.generateCensoredData(1, 0.5, 0, 0, 10, 10, 20, 20, false, ran),10);
		boolean[][] dataEq01 = convertToBools(this.generateCensoredData(0.5, 0.5, 0.1, 0, 10, 10, 20, 20, false, ran),10);
		double r1 = ran.nextInt(50);
		double r2 = ran.nextInt(50);
		boolean[][] dataRan1 = convertToBools(this.generateCensoredData(r1/50.0, r2/50.0, 0, 0, 10, 10, 50, 50, false, ran),10);
		double r3 = ran.nextInt(50);
		double r4 = ran.nextInt(50);
		boolean[][] dataRan2 = convertToBools(this.generateCensoredData(r3/50.0, r4/50.0, 0, 0, 10, 10, 50, 50, false, ran),10);
		double r5 = ran.nextInt(50);
		double r6 = ran.nextInt(50);
		boolean[][] dataCV0 = convertToBools(this.generateCensoredData(r5/50.0, r6/50.0, 0, 0, 0, 10, 50, 50, false, ran),0);
		double r7 = ran.nextInt(50);
		double r8 = ran.nextInt(50);
		boolean[][] dataCont = convertToBools(this.generateCensoredData(r7/50.0, r8/50.0, 0, 0, 0, 10, 50, 50, true, ran),0);
		//testing large data sets as Arcuri2012 highlights accuracy issues when n >= 100
		double r9 = ran.nextInt(100);
		double r10 = ran.nextInt(100);
		boolean[][] dataN100 = convertToBools(this.generateCensoredData(r9/100.0, r10/100.0, 0, 0, 10, 10, 100, 100, false, ran),10);
		double r11 = ran.nextInt(200);
		double r12 = ran.nextInt(200);
		boolean[][] dataN200 = convertToBools(this.generateCensoredData(r11/200.0, r12/200.0, 0, 0, 10, 10, 200, 200, false, ran),10);
		
		Result res = Layer1.compareCensored(data[0],data[1],0.05,true);
		double expectedP = 0.00596;
		assertEquals("p value must be accurately using McNemar test, using R function mcnemar.test as an oracle", expectedP, res.getPValue(),0.0001);
		assertTrue("significance must be true", res.isSignificant());
		
		res = Layer1.compareCensored(data0208[0],data0208[1],0.05,true);
		expectedP = 0.009522;
		assertEquals("p value must be accurately using McNemar test, using R function mcnemar.test as an oracle", expectedP, res.getPValue(),0.0001);
		assertTrue("significance must be true", res.isSignificant());
		
		res = Layer1.compareCensored(data050[0],data050[1],0.05,true);
		expectedP = 0.004427;
		assertEquals("p value must be accurately using McNemar test, using R function mcnemar.test as an oracle", expectedP, res.getPValue(),0.0001);
		assertTrue("significance must be true", res.isSignificant());

		res = Layer1.compareCensored(data005[0],data005[1],0.05,true);
		expectedP = 0.004427;
		assertEquals("p value must be accurately using McNemar test, using R function mcnemar.test as an oracle", expectedP, res.getPValue(),0.0001);
		assertTrue("significance must be true", res.isSignificant());
		
		res = Layer1.compareCensored(data105[0],data105[1],0.05,true);
		expectedP = 0.004427;
		assertEquals("p value must be accurately using McNemar test, using R function mcnemar.test as an oracle", expectedP, res.getPValue(),0.0001);
		assertTrue("significance must be true", res.isSignificant());
		
		res = Layer1.compareCensored(dataEq01[0],dataEq01[1],0.05,true);
		expectedP = 0.7518;
		assertEquals("p value must be accurately using McNemar test, using R function mcnemar.test as an oracle", expectedP, res.getPValue(),0.0001);
		assertFalse("significance must be false", res.isSignificant());
		
		res = Layer1.compareCensored(dataRan1[0],dataRan1[1],0.05,true);
		expectedP = 0.0005202;
		assertEquals("p value must be accurately using McNemar test, using R function mcnemar.test as an oracle", expectedP, res.getPValue(),0.0001);
		assertTrue("significance must be true", res.isSignificant());
		
		res = Layer1.compareCensored(dataRan2[0],dataRan2[1],0.05,true);
		expectedP = 0.4042;
		assertEquals("p value must be accurately using McNemar test, using R function mcnemar.test as an oracle", expectedP, res.getPValue(),0.0001);
		assertFalse("significance must be false", res.isSignificant());

		res = Layer1.compareCensored(dataCV0[0],dataCV0[1],0.05,true);
		expectedP = 0.3768;
		assertEquals("p value must be accurately using McNemar test, using R function mcnemar.test as an oracle", expectedP, res.getPValue(),0.0001);
		assertFalse("significance must be false", res.isSignificant());
		
		res = Layer1.compareCensored(dataCont[0],dataCont[1],0.05,true);
		expectedP = 0.08086;
		assertEquals("p value must be accurately using McNemar test, using R function mcnemar.test as an oracle", expectedP, res.getPValue(),0.0001);
		assertFalse("significance must be false", res.isSignificant());

		res = Layer1.compareCensored(dataN100[0],dataN100[1],0.05,true);
		expectedP = 0.0005226;
		assertEquals("p value must be accurately using McNemar test, using R function mcnemar.test as an oracle", expectedP, res.getPValue(),0.0001);
		assertTrue("significance must be true", res.isSignificant());
		
		res = Layer1.compareCensored(dataN200[0],dataN200[1],0.05,true);
		expectedP = 1.0;
		assertEquals("p value must be accurately using McNemar test, using R function mcnemar.test as an oracle", expectedP, res.getPValue(),0.0001);
		assertFalse("significance must be true", res.isSignificant());
	}
}

// End ///////////////////////////////////////////////////////////////

