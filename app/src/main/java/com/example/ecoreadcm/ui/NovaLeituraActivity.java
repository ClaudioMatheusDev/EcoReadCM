package com.example.ecoreadcm.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ecoreadcm.R;
import com.example.ecoreadcm.dao.IApartamentoDAO;
import com.example.ecoreadcm.dao.ILeituraDAO;
import com.example.ecoreadcm.dao.impl.ApartamentoDAOImpl;
import com.example.ecoreadcm.dao.impl.LeituraDAOImpl;
import com.example.ecoreadcm.database.DatabaseHelper;
import com.example.ecoreadcm.model.Apartamento;
import com.example.ecoreadcm.model.Leitura;

import java.util.Calendar;
import java.util.List;

public class NovaLeituraActivity extends AppCompatActivity {

    private IApartamentoDAO apartamentoDAO;
    private ILeituraDAO leituraDAO;

    private List<Apartamento> listaApartamentos;
    private Spinner spinnerApartamento, spinnerMes, spinnerAno;
    private EditText etValorLuz, etValorGas;
    private TextView tvAviso;
    private Button btnSalvar;

    private long apartamentoPreSelecionado = -1;

    private static final String[] MESES_NOMES = {
            "Janeiro","Fevereiro","Março","Abril","Maio","Junho",
            "Julho","Agosto","Setembro","Outubro","Novembro","Dezembro"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nova_leitura);

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        apartamentoDAO = new ApartamentoDAOImpl(db);
        leituraDAO = new LeituraDAOImpl(db);

        apartamentoPreSelecionado = getIntent().getLongExtra("apartamento_id", -1);
        initViews();
        carregarSpinners();
    }

    private void initViews() {
        ImageButton btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> finish());

        spinnerApartamento = findViewById(R.id.spinnerApartamento);
        spinnerMes = findViewById(R.id.spinnerMes);
        spinnerAno = findViewById(R.id.spinnerAno);
        etValorLuz = findViewById(R.id.etValorLuz);
        etValorGas = findViewById(R.id.etValorGas);
        tvAviso = findViewById(R.id.tvAviso);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnSalvar.setOnClickListener(v -> salvarLeitura());
    }

    private void carregarSpinners() {
        // Apartamentos
        listaApartamentos = apartamentoDAO.listarTodos();
        if (listaApartamentos.isEmpty()) {
            Toast.makeText(this, "Cadastre apartamentos antes de registrar leituras", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String[] aptNomes = new String[listaApartamentos.size()];
        int selecaoInicial = 0;
        for (int i = 0; i < listaApartamentos.size(); i++) {
            Apartamento apt = listaApartamentos.get(i);
            aptNomes[i] = apt.getDescricaoCompleta() +
                    (apt.getProprietarioNome() != null ? " – " + apt.getProprietarioNome() : "");
            if (apt.getId() == apartamentoPreSelecionado) selecaoInicial = i;
        }
        ArrayAdapter<String> aptAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, aptNomes);
        aptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerApartamento.setAdapter(aptAdapter);
        spinnerApartamento.setSelection(selecaoInicial);
        spinnerApartamento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { verificarDuplicidade(); }
            public void onNothingSelected(AdapterView<?> p) {}
        });

        // Meses
        ArrayAdapter<String> mesAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, MESES_NOMES);
        mesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMes.setAdapter(mesAdapter);
        Calendar cal = Calendar.getInstance();
        spinnerMes.setSelection(cal.get(Calendar.MONTH));
        spinnerMes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { verificarDuplicidade(); }
            public void onNothingSelected(AdapterView<?> p) {}
        });

        // Anos
        int anoAtual = cal.get(Calendar.YEAR);
        String[] anos = {String.valueOf(anoAtual - 1), String.valueOf(anoAtual), String.valueOf(anoAtual + 1)};
        ArrayAdapter<String> anoAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, anos);
        anoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAno.setAdapter(anoAdapter);
        spinnerAno.setSelection(1);
        spinnerAno.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { verificarDuplicidade(); }
            public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    private void verificarDuplicidade() {
        int aptPos = spinnerApartamento.getSelectedItemPosition();
        if (aptPos < 0 || aptPos >= listaApartamentos.size()) return;

        long aptId = listaApartamentos.get(aptPos).getId();
        int mes = spinnerMes.getSelectedItemPosition() + 1;
        int ano = Integer.parseInt(spinnerAno.getSelectedItem().toString());

        if (leituraDAO.leituraJaExiste(aptId, mes, ano)) {
            tvAviso.setVisibility(View.VISIBLE);
            tvAviso.setText("⚠ Já existe leitura para " + MESES_NOMES[mes - 1] + "/" + ano +
                    ". Salvar irá substituí-la.");
        } else {
            tvAviso.setVisibility(View.GONE);
        }
    }

    private void salvarLeitura() {
        int aptPos = spinnerApartamento.getSelectedItemPosition();
        if (aptPos < 0) { Toast.makeText(this, "Selecione um apartamento", Toast.LENGTH_SHORT).show(); return; }

        String strLuz = etValorLuz.getText().toString().trim();
        String strGas = etValorGas.getText().toString().trim();

        if (strLuz.isEmpty() || strGas.isEmpty()) {
            Toast.makeText(this, "Preencha os valores de luz e gás", Toast.LENGTH_SHORT).show();
            return;
        }

        double valorLuz, valorGas;
        try {
            valorLuz = Double.parseDouble(strLuz);
            valorGas = Double.parseDouble(strGas);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Valores numéricos inválidos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (valorLuz < 0 || valorGas < 0) {
            Toast.makeText(this, "Valores não podem ser negativos", Toast.LENGTH_SHORT).show();
            return;
        }

        long aptId = listaApartamentos.get(aptPos).getId();

        // RF integridade: verificar se apartamento existe
        if (!apartamentoDAO.apartamentoExiste(aptId)) {
            Toast.makeText(this, "Apartamento inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        int mes = spinnerMes.getSelectedItemPosition() + 1;
        int ano = Integer.parseInt(spinnerAno.getSelectedItem().toString());

        // Se já existe, atualizar
        Leitura existente = leituraDAO.buscarPorApartamentoMesAno(aptId, mes, ano);
        if (existente != null) {
            existente.setValorLuz(valorLuz);
            existente.setValorGas(valorGas);
            leituraDAO.atualizar(existente);
            Toast.makeText(this, "Leitura atualizada com sucesso!", Toast.LENGTH_SHORT).show();
        } else {
            Leitura nova = new Leitura(aptId, mes, ano, valorLuz, valorGas);
            long id = leituraDAO.inserir(nova);
            if (id > 0) Toast.makeText(this, "Leitura registrada com sucesso!", Toast.LENGTH_SHORT).show();
            else { Toast.makeText(this, "Erro ao salvar leitura", Toast.LENGTH_SHORT).show(); return; }
        }
        finish();
    }
}