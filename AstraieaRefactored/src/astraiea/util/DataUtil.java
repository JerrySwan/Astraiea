package astraiea.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.util.Pair;

import com.google.common.primitives.Doubles;

/**Utility class for examining and manipulating and examining data sets.
 * 
 * @author Geoffrey
 *
 */
public class DataUtil {

	/**
	 * Determines whether there are ties (duplicate values) within a data set, represented as an array of doubles.
	 * 
	 * @param data
	 * @return
	 */
	public static boolean hasTiesOrZeros(double[] data) {
		
		Set< Double > asSet = new HashSet< Double >();
		asSet.addAll( Doubles.asList( data ) );		
		
		return asSet.size() < data.length || asSet.contains( 0.0 );
	}

	/**
	 * Determines whether there are ties (duplicate values) either between two data sets or within one of the data sets.
	 * Each data set is an array of doubles.
	 * 
	 * @param data1
	 * @param data2
	 * @return
	 */
	public static boolean hasTiesOrZeros(double[] data1,double[] data2) {
		
		Set< Double > asSet = new HashSet< Double >();
		asSet.addAll( Doubles.asList( data1 ) );
		asSet.addAll( Doubles.asList( data2 ) );		
		
		return asSet.size() < data1.length + data2.length || asSet.contains( 0.0 );
	}
	
	/**
	 * Converts an input array to an output array with no zero values.
	 * 
	 * @param dataSub
	 * @return
	 */
	public static double [] removeZeros(double[] dataSub) {
		List<Double> noZeros = new ArrayList<Double>();
		for(int i =0; i < dataSub.length; i++){
			if( dataSub[i] != 0)
				noZeros.add(dataSub[i]);
		}

		double[] newArr = new double[noZeros.size()];
		for(int i=0; i<noZeros.size(); ++i )
			newArr[i] = noZeros.get(i);

		return newArr;
	}

	public static double getMedian(double[] vals) {
		double len = vals.length;
		Arrays.sort(vals);
		if(len % 2 == 0)
			return vals[(int)(len/2) - 1] + ((vals[(int)(len/2)] - vals[(int)((len/2) - 1)]) / 2);
		else
			return vals[(int)(Math.floor(len/2))];
	}



}

// End ///////////////////////////////////////////////////////////////

