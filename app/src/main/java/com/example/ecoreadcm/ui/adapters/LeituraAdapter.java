package com.example.ecoreadcm.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecoreadcm.R;
import com.example.ecoreadcm.model.Leitura;

import java.util.List;

public class LeituraAdapter extends RecyclerView.Adapter<LeituraAdapter.ViewHolder> {

    public interface OnClickListener { void onClick(Leitura leitura); }

    private final List<Leitura> lista;
    private final OnClickListener clickListener;

    public LeituraAdapter(List<Leitura> lista, OnClickListener clickListener) {
        this.lista = lista;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leitura, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Leitura l = lista.get(position);
        holder.tvMes.setText(l.getMesAbreviado());
        holder.tvAno.setText(String.valueOf(l.getAno()));
        holder.tvValorLuz.setText(String.format("%.1f kWh", l.getValorLuz()));
        holder.tvValorGas.setText(String.format("%.1f m³", l.getValorGas()));
        holder.itemView.setOnClickListener(v -> clickListener.onClick(l));
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMes, tvAno, tvValorLuz, tvValorGas;

        ViewHolder(View v) {
            super(v);
            tvMes = v.findViewById(R.id.tvMes);
            tvAno = v.findViewById(R.id.tvAno);
            tvValorLuz = v.findViewById(R.id.tvValorLuz);
            tvValorGas = v.findViewById(R.id.tvValorGas);
        }
    }
}