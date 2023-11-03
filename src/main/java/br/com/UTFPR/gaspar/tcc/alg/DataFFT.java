package br.com.UTFPR.gaspar.tcc.alg;

import org.apache.commons.math4.transform.FastFourierTransform;
import org.apache.commons.numbers.complex.Complex;

public class DataFFT {

    public static Complex[] transformArray(double[] array) {
        Complex[] transformedArray;
        FastFourierTransform fastFourierTransform = new FastFourierTransform(FastFourierTransform.Norm.STD);
        transformedArray = fastFourierTransform.apply(array);

        return transformedArray;
    }

}

