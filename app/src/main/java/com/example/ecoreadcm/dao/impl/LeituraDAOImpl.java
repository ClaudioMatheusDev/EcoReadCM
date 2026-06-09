package com.example.ecoreadcm.dao.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ecoreadcm.dao.ILeituraDAO;
import com.example.ecoreadcm.database.DatabaseHelper;
import com.example.ecoreadcm.model.Leitura;

import java.util.ArrayList;
import java.util.List;

public class LeituraDAOImpl implements ILeituraDAO {

    private final DatabaseHelper dbHelper;

    public LeituraDAOImpl(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    private ContentValues toContentValues(Leitura l) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_LEIT_APARTAMENTO_ID, l.getApartamentoId());
        cv.put(DatabaseHelper.COL_LEIT_MES, l.getMes());
        cv.put(DatabaseHelper.COL_LEIT_ANO, l.getAno());
        cv.put(DatabaseHelper.COL_LEIT_VALOR_LUZ, l.getValorLuz());
        cv.put(DatabaseHelper.COL_LEIT_VALOR_GAS, l.getValorGas());
        return cv;
    }

    private Leitura fromCursor(Cursor c) {
        Leitura l = new Leitura();
        l.setId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_LEIT_ID)));
        l.setApartamentoId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_LEIT_APARTAMENTO_ID)));
        l.setMes(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_LEIT_MES)));
        l.setAno(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_LEIT_ANO)));
        l.setValorLuz(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_LEIT_VALOR_LUZ)));
        l.setValorGas(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_LEIT_VALOR_GAS)));
        return l;
    }

    @Override
    public long inserir(Leitura leitura) {
        // RF integridade: verificar se apartamento existe é feito na Activity antes de chamar
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            return db.insertOrThrow(DatabaseHelper.TABLE_LEITURA, null, toContentValues(leitura));
        } finally {
            db.close();
        }
    }

    @Override
    public int atualizar(Leitura leitura) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            return db.update(
                    DatabaseHelper.TABLE_LEITURA,
                    toContentValues(leitura),
                    DatabaseHelper.COL_LEIT_ID + "=?",
                    new String[]{String.valueOf(leitura.getId())}
            );
        } finally {
            db.close();
        }
    }

    @Override
    public int deletar(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            return db.delete(
                    DatabaseHelper.TABLE_LEITURA,
                    DatabaseHelper.COL_LEIT_ID + "=?",
                    new String[]{String.valueOf(id)}
            );
        } finally {
            db.close();
        }
    }

    @Override
    public Leitura buscarPorId(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = null;
        try {
            c = db.query(DatabaseHelper.TABLE_LEITURA, null,
                    DatabaseHelper.COL_LEIT_ID + "=?",
                    new String[]{String.valueOf(id)},
                    null, null, null);
            if (c.moveToFirst()) return fromCursor(c);
            return null;
        } finally {
            if (c != null) c.close();
            db.close();
        }
    }

    @Override
    public Leitura buscarPorApartamentoMesAno(long apartamentoId, int mes, int ano) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = null;
        try {
            c = db.query(DatabaseHelper.TABLE_LEITURA, null,
                    DatabaseHelper.COL_LEIT_APARTAMENTO_ID + "=? AND " +
                            DatabaseHelper.COL_LEIT_MES + "=? AND " +
                            DatabaseHelper.COL_LEIT_ANO + "=?",
                    new String[]{String.valueOf(apartamentoId), String.valueOf(mes), String.valueOf(ano)},
                    null, null, null);
            if (c.moveToFirst()) return fromCursor(c);
            return null;
        } finally {
            if (c != null) c.close();
            db.close();
        }
    }

    @Override
    public List<Leitura> listarPorApartamento(long apartamentoId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = null;
        List<Leitura> lista = new ArrayList<>();
        try {
            c = db.query(DatabaseHelper.TABLE_LEITURA, null,
                    DatabaseHelper.COL_LEIT_APARTAMENTO_ID + "=?",
                    new String[]{String.valueOf(apartamentoId)},
                    null, null,
                    DatabaseHelper.COL_LEIT_ANO + " DESC, " + DatabaseHelper.COL_LEIT_MES + " DESC");
            while (c.moveToNext()) lista.add(fromCursor(c));
            return lista;
        } finally {
            if (c != null) c.close();
            db.close();
        }
    }

    @Override
    public List<Leitura> listarUltimasPorApartamento(long apartamentoId, int quantidade) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = null;
        List<Leitura> lista = new ArrayList<>();
        try {
            c = db.query(DatabaseHelper.TABLE_LEITURA, null,
                    DatabaseHelper.COL_LEIT_APARTAMENTO_ID + "=?",
                    new String[]{String.valueOf(apartamentoId)},
                    null, null,
                    DatabaseHelper.COL_LEIT_ANO + " DESC, " + DatabaseHelper.COL_LEIT_MES + " DESC",
                    String.valueOf(quantidade));
            while (c.moveToNext()) lista.add(fromCursor(c));
            return lista;
        } finally {
            if (c != null) c.close();
            db.close();
        }
    }

    @Override
    public List<Leitura> listarPorMesAno(int mes, int ano) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = null;
        List<Leitura> lista = new ArrayList<>();
        try {
            c = db.query(DatabaseHelper.TABLE_LEITURA, null,
                    DatabaseHelper.COL_LEIT_MES + "=? AND " + DatabaseHelper.COL_LEIT_ANO + "=?",
                    new String[]{String.valueOf(mes), String.valueOf(ano)},
                    null, null, null);
            while (c.moveToNext()) lista.add(fromCursor(c));
            return lista;
        } finally {
            if (c != null) c.close();
            db.close();
        }
    }

    @Override
    public double calcularMediaLuz(long apartamentoId, int ultimosMeses) {
        List<Leitura> leituras = listarUltimasPorApartamento(apartamentoId, ultimosMeses);
        if (leituras.isEmpty()) return 0;
        double soma = 0;
        for (Leitura l : leituras) soma += l.getValorLuz();
        return soma / leituras.size();
    }

    @Override
    public double calcularMediaGas(long apartamentoId, int ultimosMeses) {
        List<Leitura> leituras = listarUltimasPorApartamento(apartamentoId, ultimosMeses);
        if (leituras.isEmpty()) return 0;
        double soma = 0;
        for (Leitura l : leituras) soma += l.getValorGas();
        return soma / leituras.size();
    }

    @Override
    public Leitura buscarUltimaLeitura(long apartamentoId) {
        List<Leitura> leituras = listarUltimasPorApartamento(apartamentoId, 1);
        if (leituras.isEmpty()) return null;
        return leituras.get(0);
    }

    @Override
    public boolean leituraJaExiste(long apartamentoId, int mes, int ano) {
        return buscarPorApartamentoMesAno(apartamentoId, mes, ano) != null;
    }

    @Override
    public int contarLeituras(long apartamentoId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = null;
        try {
            c = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_LEITURA +
                            " WHERE " + DatabaseHelper.COL_LEIT_APARTAMENTO_ID + "=?",
                    new String[]{String.valueOf(apartamentoId)}
            );
            if (c.moveToFirst()) return c.getInt(0);
            return 0;
        } finally {
            if (c != null) c.close();
            db.close();
        }
    }
}