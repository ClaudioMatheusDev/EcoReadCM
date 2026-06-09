package com.example.ecoreadcm.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class ProprietarioDetalheActivity extends AppCompatActivity {

    private IProprietarioDAO proprietarioDAO;
    private IApartamentoDAO apartamentoDAO;

    private long proprietarioId;
    private Proprietario proprietario;

    private TextView tvNome, tvCpf, tvContato, tvIniciais, tvApartamentosCount;
    private RecyclerView rvApartamentos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proprietario_detalhe);

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        proprietarioDAO = new ProprietarioDAOImpl(db);
        apartamentoDAO = new ApartamentoDAOImpl(db);

        proprietarioId = getIntent().getLongExtra("proprietario_id", -1);
        if (proprietarioId == -1) { finish(); return; }

        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarDados();
    }

    private void initViews() {
        ImageButton btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> finish());

        tvIniciais = findViewById(R.id.tvIniciais);
        tvNome = findViewById(R.id.tvNome);
        tvCpf = findViewById(R.id.tvCpf);
        tvContato = findViewById(R.id.tvContato);
        tvApartamentosCount = findViewById(R.id.tvApartamentosCount);

        rvApartamentos = findViewById(R.id.rvApartamentos);
        rvApartamentos.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fabNovoApt = findViewById(R.id.fabNovoApt);
        fabNovoApt.setOnClickListener(v -> mostrarDialogNovoApartamento());
    }

    private void carregarDados() {
        proprietario = proprietarioDAO.buscarPorId(proprietarioId);
        if (proprietario == null) { finish(); return; }

        tvIniciais.setText(proprietario.getIniciais());
        tvNome.setText(proprietario.getNome());
        tvCpf.setText(proprietario.getCpf());
        tvContato.setText(proprietario.getContato() != null ? proprietario.getContato() : "—");

        List<Apartamento> apartamentos = apartamentoDAO.listarPorProprietario(proprietarioId);
        tvApartamentosCount.setText(apartamentos.size() + " unidade(s) vinculada(s)");

        ApartamentoAdapter adapter = new ApartamentoAdapter(apartamentos, apt -> {
            Intent intent = new Intent(this, ApartamentoDetalheActivity.class);
            intent.putExtra("apartamento_id", apt.getId());
            startActivity(intent);
        });
        adapter.setOnLongClickListener(apt -> mostrarDialogOpcoesApt(apt));
        rvApartamentos.setAdapter(adapter);
    }

    private void mostrarDialogNovoApartamento() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_apartamento, null);
        EditText etNumero = view.findViewById(R.id.etNumero);
        EditText etBloco = view.findViewById(R.id.etBloco);

        new AlertDialog.Builder(this)
                .setTitle("Novo Apartamento")
                .setView(view)
                .setPositiveButton("Adicionar", (dialog, which) -> {
                    String numero = etNumero.getText().toString().trim();
                    if (numero.isEmpty()) {
                        Toast.makeText(this, "Número do apartamento é obrigatório", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String bloco = etBloco.getText().toString().trim();
                    Apartamento apt = new Apartamento(numero, bloco, proprietarioId);
                    long id = apartamentoDAO.inserir(apt);
                    if (id > 0) Toast.makeText(this, "Apartamento adicionado", Toast.LENGTH_SHORT).show();
                    carregarDados();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogOpcoesApt(Apartamento apt) {
        new AlertDialog.Builder(this)
                .setTitle("Apt " + apt.getNumero())
                .setItems(new String[]{"Ver leituras", "Excluir"}, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(this, ApartamentoDetalheActivity.class);
                        intent.putExtra("apartamento_id", apt.getId());
                        startActivity(intent);
                    } else {
                        new AlertDialog.Builder(this)
                                .setTitle("Excluir apartamento?")
                                .setMessage("Todas as leituras do Apt " + apt.getNumero() + " também serão removidas.")
                                .setPositiveButton("Excluir", (d, w) -> {
                                    apartamentoDAO.deletar(apt.getId());
                                    Toast.makeText(this, "Apartamento removido", Toast.LENGTH_SHORT).show();
                                    carregarDados();
                                })
                                .setNegativeButton("Cancelar", null)
                                .show();
                    }
                })
                .show();
    }
}