package frgp.utn.edu.actividadesdeiara.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import frgp.utn.edu.actividadesdeiara.R;
import frgp.utn.edu.actividadesdeiara.model.Tarea;

public class TareaAdapter extends RecyclerView.Adapter<TareaAdapter.TareaViewHolder> {

    public interface OnTareaClickListener {
        void onTareaClick(Tarea tarea);
        void onTareaLongClick(Tarea tarea);
        void onCompletadaToggle(Tarea tarea, boolean isChecked);
    }

    private final Context context;
    private List<Tarea> listaTareas;
    private final OnTareaClickListener listener;

    public TareaAdapter(Context context, List<Tarea> listaTareas, OnTareaClickListener listener) {
        this.context = context;
        this.listaTareas = listaTareas != null ? listaTareas : new ArrayList<>();
        this.listener = listener;
    }

    public void setListaTareas(List<Tarea> listaTareas) {
        this.listaTareas = listaTareas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TareaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tarea, parent, false);
        return new TareaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TareaViewHolder holder, int position) {
        Tarea tarea = listaTareas.get(position);

        holder.tvNombre.setText(tarea.getNombre());

        if (tarea.getDescripcion() != null && !tarea.getDescripcion().trim().isEmpty()) {
            holder.tvDescripcion.setText(tarea.getDescripcion());
            holder.tvDescripcion.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescripcion.setVisibility(View.GONE);
        }

        if (tarea.getFecha() != null && !tarea.getFecha().trim().isEmpty()) {
            holder.tvFecha.setText(tarea.getFecha());
            holder.tvFecha.setVisibility(View.VISIBLE);
            if (holder.ivFecha != null) holder.ivFecha.setVisibility(View.VISIBLE);
        } else {
            holder.tvFecha.setVisibility(View.GONE);
            if (holder.ivFecha != null) holder.ivFecha.setVisibility(View.GONE);
        }

        if (tarea.getHorario() != null && !tarea.getHorario().trim().isEmpty()) {
            holder.tvHorario.setText(tarea.getHorario());
            holder.tvHorario.setVisibility(View.VISIBLE);
            if (holder.ivHorario != null) holder.ivHorario.setVisibility(View.VISIBLE);
        } else {
            holder.tvHorario.setVisibility(View.GONE);
            if (holder.ivHorario != null) holder.ivHorario.setVisibility(View.GONE);
        }

        if (holder.ivRecordatorio != null) {
            holder.ivRecordatorio.setVisibility(tarea.isRecordatorio() ? View.VISIBLE : View.GONE);
        }

        holder.cbCompletada.setOnCheckedChangeListener(null);
        holder.cbCompletada.setChecked(tarea.isCompletada());

        if (tarea.isCompletada()) {
            holder.tvNombre.setPaintFlags(holder.tvNombre.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvNombre.setPaintFlags(holder.tvNombre.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        holder.cbCompletada.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onCompletadaToggle(tarea, isChecked);
            }
        });

        // Short click: edit/view task
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTareaClick(tarea);
            }
        });

        // Long click: delete task confirmation
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onTareaLongClick(tarea);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return listaTareas.size();
    }

    static class TareaViewHolder extends RecyclerView.ViewHolder {

        TextView tvNombre;
        TextView tvDescripcion;
        TextView tvFecha;
        TextView tvHorario;
        ImageView ivFecha;
        ImageView ivHorario;
        ImageView ivRecordatorio;
        CheckBox cbCompletada;

        public TareaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreTarea);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionTarea);
            tvFecha = itemView.findViewById(R.id.tvFechaTarea);
            tvHorario = itemView.findViewById(R.id.tvHorarioTarea);
            ivFecha = itemView.findViewById(R.id.ivFechaTarea);
            ivHorario = itemView.findViewById(R.id.ivHorarioTarea);
            ivRecordatorio = itemView.findViewById(R.id.ivRecordatorioTarea);
            cbCompletada = itemView.findViewById(R.id.cbCompletadaTarea);
        }
    }
}
