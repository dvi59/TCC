package br.com.UTFPR.gaspar.tcc.http.controller;

import br.com.UTFPR.gaspar.tcc.dto.DataDTO;
import br.com.UTFPR.gaspar.tcc.entity.Data;
import br.com.UTFPR.gaspar.tcc.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/data")
public class DataController {

    @Autowired
    private DataService dataService;

    @CrossOrigin
    @PostMapping("/get-transformada")
    @ResponseStatus(HttpStatus.OK)
    public Data getTransformadaTensao(@RequestBody DataDTO dataDTO) {
        System.out.println("TESTE");
        return dataService.transformadaTensao(dataDTO);
    }

}
