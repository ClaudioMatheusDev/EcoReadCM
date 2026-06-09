package com.example.ecoreadcm.dao;

import com.example.ecoreadcm.model.Proprietario;
import java.util.List;

public interface IProprietarioDAO {
    long inserir(Proprietario proprietario);
    int atualizar(Proprietario proprietario);
    int deletar(long id);
    Proprietario buscarPorId(long id);
    Proprietario buscarPorCpf(String cpf);
    List<Proprietario> listarTodos();
    List<Proprietario> buscarPorNome(String nome);
    boolean cpfJaCadastrado(String cpf);
}
