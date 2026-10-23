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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import frgp.utn.edu.actividadesdeiara.AddEditActividadActivity;
import frgp.utn.edu.actividadesdeiara.AddEditTareaActivity;
import frgp.utn.edu.actividadesdeiara.R;
import frgp.utn.edu.actividadesdeiara.adapter.AgendaAdapter;
import frgp.utn.edu.actividadesdeiara.adapter.MonthCalendarAdapter;
import frgp.utn.edu.actividadesdeiara.model.Actividad;
import frgp.utn.edu.actividadesdeiara.model.ElementoAgenda;
import frgp.utn.edu.actividadesdeiara.model.Tarea;

public class CalendarioFragment extends Fragment implements MonthCalendarAdapter.OnDiaClickListener, AgendaAdapter.OnAgendaClickListener {

    private FirebaseFirestore db;
    private String currentUserId;

    private TextView tvMesAnio;
    private TextView tvDiaSeleccionadoHeader;
    private RecyclerView rvGridMes;
    private RecyclerView rvAgendaDia;
    private LinearLayout llEmptyState;
    private ProgressBar pbCargando;

    private MonthCalendarAdapter monthAdapter;
    private AgendaAdapter agendaAdapter;

    private Calendar mesActual = Calendar.getInstance();
    private Calendar fechaSeleccionada = Calendar.getInstance();

    private final List<Actividad> listaTodasActividades = new ArrayList<>();
    private final List<Tarea> listaTodasTareas = new ArrayList<>();
    private final Map<String, Integer> conteoEventosPorFecha = new HashMap<>();

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

        tvMesAnio = view.findViewById(R.id.tvMesAnio);
        tvDiaSeleccionadoHeader = view.findViewById(R.id.tvDiaSeleccionadoHeader);
        TextView btnMesAnterior = view.findViewById(R.id.btnMesAnterior);
        TextView btnMesSiguiente = view.findViewById(R.id.btnMesSiguiente);

        rvGridMes = view.findViewById(R.id.rvGridMes);
        rvAgendaDia = view.findViewById(R.id.rvAgendaDia);
        llEmptyState = view.findViewById(R.id.llEmptyStateAgenda);
        pbCargando = view.findViewById(R.id.pbCargandoAgenda);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAdd);

        // Grid 7 columnas para el mes
        rvGridMes.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        monthAdapter = new MonthCalendarAdapter(requireContext(), this);
        rvGridMes.setAdapter(monthAdapter);

        // Lista para la agenda combinada del día seleccionado
        rvAgendaDia.setLayoutManager(new LinearLayoutManager(requireContext()));
        agendaAdapter = new AgendaAdapter(requireContext(), new ArrayList<>(), this);
        rvAgendaDia.setAdapter(agendaAdapter);

        fabAdd.setOnClickListener(v -> startActivity(new Intent(requireContext(), AddEditActividadActivity.class)));

        btnMesAnterior.setOnClickListener(v -> {
            mesActual.add(Calendar.MONTH, -1);
            actualizarVistaMes();
        });

        btnMesSiguiente.setOnClickListener(v -> {
            mesActual.add(Calendar.MONTH, 1);
            actualizarVistaMes();
        });

        mesActual.set(Calendar.DAY_OF_MONTH, 1);
        Calendar hoy = Calendar.getInstance();
        fechaSeleccionada.set(hoy.get(Calendar.YEAR), hoy.get(Calendar.MONTH), hoy.get(Calendar.DAY_OF_MONTH));

        actualizarVistaMes();
        cargarDatosDesdeFirestore();
    }

    private void actualizarVistaMes() {
        SimpleDateFormat formatoMesAnio = new SimpleDateFormat("MMMM yyyy", new Locale("es", "AR"));
        String textoMes = formatoMesAnio.format(mesActual.getTime());
        textoMes = textoMes.substring(0, 1).toUpperCase(new Locale("es", "AR")) + textoMes.substring(1);
        tvMesAnio.setText(textoMes);

        List<Calendar> diasGrid = generarDiasMesGrid(mesActual);
        monthAdapter.setDatos(diasGrid, mesActual, fechaSeleccionada, conteoEventosPorFecha);

        actualizarHeaderDiaSeleccionado();
        filtrarYMostrarAgendaDelDia();
    }

    private List<Calendar> generarDiasMesGrid(Calendar mes) {
        List<Calendar> dias = new ArrayList<>();
        Calendar cal = (Calendar) mes.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int offset = dayOfWeek - Calendar.MONDAY;
        if (offset < 0) offset += 7;

        cal.add(Calendar.DAY_OF_MONTH, -offset);

        for (int i = 0; i < 35; i++) {
            dias.add((Calendar) cal.clone());
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        return dias;
    }

    private void actualizarHeaderDiaSeleccionado() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd 'de' MMMM", new Locale("es", "AR"));
        String textoDia = sdf.format(fechaSeleccionada.getTime());
        textoDia = textoDia.substring(0, 1).toUpperCase(new Locale("es", "AR")) + textoDia.substring(1);
        tvDiaSeleccionadoHeader.setText("Agenda del " + textoDia);
    }

    @Override
    public void onDiaClick(Calendar fecha) {
        fechaSeleccionada = (Calendar) fecha.clone();

        // Si tocó un día de otro mes, cambiar mes actual automáticamente
        if (fechaSeleccionada.get(Calendar.MONTH) != mesActual.get(Calendar.MONTH)) {
            mesActual.set(Calendar.YEAR, fechaSeleccionada.get(Calendar.YEAR));
            mesActual.set(Calendar.MONTH, fechaSeleccionada.get(Calendar.MONTH));
            mesActual.set(Calendar.DAY_OF_MONTH, 1);
        }

        actualizarVistaMes();
    }

    private void cargarDatosDesdeFirestore() {
        if (currentUserId == null) return;
        pbCargando.setVisibility(View.VISIBLE);

        // 1. Cargar Actividades
        db.collection("users")
                .document(currentUserId)
                .collection("actividades")
                .addSnapshotListener((actSnap, actErr) -> {
                    if (!isAdded()) return;

                    listaTodasActividades.clear();
                    if (actSnap != null) {
                        for (DocumentSnapshot doc : actSnap.getDocuments()) {
                            Actividad act = doc.toObject(Actividad.class);
                            if (act != null) {
                                act.setId(doc.getId());
                                listaTodasActividades.add(act);
                            }
                        }
                    }

                    // 2. Cargar Tareas
                    db.collection("users")
                            .document(currentUserId)
                            .collection("tareas")
                            .addSnapshotListener((tarSnap, tarErr) -> {
                                if (!isAdded()) return;
                                pbCargando.setVisibility(View.GONE);

                                listaTodasTareas.clear();
                                if (tarSnap != null) {
                                    for (DocumentSnapshot doc : tarSnap.getDocuments()) {
                                        Tarea tarea = doc.toObject(Tarea.class);
                                        if (tarea != null) {
                                            tarea.setId(doc.getId());
                                            listaTodasTareas.add(tarea);
                                        }
                                    }
                                }

                                recalcularConteoEventos();
                                actualizarVistaMes();
                            });
                });
    }

    private void recalcularConteoEventos() {
        conteoEventosPorFecha.clear();
        SimpleDateFormat sdfKey = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (Actividad act : listaTodasActividades) {
            Date d = parsearFecha(act.getFecha());
            if (d != null) {
                String key = sdfKey.format(d);
                conteoEventosPorFecha.put(key, conteoEventosPorFecha.getOrDefault(key, 0) + 1);
            }
        }

        for (Tarea tar : listaTodasTareas) {
            Date d = parsearFecha(tar.getFecha());
            if (d != null) {
                String key = sdfKey.format(d);
                conteoEventosPorFecha.put(key, conteoEventosPorFecha.getOrDefault(key, 0) + 1);
            }
        }
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

    private boolean esMismoDia(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
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

    private void filtrarYMostrarAgendaDelDia() {
        List<ElementoAgenda> agendaCombinada = new ArrayList<>();

        // Actividades del día
        for (Actividad act : listaTodasActividades) {
            Date d = parsearFecha(act.getFecha());
            if (d != null) {
                Calendar calAct = Calendar.getInstance();
                calAct.setTime(d);
                if (esMismoDia(calAct, fechaSeleccionada)) {
                    agendaCombinada.add(new ElementoAgenda(act));
                }
            }
        }

        // Tareas del día
        for (Tarea tar : listaTodasTareas) {
            Date d = parsearFecha(tar.getFecha());
            if (d != null) {
                Calendar calTar = Calendar.getInstance();
                calTar.setTime(d);
                if (esMismoDia(calTar, fechaSeleccionada)) {
                    agendaCombinada.add(new ElementoAgenda(tar));
                }
            }
        }

        // Ordenamiento por hora de menor a mayor
        Collections.sort(agendaCombinada, (item1, item2) -> compararHoras(item1.getHora(), item2.getHora()));

        agendaAdapter.setListaElementos(agendaCombinada);

        boolean vacio = agendaCombinada.isEmpty();
        llEmptyState.setVisibility(vacio ? View.VISIBLE : View.GONE);
        rvAgendaDia.setVisibility(vacio ? View.GONE : View.VISIBLE);
    }

    // --- OnAgendaClickListener Callbacks ---

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
    public void onActividadCompletadaToggle(Actividad actividad, boolean isChecked) {
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
    public void onActividadAbonadaToggle(Actividad actividad, boolean isChecked) {
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

    @Override
    public void onTareaClick(Tarea tarea) {
        Intent intent = new Intent(requireContext(), AddEditTareaActivity.class);
        intent.putExtra(AddEditTareaActivity.EXTRA_TAREA, tarea);
        startActivity(intent);
    }

    @Override
    public void onTareaLongClick(Tarea tarea) {
        if (tarea == null || tarea.getId() == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar Tarea")
                .setMessage("¿Deseas eliminar la tarea '" + tarea.getNombre() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    db.collection("users")
                            .document(currentUserId)
                            .collection("tareas")
                            .document(tarea.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                if (isAdded()) {
                                    Toast.makeText(requireContext(), "Tarea eliminada", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (isAdded()) {
                                    Toast.makeText(requireContext(), "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onTareaCompletadaToggle(Tarea tarea, boolean isChecked) {
        if (tarea == null || tarea.getId() == null) return;

        tarea.setCompletada(isChecked);

        db.collection("users")
                .document(currentUserId)
                .collection("tareas")
                .document(tarea.getId())
                .update("completada", isChecked)
                .addOnSuccessListener(aVoid -> {
                    if (isAdded()) {
                        String msj = isChecked ? "Tarea marcada como realizada ✓" : "Tarea marcada como pendiente";
                        Toast.makeText(requireContext(), msj, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Error al actualizar tarea", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}