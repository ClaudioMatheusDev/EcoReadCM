package com.example.ecoreadcm.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecoreadcm.R;
import com.example.ecoreadcm.model.Apartamento;

import java.util.List;

public class ApartamentoAdapter extends RecyclerView.Adapter<ApartamentoAdapter.ViewHolder> {

    public interface OnClickListener { void onClick(Apartamento apartamento); }

    private final List<Apartamento> lista;
    private final OnClickListener clickListener;
    private OnClickListener longClickListener;

    public ApartamentoAdapter(List<Apartamento> lista, OnClickListener clickListener) {
        this.lista = lista;
        this.clickListener = clickListener;
    }

    public void setOnLongClickListener(OnClickListener l) { this.longClickListener = l; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_apartamento, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Apartamento apt = lista.get(position);
        holder.tvNumero.setText(apt.getNumero());
        holder.tvDescricao.setText(apt.getDescricaoCompleta());
        holder.tvProprietario.setText(apt.getProprietarioNome() != null
                ? apt.getProprietarioNome() : "");

        holder.itemView.setOnClickListener(v -> clickListener.onClick(apt));
        if (longClickListener != null) {
            holder.itemView.setOnLongClickListener(v -> {
                longClickListener.onClick(apt);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumero, tvDescricao, tvProprietario;

        ViewHolder(View v) {
            super(v);
            tvNumero = v.findViewById(R.id.tvNumero);
            tvDescricao = v.findViewById(R.id.tvDescricao);
            tvProprietario = v.findViewById(R.id.tvProprietario);
        }
    }
}