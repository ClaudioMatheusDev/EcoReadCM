package com.example.ecoreadcm.dao.impl;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ecoreadcm.dao.IApartamentoDAO;
import com.example.ecoreadcm.database.DatabaseHelper;
import com.example.ecoreadcm.model.Apartamento;

import java.util.ArrayList;
import java.util.List;

public class ApartamentoDAOImpl implements IApartamentoDAO {

    private final DatabaseHelper dbHelper;

    public ApartamentoDAOImpl(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    private ContentValues toContentValues(Apartamento a) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_APT_NUMERO, a.getNumero());
        cv.put(DatabaseHelper.COL_APT_BLOCO, a.getBloco());
        cv.put(DatabaseHelper.COL_APT_PROPRIETARIO_ID, a.getProprietarioId());
        return cv;
    }

    private Apartamento fromCursor(Cursor c) {
        Apartamento a = new Apartamento();
        a.setId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_APT_ID)));
        a.setNumero(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_APT_NUMERO)));
        a.setBloco(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_APT_BLOCO)));
        a.setProprietarioId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_APT_PROPRIETARIO_ID)));
        // nome do proprietário via JOIN quando disponível
        int nomeIdx = c.getColumnIndex("proprietario_nome");
        if (nomeIdx >= 0) a.setProprietarioNome(c.getString(nomeIdx));
        return a;
    }

    @Override
    public long inserir(Apartamento apartamento) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            return db.insertOrThrow(DatabaseHelper.TABLE_APARTAMENTO, null, toContentValues(apartamento));
        } finally {
            db.close();
        }
    }

    @Override
    public int atualizar(Apartamento apartamento) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            return db.update(
                    DatabaseHelper.TABLE_APARTAMENTO,
                    toContentValues(apartamento),
                    DatabaseHelper.COL_APT_ID + "=?",
                    new String[]{String.valueOf(apartamento.getId())}
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
                    DatabaseHelper.TABLE_APARTAMENTO,
                    DatabaseHelper.COL_APT_ID + "=?",
                    new String[]{String.valueOf(id)}
            );
        } finally {
            db.close();
        }
    }

    @Override
    public Apartamento buscarPorId(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = null;
        try {
            String sql = "SELECT a.*, p." + DatabaseHelper.COL_PROP_NOME + " AS proprietario_nome" +
                    " FROM " + DatabaseHelper.TABLE_APARTAMENTO + " a" +
                    " JOIN " + DatabaseHelper.TABLE_PROPRIETARIO + " p ON a." +
                    DatabaseHelper.COL_APT_PROPRIETARIO_ID + " = p." + DatabaseHelper.COL_PROP_ID +
                    " WHERE a." + DatabaseHelper.COL_APT_ID + " = ?";
            c = db.rawQuery(sql, new String[]{String.valueOf(id)});
            if (c.moveToFirst()) return fromCursor(c);
            return null;
        } finally {
            if (c != null) c.close();
            db.close();
        }
    }

    @Override
    public List<Apartamento> listarPorProprietario(long proprietarioId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = null;
        List<Apartamento> lista = new ArrayList<>();
        try {
            c = db.query(
                    DatabaseHelper.TABLE_APARTAMENTO,
                    null,
                    DatabaseHelper.COL_APT_PROPRIETARIO_ID + "=?",
                    new String[]{String.valueOf(proprietarioId)},
                    null, null,
                    DatabaseHelper.COL_APT_NUMERO + " ASC"
            );
            while (c.moveToNext()) lista.add(fromCursor(c));
            return lista;
        } finally {
            if (c != null) c.close();
            db.close();
        }
    }

    @Override
    public List<Apartamento> listarTodos() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = null;
        List<Apartamento> lista = new ArrayList<>();
        try {
            String sql = "SELECT a.*, p." + DatabaseHelper.COL_PROP_NOME + " AS proprietario_nome" +
                    " FROM " + DatabaseHelper.TABLE_APARTAMENTO + " a" +
                    " JOIN " + DatabaseHelper.TABLE_PROPRIETARIO + " p ON a." +
                    DatabaseHelper.COL_APT_PROPRIETARIO_ID + " = p." + DatabaseHelper.COL_PROP_ID +
                    " ORDER BY a." + DatabaseHelper.COL_APT_NUMERO + " ASC";
            c = db.rawQuery(sql, null);
            while (c.moveToNext()) lista.add(fromCursor(c));
            return lista;
        } finally {
            if (c != null) c.close();
            db.close();
        }
    }

    @Override
    public boolean apartamentoExiste(long id) {
        return buscarPorId(id) != null;
    }

    @Override
    public boolean verificarDuplicado(String numero, String bloco, long excluirId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = null;
        try {
            boolean blocoVazio = bloco == null || bloco.trim().isEmpty();
            String sql;
            String[] args;
            if (blocoVazio) {
                if (excluirId > 0) {
                    sql = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_APARTAMENTO +
                          " WHERE " + DatabaseHelper.COL_APT_NUMERO + " = ? AND (" +
                          DatabaseHelper.COL_APT_BLOCO + " IS NULL OR " +
                          DatabaseHelper.COL_APT_BLOCO + " = '') AND " +
                          DatabaseHelper.COL_APT_ID + " != ?";
                    args = new String[]{numero, String.valueOf(excluirId)};
                } else {
                    sql = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_APARTAMENTO +
                          " WHERE " + DatabaseHelper.COL_APT_NUMERO + " = ? AND (" +
                          DatabaseHelper.COL_APT_BLOCO + " IS NULL OR " +
                          DatabaseHelper.COL_APT_BLOCO + " = '')";
                    args = new String[]{numero};
                }
            } else {
                if (excluirId > 0) {
                    sql = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_APARTAMENTO +
                          " WHERE " + DatabaseHelper.COL_APT_NUMERO + " = ? AND " +
                          DatabaseHelper.COL_APT_BLOCO + " = ? AND " +
                          DatabaseHelper.COL_APT_ID + " != ?";
                    args = new String[]{numero, bloco.trim(), String.valueOf(excluirId)};
                } else {
                    sql = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_APARTAMENTO +
                          " WHERE " + DatabaseHelper.COL_APT_NUMERO + " = ? AND " +
                          DatabaseHelper.COL_APT_BLOCO + " = ?";
                    args = new String[]{numero, bloco.trim()};
                }
            }
            c = db.rawQuery(sql, args);
            if (c.moveToFirst()) return c.getInt(0) > 0;
            return false;
        } finally {
            if (c != null) c.close();
            db.close();
        }
    }
}