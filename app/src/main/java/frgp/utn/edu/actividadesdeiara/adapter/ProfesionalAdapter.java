package frgp.utn.edu.actividadesdeiara.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import frgp.utn.edu.actividadesdeiara.R;
import frgp.utn.edu.actividadesdeiara.model.Profesional;

public class ProfesionalAdapter extends RecyclerView.Adapter<ProfesionalAdapter.ProfesionalViewHolder> {

    public interface OnProfesionalClickListener {
        void onProfesionalClick(Profesional profesional);
        void onProfesionalLongClick(Profesional profesional);
    }

    private final Context context;
    private List<Profesional> listaProfesionales;
    private final OnProfesionalClickListener listener;

    public ProfesionalAdapter(Context context, List<Profesional> listaProfesionales, OnProfesionalClickListener listener) {
        this.context = context;
        this.listaProfesionales = listaProfesionales != null ? listaProfesionales : new ArrayList<>();
        this.listener = listener;
    }

    public void setListaProfesionales(List<Profesional> listaProfesionales) {
        this.listaProfesionales = listaProfesionales;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProfesionalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(context).inflate(R.layout.item_profesional, parent, false);
        return new ProfesionalViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfesionalViewHolder holder, int position) {
        Profesional prof = listaProfesionales.get(position);

        // Nombre Completo: Apellido, Nombre
        String apellido = prof.getApellido() != null ? prof.getApellido().trim() : "";
        String nombre = prof.getNombre() != null ? prof.getNombre().trim() : "";
        String nombreCompleto;
        if (!apellido.isEmpty() && !nombre.isEmpty()) {
            nombreCompleto = apellido + ", " + nombre;
        } else {
            nombreCompleto = !apellido.isEmpty() ? apellido : nombre;
        }
        holder.tvNombreApellido.setText(nombreCompleto);

        // Rubro y Especialidad
        String rubro = prof.getRubro() != null ? prof.getRubro() : "";
        String especialidad = prof.getEspecialidad() != null ? prof.getEspecialidad() : "";
        if (!rubro.isEmpty() && !especialidad.isEmpty()) {
            holder.tvRubroEspecialidad.setText(rubro + " · " + especialidad);
            holder.tvRubroEspecialidad.setVisibility(View.VISIBLE);
        } else if (!rubro.isEmpty() || !especialidad.isEmpty()) {
            holder.tvRubroEspecialidad.setText(!rubro.isEmpty() ? rubro : especialidad);
            holder.tvRubroEspecialidad.setVisibility(View.VISIBLE);
        } else {
            holder.tvRubroEspecialidad.setVisibility(View.GONE);
        }

        // Contacto (Teléfono y Email opcional)
        String telefono = prof.getTelefono() != null && !prof.getTelefono().trim().isEmpty() ? "📞 " + prof.getTelefono().trim() : "";
        String email = prof.getEmail() != null && !prof.getEmail().trim().isEmpty() ? "✉️ " + prof.getEmail().trim() : "";
        String contacto = TextUtils.join("  |  ", new String[]{telefono, email}).replaceAll("^  \\|  |  \\|  $", "");
        holder.tvContacto.setText(contacto);
        holder.tvContacto.setVisibility(contacto.trim().isEmpty() ? View.GONE : View.VISIBLE);

        // Atención Domicilio o Dirección
        if (prof.isAtencionDomicilio()) {
            holder.tvAtencion.setText("🏡 Atención a Domicilio");
            holder.tvAtencion.setVisibility(View.VISIBLE);
        } else if (prof.getDireccionAtencion() != null && !prof.getDireccionAtencion().trim().isEmpty()) {
            holder.tvAtencion.setText("📍 " + prof.getDireccionAtencion().trim());
            holder.tvAtencion.setVisibility(View.VISIBLE);
        } else {
            holder.tvAtencion.setVisibility(View.GONE);
        }

        // Pago: Obra Social o Particular
        if (prof.isAtiendeObraSocial()) {
            String bonos = prof.getCantidadBonos() != null ? prof.getCantidadBonos() : "1";
            String adicional = prof.getAdicional() != null && !prof.getAdicional().trim().isEmpty() ? prof.getAdicional().trim() : null;

            String textoPago = "🏥 Obra Social (Bonos: " + bonos;
            if (adicional != null) {
                textoPago += " | Adicional: $" + adicional;
            }
            textoPago += ")";

            holder.tvAtencionPago.setText(textoPago);
            holder.tvAtencionPago.setVisibility(View.VISIBLE);
        } else if (prof.getHonorarios() != null && !prof.getHonorarios().trim().isEmpty()) {
            holder.tvAtencionPago.setText("💵 Particular - Honorarios: $" + prof.getHonorarios().trim());
            holder.tvAtencionPago.setVisibility(View.VISIBLE);
        } else {
            holder.tvAtencionPago.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProfesionalClick(prof);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onProfesionalLongClick(prof);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return listaProfesionales.size();
    }

    static class ProfesionalViewHolder extends RecyclerView.ViewHolder {

        TextView tvNombreApellido;
        TextView tvRubroEspecialidad;
        TextView tvContacto;
        TextView tvAtencion;
        TextView tvAtencionPago;

        ProfesionalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreApellido = itemView.findViewById(R.id.tvNombreApellido);
            tvRubroEspecialidad = itemView.findViewById(R.id.tvRubroEspecialidad);
            tvContacto = itemView.findViewById(R.id.tvContacto);
            tvAtencion = itemView.findViewById(R.id.tvAtencion);
            tvAtencionPago = itemView.findViewById(R.id.tvAtencionPago);
        }
    }
}
