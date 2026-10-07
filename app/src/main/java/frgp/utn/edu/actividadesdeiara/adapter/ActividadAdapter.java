package frgp.utn.edu.actividadesdeiara.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import frgp.utn.edu.actividadesdeiara.R;
import frgp.utn.edu.actividadesdeiara.model.Actividad;

public class ActividadAdapter extends RecyclerView.Adapter<ActividadAdapter.ActividadViewHolder> {

    public interface OnActividadClickListener {
        void onActividadClick(Actividad actividad);
        void onActividadLongClick(Actividad actividad);
        void onCompletadaToggle(Actividad actividad, boolean isChecked);
        void onAbonadaToggle(Actividad actividad, boolean isChecked);
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

        if (holder.tvLugarAtencion != null) {
            if (actividad.isAtencionDomicilio()) {
                holder.tvLugarAtencion.setText("🏡 Atención a Domicilio");
                holder.tvLugarAtencion.setVisibility(View.VISIBLE);
                holder.tvLugarAtencion.setOnClickListener(v ->
                        Toast.makeText(context, "Atención agendada en domicilio", Toast.LENGTH_SHORT).show()
                );
            } else if (actividad.getDireccionAtencion() != null && !actividad.getDireccionAtencion().trim().isEmpty()) {
                String direccion = actividad.getDireccionAtencion().trim();
                holder.tvLugarAtencion.setText("📍 " + direccion + " (Abrir en Maps)");
                holder.tvLugarAtencion.setVisibility(View.VISIBLE);
                holder.tvLugarAtencion.setOnClickListener(v -> abrirGoogleMaps(direccion));
            } else {
                holder.tvLugarAtencion.setVisibility(View.GONE);
            }
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

        if (holder.ivRecordatorio != null) {
            holder.ivRecordatorio.setVisibility(actividad.isRecordatorio() ? View.VISIBLE : View.GONE);
        }

        if (actividad.getDescripcion() != null && !actividad.getDescripcion().trim().isEmpty()) {
            holder.tvDescripcion.setText(actividad.getDescripcion());
            holder.tvDescripcion.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescripcion.setVisibility(View.GONE);
        }

        // Category Styling
        aplicarEstiloCategoria(holder, actividad);

        // Payment / Abonada checkbox
        if (holder.cbAbonada != null) {
            holder.cbAbonada.setOnCheckedChangeListener(null);
            holder.cbAbonada.setChecked(actividad.isAbonada());
            holder.cbAbonada.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onAbonadaToggle(actividad, isChecked);
                }
            });
        }

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

    private void aplicarEstiloCategoria(ActividadViewHolder holder, Actividad actividad) {
        String cat = actividad.getCategoria() != null ? actividad.getCategoria().trim().toLowerCase(Locale.getDefault()) : "";
        String rubro = actividad.getRubro() != null ? actividad.getRubro().trim().toLowerCase(Locale.getDefault()) : "";

        String textoMostrar = (actividad.getRubro() != null && !actividad.getRubro().trim().isEmpty())
                ? actividad.getRubro().trim()
                : ((actividad.getCategoria() != null && !actividad.getCategoria().trim().isEmpty()) ? actividad.getCategoria().trim() : "Médico");

        holder.tvCategory.setText(textoMostrar);

        int textColor;
        int bgColor;
        int iconRes;

        if (cat.contains("física") || cat.contains("fisica") || cat.contains("deport") || cat.contains("gym") || cat.contains("gimnas") || cat.contains("entren") ||
            cat.contains("kinesi") || cat.contains("pilates") || cat.contains("yoga") || cat.contains("natac") || cat.contains("fisiot") || cat.contains("rehabilit") ||
            cat.contains("futbol") || cat.contains("fútbol") || cat.contains("padel") || cat.contains("tenis") || cat.contains("zumba") || cat.contains("spin") || cat.contains("crossfit") ||
            cat.contains("baile") || cat.contains("danza") || cat.contains("run") || cat.contains("caminat") || cat.contains("musculac") ||
            rubro.contains("física") || rubro.contains("fisica") || rubro.contains("deport") || rubro.contains("gym") || rubro.contains("gimnas") || rubro.contains("entren") ||
            rubro.contains("kinesi") || rubro.contains("pilates") || rubro.contains("yoga") || rubro.contains("natac") || rubro.contains("fisiot") || rubro.contains("rehabilit") ||
            rubro.contains("futbol") || rubro.contains("fútbol") || rubro.contains("padel") || rubro.contains("tenis") || rubro.contains("zumba") || rubro.contains("spin") || rubro.contains("crossfit") ||
            rubro.contains("baile") || rubro.contains("danza") || rubro.contains("run") || rubro.contains("caminat") || rubro.contains("musculac")) {
            textColor = ContextCompat.getColor(context, R.color.cat_fisica);
            bgColor = ContextCompat.getColor(context, R.color.cat_fisica_bg);
            iconRes = R.drawable.ic_fitness;
        } else if (cat.contains("docente") || cat.contains("escuela") || cat.contains("estudio") || cat.contains("profesor") || cat.contains("curso") || cat.contains("clase") ||
                   rubro.contains("docente") || rubro.contains("escuela") || rubro.contains("estudio") || rubro.contains("profesor") || rubro.contains("curso") || rubro.contains("clase")) {
            textColor = ContextCompat.getColor(context, R.color.cat_docente);
            bgColor = ContextCompat.getColor(context, R.color.cat_docente_bg);
            iconRes = R.drawable.ic_school;
        } else if (cat.contains("profesional") || cat.contains("abogad") || cat.contains("contador") || cat.contains("psicolog") || cat.contains("trabajo") || cat.contains("tramite") ||
                   rubro.contains("profesional") || rubro.contains("abogad") || rubro.contains("contador") || rubro.contains("psicolog") || rubro.contains("trabajo") || rubro.contains("tramite")) {
            textColor = ContextCompat.getColor(context, R.color.cat_profesional);
            bgColor = ContextCompat.getColor(context, R.color.cat_profesional_bg);
            iconRes = R.drawable.ic_professional;
        } else {
            // Médico / Salud / Default
            textColor = ContextCompat.getColor(context, R.color.cat_medico);
            bgColor = ContextCompat.getColor(context, R.color.cat_medico_bg);
            iconRes = R.drawable.ic_medical;
        }

        holder.tvCategory.setTextColor(textColor);
        holder.ivCategoryIcon.setImageResource(iconRes);
        holder.ivCategoryIcon.setImageTintList(ColorStateList.valueOf(textColor));

        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setCornerRadius(16f);
        badgeBg.setColor(bgColor);
        holder.llCategoryBadge.setBackground(badgeBg);
    }

    private void abrirGoogleMaps(String direccion) {
        if (direccion == null || direccion.trim().isEmpty()) return;
        try {
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(direccion));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(mapIntent);
            } else {
                Intent genericMapIntent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(direccion))
                );
                context.startActivity(genericMapIntent);
            }
        } catch (Exception e) {
            Toast.makeText(context, "No se pudo abrir mapas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
        ImageView ivRecordatorio;
        TextView tvCategory;
        TextView tvTitulo;
        TextView tvResponsable;
        TextView tvLugarAtencion;
        TextView tvFecha;
        TextView tvHora;
        TextView tvDescripcion;
        CheckBox cbCompletada;
        CheckBox cbAbonada;

        public ActividadViewHolder(@NonNull View itemView) {
            super(itemView);
            llCategoryBadge = itemView.findViewById(R.id.llCategoryBadge);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            ivFecha = itemView.findViewById(R.id.ivFecha);
            ivHora = itemView.findViewById(R.id.ivHora);
            ivRecordatorio = itemView.findViewById(R.id.ivRecordatorioActividad);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvResponsable = itemView.findViewById(R.id.tvResponsable);
            tvLugarAtencion = itemView.findViewById(R.id.tvLugarAtencion);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvHora = itemView.findViewById(R.id.tvHora);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            cbCompletada = itemView.findViewById(R.id.cbCompletada);
            cbAbonada = itemView.findViewById(R.id.cbAbonada);
        }
    }
}
