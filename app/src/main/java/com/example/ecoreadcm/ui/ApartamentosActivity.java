package com.example.ecoreadcm.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApartamentosActivity extends AppCompatActivity {

    private IApartamentoDAO apartamentoDAO;
    private IProprietarioDAO proprietarioDAO;
    private RecyclerView rvApartamentos;
    private TextView tvContagem;
    private android.widget.TextView tvEmpty;
    private EditText etBusca;
    private List<Proprietario> listaProprietarios;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

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
        tvEmpty = findViewById(R.id.tvEmpty);
        rvApartamentos = findViewById(R.id.rvApartamentos);
        rvApartamentos.setLayoutManager(new LinearLayoutManager(this));

        etBusca = findViewById(R.id.etBusca);
        etBusca.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                carregarLista(s.toString());
            }
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarLista(etBusca != null ? etBusca.getText().toString() : "");
    }

    private void carregarLista(String filtro) {
        executor.execute(() -> {
            List<Apartamento> lista = apartamentoDAO.listarTodos();
            // Filtro por número ou proprietário
            if (filtro != null && !filtro.isEmpty()) {
                String f = filtro.toLowerCase().trim();
                lista.removeIf(a ->
                        !a.getNumero().toLowerCase().contains(f) &&
                        (a.getProprietarioNome() == null || !a.getProprietarioNome().toLowerCase().contains(f)) &&
                        (a.getBloco() == null || !a.getBloco().toLowerCase().contains(f))
                );
            }

            final int total = lista.size();
            uiHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                tvContagem.setText(total + (total == 1 ? " unidade cadastrada" : " unidades cadastradas"));
                tvEmpty.setVisibility(total == 0 ? View.VISIBLE : View.GONE);
                rvApartamentos.setVisibility(total == 0 ? View.GONE : View.VISIBLE);

                ApartamentoAdapter adapter = new ApartamentoAdapter(lista, apt -> {
                    Intent intent = new Intent(this, ApartamentoDetalheActivity.class);
                    intent.putExtra("apartamento_id", apt.getId());
                    startActivity(intent);
                });
                adapter.setOnLongClickListener(apt -> mostrarDialogOpcoesApt(apt));
                rvApartamentos.setAdapter(adapter);
            });
        });
    }

    private void mostrarDialogNovaUnidade() {
        executor.execute(() -> {
            List<Proprietario> props = proprietarioDAO.listarTodos();
            uiHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                listaProprietarios = props;
                if (listaProprietarios.isEmpty()) {
                    Toast.makeText(this, "Cadastre um proprietário antes de adicionar unidades", Toast.LENGTH_LONG).show();
                    return;
                }
                exibirDialogNovaUnidade(null);
            });
        });
    }

    private void exibirDialogNovaUnidade(Apartamento editando) {
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

        if (editando != null) {
            etNumero.setText(editando.getNumero());
            etBloco.setText(editando.getBloco() != null ? editando.getBloco() : "");
            for (int i = 0; i < listaProprietarios.size(); i++) {
                if (listaProprietarios.get(i).getId() == editando.getProprietarioId()) {
                    spinnerProprietario.setSelection(i);
                    break;
                }
            }
        }

        String titulo = editando == null ? "Nova Unidade" : "Editar Unidade";
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setView(view)
                .setPositiveButton(editando == null ? "Adicionar" : "Salvar", (dialog, which) -> {
                    String numero = etNumero.getText().toString().trim();
                    if (numero.isEmpty()) {
                        Toast.makeText(this, "Número do apartamento é obrigatório", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int pos = spinnerProprietario.getSelectedItemPosition();
                    long proprietarioId = listaProprietarios.get(pos).getId();
                    String bloco = etBloco.getText().toString().trim();

                    executor.execute(() -> {
                        long excluirId = editando != null ? editando.getId() : -1;
                        boolean duplicado = apartamentoDAO.verificarDuplicado(numero, bloco, excluirId);
                        if (duplicado) {
                            uiHandler.post(() -> {
                                if (isFinishing() || isDestroyed()) return;
                                Toast.makeText(this, getString(R.string.apartamento_duplicado), Toast.LENGTH_SHORT).show();
                            });
                            return;
                        }

                        if (editando != null) {
                            editando.setNumero(numero);
                            editando.setBloco(bloco);
                            editando.setProprietarioId(proprietarioId);
                            apartamentoDAO.atualizar(editando);
                        } else {
                            Apartamento apt = new Apartamento(numero, bloco, proprietarioId);
                            apartamentoDAO.inserir(apt);
                        }

                        uiHandler.post(() -> {
                            if (isFinishing() || isDestroyed()) return;
                            String msg = editando == null ? "Unidade adicionada com sucesso" : "Unidade atualizada";
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                            carregarLista(etBusca.getText().toString());
                        });
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogOpcoesApt(Apartamento apt) {
        new AlertDialog.Builder(this)
                .setTitle("Apt " + apt.getNumero())
                .setItems(new String[]{"Ver leituras", "Editar", "Excluir"}, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(this, ApartamentoDetalheActivity.class);
                        intent.putExtra("apartamento_id", apt.getId());
                        startActivity(intent);
                    } else if (which == 1) {
                        executor.execute(() -> {
                            listaProprietarios = proprietarioDAO.listarTodos();
                            uiHandler.post(() -> {
                                if (!isFinishing() && !isDestroyed()) exibirDialogNovaUnidade(apt);
                            });
                        });
                    } else {
                        new AlertDialog.Builder(this)
                                .setTitle("Excluir apartamento?")
                                .setMessage("Todas as leituras do Apt " + apt.getNumero() + " também serão removidas.")
                                .setPositiveButton("Excluir", (d, w) -> {
                                    executor.execute(() -> {
                                        apartamentoDAO.deletar(apt.getId());
                                        uiHandler.post(() -> {
                                            if (isFinishing() || isDestroyed()) return;
                                            Toast.makeText(this, "Apartamento removido", Toast.LENGTH_SHORT).show();
                                            carregarLista(etBusca.getText().toString());
                                        });
                                    });
                                })
                                .setNegativeButton("Cancelar", null)
                                .show();
                    }
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}