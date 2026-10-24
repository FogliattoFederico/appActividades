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

import frgp.utn.edu.actividadesdeiara.AddEditNotaActivity;
import frgp.utn.edu.actividadesdeiara.R;
import frgp.utn.edu.actividadesdeiara.adapter.NotaAdapter;
import frgp.utn.edu.actividadesdeiara.model.Nota;

public class NotasFragment extends Fragment implements NotaAdapter.OnNotaClickListener {

    private FirebaseFirestore db;
    private String currentUserId;

    private RecyclerView rvNotas;
    private NotaAdapter adapter;
    private LinearLayout llEmptyState;
    private ProgressBar pbCargando;

    private final List<Nota> listaTodasNotas = new ArrayList<>();
    private String textoBusquedaActual = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUserId = FirebaseAuth.getInstance().getUid();
        db = FirebaseFirestore.getInstance();

        rvNotas = view.findViewById(R.id.rvNotas);
        llEmptyState = view.findViewById(R.id.llEmptyStateNotas);
        pbCargando = view.findViewById(R.id.pbCargandoNotas);
        EditText etBuscar = view.findViewById(R.id.etBuscarNota);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAddNota);

        rvNotas.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NotaAdapter(requireContext(), new ArrayList<>(), this);
        rvNotas.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> startActivity(new Intent(requireContext(), AddEditNotaActivity.class)));

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

        cargarNotasDesdeFirestore();
    }

    private void cargarNotasDesdeFirestore() {
        if (currentUserId == null) return;
        pbCargando.setVisibility(View.VISIBLE);

        db.collection("users")
                .document(currentUserId)
                .collection("notas")
                .addSnapshotListener((value, error) -> {
                    if (!isAdded()) return;
                    pbCargando.setVisibility(View.GONE);

                    if (error != null) {
                        Toast.makeText(requireContext(), "Error al cargar notas: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    listaTodasNotas.clear();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Nota nota = doc.toObject(Nota.class);
                            if (nota != null) {
                                nota.setId(doc.getId());
                                listaTodasNotas.add(nota);
                            }
                        }
                    }

                    // Ordenar de la más reciente a la menos reciente (fechaModificacion descendente)
                    Collections.sort(listaTodasNotas, (n1, n2) -> Long.compare(n2.getFechaModificacion(), n1.getFechaModificacion()));

                    filtrarLista(textoBusquedaActual);
                });
    }

    private void filtrarLista(String textoBusqueda) {
        String busqueda = textoBusqueda.trim().toLowerCase(Locale.getDefault());
        List<Nota> listaFiltrada = new ArrayList<>();

        for (Nota nota : listaTodasNotas) {
            boolean coincide = busqueda.isEmpty()
                    || contiene(nota.getTitulo(), busqueda)
                    || contiene(nota.getContenido(), busqueda);
            if (coincide) {
                listaFiltrada.add(nota);
            }
        }

        adapter.setListaNotas(listaFiltrada);

        boolean vacia = listaFiltrada.isEmpty();
        llEmptyState.setVisibility(vacia ? View.VISIBLE : View.GONE);
        rvNotas.setVisibility(vacia ? View.GONE : View.VISIBLE);
    }

    private boolean contiene(String campo, String busqueda) {
        return campo != null && campo.toLowerCase(Locale.getDefault()).contains(busqueda);
    }

    @Override
    public void onNotaClick(Nota nota) {
        Intent intent = new Intent(requireContext(), AddEditNotaActivity.class);
        intent.putExtra(AddEditNotaActivity.EXTRA_NOTA, nota);
        startActivity(intent);
    }

    @Override
    public void onNotaLongClick(Nota nota) {
        if (nota == null || nota.getId() == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar Nota")
                .setMessage("¿Deseas eliminar la nota '" + nota.getTitulo() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    db.collection("users")
                            .document(currentUserId)
                            .collection("notas")
                            .document(nota.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                if (isAdded()) {
                                    Toast.makeText(requireContext(), "Nota eliminada", Toast.LENGTH_SHORT).show();
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