package com.example.ecoreadcm.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecoreadcm.R;
import com.example.ecoreadcm.dao.IApartamentoDAO;
import com.example.ecoreadcm.dao.ILeituraDAO;
import com.example.ecoreadcm.dao.impl.ApartamentoDAOImpl;
import com.example.ecoreadcm.dao.impl.LeituraDAOImpl;
import com.example.ecoreadcm.database.DatabaseHelper;
import com.example.ecoreadcm.model.Apartamento;
import com.example.ecoreadcm.model.Leitura;
import com.example.ecoreadcm.ui.adapters.LeituraDoMesAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LeiturasDoMesActivity extends AppCompatActivity {

    private IApartamentoDAO apartamentoDAO;
    private ILeituraDAO leituraDAO;

    private int mesAtual, anoAtual;
    private int mesHoje, anoHoje;

    private TextView tvMesAno, tvColetadas, tvPendentes, tvTotal;
    private RecyclerView rvLeituras;
    private ImageButton btnMesAnterior, btnProximoMes;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    private static final String[] MESES_NOMES = {
            "","Janeiro","Fevereiro","Março","Abril","Maio","Junho",
            "Julho","Agosto","Setembro","Outubro","Novembro","Dezembro"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leituras_do_mes);

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        apartamentoDAO = new ApartamentoDAOImpl(db);
        leituraDAO = new LeituraDAOImpl(db);

        Calendar cal = Calendar.getInstance();
        mesAtual = cal.get(Calendar.MONTH) + 1;
        anoAtual = cal.get(Calendar.YEAR);
        mesHoje = mesAtual;
        anoHoje = anoAtual;

        initViews();
        carregarDados();
    }

    private void initViews() {
        ImageButton btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> finish());

        tvMesAno = findViewById(R.id.tvMesAno);
        tvColetadas = findViewById(R.id.tvColetadas);
        tvPendentes = findViewById(R.id.tvPendentes);
        tvTotal = findViewById(R.id.tvTotal);

        rvLeituras = findViewById(R.id.rvLeituras);
        rvLeituras.setLayoutManager(new LinearLayoutManager(this));

        btnMesAnterior = findViewById(R.id.btnMesAnterior);
        btnProximoMes = findViewById(R.id.btnProximoMes);

        btnMesAnterior.setOnClickListener(v -> {
            mesAtual--;
            if (mesAtual < 1) { mesAtual = 12; anoAtual--; }
            atualizarBotaoProximoMes();
            carregarDados();
        });

        btnProximoMes.setOnClickListener(v -> {
            // Não permite navegar para além do mês atual
            if (anoAtual > anoHoje || (anoAtual == anoHoje && mesAtual >= mesHoje)) return;
            mesAtual++;
            if (mesAtual > 12) { mesAtual = 1; anoAtual++; }
            atualizarBotaoProximoMes();
            carregarDados();
        });
    }

    private void atualizarBotaoProximoMes() {
        boolean noFuturo = anoAtual > anoHoje || (anoAtual == anoHoje && mesAtual >= mesHoje);
        btnProximoMes.setAlpha(noFuturo ? 0.3f : 1.0f);
        btnProximoMes.setEnabled(!noFuturo);
    }

    private void carregarDados() {
        tvMesAno.setText(MESES_NOMES[mesAtual] + " " + anoAtual);

        final int mes = mesAtual;
        final int ano = anoAtual;

        executor.execute(() -> {
            List<Apartamento> todosApartamentos = apartamentoDAO.listarTodos();
            List<Leitura> leiturasDoMes = leituraDAO.listarPorMesAno(mes, ano);

            Map<Long, Leitura> mapaLeituras = new HashMap<>();
            for (Leitura l : leiturasDoMes) mapaLeituras.put(l.getApartamentoId(), l);

            List<LeituraDoMesAdapter.ItemLeituraDoMes> itens = new ArrayList<>();
            int coletadas = 0, pendentes = 0;
            for (Apartamento apt : todosApartamentos) {
                Leitura leitura = mapaLeituras.get(apt.getId());
                itens.add(new LeituraDoMesAdapter.ItemLeituraDoMes(apt, leitura));
                if (leitura != null) coletadas++;
                else pendentes++;
            }

            final int totalColetadas = coletadas;
            final int totalPendentes = pendentes;
            final int total = todosApartamentos.size();
            final List<LeituraDoMesAdapter.ItemLeituraDoMes> itensFinal = itens;

            uiHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                tvColetadas.setText(String.valueOf(totalColetadas));
                tvPendentes.setText(String.valueOf(totalPendentes));
                tvTotal.setText(String.valueOf(total));

                LeituraDoMesAdapter adapter = new LeituraDoMesAdapter(itensFinal, item -> {
                    Intent intent = new Intent(this, NovaLeituraActivity.class);
                    intent.putExtra("apartamento_id", item.apartamento.getId());
                    startActivity(intent);
                });
                rvLeituras.setAdapter(adapter);
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        atualizarBotaoProximoMes();
        carregarDados();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}