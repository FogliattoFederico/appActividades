package frgp.utn.edu.actividadesdeiara.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import frgp.utn.edu.actividadesdeiara.R;
import frgp.utn.edu.actividadesdeiara.model.Tarea;

public class PapeleraAdapter extends RecyclerView.Adapter<PapeleraAdapter.PapeleraViewHolder> {

    public interface OnPapeleraActionListener {
        void onRestaurarTarea(Tarea tarea);
        void onEliminarDefinitivoTarea(Tarea tarea);
    }

    private final Context context;
    private List<Tarea> listaTareasPapelera;
    private final OnPapeleraActionListener listener;

    public PapeleraAdapter(Context context, List<Tarea> listaTareasPapelera, OnPapeleraActionListener listener) {
        this.context = context;
        this.listaTareasPapelera = listaTareasPapelera != null ? listaTareasPapelera : new ArrayList<>();
        this.listener = listener;
    }

    public void setListaTareasPapelera(List<Tarea> listaTareasPapelera) {
        this.listaTareasPapelera = listaTareasPapelera != null ? listaTareasPapelera : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PapeleraViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tarea_papelera, parent, false);
        return new PapeleraViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PapeleraViewHolder holder, int position) {
        Tarea tarea = listaTareasPapelera.get(position);

        holder.tvNombre.setText(tarea.getNombre());

        if (tarea.getDescripcion() != null && !tarea.getDescripcion().trim().isEmpty()) {
            holder.tvDescripcion.setText(tarea.getDescripcion());
            holder.tvDescripcion.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescripcion.setVisibility(View.GONE);
        }

        if (tarea.getFecha() != null && !tarea.getFecha().trim().isEmpty()) {
            holder.tvFecha.setText(tarea.getFecha().trim());
            holder.tvFecha.setVisibility(View.VISIBLE);
            if (holder.ivFecha != null) holder.ivFecha.setVisibility(View.VISIBLE);
        } else {
            holder.tvFecha.setVisibility(View.GONE);
            if (holder.ivFecha != null) holder.ivFecha.setVisibility(View.GONE);
        }

        if (tarea.getHorario() != null && !tarea.getHorario().trim().isEmpty()) {
            holder.tvHorario.setText(tarea.getHorario().trim());
            holder.tvHorario.setVisibility(View.VISIBLE);
            if (holder.ivHorario != null) holder.ivHorario.setVisibility(View.VISIBLE);
        } else {
            holder.tvHorario.setVisibility(View.GONE);
            if (holder.ivHorario != null) holder.ivHorario.setVisibility(View.GONE);
        }

        holder.btnRestaurar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRestaurarTarea(tarea);
            }
        });

        holder.btnEliminarDefinitivo.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEliminarDefinitivoTarea(tarea);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaTareasPapelera.size();
    }

    static class PapeleraViewHolder extends RecyclerView.ViewHolder {

        TextView tvNombre;
        TextView tvDescripcion;
        TextView tvFecha;
        TextView tvHorario;
        ImageView ivFecha;
        ImageView ivHorario;
        MaterialButton btnRestaurar;
        MaterialButton btnEliminarDefinitivo;

        public PapeleraViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreTareaPapelera);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionTareaPapelera);
            tvFecha = itemView.findViewById(R.id.tvFechaTareaPapelera);
            tvHorario = itemView.findViewById(R.id.tvHorarioTareaPapelera);
            ivFecha = itemView.findViewById(R.id.ivFechaTareaPapelera);
            ivHorario = itemView.findViewById(R.id.ivHorarioTareaPapelera);
            btnRestaurar = itemView.findViewById(R.id.btnRestaurarTarea);
            btnEliminarDefinitivo = itemView.findViewById(R.id.btnEliminarDefinitivoTarea);
        }
    }
}