package frgp.utn.edu.actividadesdeiara.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import frgp.utn.edu.actividadesdeiara.R;
import frgp.utn.edu.actividadesdeiara.adapter.EspecialidadAdapter;
import frgp.utn.edu.actividadesdeiara.adapter.RubroAdapter;
import frgp.utn.edu.actividadesdeiara.model.Especialidad;
import frgp.utn.edu.actividadesdeiara.model.Rubro;

public class RubrosFragment extends Fragment implements RubroAdapter.OnRubroClickListener, EspecialidadAdapter.OnEspecialidadClickListener {

    private FirebaseFirestore db;
    private String currentUserId;

    private RecyclerView rvRubros;
    private RecyclerView rvEspecialidades;
    private LinearLayout llEmptyStateRubros;
    private LinearLayout llEmptyStateEspecialidades;
    private TextView tvEmptyStateEspecialidadesTexto;
    private TextView tvHeaderEspecialidades;
    private MaterialButton btnNuevoRubro;
    private MaterialButton btnNuevaEspecialidad;
    private ProgressBar pbCargando;

    private RubroAdapter rubroAdapter;
    private EspecialidadAdapter especialidadAdapter;

    private final List<Rubro> listaRubros = new ArrayList<>();
    private final List<Especialidad> listaEspecialidades = new ArrayList<>();

    private Rubro rubroSeleccionado = null;
    private ListenerRegistration especialidadesListener = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rubros, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUserId = FirebaseAuth.getInstance().getUid();
        db = FirebaseFirestore.getInstance();

        rvRubros = view.findViewById(R.id.rvRubros);
        rvEspecialidades = view.findViewById(R.id.rvEspecialidades);
        llEmptyStateRubros = view.findViewById(R.id.llEmptyStateRubros);
        llEmptyStateEspecialidades = view.findViewById(R.id.llEmptyStateEspecialidades);
        tvEmptyStateEspecialidadesTexto = view.findViewById(R.id.tvEmptyStateEspecialidadesTexto);
        tvHeaderEspecialidades = view.findViewById(R.id.tvHeaderEspecialidades);
        btnNuevoRubro = view.findViewById(R.id.btnNuevoRubro);
        btnNuevaEspecialidad = view.findViewById(R.id.btnNuevaEspecialidad);
        pbCargando = view.findViewById(R.id.pbCargandoRubros);

        rvRubros.setLayoutManager(new LinearLayoutManager(requireContext()));
        rubroAdapter = new RubroAdapter(requireContext(), new ArrayList<>(), this);
        rvRubros.setAdapter(rubroAdapter);

        rvEspecialidades.setLayoutManager(new LinearLayoutManager(requireContext()));
        especialidadAdapter = new EspecialidadAdapter(requireContext(), new ArrayList<>(), this);
        rvEspecialidades.setAdapter(especialidadAdapter);

        btnNuevoRubro.setOnClickListener(v -> mostrarDialogoNuevoRubro());
        btnNuevaEspecialidad.setOnClickListener(v -> {
            if (rubroSeleccionado != null) {
                mostrarDialogoNuevaEspecialidad(rubroSeleccionado);
            }
        });

        cargarRubros();
    }

    private void cargarRubros() {
        if (currentUserId == null) return;
        pbCargando.setVisibility(View.VISIBLE);

        db.collection("users")
                .document(currentUserId)
                .collection("rubros")
                .addSnapshotListener((snapshot, error) -> {
                    if (!isAdded()) return;
                    pbCargando.setVisibility(View.GONE);

                    if (error != null) {
                        Toast.makeText(requireContext(), "Error al cargar rubros: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    listaRubros.clear();
                    if (snapshot != null) {
                        for (QueryDocumentSnapshot doc : snapshot) {
                            Rubro rubro = doc.toObject(Rubro.class);
                            rubro.setId(doc.getId());
                            listaRubros.add(rubro);
                        }
                    }

                    rubroAdapter.setListaRubros(listaRubros);
                    boolean vacio = listaRubros.isEmpty();
                    llEmptyStateRubros.setVisibility(vacio ? View.VISIBLE : View.GONE);
                    rvRubros.setVisibility(vacio ? View.GONE : View.VISIBLE);

                    if (vacio) {
                        rubroSeleccionado = null;
                        limpiarEspecialidades();
                    } else if (rubroSeleccionado != null) {
                        // Re-select previously selected Rubro if still exists
                        boolean sigueExistiendo = false;
                        for (int i = 0; i < listaRubros.size(); i++) {
                            if (listaRubros.get(i).getId().equals(rubroSeleccionado.getId())) {
                                rubroSeleccionado = listaRubros.get(i);
                                rubroAdapter.setPosicionSeleccionada(i);
                                cargarEspecialidadesDelRubro(rubroSeleccionado);
                                sigueExistiendo = true;
                                break;
                            }
                        }
                        if (!sigueExistiendo) {
                            rubroSeleccionado = null;
                            limpiarEspecialidades();
                        }
                    } else {
                        // Select first Rubro by default
                        rubroSeleccionado = listaRubros.get(0);
                        rubroAdapter.setPosicionSeleccionada(0);
                        cargarEspecialidadesDelRubro(rubroSeleccionado);
                    }
                });
    }

    private void cargarEspecialidadesDelRubro(Rubro rubro) {
        if (rubro == null || rubro.getId() == null) {
            limpiarEspecialidades();
            return;
        }

        tvHeaderEspecialidades.setText("Especialidades de " + rubro.getNombre());
        btnNuevaEspecialidad.setEnabled(true);

        if (especialidadesListener != null) {
            especialidadesListener.remove();
        }

        especialidadesListener = db.collection("users")
                .document(currentUserId)
                .collection("rubros")
                .document(rubro.getId())
                .collection("especialidades")
                .addSnapshotListener((snapshot, error) -> {
                    if (!isAdded()) return;

                    listaEspecialidades.clear();
                    if (snapshot != null) {
                        for (QueryDocumentSnapshot doc : snapshot) {
                            Especialidad esp = doc.toObject(Especialidad.class);
                            esp.setId(doc.getId());
                            listaEspecialidades.add(esp);
                        }
                    }

                    especialidadAdapter.setListaEspecialidades(listaEspecialidades);
                    boolean sinEsp = listaEspecialidades.isEmpty();

                    if (sinEsp) {
                        tvEmptyStateEspecialidadesTexto.setText("No hay especialidades en " + rubro.getNombre());
                        llEmptyStateEspecialidades.setVisibility(View.VISIBLE);
                        rvEspecialidades.setVisibility(View.GONE);
                    } else {
                        llEmptyStateEspecialidades.setVisibility(View.GONE);
                        rvEspecialidades.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void limpiarEspecialidades() {
        if (especialidadesListener != null) {
            especialidadesListener.remove();
            especialidadesListener = null;
        }

        tvHeaderEspecialidades.setText("Especialidades");
        btnNuevaEspecialidad.setEnabled(false);
        listaEspecialidades.clear();
        especialidadAdapter.setListaEspecialidades(listaEspecialidades);
        tvEmptyStateEspecialidadesTexto.setText("Selecciona un rubro arriba para ver sus especialidades");
        llEmptyStateEspecialidades.setVisibility(View.VISIBLE);
        rvEspecialidades.setVisibility(View.GONE);
    }

    @Override
    public void onRubroClick(Rubro rubro, int position) {
        rubroSeleccionado = rubro;
        cargarEspecialidadesDelRubro(rubro);
    }

    @Override
    public void onRubroLongClick(Rubro rubro) {
        if (rubro == null || rubro.getId() == null) return;

        CharSequence[] opciones = {"Modificar nombre", "Eliminar rubro"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Opciones de " + rubro.getNombre())
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        mostrarDialogoEditarRubro(rubro);
                    } else if (which == 1) {
                        confirmarEliminarRubro(rubro);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onEspecialidadClick(Especialidad especialidad) {
        if (rubroSeleccionado == null || especialidad == null) return;
        mostrarDialogoEditarEspecialidad(rubroSeleccionado, especialidad);
    }

    @Override
    public void onEspecialidadLongClick(Especialidad especialidad) {
        if (rubroSeleccionado == null || especialidad == null || especialidad.getId() == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar Especialidad")
                .setMessage("¿Deseas eliminar la especialidad '" + especialidad.getNombre() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    db.collection("users")
                            .document(currentUserId)
                            .collection("rubros")
                            .document(rubroSeleccionado.getId())
                            .collection("especialidades")
                            .document(especialidad.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                if (isAdded()) {
                                    Toast.makeText(requireContext(), "Especialidad eliminada", Toast.LENGTH_SHORT).show();
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

    private void mostrarDialogoNuevoRubro() {
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setHint("Ej: Médico");

        new AlertDialog.Builder(requireContext())
                .setTitle("Nuevo rubro")
                .setView(input)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre = input.getText().toString().trim();
                    if (nombre.isEmpty()) return;

                    for (Rubro r : listaRubros) {
                        if (r.getNombre() != null && r.getNombre().trim().equalsIgnoreCase(nombre)) {
                            Toast.makeText(requireContext(), "El rubro '" + nombre + "' ya existe.", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    Map<String, Object> datos = new HashMap<>();
                    datos.put("nombre", nombre);

                    db.collection("users")
                            .document(currentUserId)
                            .collection("rubros")
                            .add(datos)
                            .addOnFailureListener(e -> {
                                if (isAdded()) {
                                    Toast.makeText(requireContext(), "Error al guardar rubro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoEditarRubro(Rubro rubro) {
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setText(rubro.getNombre());

        new AlertDialog.Builder(requireContext())
                .setTitle("Modificar nombre de rubro")
                .setView(input)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nuevoNombre = input.getText().toString().trim();
                    if (nuevoNombre.isEmpty()) return;

                    for (Rubro r : listaRubros) {
                        if (r.getId() != null && !r.getId().equals(rubro.getId()) && r.getNombre() != null && r.getNombre().trim().equalsIgnoreCase(nuevoNombre)) {
                            Toast.makeText(requireContext(), "Ya existe un rubro llamado '" + nuevoNombre + "'.", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    db.collection("users")
                            .document(currentUserId)
                            .collection("rubros")
                            .document(rubro.getId())
                            .update("nombre", nuevoNombre)
                            .addOnSuccessListener(unused -> {
                                if (isAdded()) {
                                    Toast.makeText(requireContext(), "Rubro modificado", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (isAdded()) {
                                    Toast.makeText(requireContext(), "Error al modificar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarEliminarRubro(Rubro rubro) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar Rubro")
                .setMessage("¿Deseas eliminar el rubro '" + rubro.getNombre() + "' y todas sus especialidades?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    db.collection("users")
                            .document(currentUserId)
                            .collection("rubros")
                            .document(rubro.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                if (isAdded()) {
                                    Toast.makeText(requireContext(), "Rubro eliminado", Toast.LENGTH_SHORT).show();
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

    private void mostrarDialogoNuevaEspecialidad(Rubro rubro) {
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setHint("Ej: Traumatología");

        new AlertDialog.Builder(requireContext())
                .setTitle("Nueva especialidad en " + rubro.getNombre())
                .setView(input)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre = input.getText().toString().trim();
                    if (nombre.isEmpty()) return;

                    for (Especialidad esp : listaEspecialidades) {
                        if (esp.getNombre() != null && esp.getNombre().trim().equalsIgnoreCase(nombre)) {
                            Toast.makeText(requireContext(), "La especialidad '" + nombre + "' ya existe en " + rubro.getNombre() + ".", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    Map<String, Object> datos = new HashMap<>();
                    datos.put("nombre", nombre);

                    db.collection("users")
                            .document(currentUserId)
                            .collection("rubros")
                            .document(rubro.getId())
                            .collection("especialidades")
                            .add(datos)
                            .addOnFailureListener(e -> {
                                if (isAdded()) {
                                    Toast.makeText(requireContext(), "Error al guardar especialidad: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoEditarEspecialidad(Rubro rubro, Especialidad especialidad) {
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setText(especialidad.getNombre());

        new AlertDialog.Builder(requireContext())
                .setTitle("Modificar especialidad")
                .setView(input)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nuevoNombre = input.getText().toString().trim();
                    if (nuevoNombre.isEmpty()) return;

                    for (Especialidad esp : listaEspecialidades) {
                        if (esp.getId() != null && !esp.getId().equals(especialidad.getId()) && esp.getNombre() != null && esp.getNombre().trim().equalsIgnoreCase(nuevoNombre)) {
                            Toast.makeText(requireContext(), "Ya existe una especialidad llamada '" + nuevoNombre + "' en " + rubro.getNombre() + ".", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    db.collection("users")
                            .document(currentUserId)
                            .collection("rubros")
                            .document(rubro.getId())
                            .collection("especialidades")
                            .document(especialidad.getId())
                            .update("nombre", nuevoNombre)
                            .addOnSuccessListener(unused -> {
                                if (isAdded()) {
                                    Toast.makeText(requireContext(), "Especialidad modificada", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (isAdded()) {
                                    Toast.makeText(requireContext(), "Error al modificar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (especialidadesListener != null) {
            especialidadesListener.remove();
            especialidadesListener = null;
        }
    }
}
