package frgp.utn.edu.actividadesdeiara.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import frgp.utn.edu.actividadesdeiara.AddEditProfesionalActivity;
import frgp.utn.edu.actividadesdeiara.R;
import frgp.utn.edu.actividadesdeiara.adapter.ProfesionalAdapter;
import frgp.utn.edu.actividadesdeiara.model.Profesional;

public class ProfesionalesFragment extends Fragment implements ProfesionalAdapter.OnProfesionalClickListener {

    private FirebaseFirestore db;
    private String currentUserId;

    private RecyclerView rvProfesionales;
    private ProfesionalAdapter adapter;
    private LinearLayout llEmptyState;
    private ProgressBar pbCargando;

    private final List<Profesional> listaTodosProfesionales = new ArrayList<>();
    private String textoBusquedaActual = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profesionales, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUserId = FirebaseAuth.getInstance().getUid();
        db = FirebaseFirestore.getInstance();

        rvProfesionales = view.findViewById(R.id.rvProfesionales);
        llEmptyState = view.findViewById(R.id.llEmptyStateProfesionales);
        pbCargando = view.findViewById(R.id.pbCargandoProfesionales);
        EditText etBuscar = view.findViewById(R.id.etBuscarProfesional);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAddProfesional);

        rvProfesionales.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ProfesionalAdapter(requireContext(), new ArrayList<>(), this);
        rvProfesionales.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> startActivity(new Intent(requireContext(), AddEditProfesionalActivity.class)));

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textoBusquedaActual = s.toString();
                filtrarLista(textoBusquedaActual);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        cargarProfesionalesDesdeFirestore();
    }

    private void cargarProfesionalesDesdeFirestore() {
        if (currentUserId == null) return;
        pbCargando.setVisibility(View.VISIBLE);

        db.collection("users")
                .document(currentUserId)
                .collection("profesionales")
                .addSnapshotListener((value, error) -> {
                    if (!isAdded()) return;
                    pbCargando.setVisibility(View.GONE);

                    if (error != null) {
                        Toast.makeText(requireContext(), "Error al cargar profesionales: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    listaTodosProfesionales.clear();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Profesional profesional = doc.toObject(Profesional.class);
                            if (profesional != null) {
                                profesional.setId(doc.getId());
                                listaTodosProfesionales.add(profesional);
                            }
                        }
                    }

                    Collections.sort(listaTodosProfesionales, (p1, p2) -> {
                        String a1 = p1.getApellido() != null ? p1.getApellido().toLowerCase(Locale.getDefault()) : "";
                        String a2 = p2.getApellido() != null ? p2.getApellido().toLowerCase(Locale.getDefault()) : "";
                        if (a1.equals(a2)) {
                            String n1 = p1.getNombre() != null ? p1.getNombre().toLowerCase(Locale.getDefault()) : "";
                            String n2 = p2.getNombre() != null ? p2.getNombre().toLowerCase(Locale.getDefault()) : "";
                            return n1.compareTo(n2);
                        }
                        return a1.compareTo(a2);
                    });

                    filtrarLista(textoBusquedaActual);
                });
    }

    private void filtrarLista(String textoBusqueda) {
        String busqueda = textoBusqueda.trim().toLowerCase(new Locale("es", "AR"));
        List<Profesional> listaFiltrada = new ArrayList<>();

        for (Profesional prof : listaTodosProfesionales) {
            boolean coincide = busqueda.isEmpty()
                    || contiene(prof.getNombre(), busqueda)
                    || contiene(prof.getApellido(), busqueda)
                    || contiene(prof.getRubro(), busqueda)
                    || contiene(prof.getEspecialidad(), busqueda);
            if (coincide) {
                listaFiltrada.add(prof);
            }
        }

        adapter.setListaProfesionales(listaFiltrada);

        if (listaFiltrada.isEmpty()) {
            llEmptyState.setVisibility(View.VISIBLE);
            rvProfesionales.setVisibility(View.GONE);
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvProfesionales.setVisibility(View.VISIBLE);
        }
    }

    private boolean contiene(String campo, String busqueda) {
        return campo != null && campo.toLowerCase(new Locale("es", "AR")).contains(busqueda);
    }

    @Override
    public void onProfesionalClick(Profesional profesional) {
        Intent intent = new Intent(requireContext(), AddEditProfesionalActivity.class);
        intent.putExtra(AddEditProfesionalActivity.EXTRA_PROFESIONAL, profesional);
        startActivity(intent);
    }

    @Override
    public void onProfesionalLongClick(Profesional profesional) {
        if (profesional == null || profesional.getId() == null) return;

        String nombreMostrar = (profesional.getApellido() != null ? profesional.getApellido() + " " : "") +
                (profesional.getNombre() != null ? profesional.getNombre() : "");

        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar Profesional")
                .setMessage("¿Deseas eliminar al profesional '" + nombreMostrar.trim() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    db.collection("users")
                            .document(currentUserId)
                            .collection("profesionales")
                            .document(profesional.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                if (isAdded()) {
                                    Toast.makeText(requireContext(), "Profesional eliminado", Toast.LENGTH_SHORT).show();
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
}
