package frgp.utn.edu.actividadesdeiara.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import frgp.utn.edu.actividadesdeiara.R;

public class MonthCalendarAdapter extends RecyclerView.Adapter<MonthCalendarAdapter.DiaViewHolder> {

    public interface OnDiaClickListener {
        void onDiaClick(Calendar fecha);
    }

    private final Context context;
    private List<Calendar> listaDias = new ArrayList<>();
    private Calendar mesActual = Calendar.getInstance();
    private Calendar fechaSeleccionada = Calendar.getInstance();
    private Map<String, Integer> conteoEventos = new HashMap<>();
    private final OnDiaClickListener listener;

    public MonthCalendarAdapter(Context context, OnDiaClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setDatos(List<Calendar> listaDias, Calendar mesActual, Calendar fechaSeleccionada, Map<String, Integer> conteoEventos) {
        this.listaDias = listaDias != null ? listaDias : new ArrayList<>();
        this.mesActual = mesActual != null ? mesActual : Calendar.getInstance();
        this.fechaSeleccionada = fechaSeleccionada != null ? fechaSeleccionada : Calendar.getInstance();
        this.conteoEventos = conteoEventos != null ? conteoEventos : new HashMap<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DiaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dia_mes, parent, false);
        return new DiaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiaViewHolder holder, int position) {
        Calendar dia = listaDias.get(position);
        if (dia == null) return;

        int numeroDia = dia.get(Calendar.DAY_OF_MONTH);
        holder.tvNumeroDia.setText(String.valueOf(numeroDia));

        boolean esMismoMes = (dia.get(Calendar.MONTH) == mesActual.get(Calendar.MONTH)
                && dia.get(Calendar.YEAR) == mesActual.get(Calendar.YEAR));

        boolean esSeleccionado = (dia.get(Calendar.YEAR) == fechaSeleccionada.get(Calendar.YEAR)
                && dia.get(Calendar.DAY_OF_YEAR) == fechaSeleccionada.get(Calendar.DAY_OF_YEAR));

        SimpleDateFormat sdfKey = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String keyFecha = sdfKey.format(dia.getTime());
        Integer count = conteoEventos.get(keyFecha);
        boolean tieneEventos = count != null && count > 0;

        holder.viewIndicador.setVisibility(tieneEventos ? View.VISIBLE : View.INVISIBLE);

        if (esSeleccionado) {
            holder.tvNumeroDia.setBackgroundResource(R.drawable.bg_day_selected);
            holder.tvNumeroDia.setTextColor(ContextCompat.getColor(context, R.color.white));
        } else {
            holder.tvNumeroDia.setBackgroundResource(android.R.color.transparent);
            if (esMismoMes) {
                holder.tvNumeroDia.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
            } else {
                holder.tvNumeroDia.setTextColor(ContextCompat.getColor(context, R.color.text_hint));
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDiaClick(dia);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaDias.size();
    }

    static class DiaViewHolder extends RecyclerView.ViewHolder {

        TextView tvNumeroDia;
        View viewIndicador;

        public DiaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumeroDia = itemView.findViewById(R.id.tvNumeroDiaMes);
            viewIndicador = itemView.findViewById(R.id.viewIndicadorEvento);
        }
    }
}