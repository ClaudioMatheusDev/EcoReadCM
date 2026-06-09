package com.example.ecoreadcm.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
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
import com.example.ecoreadcm.model.Proprietario;
import com.example.ecoreadcm.ui.adapters.ProprietarioAdapter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProprietariosActivity extends AppCompatActivity {

    private IProprietarioDAO proprietarioDAO;
    private IApartamentoDAO apartamentoDAO;
    private RecyclerView rvProprietarios;
    private EditText etBusca;
    private TextView tvContagem;
    private android.widget.TextView tvEmpty;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proprietarios);

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        proprietarioDAO = new ProprietarioDAOImpl(db);
        apartamentoDAO = new ApartamentoDAOImpl(db);

        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarLista("");
    }

    private void initViews() {
        ImageButton btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> finish());

        ImageButton fab = findViewById(R.id.fabNovo);
        fab.setOnClickListener(v -> mostrarDialogCadastro(null));

        tvContagem = findViewById(R.id.tvContagem);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvProprietarios = findViewById(R.id.rvProprietarios);
        rvProprietarios.setLayoutManager(new LinearLayoutManager(this));

        etBusca = findViewById(R.id.etBusca);
        etBusca.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                carregarLista(s.toString());
            }
            public void afterTextChanged(Editable s) {}
        });
    }

    private void carregarLista(String filtro) {
        executor.execute(() -> {
            List<Proprietario> lista = filtro.isEmpty()
                    ? proprietarioDAO.listarTodos()
                    : proprietarioDAO.buscarPorNome(filtro);

            for (Proprietario p : lista) {
                p.setApartamentos(apartamentoDAO.listarPorProprietario(p.getId()));
            }

            uiHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                tvContagem.setText(lista.size() + " proprietário(s) cadastrado(s)");
                tvEmpty.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
                rvProprietarios.setVisibility(lista.isEmpty() ? View.GONE : View.VISIBLE);

                ProprietarioAdapter adapter = new ProprietarioAdapter(lista, proprietario -> {
                    Intent intent = new Intent(this, ProprietarioDetalheActivity.class);
                    intent.putExtra("proprietario_id", proprietario.getId());
                    startActivity(intent);
                });
                adapter.setOnLongClickListener(proprietario -> mostrarDialogOpcoes(proprietario));
                rvProprietarios.setAdapter(adapter);
            });
        });
    }

    private void mostrarDialogCadastro(Proprietario existente) {
        boolean editando = existente != null;
        android.view.LayoutInflater inflater = android.view.LayoutInflater.from(this);
        android.view.View view = inflater.inflate(R.layout.dialog_proprietario, null);

        EditText etNome = view.findViewById(R.id.etNome);
        EditText etCpf = view.findViewById(R.id.etCpf);
        EditText etContato = view.findViewById(R.id.etContato);

        if (editando) {
            etNome.setText(existente.getNome());
            etCpf.setText(existente.getCpf());
            etContato.setText(existente.getContato());
            etCpf.setEnabled(false);
        }

        new AlertDialog.Builder(this)
                .setTitle(editando ? "Editar Proprietário" : "Novo Proprietário")
                .setView(view)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String nome = etNome.getText().toString().trim();
                    String cpf = etCpf.getText().toString().trim();
                    String contato = etContato.getText().toString().trim();

                    if (nome.isEmpty() || cpf.isEmpty()) {
                        Toast.makeText(this, "Nome e CPF são obrigatórios", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!editando && !validarCpf(cpf)) {
                        Toast.makeText(this, getString(R.string.cpf_invalido), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Normaliza CPF para apenas dígitos antes de salvar
                    final String cpfLimpo = cpf.replaceAll("[^0-9]", "");

                    if (!editando && proprietarioDAO.cpfJaCadastrado(cpfLimpo)) {
                        Toast.makeText(this, "CPF já cadastrado", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (editando) {
                        existente.setNome(nome);
                        existente.setContato(contato);
                        executor.execute(() -> {
                            proprietarioDAO.atualizar(existente);
                            uiHandler.post(() -> {
                                if (isFinishing() || isDestroyed()) return;
                                Toast.makeText(this, "Proprietário atualizado", Toast.LENGTH_SHORT).show();
                                carregarLista(etBusca.getText().toString());
                            });
                        });
                    } else {
                        Proprietario novo = new Proprietario(nome, cpfLimpo, contato);
                        executor.execute(() -> {
                            long id = proprietarioDAO.inserir(novo);
                            uiHandler.post(() -> {
                                if (isFinishing() || isDestroyed()) return;
                                if (id > 0) Toast.makeText(this, "Proprietário cadastrado", Toast.LENGTH_SHORT).show();
                                else Toast.makeText(this, "Erro ao cadastrar", Toast.LENGTH_SHORT).show();
                                carregarLista(etBusca.getText().toString());
                            });
                        });
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogOpcoes(Proprietario proprietario) {
        new AlertDialog.Builder(this)
                .setTitle(proprietario.getNome())
                .setItems(new String[]{"Editar", "Excluir"}, (dialog, which) -> {
                    if (which == 0) mostrarDialogCadastro(proprietario);
                    else confirmarExclusao(proprietario);
                })
                .show();
    }

    private void confirmarExclusao(Proprietario proprietario) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir proprietário?")
                .setMessage("Todos os apartamentos vinculados também serão removidos.")
                .setPositiveButton("Excluir", (d, w) -> {
                    executor.execute(() -> {
                        proprietarioDAO.deletar(proprietario.getId());
                        uiHandler.post(() -> {
                            if (isFinishing() || isDestroyed()) return;
                            Toast.makeText(this, "Proprietário removido", Toast.LENGTH_SHORT).show();
                            carregarLista(etBusca.getText().toString());
                        });
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Valida CPF pelo algoritmo dos dígitos verificadores (Módulo 11).
     * Aceita CPF com ou sem formatação (xxx.xxx.xxx-xx).
     */
    private static boolean validarCpf(String cpf) {
        cpf = cpf.replaceAll("[^0-9]", "");
        if (cpf.length() != 11) return false;
        if (cpf.matches("(\\d)\\1{10}")) return false; // rejeita 000...0, 111...1, etc.

        int soma = 0;
        for (int i = 0; i < 9; i++) soma += (cpf.charAt(i) - '0') * (10 - i);
        int primeiro = 11 - (soma % 11);
        if (primeiro >= 10) primeiro = 0;
        if (primeiro != (cpf.charAt(9) - '0')) return false;

        soma = 0;
        for (int i = 0; i < 10; i++) soma += (cpf.charAt(i) - '0') * (11 - i);
        int segundo = 11 - (soma % 11);
        if (segundo >= 10) segundo = 0;
        return segundo == (cpf.charAt(10) - '0');
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}