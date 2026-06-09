package com.example.ecoreadcm.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecoreadcm.R;
import com.example.ecoreadcm.model.Apartamento;
import com.example.ecoreadcm.model.Proprietario;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class ProprietarioAdapter extends RecyclerView.Adapter<ProprietarioAdapter.ViewHolder> {

    public interface OnClickListener { void onClick(Proprietario proprietario); }

    private final List<Proprietario> lista;
    private final OnClickListener clickListener;
    private OnClickListener longClickListener;

    public ProprietarioAdapter(List<Proprietario> lista, OnClickListener clickListener) {
        this.lista = lista;
        this.clickListener = clickListener;
    }

    public void setOnLongClickListener(OnClickListener l) { this.longClickListener = l; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_proprietario, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Proprietario p = lista.get(position);
        holder.tvIniciais.setText(p.getIniciais());
        holder.tvNome.setText(p.getNome());
        holder.tvCpf.setText(p.getCpf());

        holder.chipGroup.removeAllViews();
        for (Apartamento apt : p.getApartamentos()) {
            Chip chip = new Chip(holder.itemView.getContext());
            chip.setText("Apt " + apt.getNumero());
            chip.setClickable(false);
            chip.setCheckable(false);
            holder.chipGroup.addView(chip);
        }
        if (p.getApartamentos().isEmpty()) {
            Chip chip = new Chip(holder.itemView.getContext());
            chip.setText("Sem unidades");
            chip.setClickable(false);
            chip.setCheckable(false);
            holder.chipGroup.addView(chip);
        }

        holder.itemView.setOnClickListener(v -> clickListener.onClick(p));
        if (longClickListener != null) {
            holder.itemView.setOnLongClickListener(v -> {
                longClickListener.onClick(p);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIniciais, tvNome, tvCpf;
        ChipGroup chipGroup;

        ViewHolder(View itemView) {
            super(itemView);
            tvIniciais = itemView.findViewById(R.id.tvIniciais);
            tvNome = itemView.findViewById(R.id.tvNome);
            tvCpf = itemView.findViewById(R.id.tvCpf);
            chipGroup = itemView.findViewById(R.id.chipGroup);
        }
    }
}