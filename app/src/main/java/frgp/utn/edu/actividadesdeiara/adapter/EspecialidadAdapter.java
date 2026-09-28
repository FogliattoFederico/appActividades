package frgp.utn.edu.actividadesdeiara.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import frgp.utn.edu.actividadesdeiara.R;
import frgp.utn.edu.actividadesdeiara.model.Especialidad;

public class EspecialidadAdapter extends RecyclerView.Adapter<EspecialidadAdapter.EspecialidadViewHolder> {

    public interface OnEspecialidadClickListener {
        void onEspecialidadClick(Especialidad especialidad);
        void onEspecialidadLongClick(Especialidad especialidad);
    }

    private final Context context;
    private List<Especialidad> listaEspecialidades;
    private final OnEspecialidadClickListener listener;

    public EspecialidadAdapter(Context context, List<Especialidad> listaEspecialidades, OnEspecialidadClickListener listener) {
        this.context = context;
        this.listaEspecialidades = listaEspecialidades != null ? listaEspecialidades : new ArrayList<>();
        this.listener = listener;
    }

    public void setListaEspecialidades(List<Especialidad> listaEspecialidades) {
        this.listaEspecialidades = listaEspecialidades;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EspecialidadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_especialidad_card, parent, false);
        return new EspecialidadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EspecialidadViewHolder holder, int position) {
        Especialidad especialidad = listaEspecialidades.get(position);

        holder.tvNombre.setText(especialidad.getNombre());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEspecialidadClick(especialidad);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onEspecialidadLongClick(especialidad);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return listaEspecialidades.size();
    }

    static class EspecialidadViewHolder extends RecyclerView.ViewHolder {

        TextView tvNombre;

        public EspecialidadViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreEspecialidad);
        }
    }
}
