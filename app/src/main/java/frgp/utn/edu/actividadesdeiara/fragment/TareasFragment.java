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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import frgp.utn.edu.actividadesdeiara.AddEditTareaActivity;
import frgp.utn.edu.actividadesdeiara.R;
import frgp.utn.edu.actividadesdeiara.adapter.TareaAdapter;
import frgp.utn.edu.actividadesdeiara.model.Tarea;

public class TareasFragment extends Fragment implements TareaAdapter.OnTareaClickListener {

    private FirebaseFirestore db;
    private String currentUserId;

    private RecyclerView rvTareasPendientes, rvTareasRealizadas;
    private TareaAdapter adapterPendientes, adapterRealizadas;
    private TextView tvHeaderPendientes, tvHeaderRealizadas;
    private LinearLayout llEmptyState;
    private ProgressBar pbCargando;

    private final List<Tarea> listaTodasTareas = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tareas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUserId = FirebaseAuth.getInstance().getUid();
        db = FirebaseFirestore.getInstance();

        tvHeaderPendientes = view.findViewById(R.id.tvHeaderPendientes);
        tvHeaderRealizadas = view.findViewById(R.id.tvHeaderRealizadas);
        rvTareasPendientes = view.findViewById(R.id.rvTareasPendientes);
        rvTareasRealizadas = view.findViewById(R.id.rvTareasRealizadas);
        llEmptyState = view.findViewById(R.id.llEmptyStateTareas);
        pbCargando = view.findViewById(R.id.pbCargandoTareas);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAddTarea);

        rvTareasPendientes.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapterPendientes = new TareaAdapter(requireContext(), new ArrayList<>(), this);
        rvTareasPendientes.setAdapter(adapterPendientes);

        rvTareasRealizadas.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapterRealizadas = new TareaAdapter(requireContext(), new ArrayList<>(), this);
        rvTareasRealizadas.setAdapter(adapterRealizadas);

        fabAdd.setOnClickListener(v -> startActivity(new Intent(requireContext(), AddEditTareaActivity.class)));

        cargarTareasDesdeFirestore();
    }

    private void cargarTareasDesdeFirestore() {
        if (currentUserId == null) return;
        pbCargando.setVisibility(View.VISIBLE);

        db.collection("users")
                .document(currentUserId)
                .collection("tareas")
                .addSnapshotListener((value, error) -> {
                    if (!isAdded()) return;
                    pbCargando.setVisibility(View.GONE);

                    if (error != null) {
                        Toast.makeText(requireContext(), "Error al cargar tareas: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    listaTodasTareas.clear();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Tarea tarea = doc.toObject(Tarea.class);
                            if (tarea != null) {
                                tarea.setId(doc.getId());
                                listaTodasTareas.add(tarea);
                            }
                        }
                    }

                    actualizarLista();
                });
    }

    private void actualizarLista() {
        List<Tarea> listaPendientes = new ArrayList<>();
        List<Tarea> listaRealizadas = new ArrayList<>();

        for (Tarea t : listaTodasTareas) {
            if (t.isCompletada()) {
                listaRealizadas.add(t);
            } else {
                listaPendientes.add(t);
            }
        }

        adapterPendientes.setListaTareas(listaPendientes);
        adapterRealizadas.setListaTareas(listaRealizadas);

        tvHeaderPendientes.setVisibility(listaPendientes.isEmpty() ? View.GONE : View.VISIBLE);
        rvTareasPendientes.setVisibility(listaPendientes.isEmpty() ? View.GONE : View.VISIBLE);

        tvHeaderRealizadas.setVisibility(listaRealizadas.isEmpty() ? View.GONE : View.VISIBLE);
        rvTareasRealizadas.setVisibility(listaRealizadas.isEmpty() ? View.GONE : View.VISIBLE);

        boolean sinTareas = listaTodasTareas.isEmpty();
        llEmptyState.setVisibility(sinTareas ? View.VISIBLE : View.GONE);
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
    public void onCompletadaToggle(Tarea tarea, boolean isChecked) {
        if (tarea == null || tarea.getId() == null) return;

        tarea.setCompletada(isChecked);

        db.collection("users")
                .document(currentUserId)
                .collection("tareas")
                .document(tarea.getId())
                .update("completada", isChecked)
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Error al actualizar tarea", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
