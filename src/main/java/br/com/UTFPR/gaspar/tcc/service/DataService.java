package br.com.UTFPR.gaspar.tcc.service;

import br.com.UTFPR.gaspar.tcc.alg.DataFFT;
import br.com.UTFPR.gaspar.tcc.dto.DataDTO;
import br.com.UTFPR.gaspar.tcc.entity.Data;
import br.com.UTFPR.gaspar.tcc.repository.DataRepository;
import org.apache.commons.numbers.complex.Complex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataService {

    private static final int SAMPLE_SIZE = 128;
    private static final double AMOSTRAS = 128.0;
    private static final double T_CICLO = 1.0 / 60;

    //TODO PERSISTENCIA
    @Autowired
    private DataRepository dataRepository;

    public Data transformadaTensao(DataDTO dataDTO) {
        Data data = new Data();

        double[] tensao_2ciclo = new double[SAMPLE_SIZE];
        double[] corrente_2ciclo = new double[SAMPLE_SIZE];
        fillSecondCycle(dataDTO.getTensao(), dataDTO.getCorrente(), tensao_2ciclo, corrente_2ciclo);

        Complex[] fft_tensao = DataFFT.transformArray(tensao_2ciclo);
        Complex[] fft_corrente = DataFFT.transformArray(corrente_2ciclo);

        double[] modulo_tensao = calculateModulo(fft_tensao);
        double[] modulo_corrente = calculateModulo(fft_corrente);

        double[] fases_tensao = calculatePhases(fft_tensao, modulo_tensao);
        double[] fases_corrente = calculatePhases(fft_corrente, modulo_corrente);
        double[] fi = calculateFi(fases_tensao, fases_corrente);

        data.setTensao(dataDTO.getTensao());
        data.setCorrente(dataDTO.getCorrente());
        data.setModuloTensao(modulo_tensao);
        data.setModuloCorrente(modulo_corrente);

        calculatePowerIndicators(data, modulo_tensao, modulo_corrente, fi);

        return data;
    }

    private void fillSecondCycle(double[] tensao, double[] corrente, double[] tensao_2ciclo, double[] corrente_2ciclo) {
        for (int i = 0; i < SAMPLE_SIZE; i++) {
            tensao_2ciclo[i] = tensao[i + SAMPLE_SIZE];
            corrente_2ciclo[i] = corrente[i + SAMPLE_SIZE];
        }
    }

    private double[] calculateModulo(Complex[] fft) {
        double[] modulo = new double[16];
        for (int i = 0; i < 16; i++) {
            modulo[i] = (Math.hypot(fft[i].getReal(), fft[i].getImaginary())) / (AMOSTRAS / 2.0);
        }
        return modulo;
    }

    private double[] calculatePhases(Complex[] fft, double[] modulo) {
        double[] phases = new double[16];
        for (int i = 0; i < 16; i++) {
            if (modulo[i] <= 1E-6) {
                phases[i] = 0;
            } else {
                phases[i] = Math.toDegrees(Math.atan2(fft[i].getImaginary(), fft[i].getReal()));
            }
        }
        return phases;
    }

    private double[] calculateFi(double[] fases_tensao, double[] fases_corrente) {
        double[] fi = new double[16];
        for (int i = 0; i < 16; i++) {
            fi[i] = fases_tensao[i] - fases_corrente[i];
        }
        return fi;
    }
    private void calculatePowerIndicators(Data data, double[] modulo_tensao, double[] modulo_corrente, double[] fi) {
        double acum_harm_tensao = 0.0;
        double acum_harm_corrente = 0.0;
        double acum_tensao = 0.0;
        double acum_corrente = 0.0;
        double acum_tensao_residual = 0.0;
        double acum_corrente_residual = 0.0;

        double[] modulo_tensao_RMS = new double[16];
        double[] modulo_corrente_RMS = new double[16];

        for (int i = 0; i < 16; i++) {
            modulo_tensao_RMS[i] = modulo_tensao[i] / Math.sqrt(2);
            modulo_corrente_RMS[i] = modulo_corrente[i] / Math.sqrt(2);

            if (i >= 2) {
                acum_harm_tensao += Math.pow(modulo_tensao_RMS[i], 2.0);
                acum_harm_corrente += Math.pow(modulo_corrente_RMS[i], 2.0);
            }

            acum_tensao += Math.pow(modulo_tensao_RMS[i], 2.0);
            acum_corrente += Math.pow(modulo_corrente_RMS[i], 2.0);

            if (i >= 2) {
                acum_tensao_residual += Math.pow(modulo_tensao_RMS[i], 2.0);
                acum_corrente_residual += Math.pow(modulo_corrente_RMS[i], 2.0);
            }
        }

        double DHT_V_porcent = (100 * Math.sqrt(acum_harm_tensao)) / modulo_tensao_RMS[1];
        double DHT_I_porcent = (100 * Math.sqrt(acum_harm_corrente)) / modulo_corrente_RMS[1];

        data.setDistorcaoHarmonicaTotalPercentCorrente(DHT_I_porcent);
        data.setDistorcaoHarmonicaTotalPercentTensao(DHT_V_porcent);

        double V_1 = modulo_tensao_RMS[1];
        double I_1 = modulo_corrente_RMS[1];

        data.setRmsFundamentalTensao(V_1);
        data.setRmsFundamentalCorrente(I_1);

        double V_True_RMS = Math.sqrt(acum_tensao);
        double I_True_RMS = Math.sqrt(acum_corrente);

        data.setRmsVerdadeiroTensao(V_True_RMS);
        data.setRmsVerdadeiroTCorrente(I_True_RMS);

        double V_RMS_residual = Math.sqrt(acum_tensao_residual);
        double I_RMS_residual = Math.sqrt(acum_corrente_residual);

        data.setRmsResidualTensao(V_RMS_residual);
        data.setRmsResidualTCorrente(I_RMS_residual);

        double P1 = V_1 * I_1 * Math.cos(Math.toRadians(fi[1]));
        data.setPotenciaAtivaFundamental(P1);

        double PH = 0.0;
        for (int i = 2; i <= 15; i++) {
            PH += modulo_tensao_RMS[i] * modulo_corrente_RMS[i] * Math.cos(Math.toRadians(fi[i]));
        }
        data.setPotenciaAtivaNaoFundamental(PH);

        double P = P1 + PH;
        data.setPotenciaAtivaVerdadeira(P);

        double Q = V_1 * I_1 * Math.sin(Math.toRadians(fi[1]));
        data.setPotenciaReativa(Q);

        double S = V_True_RMS * I_True_RMS;
        data.setPotenciaAparenteVerdadeira(S);

        double S1 = modulo_tensao_RMS[1] * modulo_corrente_RMS[1];
        data.setPotenciaAparenteFundamental(S1);

        double SH = V_RMS_residual * I_RMS_residual;
        data.setPotenciaAparenteHarmonica(SH);

        double DI = V_1 * I_RMS_residual;
        data.setPotenciaDistorcaoCorrente(DI);

        double DV = I_1 * V_RMS_residual;
        data.setPotenciaDistorcaoTensao(DV);

        double SN = Math.sqrt(Math.pow(DI, 2) + Math.pow(DV, 2) + Math.pow(SH, 2));
        data.setPotenciaAparenteNaoFundamental(SN);

        double N = Math.sqrt(Math.pow(S, 2) - Math.pow(P, 2));
        data.setPotenciaNaoAtiva(N);

        double FP_1 = P1 / S1;
        data.setFatorPotenciaFundamental(FP_1);

        double FP_v = (P1 + PH) / Math.sqrt(Math.pow(S1, 2) + Math.pow(SN, 2));
        data.setFatorPotenciaVerdadeiro(FP_v);

        double FD = I_1 / I_True_RMS;
        data.setFatorDistorcao(FD);
    }
}