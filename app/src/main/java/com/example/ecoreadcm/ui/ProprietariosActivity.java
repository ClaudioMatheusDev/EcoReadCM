package com.example.ecoreadcm.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

public class ProprietariosActivity extends AppCompatActivity {

    private IProprietarioDAO proprietarioDAO;
    private IApartamentoDAO apartamentoDAO;
    private RecyclerView rvProprietarios;
    private EditText etBusca;
    private TextView tvContagem;

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
        List<Proprietario> lista = filtro.isEmpty()
                ? proprietarioDAO.listarTodos()
                : proprietarioDAO.buscarPorNome(filtro);

        for (Proprietario p : lista) {
            p.setApartamentos(apartamentoDAO.listarPorProprietario(p.getId()));
        }

        tvContagem.setText(lista.size() + " proprietário(s) cadastrado(s)");

        ProprietarioAdapter adapter = new ProprietarioAdapter(lista, proprietario -> {
            Intent intent = new Intent(this, ProprietarioDetalheActivity.class);
            intent.putExtra("proprietario_id", proprietario.getId());
            startActivity(intent);
        });
        adapter.setOnLongClickListener(proprietario -> mostrarDialogOpcoes(proprietario));
        rvProprietarios.setAdapter(adapter);
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

                    if (!editando && proprietarioDAO.cpfJaCadastrado(cpf)) {
                        Toast.makeText(this, "CPF já cadastrado", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (editando) {
                        existente.setNome(nome);
                        existente.setContato(contato);
                        proprietarioDAO.atualizar(existente);
                        Toast.makeText(this, "Proprietário atualizado", Toast.LENGTH_SHORT).show();
                    } else {
                        Proprietario novo = new Proprietario(nome, cpf, contato);
                        long id = proprietarioDAO.inserir(novo);
                        if (id > 0) Toast.makeText(this, "Proprietário cadastrado", Toast.LENGTH_SHORT).show();
                        else Toast.makeText(this, "Erro ao cadastrar", Toast.LENGTH_SHORT).show();
                    }
                    carregarLista(etBusca.getText().toString());
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
                    proprietarioDAO.deletar(proprietario.getId());
                    Toast.makeText(this, "Proprietário removido", Toast.LENGTH_SHORT).show();
                    carregarLista(etBusca.getText().toString());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}