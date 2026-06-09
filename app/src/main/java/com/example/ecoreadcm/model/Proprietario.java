package com.example.ecoreadcm.model;

import java.util.ArrayList;
import java.util.List;

public class Proprietario {
    private long id;
    private String nome;
    private String cpf;
    private String contato;
    private List<Apartamento> apartamentos;

    public Proprietario() {
        this.apartamentos = new ArrayList<>();
    }

    public Proprietario(String nome, String cpf, String contato) {
        this.nome = nome;
        this.cpf = cpf;
        this.contato = contato;
        this.apartamentos = new ArrayList<>();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getContato() { return contato; }
    public void setContato(String contato) { this.contato = contato; }

    public List<Apartamento> getApartamentos() { return apartamentos; }
    public void setApartamentos(List<Apartamento> apartamentos) { this.apartamentos = apartamentos; }

    public String getIniciais() {
        if (nome == null || nome.isEmpty()) return "?";
        String[] partes = nome.trim().split("\\s+");
        if (partes.length == 1) return String.valueOf(partes[0].charAt(0)).toUpperCase();
        return (String.valueOf(partes[0].charAt(0)) + String.valueOf(partes[partes.length - 1].charAt(0))).toUpperCase();
    }

    @Override
    public String toString() {
        return nome;
    }
}