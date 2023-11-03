package br.com.UTFPR.gaspar.tcc.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class DataDTO {
    private double[] corrente;
    private double[] tensao;
}
