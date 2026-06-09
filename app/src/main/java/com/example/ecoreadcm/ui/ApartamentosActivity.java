package com.example.ecoreadcm.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecoreadcm.R;
import com.example.ecoreadcm.dao.IApartamentoDAO;
import com.example.ecoreadcm.dao.IProprietarioDAO;
import com.example.ecoreadcm.dao.impl.ApartamentoDAOImpl;
import com.example.ecoreadcm.dao.impl.ProprietarioDAOImpl;
import com.example.ecoreadcm.database.DatabaseHelper;
import com.example.ecoreadcm.model.Apartamento;
import com.example.ecoreadcm.model.Proprietario;
import com.example.ecoreadcm.ui.adapters.ApartamentoAdapter;

import java.util.List;

public class ApartamentosActivity extends AppCompatActivity {

    private IApartamentoDAO apartamentoDAO;
    private IProprietarioDAO proprietarioDAO;
    private RecyclerView rvApartamentos;
    private TextView tvContagem;
    private List<Proprietario> listaProprietarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apartamentos);

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        apartamentoDAO = new ApartamentoDAOImpl(db);
        proprietarioDAO = new ProprietarioDAOImpl(db);

        ImageButton btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> finish());

        ImageButton btnAdicionar = findViewById(R.id.btnAdicionar);
        btnAdicionar.setOnClickListener(v -> mostrarDialogNovaUnidade());

        tvContagem = findViewById(R.id.tvContagem);
        rvApartamentos = findViewById(R.id.rvApartamentos);
        rvApartamentos.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarLista();
    }

    private void carregarLista() {
        List<Apartamento> lista = apartamentoDAO.listarTodos();
        int total = lista.size();
        tvContagem.setText(total + (total == 1 ? " unidade cadastrada" : " unidades cadastradas"));

        ApartamentoAdapter adapter = new ApartamentoAdapter(lista, apt -> {
            Intent intent = new Intent(this, ApartamentoDetalheActivity.class);
            intent.putExtra("apartamento_id", apt.getId());
            startActivity(intent);
        });
        rvApartamentos.setAdapter(adapter);
    }

    private void mostrarDialogNovaUnidade() {
        listaProprietarios = proprietarioDAO.listarTodos();
        if (listaProprietarios.isEmpty()) {
            Toast.makeText(this, "Cadastre um proprietário antes de adicionar unidades", Toast.LENGTH_LONG).show();
            return;
        }

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_apartamento_global, null);
        Spinner spinnerProprietario = view.findViewById(R.id.spinnerProprietario);
        EditText etNumero = view.findViewById(R.id.etNumero);
        EditText etBloco = view.findViewById(R.id.etBloco);

        String[] nomes = new String[listaProprietarios.size()];
        for (int i = 0; i < listaProprietarios.size(); i++) {
            nomes[i] = listaProprietarios.get(i).getNome();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, nomes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProprietario.setAdapter(adapter);

        new AlertDialog.Builder(this)
                .setTitle("Nova Unidade")
                .setView(view)
                .setPositiveButton("Adicionar", (dialog, which) -> {
                    String numero = etNumero.getText().toString().trim();
                    if (numero.isEmpty()) {
                        Toast.makeText(this, "Número do apartamento é obrigatório", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int pos = spinnerProprietario.getSelectedItemPosition();
                    long proprietarioId = listaProprietarios.get(pos).getId();
                    String bloco = etBloco.getText().toString().trim();

                    Apartamento apt = new Apartamento(numero, bloco, proprietarioId);
                    long id = apartamentoDAO.inserir(apt);
                    if (id > 0) {
                        Toast.makeText(this, "Unidade adicionada com sucesso", Toast.LENGTH_SHORT).show();
                        carregarLista();
                    } else {
                        Toast.makeText(this, "Erro ao adicionar unidade", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}