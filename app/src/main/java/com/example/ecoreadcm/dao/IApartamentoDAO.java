package com.example.ecoreadcm.dao;

import com.example.ecoreadcm.model.Apartamento;
import java.util.List;

public interface IApartamentoDAO {
    long inserir(Apartamento apartamento);
    int atualizar(Apartamento apartamento);
    int deletar(long id);
    Apartamento buscarPorId(long id);
    List<Apartamento> listarPorProprietario(long proprietarioId);
    List<Apartamento> listarTodos();
    boolean apartamentoExiste(long id);
}
