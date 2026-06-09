package com.example.ecoreadcm.model;

public class Leitura {
    private long id;
    private long apartamentoId;
    private int mes;
    private int ano;
    private double valorLuz;
    private double valorGas;

    public Leitura() {}

    public Leitura(long apartamentoId, int mes, int ano, double valorLuz, double valorGas) {
        this.apartamentoId = apartamentoId;
        this.mes = mes;
        this.ano = ano;
        this.valorLuz = valorLuz;
        this.valorGas = valorGas;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getApartamentoId() { return apartamentoId; }
    public void setApartamentoId(long apartamentoId) { this.apartamentoId = apartamentoId; }

    public int getMes() { return mes; }
    public void setMes(int mes) { this.mes = mes; }

    public int getAno() { return ano; }
    public void setAno(int ano) { this.ano = ano; }

    public double getValorLuz() { return valorLuz; }
    public void setValorLuz(double valorLuz) { this.valorLuz = valorLuz; }

    public double getValorGas() { return valorGas; }
    public void setValorGas(double valorGas) { this.valorGas = valorGas; }

    public String getPeriodoFormatado() {
        String[] meses = {"Jan","Fev","Mar","Abr","Mai","Jun","Jul","Ago","Set","Out","Nov","Dez"};
        if (mes >= 1 && mes <= 12) return meses[mes - 1] + "/" + ano;
        return mes + "/" + ano;
    }

    public String getMesAbreviado() {
        String[] meses = {"JAN","FEV","MAR","ABR","MAI","JUN","JUL","AGO","SET","OUT","NOV","DEZ"};
        if (mes >= 1 && mes <= 12) return meses[mes - 1];
        return String.valueOf(mes);
    }
}