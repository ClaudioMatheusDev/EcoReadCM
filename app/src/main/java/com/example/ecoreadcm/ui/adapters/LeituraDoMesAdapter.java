package com.example.ecoreadcm.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecoreadcm.R;
import com.example.ecoreadcm.model.Apartamento;
import com.example.ecoreadcm.model.Leitura;

import java.util.List;

public class LeituraDoMesAdapter extends RecyclerView.Adapter<LeituraDoMesAdapter.ViewHolder> {

    public static class ItemLeituraDoMes {
        public final Apartamento apartamento;
        public final Leitura leitura; // null = não coletada

        public ItemLeituraDoMes(Apartamento apartamento, Leitura leitura) {
            this.apartamento = apartamento;
            this.leitura = leitura;
        }

        public boolean isColetada() { return leitura != null; }
    }

    public interface OnClickListener { void onClick(ItemLeituraDoMes item); }

    private final List<ItemLeituraDoMes> lista;
    private final OnClickListener clickListener;

    public LeituraDoMesAdapter(List<ItemLeituraDoMes> lista, OnClickListener clickListener) {
        this.lista = lista;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leitura_do_mes, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemLeituraDoMes item = lista.get(position);
        Apartamento apt = item.apartamento;

        holder.tvNumeroApt.setText(apt.getNumero());
        holder.tvNomeApt.setText("Apartamento " + apt.getNumero());
        holder.tvProprietario.setText(apt.getProprietarioNome() != null ? apt.getProprietarioNome() : "");

        if (item.isColetada()) {
            holder.tvStatus.setText("OK");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_ok);
            holder.tvValorLuz.setVisibility(View.VISIBLE);
            holder.tvValorGas.setVisibility(View.VISIBLE);
            holder.tvValorLuz.setText(String.format("%.1f kWh", item.leitura.getValorLuz()));
            holder.tvValorGas.setText(String.format("%.1f m³", item.leitura.getValorGas()));
            holder.tvNumeroApt.setBackgroundResource(R.drawable.bg_apt_coletado);
        } else {
            holder.tvStatus.setText("Pendente");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pendente);
            holder.tvValorLuz.setVisibility(View.GONE);
            holder.tvValorGas.setVisibility(View.GONE);
            holder.tvNumeroApt.setBackgroundResource(R.drawable.bg_apt_pendente);
        }

        holder.itemView.setOnClickListener(v -> clickListener.onClick(item));
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumeroApt, tvNomeApt, tvProprietario, tvValorLuz, tvValorGas, tvStatus;

        ViewHolder(View v) {
            super(v);
            tvNumeroApt = v.findViewById(R.id.tvNumeroApt);
            tvNomeApt = v.findViewById(R.id.tvNomeApt);
            tvProprietario = v.findViewById(R.id.tvProprietario);
            tvValorLuz = v.findViewById(R.id.tvValorLuz);
            tvValorGas = v.findViewById(R.id.tvValorGas);
            tvStatus = v.findViewById(R.id.tvStatus);
        }
    }
}