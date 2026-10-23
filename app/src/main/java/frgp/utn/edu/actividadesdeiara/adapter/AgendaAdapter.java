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
import frgp.utn.edu.actividadesdeiara.model.ElementoAgenda;
import frgp.utn.edu.actividadesdeiara.model.Tarea;

public class AgendaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnAgendaClickListener {
        void onActividadClick(Actividad actividad);
        void onActividadLongClick(Actividad actividad);
        void onActividadCompletadaToggle(Actividad actividad, boolean isChecked);
        void onActividadAbonadaToggle(Actividad actividad, boolean isChecked);

        void onTareaClick(Tarea tarea);
        void onTareaLongClick(Tarea tarea);
        void onTareaCompletadaToggle(Tarea tarea, boolean isChecked);
    }

    private final Context context;
    private List<ElementoAgenda> listaElementos;
    private final OnAgendaClickListener listener;

    public AgendaAdapter(Context context, List<ElementoAgenda> listaElementos, OnAgendaClickListener listener) {
        this.context = context;
        this.listaElementos = listaElementos != null ? listaElementos : new ArrayList<>();
        this.listener = listener;
    }

    public void setListaElementos(List<ElementoAgenda> listaElementos) {
        this.listaElementos = listaElementos != null ? listaElementos : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return listaElementos.get(position).getTipo();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ElementoAgenda.TIPO_ACTIVIDAD) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_actividad, parent, false);
            return new ActividadViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_tarea, parent, false);
            return new TareaViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ElementoAgenda item = listaElementos.get(position);

        if (getItemViewType(position) == ElementoAgenda.TIPO_ACTIVIDAD) {
            bindActividad((ActividadViewHolder) holder, item.getActividad());
        } else {
            bindTarea((TareaViewHolder) holder, item.getTarea());
        }
    }

    private void bindActividad(ActividadViewHolder holder, Actividad actividad) {
        if (actividad == null) return;

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

        aplicarEstiloCategoria(holder, actividad);

        if (holder.cbAbonada != null) {
            holder.cbAbonada.setOnCheckedChangeListener(null);
            holder.cbAbonada.setChecked(actividad.isAbonada());
            holder.cbAbonada.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onActividadAbonadaToggle(actividad, isChecked);
                }
            });
        }

        holder.cbCompletada.setOnCheckedChangeListener(null);
        holder.cbCompletada.setChecked(actividad.isCompletada());

        if (actividad.isCompletada()) {
            holder.tvTitulo.setPaintFlags(holder.tvTitulo.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvTitulo.setPaintFlags(holder.tvTitulo.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        holder.cbCompletada.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onActividadCompletadaToggle(actividad, isChecked);
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

    private void bindTarea(TareaViewHolder holder, Tarea tarea) {
        if (tarea == null) return;

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
                listener.onTareaCompletadaToggle(tarea, isChecked);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTareaClick(tarea);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onTareaLongClick(tarea);
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

        if (cat.contains("arte") || cat.contains("artístic") || cat.contains("artistico") || cat.contains("pintur") || cat.contains("canto") || cat.contains("música") || cat.contains("musica") || cat.contains("teatro") || cat.contains("dibujo") || cat.contains("escultura") || cat.contains("manualidad") || cat.contains("fotograf") || cat.contains("diseño") || cat.contains("guitarr") || cat.contains("piano") || cat.contains("coro") ||
            rubro.contains("arte") || rubro.contains("artístic") || rubro.contains("artistico") || rubro.contains("pintur") || rubro.contains("canto") || rubro.contains("música") || rubro.contains("musica") || rubro.contains("teatro") || rubro.contains("dibujo") || rubro.contains("escultura") || rubro.contains("manualidad") || rubro.contains("fotograf") || rubro.contains("diseño") || rubro.contains("guitarr") || rubro.contains("piano") || rubro.contains("coro")) {
            textColor = ContextCompat.getColor(context, R.color.cat_arte);
            bgColor = ContextCompat.getColor(context, R.color.cat_arte_bg);
            iconRes = R.drawable.ic_art;
        } else if (cat.contains("física") || cat.contains("fisica") || cat.contains("deport") || cat.contains("gym") || cat.contains("gimnas") || cat.contains("entren") ||
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
                   cat.contains("educac") || cat.contains("pedag") || cat.contains("colegio") || cat.contains("universid") || cat.contains("facultad") || cat.contains("capacit") || cat.contains("taller") ||
                   rubro.contains("docente") || rubro.contains("escuela") || rubro.contains("estudio") || rubro.contains("profesor") || rubro.contains("curso") || rubro.contains("clase") ||
                   rubro.contains("educac") || rubro.contains("pedag") || rubro.contains("colegio") || rubro.contains("universid") || rubro.contains("facultad") || rubro.contains("capacit") || rubro.contains("taller")) {
            textColor = ContextCompat.getColor(context, R.color.cat_docente);
            bgColor = ContextCompat.getColor(context, R.color.cat_docente_bg);
            iconRes = R.drawable.ic_school;
        } else if (cat.contains("profesional") || cat.contains("abogad") || cat.contains("contador") || cat.contains("psicolog") || cat.contains("trabajo") || cat.contains("tramite") || cat.contains("trámite") || cat.contains("reunion") || cat.contains("reunión") || cat.contains("cliente") || cat.contains("asesor") ||
                   rubro.contains("profesional") || rubro.contains("abogad") || rubro.contains("contador") || rubro.contains("psicolog") || rubro.contains("trabajo") || rubro.contains("tramite") || rubro.contains("trámite") || rubro.contains("reunion") || rubro.contains("reunión") || rubro.contains("cliente") || rubro.contains("asesor")) {
            textColor = ContextCompat.getColor(context, R.color.cat_profesional);
            bgColor = ContextCompat.getColor(context, R.color.cat_profesional_bg);
            iconRes = R.drawable.ic_professional;
        } else {
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
        return listaElementos.size();
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