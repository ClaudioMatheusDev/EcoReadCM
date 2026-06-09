package com.example.ecoreadcm.dao.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ecoreadcm.dao.IProprietarioDAO;
import com.example.ecoreadcm.database.DatabaseHelper;
import com.example.ecoreadcm.model.Proprietario;

import java.util.ArrayList;
import java.util.List;

public class ProprietarioDAOImpl implements IProprietarioDAO {

    private final DatabaseHelper dbHelper;

    public ProprietarioDAOImpl(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    private ContentValues toContentValues(Proprietario p) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_PROP_NOME, p.getNome());
        cv.put(DatabaseHelper.COL_PROP_CPF, p.getCpf());
        cv.put(DatabaseHelper.COL_PROP_CONTATO, p.getContato());
        return cv;
    }

    private Proprietario fromCursor(Cursor c) {
        Proprietario p = new Proprietario();
        p.setId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_PROP_ID)));
        p.setNome(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PROP_NOME)));
        p.setCpf(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PROP_CPF)));
        p.setContato(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PROP_CONTATO)));
        return p;
    }

    @Override
    public long inserir(Proprietario proprietario) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            return db.insertOrThrow(DatabaseHelper.TABLE_PROPRIETARIO, null, toContentValues(proprietario));
        } finally {
            db.close();
        }
    }

    @Override
    public int atualizar(Proprietario proprietario) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            return db.update(
                    DatabaseHelper.TABLE_PROPRIETARIO,
                    toContentValues(proprietario),
                    DatabaseHelper.COL_PROP_ID + "=?",
                    new String[]{String.valueOf(proprietario.getId())}
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
                    DatabaseHelper.TABLE_PROPRIETARIO,
                    DatabaseHelper.COL_PROP_ID + "=?",
                    new String[]{String.valueOf(id)}
            );
        } finally {
            db.close();
        }
    }

    @Override
    public Proprietario buscarPorId(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = null;
        try {
            c = db.query(
                    DatabaseHelper.TABLE_PROPRIETARIO,
                    null,
                    DatabaseHelper.COL_PROP_ID + "=?",
                    new String[]{String.valueOf(id)},
                    null, null, null
            );
            if (c.moveToFirst()) return fromCursor(c);
            return null;
        } finally {
            if (c != null) c.close();
            db.close();
        }
    }

    @Override
    public Proprietario buscarPorCpf(String cpf) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = null;
        try {
            c = db.query(
                    DatabaseHelper.TABLE_PROPRIETARIO,
                    null,
                    DatabaseHelper.COL_PROP_CPF + "=?",
                    new String[]{cpf},
                    null, null, null
            );
            if (c.moveToFirst()) return fromCursor(c);
            return null;
        } finally {
            if (c != null) c.close();
            db.close();
        }
    }

    @Override
    public List<Proprietario> listarTodos() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = null;
        List<Proprietario> lista = new ArrayList<>();
        try {
            c = db.query(
                    DatabaseHelper.TABLE_PROPRIETARIO,
                    null, null, null, null, null,
                    DatabaseHelper.COL_PROP_NOME + " ASC"
            );
            while (c.moveToNext()) lista.add(fromCursor(c));
            return lista;
        } finally {
            if (c != null) c.close();
            db.close();
        }
    }

    @Override
    public List<Proprietario> buscarPorNome(String nome) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = null;
        List<Proprietario> lista = new ArrayList<>();
        try {
            c = db.query(
                    DatabaseHelper.TABLE_PROPRIETARIO,
                    null,
                    DatabaseHelper.COL_PROP_NOME + " LIKE ?",
                    new String[]{"%" + nome + "%"},
                    null, null,
                    DatabaseHelper.COL_PROP_NOME + " ASC"
            );
            while (c.moveToNext()) lista.add(fromCursor(c));
            return lista;
        } finally {
            if (c != null) c.close();
            db.close();
        }
    }

    @Override
    public boolean cpfJaCadastrado(String cpf) {
        return buscarPorCpf(cpf) != null;
    }
}