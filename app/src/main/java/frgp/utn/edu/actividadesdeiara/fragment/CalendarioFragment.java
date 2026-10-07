package frgp.utn.edu.actividadesdeiara.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import frgp.utn.edu.actividadesdeiara.AddEditActividadActivity;
import frgp.utn.edu.actividadesdeiara.R;
import frgp.utn.edu.actividadesdeiara.adapter.ActividadAdapter;
import frgp.utn.edu.actividadesdeiara.model.Actividad;

public class CalendarioFragment extends Fragment implements ActividadAdapter.OnActividadClickListener {

    private static final int CANTIDAD_DIAS = 7;
    private static final String[] NOMBRES_DIAS = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};

    private FirebaseFirestore db;
    private String currentUserId;

    private RecyclerView rvActividades;
    private ActividadAdapter adapter;
    private LinearLayout llEmptyState;
    private ProgressBar pbCargando;

    private TextView tvMesAnio;
    private final LinearLayout[] contenedoresDias = new LinearLayout[CANTIDAD_DIAS];
    private final TextView[] tvNumerosDias = new TextView[CANTIDAD_DIAS];
    private final Calendar[] fechasSemanaActual = new Calendar[CANTIDAD_DIAS];
    private Calendar lunesReferencia;
    private Calendar fechaSeleccionada;
    private int offsetSemanas = 0;

    private final List<Actividad> listaTodasActividades = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendario, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUserId = FirebaseAuth.getInstance().getUid();
        db = FirebaseFirestore.getInstance();

        rvActividades = view.findViewById(R.id.rvActividades);
        llEmptyState = view.findViewById(R.id.llEmptyState);
        pbCargando = view.findViewById(R.id.pbCargando);
        tvMesAnio = view.findViewById(R.id.tvMesAnio);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAdd);
        LinearLayout llDiasSemana = view.findViewById(R.id.llDiasSemana);
        TextView btnSemanaAnterior = view.findViewById(R.id.btnSemanaAnterior);
        TextView btnSemanaSiguiente = view.findViewById(R.id.btnSemanaSiguiente);

        rvActividades.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ActividadAdapter(requireContext(), new ArrayList<>(), this);
        rvActividades.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> startActivity(new Intent(requireContext(), AddEditActividadActivity.class)));

        inflarCeldasDeDias(llDiasSemana);

        btnSemanaAnterior.setOnClickListener(v -> {
            offsetSemanas--;
            actualizarSemana();
        });

        btnSemanaSiguiente.setOnClickListener(v -> {
            offsetSemanas++;
            actualizarSemana();
        });

        lunesReferencia = obtenerLunesDeSemana(Calendar.getInstance());
        fechaSeleccionada = obtenerLunesDeSemana(Calendar.getInstance());
        Calendar hoy = Calendar.getInstance();
        fechaSeleccionada.set(hoy.get(Calendar.YEAR), hoy.get(Calendar.MONTH), hoy.get(Calendar.DAY_OF_MONTH));

        actualizarSemana();
        cargarActividadesDesdeFirestore();
    }

    private void inflarCeldasDeDias(LinearLayout contenedor) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        contenedor.removeAllViews();

        for (int i = 0; i < CANTIDAD_DIAS; i++) {
            View celda = inflater.inflate(R.layout.item_dia_semana, contenedor, false);
            TextView tvNombre = celda.findViewById(R.id.tvNombreDia);
            TextView tvNumero = celda.findViewById(R.id.tvNumeroDia);

            tvNombre.setText(NOMBRES_DIAS[i]);

            final int indice = i;
            celda.setOnClickListener(v -> seleccionarDia(indice));

            contenedoresDias[i] = (LinearLayout) celda;
            tvNumerosDias[i] = tvNumero;
            contenedor.addView(celda);
        }
    }

    private Calendar obtenerLunesDeSemana(Calendar fecha) {
        Calendar c = (Calendar) fecha.clone();
        int diaSemana = c.get(Calendar.DAY_OF_WEEK);
        int diferencia = diaSemana - Calendar.MONDAY;
        if (diferencia < 0) diferencia += 7;
        c.add(Calendar.DAY_OF_MONTH, -diferencia);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    private boolean esMismoDia(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    private void seleccionarDia(int indice) {
        fechaSeleccionada = (Calendar) fechasSemanaActual[indice].clone();
        actualizarSemana();
        filtrarListaPorDia();
    }

    private void actualizarSemana() {
        Calendar lunesMostrado = (Calendar) lunesReferencia.clone();
        lunesMostrado.add(Calendar.DAY_OF_MONTH, 7 * offsetSemanas);

        SimpleDateFormat formatoMesAnio = new SimpleDateFormat("MMMM yyyy", new Locale("es", "AR"));
        String mesAnio = formatoMesAnio.format(lunesMostrado.getTime());
        mesAnio = mesAnio.substring(0, 1).toUpperCase(new Locale("es", "AR")) + mesAnio.substring(1);
        tvMesAnio.setText(mesAnio);

        for (int i = 0; i < CANTIDAD_DIAS; i++) {
            Calendar dia = (Calendar) lunesMostrado.clone();
            dia.add(Calendar.DAY_OF_MONTH, i);
            fechasSemanaActual[i] = dia;

            tvNumerosDias[i].setText(String.valueOf(dia.get(Calendar.DAY_OF_MONTH)));

            boolean seleccionado = esMismoDia(dia, fechaSeleccionada);
            tvNumerosDias[i].setBackgroundResource(seleccionado ? R.drawable.bg_day_selected : android.R.color.transparent);
            tvNumerosDias[i].setTextColor(seleccionado
                    ? ContextCompat.getColor(requireContext(), R.color.white)
                    : ContextCompat.getColor(requireContext(), R.color.black));
        }
    }

    private void cargarActividadesDesdeFirestore() {
        pbCargando.setVisibility(View.VISIBLE);

        db.collection("users")
                .document(currentUserId)
                .collection("actividades")
                .addSnapshotListener((value, error) -> {
                    if (!isAdded()) return;
                    pbCargando.setVisibility(View.GONE);

                    if (error != null) {
                        Toast.makeText(requireContext(), "Error al cargar actividades: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    listaTodasActividades.clear();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Actividad actividad = doc.toObject(Actividad.class);
                            if (actividad != null) {
                                actividad.setId(doc.getId());
                                listaTodasActividades.add(actividad);
                            }
                        }
                    }

                    filtrarListaPorDia();
                });
    }

    private Date parsearFecha(String fechaTexto) {
        if (fechaTexto == null || fechaTexto.trim().isEmpty()) return null;
        String[] formatos = new String[]{"dd/MM/yyyy", "yyyy-MM-dd", "d/M/yyyy"};
        for (String formato : formatos) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(formato, Locale.getDefault());
                sdf.setLenient(false);
                return sdf.parse(fechaTexto.trim());
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    private int compararHoras(String h1, String h2) {
        if (h1 == null) h1 = "";
        if (h2 == null) h2 = "";

        h1 = h1.trim();
        h2 = h2.trim();

        if (h1.isEmpty() && h2.isEmpty()) return 0;
        if (h1.isEmpty()) return 1;
        if (h2.isEmpty()) return -1;

        try {
            String[] p1 = h1.split(":");
            String[] p2 = h2.split(":");
            int min1 = Integer.parseInt(p1[0].trim()) * 60 + (p1.length > 1 ? Integer.parseInt(p1[1].trim()) : 0);
            int min2 = Integer.parseInt(p2[0].trim()) * 60 + (p2.length > 1 ? Integer.parseInt(p2[1].trim()) : 0);
            return Integer.compare(min1, min2);
        } catch (Exception e) {
            return h1.compareTo(h2);
        }
    }

    private void filtrarListaPorDia() {
        List<Actividad> listaFiltrada = new ArrayList<>();

        for (Actividad act : listaTodasActividades) {
            Date fecha = parsearFecha(act.getFecha());
            if (fecha != null) {
                Calendar calActividad = Calendar.getInstance();
                calActividad.setTime(fecha);
                if (esMismoDia(calActividad, fechaSeleccionada)) {
                    listaFiltrada.add(act);
                }
            }
        }

        Collections.sort(listaFiltrada, (a1, a2) -> compararHoras(a1.getHora(), a2.getHora()));

        adapter.setListaActividades(listaFiltrada);

        if (listaFiltrada.isEmpty()) {
            llEmptyState.setVisibility(View.VISIBLE);
            rvActividades.setVisibility(View.GONE);
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvActividades.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActividadClick(Actividad actividad) {
        Intent intent = new Intent(requireContext(), AddEditActividadActivity.class);
        intent.putExtra(AddEditActividadActivity.EXTRA_ACTIVIDAD, actividad);
        startActivity(intent);
    }

    @Override
    public void onActividadLongClick(Actividad actividad) {
        if (actividad == null || actividad.getId() == null) return;

        boolean esRepetida = actividad.getRepeatGroupId() != null && !actividad.getRepeatGroupId().trim().isEmpty();

        if (esRepetida) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar Actividad Repetida")
                    .setMessage("La actividad '" + actividad.getTitulo() + "' forma parte de una serie. ¿Deseas eliminar únicamente esta instancia o toda la serie?")
                    .setPositiveButton("Solo esta", (dialog, which) -> eliminarSoloEstaActividad(actividad))
                    .setNeutralButton("Toda la serie", (dialog, which) -> eliminarTodaLaSerie(actividad))
                    .setNegativeButton("Cancelar", null)
                    .show();
        } else {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar Actividad")
                    .setMessage("¿Deseas eliminar la actividad '" + actividad.getTitulo() + "'?")
                    .setPositiveButton("Eliminar", (dialog, which) -> eliminarSoloEstaActividad(actividad))
                    .setNegativeButton("Cancelar", null)
                    .show();
        }
    }

    private void eliminarSoloEstaActividad(Actividad actividad) {
        if (actividad == null || actividad.getId() == null) return;

        db.collection("users")
                .document(currentUserId)
                .collection("actividades")
                .document(actividad.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Actividad eliminada", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void eliminarTodaLaSerie(Actividad actividad) {
        if (actividad == null || actividad.getRepeatGroupId() == null) return;

        db.collection("users")
                .document(currentUserId)
                .collection("actividades")
                .whereEqualTo("repeatGroupId", actividad.getRepeatGroupId())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot != null) {
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            doc.getReference().delete();
                        }
                    }
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Toda la serie de actividades fue eliminada", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Error al eliminar la serie: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onCompletadaToggle(Actividad actividad, boolean isChecked) {
        if (actividad == null || actividad.getId() == null) return;

        actividad.setCompletada(isChecked);

        db.collection("users")
                .document(currentUserId)
                .collection("actividades")
                .document(actividad.getId())
                .update("completada", isChecked)
                .addOnSuccessListener(aVoid -> {
                    if (isAdded()) {
                        String msj = isChecked ? "Actividad marcada como completada ✓" : "Actividad marcada como pendiente";
                        Toast.makeText(requireContext(), msj, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Error al actualizar estado", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onAbonadaToggle(Actividad actividad, boolean isChecked) {
        if (actividad == null || actividad.getId() == null) return;

        actividad.setAbonada(isChecked);

        db.collection("users")
                .document(currentUserId)
                .collection("actividades")
                .document(actividad.getId())
                .update("abonada", isChecked)
                .addOnSuccessListener(aVoid -> {
                    if (isAdded()) {
                        String msj = isChecked ? "Sesión / Consulta marcada como abonada 💳" : "Marca de abonado eliminada";
                        Toast.makeText(requireContext(), msj, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Error al actualizar pago: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
