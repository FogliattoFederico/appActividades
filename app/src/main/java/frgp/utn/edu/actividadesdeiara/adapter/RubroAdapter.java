package frgp.utn.edu.actividadesdeiara.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import frgp.utn.edu.actividadesdeiara.R;
import frgp.utn.edu.actividadesdeiara.model.Rubro;

public class RubroAdapter extends RecyclerView.Adapter<RubroAdapter.RubroViewHolder> {

    public interface OnRubroClickListener {
        void onRubroClick(Rubro rubro, int position);
        void onRubroLongClick(Rubro rubro);
    }

    private final Context context;
    private List<Rubro> listaRubros;
    private final OnRubroClickListener listener;
    private int posicionSeleccionada = -1;

    public RubroAdapter(Context context, List<Rubro> listaRubros, OnRubroClickListener listener) {
        this.context = context;
        this.listaRubros = listaRubros != null ? listaRubros : new ArrayList<>();
        this.listener = listener;
    }

    public void setListaRubros(List<Rubro> listaRubros) {
        this.listaRubros = listaRubros;
        notifyDataSetChanged();
    }

    public void setPosicionSeleccionada(int posicion) {
        this.posicionSeleccionada = posicion;
        notifyDataSetChanged();
    }

    public int getPosicionSeleccionada() {
        return posicionSeleccionada;
    }

    @NonNull
    @Override
    public RubroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rubro_card, parent, false);
        return new RubroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RubroViewHolder holder, int position) {
        Rubro rubro = listaRubros.get(position);

        holder.tvNombre.setText(rubro.getNombre());

        boolean seleccionado = (position == posicionSeleccionada);

        if (seleccionado) {
            holder.mcvRubro.setStrokeColor(ContextCompat.getColor(context, R.color.purple_500));
            holder.mcvRubro.setStrokeWidth(4);
            holder.mcvRubro.setCardBackgroundColor(ContextCompat.getColor(context, R.color.cat_docente_bg));
            holder.tvNombre.setTextColor(ContextCompat.getColor(context, R.color.purple_700));
        } else {
            holder.mcvRubro.setStrokeColor(ContextCompat.getColor(context, R.color.divider));
            holder.mcvRubro.setStrokeWidth(2);
            holder.mcvRubro.setCardBackgroundColor(Color.WHITE);
            holder.tvNombre.setTextColor(ContextCompat.getColor(context, R.color.black));
        }

        holder.itemView.setOnClickListener(v -> {
            posicionSeleccionada = holder.getAdapterPosition();
            notifyDataSetChanged();
            if (listener != null) {
                listener.onRubroClick(rubro, posicionSeleccionada);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onRubroLongClick(rubro);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return listaRubros.size();
    }

    static class RubroViewHolder extends RecyclerView.ViewHolder {

        MaterialCardView mcvRubro;
        TextView tvNombre;

        public RubroViewHolder(@NonNull View itemView) {
            super(itemView);
            mcvRubro = itemView.findViewById(R.id.mcvRubro);
            tvNombre = itemView.findViewById(R.id.tvNombreRubro);
        }
    }
}
