package frgp.utn.edu.actividadesdeiara.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import frgp.utn.edu.actividadesdeiara.R;
import frgp.utn.edu.actividadesdeiara.model.Nota;

public class NotaAdapter extends RecyclerView.Adapter<NotaAdapter.NotaViewHolder> {

    public interface OnNotaClickListener {
        void onNotaClick(Nota nota);
        void onNotaLongClick(Nota nota);
    }

    private final Context context;
    private List<Nota> listaNotas;
    private final OnNotaClickListener listener;

    public NotaAdapter(Context context, List<Nota> listaNotas, OnNotaClickListener listener) {
        this.context = context;
        this.listaNotas = listaNotas != null ? listaNotas : new ArrayList<>();
        this.listener = listener;
    }

    public void setListaNotas(List<Nota> listaNotas) {
        this.listaNotas = listaNotas != null ? listaNotas : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_nota, parent, false);
        return new NotaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotaViewHolder holder, int position) {
        Nota nota = listaNotas.get(position);

        holder.tvTitulo.setText(nota.getTitulo());
        holder.tvContenido.setText(nota.getContenido());

        if (nota.getFechaModificacion() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault());
            holder.tvFecha.setText(sdf.format(new Date(nota.getFechaModificacion())));
            holder.tvFecha.setVisibility(View.VISIBLE);
        } else {
            holder.tvFecha.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotaClick(nota);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onNotaLongClick(nota);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return listaNotas.size();
    }

    static class NotaViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitulo;
        TextView tvContenido;
        TextView tvFecha;

        public NotaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloNota);
            tvContenido = itemView.findViewById(R.id.tvContenidoNota);
            tvFecha = itemView.findViewById(R.id.tvFechaNota);
        }
    }
}