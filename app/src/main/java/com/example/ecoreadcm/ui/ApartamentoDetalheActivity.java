package com.example.ecoreadcm.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.ecoreadcm.ui.adapters.LeituraAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApartamentoDetalheActivity extends AppCompatActivity {

    private IApartamentoDAO apartamentoDAO;
    private ILeituraDAO leituraDAO;

    private long apartamentoId;
    private int periodoSelecionado = 3; // 3 ou 6 meses

    private TextView tvAptNumero, tvAptSubtitulo;
    private TextView tvUltimaLuz, tvUltimaGas;
    private TextView tvMediaLuz, tvMediaGas;
    private TextView tvAvisoMeses;
    private RecyclerView rvLeituras;
    private android.widget.TextView tvEmpty;
    private Chip chip3Meses, chip6Meses;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apartamento_detalhe);

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        apartamentoDAO = new ApartamentoDAOImpl(db);
        leituraDAO = new LeituraDAOImpl(db);

        apartamentoId = getIntent().getLongExtra("apartamento_id", -1);
        if (apartamentoId == -1) { finish(); return; }

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

        tvAptNumero = findViewById(R.id.tvAptNumero);
        tvAptSubtitulo = findViewById(R.id.tvAptSubtitulo);
        tvUltimaLuz = findViewById(R.id.tvUltimaLuz);
        tvUltimaGas = findViewById(R.id.tvUltimaGas);
        tvMediaLuz = findViewById(R.id.tvMediaLuz);
        tvMediaGas = findViewById(R.id.tvMediaGas);
        tvAvisoMeses = findViewById(R.id.tvAvisoMeses);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvLeituras = findViewById(R.id.rvLeituras);
        rvLeituras.setLayoutManager(new LinearLayoutManager(this));

        chip3Meses = findViewById(R.id.chip3Meses);
        chip6Meses = findViewById(R.id.chip6Meses);

        chip3Meses.setOnClickListener(v -> {
            periodoSelecionado = 3;
            chip3Meses.setChecked(true);
            chip6Meses.setChecked(false);
            atualizarMedias();
        });

        chip6Meses.setOnClickListener(v -> {
            periodoSelecionado = 6;
            chip6Meses.setChecked(true);
            chip3Meses.setChecked(false);
            atualizarMedias();
        });

        FloatingActionButton fabNovaLeitura = findViewById(R.id.fabNovaLeitura);
        fabNovaLeitura.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, NovaLeituraActivity.class);
            intent.putExtra("apartamento_id", apartamentoId);
            startActivity(intent);
        });
    }

    private void carregarDados() {
        executor.execute(() -> {
            Apartamento apt = apartamentoDAO.buscarPorId(apartamentoId);
            if (apt == null) { uiHandler.post(this::finish); return; }

            Leitura ultima = leituraDAO.buscarUltimaLeitura(apartamentoId);
            List<Leitura> leituras = leituraDAO.listarPorApartamento(apartamentoId);
            int totalLeituras = leituras.size();

            double mediaLuz = leituraDAO.calcularMediaLuz(apartamentoId, periodoSelecionado);
            double mediaGas = leituraDAO.calcularMediaGas(apartamentoId, periodoSelecionado);

            uiHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;

                tvAptNumero.setText("Apartamento " + apt.getNumero());
                String subtitulo = apt.getProprietarioNome() != null ? apt.getProprietarioNome() : "";
                if (apt.getBloco() != null && !apt.getBloco().isEmpty())
                    subtitulo += (subtitulo.isEmpty() ? "" : " · ") + "Bloco " + apt.getBloco();
                tvAptSubtitulo.setText(subtitulo);

                if (ultima != null) {
                    tvUltimaLuz.setText(String.format("%.1f kWh", ultima.getValorLuz()));
                    tvUltimaGas.setText(String.format("%.1f m³", ultima.getValorGas()));
                } else {
                    tvUltimaLuz.setText("—");
                    tvUltimaGas.setText("—");
                }

                // Médias
                if (totalLeituras == 0) {
                    tvMediaLuz.setText("—");
                    tvMediaGas.setText("—");
                    tvAvisoMeses.setVisibility(View.VISIBLE);
                    tvAvisoMeses.setText("Nenhuma leitura registrada ainda.");
                } else {
                    if (periodoSelecionado == 6 && totalLeituras < 6) {
                        tvAvisoMeses.setVisibility(View.VISIBLE);
                        tvAvisoMeses.setText("Apenas " + totalLeituras + " leitura(s) disponível(is). " +
                                "Média calculada com os dados existentes.");
                    } else {
                        tvAvisoMeses.setVisibility(View.GONE);
                    }
                    tvMediaLuz.setText(String.format("%.1f kWh", mediaLuz));
                    tvMediaGas.setText(String.format("%.1f m³", mediaGas));
                }

                LeituraAdapter adapter = new LeituraAdapter(leituras, leitura -> mostrarDialogEditarLeitura(leitura));
                rvLeituras.setAdapter(adapter);
                tvEmpty.setVisibility(leituras.isEmpty() ? View.VISIBLE : View.GONE);
                rvLeituras.setVisibility(leituras.isEmpty() ? View.GONE : View.VISIBLE);
            });
        });
    }

    private void atualizarMedias() {
        carregarDados(); // Re-usa o carregamento completo para consistência
    }

    private void mostrarDialogEditarLeitura(Leitura leitura) {
        android.view.View view = android.view.LayoutInflater.from(this)
                .inflate(R.layout.dialog_leitura, null);

        android.widget.EditText etLuz = view.findViewById(R.id.etValorLuz);
        android.widget.EditText etGas = view.findViewById(R.id.etValorGas);
        // Formata sem casas desnecessárias (ex: 150 em vez de 150.0)
        etLuz.setText(leitura.getValorLuz() % 1 == 0
                ? String.valueOf((int) leitura.getValorLuz())
                : String.format("%.2f", leitura.getValorLuz()));
        etGas.setText(leitura.getValorGas() % 1 == 0
                ? String.valueOf((int) leitura.getValorGas())
                : String.format("%.2f", leitura.getValorGas()));

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Editar " + leitura.getPeriodoFormatado())
                .setView(view)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    try {
                        double luz = Double.parseDouble(etLuz.getText().toString().trim());
                        double gas = Double.parseDouble(etGas.getText().toString().trim());
                        if (luz < 0 || gas < 0) {
                            Toast.makeText(this, getString(R.string.valor_invalido), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        leitura.setValorLuz(luz);
                        leitura.setValorGas(gas);
                        executor.execute(() -> {
                            leituraDAO.atualizar(leitura);
                            uiHandler.post(() -> {
                                if (isFinishing() || isDestroyed()) return;
                                Toast.makeText(this, "Leitura atualizada", Toast.LENGTH_SHORT).show();
                                carregarDados();
                            });
                        });
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Valores inválidos", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Excluir", (dialog, which) -> confirmarExclusaoLeitura(leitura))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarExclusaoLeitura(Leitura leitura) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirmar_exclusao))
                .setMessage(getString(R.string.excluir_leitura_msg))
                .setPositiveButton("Excluir", (d, w) -> {
                    executor.execute(() -> {
                        leituraDAO.deletar(leitura.getId());
                        uiHandler.post(() -> {
                            if (isFinishing() || isDestroyed()) return;
                            Toast.makeText(this, "Leitura removida", Toast.LENGTH_SHORT).show();
                            carregarDados();
                        });
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}