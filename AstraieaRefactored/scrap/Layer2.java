package astraiea.layer2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Logger;

import org.apache.commons.math3.util.Pair;

import astraiea.Result;
import astraiea.layer1.Layer1;
import astraiea.layer1.varghaDelaney.VDmod;
import astraiea.layer2.generators.Generator;
import astraiea.layer2.generators.GeneratorOutput;
import astraiea.layer2.generators.MultipleArtefactOutput;
import astraiea.layer2.generators.Timeseries;
import astraiea.layer2.multipleExperiments.SetOfComparisons;
import astraiea.layer2.multipleExperiments.SetOfExperiments;
import astraiea.layer2.strategies.*;
import astraiea.output.LaTeXLogFormatter;
import astraiea.output.Report;
import astraiea.util.DataUtil;
import astraiea.util.MersenneTwister;

/**
 * Layer2 organises the running of two or more metaheuristics. 
 * It then passes the results onto Layer1 for analysing.
 * 
 * @author Geoffrey Neumann, Jerry Swan
 *
 */
public final class Layer2 {
	
	///////////////////////////////USER INTERFACE METHODS///////////////////////////////
	
	/**
	 * Running with censoring with datapoint generators without multiple artefacts.
	 * See runAllImpl for an explanation of the parameters.
	 * 
	 * @param gens
	 * @param significanceThreshold
	 * @param paired
	 * @param incr
	 * @param random
	 * @return
	 */
	public static<T extends GeneratorOutput> List<ResultSet> runCensored(
			SetOfComparisons<GeneratorOutput> gens,
			double significanceThreshold,
			boolean paired,
			IncrementingStrategy incr,
			Random random) {
		return runAllImpl(gens,significanceThreshold, false,paired,new CensoringStrategy(true),incr,random,null);
	}

	/**
	 * Running without censoring with datapoint or timeseries generators without multiple artefacts.
	 * See runAllImpl for an explanation of the parameters.
	 * 
	 * @param gens
	 * @param significanceThreshold
	 * @param brunnerMunzel
	 * @param paired
	 * @param incr
	 * @param random
	 * @return
	 */
	public static<T extends GeneratorOutput> List<ResultSet> run(
			SetOfComparisons<T> gens,
			double significanceThreshold, 
			boolean brunnerMunzel, 
			boolean paired,
			IncrementingStrategy incr,			
			Random random) {
		return runAllImpl(gens,significanceThreshold,brunnerMunzel,paired,new CensoringStrategy(false),incr,random,null);
	}
	
	/** Running with or without censoring with datapoint or timeseries generators with multiple artefacts.
	 * See runAllImpl for an explanation of the parameters.
	 * 
	 * @param gens
	 * @param significanceThreshold
	 * @param brunnerMunzel
	 * @param incr
	 * @param artefactRepeats
	 * @param random
	 * @return
	 */
	public static<T extends GeneratorOutput> List<ResultSet> runArtefacts(
			SetOfComparisons<MultipleArtefactOutput<T>> gens,
			double significanceThreshold, 
			IncrementingStrategy incr,
			Random random,
			boolean censoring) {
		return runAllImpl(gens,significanceThreshold,false,true,new CensoringStrategy(censoring),incr,random,null);
	}


	/**
	 * Running with timeseries without multiple artefacts.
	 * See runAllImpl for an explanation of the parameters.
	 * 
	 * @param gens
	 * @param significanceThreshold
	 * @param brunnerMunzel
	 * @param paired
	 * @param cens
	 * @param incr
	 * @param random
	 * @return
	 */
	public static<T extends GeneratorOutput> List<ResultSet> run(
			SetOfComparisons<Timeseries<T>> gens,
			double significanceThreshold, 
			boolean brunnerMunzel, 
			boolean paired,
			CensoringStrategy cens,
			IncrementingStrategy incr,
			Random random) {
		return runAllImpl(gens,significanceThreshold,brunnerMunzel,paired,cens,incr,random,null);
	}
	
	/**
	 * Running with timeseries with multiple artefacts.
	 * See runAllImpl for an explanation of the parameters.
	 * 
	 * @param gens
	 * @param significanceThreshold
	 * @param brunnerMunzel
	 * @param paired
	 * @param cens
	 * @param incr
	 * @param random
	 * @return
	 */
	public static<T extends GeneratorOutput> List<ResultSet> runArtefacts(
			SetOfComparisons<MultipleArtefactOutput<Timeseries<T>>> gens,
			double significanceThreshold, 
			boolean brunnerMunzel, 
			boolean paired,
			CensoringStrategy cens,
			IncrementingStrategy incr,
			Random random) {
		return runAllImpl(gens,significanceThreshold,brunnerMunzel,paired,cens,incr,random,null);
	}

	
	//WITH VDMod
	
	/**
	 * Running with censoring with datapoint generators without multiple artefacts.
	 * See runAllImpl for an explanation of the parameters.
	 * 
	 * @param gens
	 * @param significanceThreshold
	 * @param paired
	 * @param incr
	 * @param random
	 * @return
	 */
	public static<T extends GeneratorOutput> List<ResultSet> runCensored(
			SetOfComparisons<T> gens,
			double significanceThreshold,
			boolean paired,
			IncrementingStrategy incr,
			Random random,
			VDmod vdmod) {
		return runAllImpl(gens,significanceThreshold, false,paired,new CensoringStrategy(true),incr,random,vdmod);
	}
	
	/** Running without censoring with datapoint generators with multiple artefacts.
	 * See runAllImpl for an explanation of the parameters.
	 * 
	 * @param gens
	 * @param significanceThreshold
	 * @param brunnerMunzel
	 * @param incr
	 * @param artefactRepeats
	 * @param random
	 * @return
	 */
	public static<T extends GeneratorOutput> List<ResultSet> runArtefacts(
			SetOfComparisons<MultipleArtefactOutput<T>> gens,
			double significanceThreshold, 
			boolean brunnerMunzel, 
			IncrementingStrategy incr,
			Random random,
			VDmod vdmod) {
		return runAllImpl(gens,significanceThreshold,brunnerMunzel,true,new CensoringStrategy(false),incr,random,vdmod);
	}
	
	/** Running with or without censoring with datapoint generators with multiple artefacts.
	 * See runAllImpl for an explanation of the parameters.
	 * 
	 * @param gens
	 * @param significanceThreshold
	 * @param brunnerMunzel
	 * @param incr
	 * @param artefactRepeats
	 * @param random
	 * @return
	 */
	public static<T extends GeneratorOutput> List<ResultSet> runArtefacts(
			SetOfComparisons<MultipleArtefactOutput<T>> gens,
			double significanceThreshold, 
			IncrementingStrategy incr,
			Random random,
			boolean censoring,
			VDmod vdmod) {
		return runAllImpl(gens,significanceThreshold,false,true,new CensoringStrategy(censoring),incr,random,null);
	}

	
	/**
	 * Running with timeseries without multiple artefacts.
	 * See runAllImpl for an explanation of the parameters.
	 * 
	 * @param gens
	 * @param significanceThreshold
	 * @param brunnerMunzel
	 * @param paired
	 * @param cens
	 * @param incr
	 * @param random
	 * @return
	 */
	public static<T extends GeneratorOutput> List<ResultSet> run(
			SetOfComparisons<Timeseries<T>> gens,
			double significanceThreshold, 
			boolean brunnerMunzel, 
			boolean paired,
			CensoringStrategy cens,
			IncrementingStrategy incr,
			Random random,
			VDmod vdmod) {
		return runAllImpl(gens,significanceThreshold,brunnerMunzel,paired,cens,incr,random,vdmod);
	}
	
	/**
	 * Running with timeseries with multiple artefacts.
	 * See runAllImpl for an explanation of the parameters.
	 * 
	 * @param gens
	 * @param significanceThreshold
	 * @param brunnerMunzel
	 * @param paired
	 * @param cens
	 * @param incr
	 * @param random
	 * @return
	 */
	public static<T extends GeneratorOutput> List<ResultSet> runArtefacts(
			SetOfComparisons<MultipleArtefactOutput<Timeseries<T>>> gens,
			double significanceThreshold, 
			boolean brunnerMunzel, 
			boolean paired,
			CensoringStrategy cens,
			IncrementingStrategy incr,
			Random random,
			VDmod vdmod) {
		return runAllImpl(gens,significanceThreshold,brunnerMunzel,paired,cens,incr,random,vdmod);
	}

	
	/**Compares a set of data point generators.
	 * @param <T>
	 * 
	 * @param gens set of generators being compared
	 * @param significanceThreshold 
	 * @param brunnerMunzel if brunner munzel test is to be used
	 * @param paired if data is paired
	 * @param cens how data should be censored, if at all
	 * @param incr how runs should be repeated, whether only for a fixed amount of runs or whether it should increment
	 * @param artefactRepeats if using multiple artefacts then each run constitutes a single artefact and this specifies how many repeats should be done with that artefact
	 * @param random random number generator for running the generators
	 * @param vdmod allows modification of the comparison function used in Vargha Delaney
	 * @return object holding p value, effect size etc. for every comparison for which results are required
	 */
	private static <T extends GeneratorOutput> List<ResultSet> runAllImpl(
			SetOfComparisons<T> gens,
			double significanceThreshold, 
			boolean brunnerMunzel, 
			boolean paired, 
			CensoringStrategy cens,
			IncrementingStrategy incr,
			Random random, 
			VDmod vdmod) {
		
		Report rep = new Report();
		
		//get a set of generators - one for each algorithm compared
		List<SetOfExperiments<T>> gensList = gens.getAllGenerators();
		int numGens = gensList.size();
		
		//maps a name (String) identifying the generator against the results (List<...>)
		Hashtable<SetOfExperiments<T>,List<T>> resultHash = new Hashtable<SetOfExperiments<T>,List<T>>();
		
		//get a different set of seeds for each generator unless paired
		long[][] seeds = generateSeeds(incr.getMaxRuns(),paired,random,numGens);
		
		//run each generator for the initial amount of runs and put the results into the hash
		ListIterator<SetOfExperiments<T>> gensIter = gensList.listIterator();
		int gen =0;
		
		//to be Output to LOGGER
		int numArts = 1;
		boolean timeSeries = false;
		int censCounter = 0;
		//end output
		
		while(gensIter.hasNext()){
			SetOfExperiments<T> nextGen = gensIter.next();
			List<T> listOfRes = nextGen.run(incr.getMinRuns(), seeds[gen]);
			
			//For Output to LOGGER
			if(listOfRes.get(0) instanceof MultipleArtefactOutput)
				numArts = ((MultipleArtefactOutput<?>)listOfRes.get(0)).getRepeats();
			timeSeries = listOfRes.get(0) instanceof Timeseries;
			//end output
			
			resultHash.put(nextGen,listOfRes);

			gen++;
		}
		//gets tests organised into the pairs which are compared against each other
		//depending on how gens is set up this may mean every generator compared to each other or one generator compared to all
		List<Pair<SetOfExperiments<T>, SetOfExperiments<T>>> testPairs = gens.getTestPairs();
		ListIterator<Pair<SetOfExperiments<T>, SetOfExperiments<T>>> pairIter = testPairs.listIterator();
		List<ResultSet> results = new ArrayList<ResultSet>();
		
		rep.printPreTestOutput(significanceThreshold,brunnerMunzel,paired,incr.getMinRuns(),incr.getMaxRuns(),cens,timeSeries,numArts);			
		
		//compare every pair
		while(pairIter.hasNext()){
			//get results and details of each member of the next pair
			Pair<SetOfExperiments<T>, SetOfExperiments<T>> pair = pairIter.next();
			SetOfExperiments<T> gen1 = pair.getFirst();
			SetOfExperiments<T> gen2 = pair.getSecond();
			String gen1Name = gen1.getName();
			String gen2Name = gen2.getName();
			List<T> results1 = resultHash.get(gen1);
			List<T> results2 = resultHash.get(gen2);
			
			Result res = null;
			if(cens.isCensoring()){
				censCounter = 0;
				while(res == null || !res.isSignificant() && cens.hasMoreSteps(censCounter)){
					if(cens.usingTimesToPass(censCounter)){
						if(paired)
							res = Layer1.comparePaired(cens.getTimesToPass(results1), cens.getTimesToPass(results2), significanceThreshold, gen1Name, gen2Name, vdmod);
						else
							res = Layer1.compare(cens.getTimesToPass(results1), cens.getTimesToPass(results2), significanceThreshold, brunnerMunzel, random, gen1Name, gen2Name, vdmod);
					}
					else
						res = Layer1.compareCensored(cens.censor(results1, censCounter), cens.censor(results2, censCounter), significanceThreshold, gen1Name, gen2Name, paired);
					censCounter++;
				}			
			}
			else
				res = Layer1.compare(results1, results2, significanceThreshold, brunnerMunzel, paired, gen1Name,gen2Name,random, vdmod);
			
			//use incrementing strategy to obtain number of runs to add (if any)
			int[] incrNum;
			int[] runsSoFar = new int[]{results1.size(), results2.size()};
			boolean finished;
			do{
				finished = true;
				incrNum = incr.getNextIncrement(results1, results2, res.getPValue(), significanceThreshold);
				
				if(incrNum[0] > 0 || incrNum[1] > 0){
					//carry out and store results from extra n runs using seeds from the next n slots in our seed array
					long[] curSeeds1 = Arrays.copyOfRange(seeds[gens.getIndexOf(gen1)],runsSoFar[0],runsSoFar[0] + incrNum[0]);
					long[] curSeeds2 = Arrays.copyOfRange(seeds[gens.getIndexOf(gen2)],runsSoFar[1],runsSoFar[1] + incrNum[1]);
					gen1.run(incrNum[0], curSeeds1);
					gen2.run(incrNum[1], curSeeds2);
					
					if(incr.runPVal()){ //if should run the p value test again. some incrementing strategies use alternatives to simple p value tests so might not be needed
						if(cens.isCensoring()){
							censCounter = 0;
							while(res == null || !res.isSignificant() && cens.hasMoreSteps(censCounter)){
								if(cens.usingTimesToPass(censCounter)){
									if(paired)
										res = Layer1.comparePaired(cens.getTimesToPass(results1), cens.getTimesToPass(results2), significanceThreshold, gen1Name, gen2Name, vdmod);
									else
										res = Layer1.compare(cens.getTimesToPass(results1), cens.getTimesToPass(results2), significanceThreshold, brunnerMunzel, random, gen1Name, gen2Name, vdmod);
								}
								else
									res = Layer1.compareCensored(cens.censor(results1, censCounter), cens.censor(results2, censCounter), significanceThreshold, gen1Name, gen2Name, paired);
								censCounter++;
							}	
						}	
						else
							res = Layer1.compare(results1, results2, significanceThreshold, brunnerMunzel, paired, gen1Name,gen2Name,random, vdmod);
					}
					finished = false;
					runsSoFar[0] += incrNum[0];
					runsSoFar[1] += incrNum[1];

				}
			}while(!finished);
			//as incrNum == 0 now we've either won or lost or incrementing isn't happening, so report results

			results.add(new ResultSet(gen1Name,gen2Name,res));
			
			//FIXME - as above, runsSoFar[0] is with assumption both samples are the same size
			rep.printPostTestOutput(runsSoFar[0] > incr.getMinRuns() || runsSoFar[1] > incr.getMinRuns(), 
					runsSoFar[0], res, cens, gen1Name, gen2Name, censCounter);
		}

		return gens.multipleTestAdjustment(results);
	}





	/**Generate random seeds.
	 * 
	 * @param numRuns total number of runs or number of artefacts
	 * @param paired if paired tests are carried out
	 * @param random random number generator
	 * @param artefactRepeats if multiple artefacts are used
	 * @return 2d array with one row of seeds for each generator
	 */
	private static long[][] generateSeeds(int numRuns, boolean paired, Random random, int numGens) {
		long[][] seeds = new long[numGens][numRuns];
		
		for( int i=0; i<numRuns; ++i ){
			long seed = random.nextLong();
			for(int i2 = 0; i2 < numGens; i2++){
				seeds[i2][i] = seed;
				if(!paired && i2 < numGens - 1)
					seed = random.nextLong();
			}
		}
		return seeds;
	}	
}		

// End ///////////////////////////////////////////////////////////////
