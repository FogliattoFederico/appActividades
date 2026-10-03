package frgp.utn.edu.actividadesdeiara.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import frgp.utn.edu.actividadesdeiara.R;
import frgp.utn.edu.actividadesdeiara.model.Actividad;

public class ActividadAdapter extends RecyclerView.Adapter<ActividadAdapter.ActividadViewHolder> {

    public interface OnActividadClickListener {
        void onActividadClick(Actividad actividad);
        void onActividadLongClick(Actividad actividad);
        void onCompletadaToggle(Actividad actividad, boolean isChecked);
    }

    private final Context context;
    private List<Actividad> listaActividades;
    private final OnActividadClickListener listener;

    public ActividadAdapter(Context context, List<Actividad> listaActividades, OnActividadClickListener listener) {
        this.context = context;
        this.listaActividades = listaActividades != null ? listaActividades : new ArrayList<>();
        this.listener = listener;
    }

    public void setListaActividades(List<Actividad> listaActividades) {
        this.listaActividades = listaActividades;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActividadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_actividad, parent, false);
        return new ActividadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActividadViewHolder holder, int position) {
        Actividad actividad = listaActividades.get(position);

        holder.tvTitulo.setText(actividad.getTitulo());

        if (actividad.getResponsable() != null && !actividad.getResponsable().trim().isEmpty()) {
            holder.tvResponsable.setText("Responsable: " + actividad.getResponsable());
            holder.tvResponsable.setVisibility(View.VISIBLE);
        } else {
            holder.tvResponsable.setVisibility(View.GONE);
        }

        if (actividad.getFecha() != null && !actividad.getFecha().trim().isEmpty()) {
            holder.tvFecha.setText(actividad.getFecha().trim());
            holder.tvFecha.setVisibility(View.VISIBLE);
            if (holder.ivFecha != null) holder.ivFecha.setVisibility(View.VISIBLE);
        } else {
            holder.tvFecha.setVisibility(View.GONE);
            if (holder.ivFecha != null) holder.ivFecha.setVisibility(View.GONE);
        }

        if (actividad.getHora() != null && !actividad.getHora().trim().isEmpty()) {
            holder.tvHora.setText(actividad.getHora().trim());
            holder.tvHora.setVisibility(View.VISIBLE);
            if (holder.ivHora != null) holder.ivHora.setVisibility(View.VISIBLE);
        } else {
            holder.tvHora.setVisibility(View.GONE);
            if (holder.ivHora != null) holder.ivHora.setVisibility(View.GONE);
        }

        if (actividad.getDescripcion() != null && !actividad.getDescripcion().trim().isEmpty()) {
            holder.tvDescripcion.setText(actividad.getDescripcion());
            holder.tvDescripcion.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescripcion.setVisibility(View.GONE);
        }

        // Category Styling
        String cat = actividad.getCategoria() != null ? actividad.getCategoria() : "Médico";
        holder.tvCategory.setText(cat);
        int textColor;
        int bgColor;
        int iconRes;

        switch (cat) {
            case "Profesional":
                textColor = ContextCompat.getColor(context, R.color.cat_profesional);
                bgColor = ContextCompat.getColor(context, R.color.cat_profesional_bg);
                iconRes = R.drawable.ic_professional;
                break;
            case "Docente":
                textColor = ContextCompat.getColor(context, R.color.cat_docente);
                bgColor = ContextCompat.getColor(context, R.color.cat_docente_bg);
                iconRes = R.drawable.ic_school;
                break;
            case "Actividad Física":
                textColor = ContextCompat.getColor(context, R.color.cat_fisica);
                bgColor = ContextCompat.getColor(context, R.color.cat_fisica_bg);
                iconRes = R.drawable.ic_fitness;
                break;
            case "Médico":
            default:
                textColor = ContextCompat.getColor(context, R.color.cat_medico);
                bgColor = ContextCompat.getColor(context, R.color.cat_medico_bg);
                iconRes = R.drawable.ic_medical;
                break;
        }

        holder.tvCategory.setTextColor(textColor);
        holder.ivCategoryIcon.setImageResource(iconRes);
        holder.ivCategoryIcon.setImageTintList(ColorStateList.valueOf(textColor));

        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setCornerRadius(16f);
        badgeBg.setColor(bgColor);
        holder.llCategoryBadge.setBackground(badgeBg);

        // Completion checkbox
        holder.cbCompletada.setOnCheckedChangeListener(null);
        holder.cbCompletada.setChecked(actividad.isCompletada());

        if (actividad.isCompletada()) {
            holder.tvTitulo.setPaintFlags(holder.tvTitulo.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvTitulo.setPaintFlags(holder.tvTitulo.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        holder.cbCompletada.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onCompletadaToggle(actividad, isChecked);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onActividadClick(actividad);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onActividadLongClick(actividad);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return listaActividades.size();
    }

    static class ActividadViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llCategoryBadge;
        ImageView ivCategoryIcon;
        ImageView ivFecha;
        ImageView ivHora;
        TextView tvCategory;
        TextView tvTitulo;
        TextView tvResponsable;
        TextView tvFecha;
        TextView tvHora;
        TextView tvDescripcion;
        CheckBox cbCompletada;

        public ActividadViewHolder(@NonNull View itemView) {
            super(itemView);
            llCategoryBadge = itemView.findViewById(R.id.llCategoryBadge);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            ivFecha = itemView.findViewById(R.id.ivFecha);
            ivHora = itemView.findViewById(R.id.ivHora);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvResponsable = itemView.findViewById(R.id.tvResponsable);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvHora = itemView.findViewById(R.id.tvHora);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            cbCompletada = itemView.findViewById(R.id.cbCompletada);
        }
    }
}
