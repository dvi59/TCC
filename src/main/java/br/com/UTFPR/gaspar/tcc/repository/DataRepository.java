package br.com.UTFPR.gaspar.tcc.repository;


import br.com.UTFPR.gaspar.tcc.entity.Data;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DataRepository extends JpaRepository <Data, Long>{
    @Query("select d.corrente from Data d")
    Object[] findAllCorrente();

    @Query("select d.tensao from Data d")
    Object[] findAllTensao();


}
