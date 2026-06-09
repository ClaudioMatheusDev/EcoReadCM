package com.example.ecoreadcm.model;

import java.util.ArrayList;
import java.util.List;

public class Apartamento {
    private long id;
    private String numero;
    private String bloco;
    private long proprietarioId;
    private String proprietarioNome;
    private List<Leitura> leituras;

    public Apartamento() {
        this.leituras = new ArrayList<>();
    }

    public Apartamento(String numero, String bloco, long proprietarioId) {
        this.numero = numero;
        this.bloco = bloco;
        this.proprietarioId = proprietarioId;
        this.leituras = new ArrayList<>();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getBloco() { return bloco; }
    public void setBloco(String bloco) { this.bloco = bloco; }

    public long getProprietarioId() { return proprietarioId; }
    public void setProprietarioId(long proprietarioId) { this.proprietarioId = proprietarioId; }

    public String getProprietarioNome() { return proprietarioNome; }
    public void setProprietarioNome(String proprietarioNome) { this.proprietarioNome = proprietarioNome; }

    public List<Leitura> getLeituras() { return leituras; }
    public void setLeituras(List<Leitura> leituras) { this.leituras = leituras; }

    public String getDescricaoCompleta() {
        if (bloco != null && !bloco.isEmpty()) return "Apt " + numero + " · Bloco " + bloco;
        return "Apt " + numero;
    }

    @Override
    public String toString() {
        return getDescricaoCompleta();
    }
}