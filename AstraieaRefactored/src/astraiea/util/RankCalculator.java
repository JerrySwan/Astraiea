package astraiea.util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Ranks data, taking into account of ties, for use in tests such as Wilcoxon and Brunner Munzel
 * 
 * @author Geoffrey Neumann
 *
 */
public final class RankCalculator {
	
	// input data
	private double[] dataA;
	private double[] dataB;
	
	/** length of both datasets combined */
	private double totLength;
	
	/** 
	 * maps to elements in the combined rank array (combinedArr) 
	 * specifying if they came from dataA (true) or dataB (false)
	 */
	private boolean[] isFromA;
	
	/** ranks of all data */
	private double[] combinedArr;
	
	/** ranks of dataA when it is ranked with dataB */
	private double[] rankBothA;
	
	// as above - vice versa
	private double[] rankBothB;
	
	//indexes with dataA and dataB
	private int iA = 0;
	private int iB = 0;
	
	/** ranks of dataA when it is ranked on its own */
	private double[] rankAloneA;
	
	// as above - vice versa
	private double[] rankAloneB;
	
	/** for each tied value, each item in this array is the quantity of that tied value */
	private List<Integer> ties = new ArrayList<Integer>();
	
	/** rank a data array for cases in which only one data array is being ranked */
	private double [] rankOneSample;

	///////////////////////////////
	
	/**
	 * Which entity are we currently ranking (either data array, 
	 * both data arrays combined or just one data array if there is only one)
	 */
	enum WhichRank{both, dataA, dataB, oneSample};
	
	public RankCalculator(double[] dataA, double[] dataB){
		//calculate ranks for copies so that original arrays don't get reordered
		calcRanks(Arrays.copyOf(dataA,dataA.length),Arrays.copyOf(dataB,dataB.length));
	}

	public RankCalculator(double[] data) {
		rankOneSample = new double[data.length];
		Arrays.sort(data);
		splitTiesAndRank(data,WhichRank.oneSample);
	}

	///////////////////////////////
	
	public List<Integer> getTies() { return ties; }

	public double[] getRanks() { return rankOneSample; }

	public double[] getCombinedRanks(int which){
		if(which == 0)
			return rankBothA;
		else
			return rankBothB;
	}

	public double[] getSeparateRanks(int which){
		if(which == 0)
			return rankAloneA;
		else
			return rankAloneB;
	}
	
	///////////////////////////////
	
	/**
	 * The main ranking method
	 * 
	 * @param dataA
	 * @param dataB
	 */
	private void calcRanks(double[] dataA, double[] dataB){
		this.dataA = dataA;
		this.dataB = dataB;
		order();
		rankBothA = new double[dataA.length];
		rankBothB = new double[dataB.length];
		rankAloneA = new double[dataA.length];
		rankAloneB = new double[dataB.length];
		splitTiesAndRank(combinedArr,WhichRank.both);//produce an array with both arrays ranked together
		splitTiesAndRank(this.dataA,WhichRank.dataA);//and separately
		splitTiesAndRank(this.dataB,WhichRank.dataB);
	}
	

	/**
	 * Produce an array of ranks, with ties.
	 * 
	 * @param inArr
	 * @param which which array to produce - for either dataA, dataB, for both (combinedArr) or in the case in which only one set of data is being ranked (rankOneSample)
	 */
	private void splitTiesAndRank(double[] inArr, WhichRank which) {
		ties.clear();
		for(int i =1; i <= inArr.length; i++){
			double rank = i;
			int iTie = i + 1;
			while(iTie < inArr.length && inArr[iTie - 1] == inArr[i - 1]){ //step forward from current index and count the number of ties ahead
				iTie++;
			}
			if(iTie - i > 1){ //if there are ties
				ties.add(iTie - i);
				rank = i + ((((double)iTie - 1) - (double)i)/2.0);
				for(;i < iTie; i++){
					addRank(rank, i - 1, which);
				}
				i--;
			}
			else{
				addRank(rank, i - 1, which);
			}
		}		
	}
	
	/**
	 * Adds a rank to the appropriate array
	 * 
	 * @param rank
	 * @param i
	 * @param which
	 */
	private void addRank(double rank, int i, WhichRank which) {
		if(which == WhichRank.both){
			if(isFromA[i]){
				rankBothA[iA] = rank;
				iA++;
			}
			else{
				rankBothB[iB] = rank;
				iB++;
			}
		}
		else if(which == WhichRank.dataA)
			rankAloneA[i] = rank;
		else if(which == WhichRank.dataB)
			rankAloneB[i] = rank;
		else
			rankOneSample[i] = rank;
	}

	/**
	 * Sort both arrays into ascending order and produce a combined array which contains data from both arrays sorted.
	 * 
	 */
	private void order() {
		//sort separately
		Arrays.sort(dataA);
		Arrays.sort(dataB);
		int lengthA = dataA.length;
		int lengthB = dataB.length;
		totLength = lengthA + lengthB;
		isFromA = new boolean[(int)totLength];
		combinedArr = new double[(int)totLength];
		int iB = 0;
		int iBoth = 0;
		//sort into combined array
		for(int iA = 0; iA < lengthA; iA++){
			while(iB< lengthB && dataB[iB] < dataA[iA]){
				isFromA[iBoth] = false;
				combinedArr[iBoth] = dataB[iB];
				iB++;
				iBoth++;
			}
			isFromA[iBoth] = true;
			combinedArr[iBoth] = dataA[iA];
			iBoth++;
		}
		
	}
}

// End ///////////////////////////////////////////////////////////////


