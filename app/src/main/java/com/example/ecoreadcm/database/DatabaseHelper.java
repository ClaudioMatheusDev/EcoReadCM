package com.example.ecoreadcm.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "ecoread.db";
    public static final int DATABASE_VERSION = 1;

    // Tabela Proprietario
    public static final String TABLE_PROPRIETARIO = "proprietario";
    public static final String COL_PROP_ID = "id";
    public static final String COL_PROP_NOME = "nome";
    public static final String COL_PROP_CPF = "cpf";
    public static final String COL_PROP_CONTATO = "contato";

    // Tabela Apartamento
    public static final String TABLE_APARTAMENTO = "apartamento";
    public static final String COL_APT_ID = "id";
    public static final String COL_APT_NUMERO = "numero";
    public static final String COL_APT_BLOCO = "bloco";
    public static final String COL_APT_PROPRIETARIO_ID = "proprietario_id";

    // Tabela Leitura
    public static final String TABLE_LEITURA = "leitura";
    public static final String COL_LEIT_ID = "id";
    public static final String COL_LEIT_APARTAMENTO_ID = "apartamento_id";
    public static final String COL_LEIT_MES = "mes";
    public static final String COL_LEIT_ANO = "ano";
    public static final String COL_LEIT_VALOR_LUZ = "valor_luz";
    public static final String COL_LEIT_VALOR_GAS = "valor_gas";

    private static final String SQL_CREATE_PROPRIETARIO =
            "CREATE TABLE " + TABLE_PROPRIETARIO + " (" +
                    COL_PROP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_PROP_NOME + " TEXT NOT NULL, " +
                    COL_PROP_CPF + " TEXT NOT NULL UNIQUE, " +
                    COL_PROP_CONTATO + " TEXT" +
                    ")";

    private static final String SQL_CREATE_APARTAMENTO =
            "CREATE TABLE " + TABLE_APARTAMENTO + " (" +
                    COL_APT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_APT_NUMERO + " TEXT NOT NULL, " +
                    COL_APT_BLOCO + " TEXT, " +
                    COL_APT_PROPRIETARIO_ID + " INTEGER NOT NULL, " +
                    "FOREIGN KEY(" + COL_APT_PROPRIETARIO_ID + ") REFERENCES " +
                    TABLE_PROPRIETARIO + "(" + COL_PROP_ID + ") ON DELETE CASCADE" +
                    ")";

    private static final String SQL_CREATE_LEITURA =
            "CREATE TABLE " + TABLE_LEITURA + " (" +
                    COL_LEIT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_LEIT_APARTAMENTO_ID + " INTEGER NOT NULL, " +
                    COL_LEIT_MES + " INTEGER NOT NULL, " +
                    COL_LEIT_ANO + " INTEGER NOT NULL, " +
                    COL_LEIT_VALOR_LUZ + " REAL NOT NULL, " +
                    COL_LEIT_VALOR_GAS + " REAL NOT NULL, " +
                    "UNIQUE(" + COL_LEIT_APARTAMENTO_ID + ", " + COL_LEIT_MES + ", " + COL_LEIT_ANO + "), " +
                    "FOREIGN KEY(" + COL_LEIT_APARTAMENTO_ID + ") REFERENCES " +
                    TABLE_APARTAMENTO + "(" + COL_APT_ID + ") ON DELETE CASCADE" +
                    ")";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = ON");
        db.execSQL(SQL_CREATE_PROPRIETARIO);
        db.execSQL(SQL_CREATE_APARTAMENTO);
        db.execSQL(SQL_CREATE_LEITURA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // ATENÇÃO: nunca use DROP TABLE aqui — todos os dados do usuário serão perdidos.
        // Implemente migrações incrementais com ALTER TABLE.
        // Exemplo:
        // if (oldVersion < 2) {
        //     db.execSQL("ALTER TABLE " + TABLE_PROPRIETARIO + " ADD COLUMN email TEXT");
        // }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys = ON");
    }
}