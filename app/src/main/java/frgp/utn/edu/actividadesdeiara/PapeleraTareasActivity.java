package frgp.utn.edu.actividadesdeiara;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import frgp.utn.edu.actividadesdeiara.adapter.PapeleraAdapter;
import frgp.utn.edu.actividadesdeiara.model.Tarea;

public class PapeleraTareasActivity extends AppCompatActivity implements PapeleraAdapter.OnPapeleraActionListener {

    private FirebaseFirestore db;
    private String currentUserId;

    private RecyclerView rvPapelera;
    private PapeleraAdapter adapter;
    private LinearLayout llEmptyState;
    private ProgressBar pbCargando;

    private final List<Tarea> listaTareasPapelera = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_papelera_tareas);

        currentUserId = FirebaseAuth.getInstance().getUid();
        db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbarPapelera);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        rvPapelera = findViewById(R.id.rvPapeleraTareas);
        llEmptyState = findViewById(R.id.llEmptyStatePapelera);
        pbCargando = findViewById(R.id.pbCargandoPapelera);
        MaterialButton btnVaciarPapelera = findViewById(R.id.btnVaciarPapelera);

        rvPapelera.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PapeleraAdapter(this, new ArrayList<>(), this);
        rvPapelera.setAdapter(adapter);

        btnVaciarPapelera.setOnClickListener(v -> confirmarVaciarPapelera());

        cargarPapeleraDesdeFirestore();
    }

    private void cargarPapeleraDesdeFirestore() {
        if (currentUserId == null) return;
        pbCargando.setVisibility(View.VISIBLE);

        db.collection("users")
                .document(currentUserId)
                .collection("tareas")
                .whereEqualTo("enPapelera", true)
                .addSnapshotListener((snapshot, error) -> {
                    pbCargando.setVisibility(View.GONE);

                    if (error != null) {
                        Toast.makeText(this, "Error al cargar papelera: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    listaTareasPapelera.clear();
                    if (snapshot != null) {
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Tarea tarea = doc.toObject(Tarea.class);
                            if (tarea != null) {
                                tarea.setId(doc.getId());
                                listaTareasPapelera.add(tarea);
                            }
                        }
                    }

                    adapter.setListaTareasPapelera(listaTareasPapelera);
                    boolean vacia = listaTareasPapelera.isEmpty();
                    llEmptyState.setVisibility(vacia ? View.VISIBLE : View.GONE);
                    rvPapelera.setVisibility(vacia ? View.GONE : View.VISIBLE);
                });
    }

    @Override
    public void onRestaurarTarea(Tarea tarea) {
        if (tarea == null || tarea.getId() == null) return;

        db.collection("users")
                .document(currentUserId)
                .collection("tareas")
                .document(tarea.getId())
                .update("enPapelera", false)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Tarea restaurada con éxito ↩️", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error al restaurar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onEliminarDefinitivoTarea(Tarea tarea) {
        if (tarea == null || tarea.getId() == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Eliminar Definitivamente")
                .setMessage("¿Estás seguro de que deseas eliminar la tarea '" + tarea.getNombre() + "'? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    db.collection("users")
                            .document(currentUserId)
                            .collection("tareas")
                            .document(tarea.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Tarea eliminada definitivamente 🗑️", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarVaciarPapelera() {
        if (listaTareasPapelera.isEmpty()) {
            Toast.makeText(this, "La papelera ya está vacía", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Vaciar Papelera")
                .setMessage("¿Deseas eliminar definitivamente todas las tareas de la papelera? Esta acción no se puede deshacer.")
                .setPositiveButton("Vaciar Todo", (dialog, which) -> {
                    db.collection("users")
                            .document(currentUserId)
                            .collection("tareas")
                            .whereEqualTo("enPapelera", true)
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                if (snapshot != null) {
                                    for (QueryDocumentSnapshot doc : snapshot) {
                                        doc.getReference().delete();
                                    }
                                }
                                Toast.makeText(this, "Papelera vaciada por completo", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error al vaciar papelera: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}