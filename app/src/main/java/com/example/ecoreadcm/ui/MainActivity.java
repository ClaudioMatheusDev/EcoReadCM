package com.example.ecoreadcm.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.ecoreadcm.R;
import com.example.ecoreadcm.dao.IApartamentoDAO;
import com.example.ecoreadcm.dao.ILeituraDAO;
import com.example.ecoreadcm.dao.IProprietarioDAO;
import com.example.ecoreadcm.dao.impl.ApartamentoDAOImpl;
import com.example.ecoreadcm.dao.impl.LeituraDAOImpl;
import com.example.ecoreadcm.dao.impl.ProprietarioDAOImpl;
import com.example.ecoreadcm.database.DatabaseHelper;
import com.example.ecoreadcm.model.Apartamento;
import com.example.ecoreadcm.model.Leitura;
import com.example.ecoreadcm.model.Proprietario;
import com.example.ecoreadcm.ui.adapters.ProprietarioAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private IProprietarioDAO proprietarioDAO;
    private IApartamentoDAO apartamentoDAO;
    private ILeituraDAO leituraDAO;

    private RecyclerView rvProprietarios;
    private TextView tvMediaLuz, tvMediaGas, tvTotalUnidades;
    private LinearLayout btnProprietarios, btnUnidades, btnRelatorios;

    // Instâncias do Adapter e da Lista mantidas no escopo da classe para reaproveitamento de memória
    private ProprietarioAdapter adapter;
    private final List<Proprietario> listaProprietarios = new ArrayList<>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        proprietarioDAO = new ProprietarioDAOImpl(db);
        apartamentoDAO = new ApartamentoDAOImpl(db);
        leituraDAO = new LeituraDAOImpl(db);

        initViews();
        setupNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarDashboard();
    }

    private void initViews() {
        tvMediaLuz = findViewById(R.id.tvMediaLuz);
        tvMediaGas = findViewById(R.id.tvMediaGas);
        tvTotalUnidades = findViewById(R.id.tvTotalUnidades);
        
        rvProprietarios = findViewById(R.id.rvProprietarios);
        rvProprietarios.setLayoutManager(new LinearLayoutManager(this));

        // Inicializa o adapter uma única vez vinculando-o à lista persistente da classe.
        // Isso impede o aviso "No adapter attached" no Logcat.
        adapter = new ProprietarioAdapter(listaProprietarios, proprietario -> {
            Intent intent = new Intent(this, ProprietarioDetalheActivity.class);
            intent.putExtra("proprietario_id", proprietario.getId());
            startActivity(intent);
        });
        rvProprietarios.setAdapter(adapter);

        TextView tvVerTodos = findViewById(R.id.tvVerTodos);
        tvVerTodos.setOnClickListener(v -> abrirProprietarios());

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> abrirNovaLeitura());

        btnProprietarios = findViewById(R.id.navProprietarios);
        btnUnidades = findViewById(R.id.navUnidades);
        btnRelatorios = findViewById(R.id.navRelatorios);
    }

    private void setupNavigation() {
        btnProprietarios.setOnClickListener(v -> abrirProprietarios());
        btnUnidades.setOnClickListener(v -> abrirUnidades());
        btnRelatorios.setOnClickListener(v -> abrirLeiturasDoMes());
    }

    private void carregarDashboard() {
        executor.execute(() -> {
            List<Apartamento> apartamentos = apartamentoDAO.listarTodos();
            int totalUnidades = apartamentos.size();

            Calendar cal = Calendar.getInstance();
            int mesAtual = cal.get(Calendar.MONTH) + 1;
            int anoAtual = cal.get(Calendar.YEAR);
            String[] meses = {"","Jan","Fev","Mar","Abr","Mai","Jun","Jul","Ago","Set","Out","Nov","Dez"};
            String mesAnoStr = meses[mesAtual] + " " + anoAtual;

            double somaLuz = 0, somaGas = 0;
            int count = 0;
            
            for (Apartamento apt : apartamentos) {
                Leitura ultima = leituraDAO.buscarUltimaLeitura(apt.getId());
                if (ultima != null) {
                    somaLuz += ultima.getValorLuz();
                    somaGas += ultima.getValorGas();
                    count++;
                }
            }
            final double mediaLuz = count > 0 ? somaLuz / count : -1;
            final double mediaGas = count > 0 ? somaGas / count : -1;

            List<Proprietario> proprietariosNovos = proprietarioDAO.listarTodos();
            for (Proprietario p : proprietariosNovos) {
                p.setApartamentos(apartamentoDAO.listarPorProprietario(p.getId()));
            }

            uiHandler.post(() -> {
                tvTotalUnidades.setText(String.valueOf(totalUnidades));
                TextView tvMesAno = findViewById(R.id.tvMesAno);
                tvMesAno.setText(mesAnoStr);

                if (mediaLuz >= 0) {
                    tvMediaLuz.setText(String.format("%.1f", mediaLuz));
                    tvMediaGas.setText(String.format("%.1f", mediaGas));
                } else {
                    tvMediaLuz.setText("--");
                    tvMediaGas.setText("--");
                }

                // Em vez de recriar o Adapter, limpamos e atualizamos a lista monitorada por ele.
                // Isso reduz drasticamente a alocação de memória e evita engasgos visuais (Davey!).
                listaProprietarios.clear();
                listaProprietarios.addAll(proprietariosNovos);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void abrirProprietarios() {
        startActivity(new Intent(this, ProprietariosActivity.class));
    }

    private void abrirUnidades() {
        startActivity(new Intent(this, ApartamentosActivity.class));
    }

    private void abrirNovaLeitura() {
        startActivity(new Intent(this, NovaLeituraActivity.class));
    }

    private void abrirLeiturasDoMes() {
        startActivity(new Intent(this, LeiturasDoMesActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}