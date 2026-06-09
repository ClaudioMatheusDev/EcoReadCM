package com.example.ecoreadcm.dao;

import com.example.ecoreadcm.model.Leitura;
import java.util.List;

public interface ILeituraDAO {
    long inserir(Leitura leitura);
    int atualizar(Leitura leitura);
    int deletar(long id);
    Leitura buscarPorId(long id);
    Leitura buscarPorApartamentoMesAno(long apartamentoId, int mes, int ano);
    List<Leitura> listarPorApartamento(long apartamentoId);
    List<Leitura> listarUltimasPorApartamento(long apartamentoId, int quantidade);
    List<Leitura> listarPorMesAno(int mes, int ano);
    double calcularMediaLuz(long apartamentoId, int ultimosMeses);
    double calcularMediaGas(long apartamentoId, int ultimosMeses);
    Leitura buscarUltimaLeitura(long apartamentoId);
    boolean leituraJaExiste(long apartamentoId, int mes, int ano);
    int contarLeituras(long apartamentoId);
}
