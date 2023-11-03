package br.com.UTFPR.gaspar.tcc.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@lombok.Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Data {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private double[] corrente;
    private double[] moduloCorrente;

    private double[] tensao;
    private double[] moduloTensao;

    private double distorcaoHarmonicaTotalPercentTensao;
    private double distorcaoHarmonicaTotalPercentCorrente;

    private double rmsFundamentalTensao;
    private double rmsFundamentalCorrente;

    private double rmsVerdadeiroTensao;
    private double rmsVerdadeiroTCorrente;

    private double rmsResidualTensao;
    private double rmsResidualTCorrente;

    private double potenciaAtivaFundamental;
    private double potenciaAtivaVerdadeira;

    private double potenciaReativa;

    private double potenciaAparenteVerdadeira;
    private double potenciaAparenteFundamental;
    private double potenciaAparenteHarmonica;
    private double potenciaAparenteNaoFundamental;

    private double potenciaDistorcaoCorrente;
    private double potenciaDistorcaoTensao;

    private double potenciaNaoAtiva;
    private double fatorPotenciaFundamental;
    private double fatorPotenciaVerdadeiro;
    private double fatorDistorcao;
    private double potenciaAtivaNaoFundamental;

}
