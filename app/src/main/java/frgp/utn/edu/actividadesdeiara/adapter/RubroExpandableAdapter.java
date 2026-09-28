package frgp.utn.edu.actividadesdeiara.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import frgp.utn.edu.actividadesdeiara.R;
import frgp.utn.edu.actividadesdeiara.model.Especialidad;
import frgp.utn.edu.actividadesdeiara.model.Rubro;

public class RubroExpandableAdapter extends BaseExpandableListAdapter {

    public interface OnRubroActionListener {
        void onAgregarEspecialidad(Rubro rubro);
        void onRubroLongClick(Rubro rubro);
        void onEditarEspecialidad(Rubro rubro, Especialidad especialidad);
        void onEliminarEspecialidad(Rubro rubro, Especialidad especialidad);
    }

    private final Context context;
    private List<Rubro> listaRubros;
    private List<List<Especialidad>> listaEspecialidadesPorRubro;
    private final OnRubroActionListener listener;

    public RubroExpandableAdapter(Context context, List<Rubro> listaRubros,
                                   List<List<Especialidad>> listaEspecialidadesPorRubro,
                                   OnRubroActionListener listener) {
        this.context = context;
        this.listaRubros = listaRubros;
        this.listaEspecialidadesPorRubro = listaEspecialidadesPorRubro;
        this.listener = listener;
    }

    public void actualizarDatos(List<Rubro> listaRubros, List<List<Especialidad>> listaEspecialidadesPorRubro) {
        this.listaRubros = listaRubros;
        this.listaEspecialidadesPorRubro = listaEspecialidadesPorRubro;
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return listaRubros.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (groupPosition >= listaEspecialidadesPorRubro.size()) return 0;
        return listaEspecialidadesPorRubro.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return listaRubros.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return listaEspecialidadesPorRubro.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_rubro_group, parent, false);
        }

        Rubro rubro = listaRubros.get(groupPosition);
        TextView tvNombre = convertView.findViewById(R.id.tvNombreRubro);
        TextView btnAgregarEspecialidad = convertView.findViewById(R.id.btnAgregarEspecialidad);

        tvNombre.setText(rubro.getNombre());

        btnAgregarEspecialidad.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAgregarEspecialidad(rubro);
            }
        });

        // Long click on Rubro group row for Options (Edit / Delete)
        convertView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onRubroLongClick(rubro);
            }
            return true;
        });

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_especialidad_child, parent, false);
        }

        Rubro rubro = listaRubros.get(groupPosition);
        Especialidad especialidad = listaEspecialidadesPorRubro.get(groupPosition).get(childPosition);
        TextView tvNombre = convertView.findViewById(R.id.tvNombreEspecialidad);
        tvNombre.setText("•  " + especialidad.getNombre());

        // Short click: edit specialty name
        convertView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditarEspecialidad(rubro, especialidad);
            }
        });

        // Long click: delete specialty
        convertView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onEliminarEspecialidad(rubro, especialidad);
            }
            return true;
        });

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
