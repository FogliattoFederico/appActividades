package frgp.utn.edu.actividadesdeiara;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import frgp.utn.edu.actividadesdeiara.model.Nota;

public class AddEditNotaActivity extends AppCompatActivity {

    public static final String EXTRA_NOTA = "extra_nota";

    private TextView tvFormTitle;
    private TextInputEditText etTitulo, etContenido;
    private MaterialButton btnGuardar, btnEliminar;

    private FirebaseFirestore db;
    private String currentUserId;
    private Nota notaAEditar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_nota);

        View root = findViewById(R.id.rootViewNota);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, Math.max(systemBars.bottom, ime.bottom));
                return insets;
            });
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }
        currentUserId = user.getUid();
        db = FirebaseFirestore.getInstance();

        tvFormTitle = findViewById(R.id.tvFormTitleNota);
        etTitulo = findViewById(R.id.etTituloNota);
        etContenido = findViewById(R.id.etContenidoNota);
        btnGuardar = findViewById(R.id.btnGuardarNota);
        btnEliminar = findViewById(R.id.btnEliminarNota);

        if (etContenido != null) {
            etContenido.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus && root instanceof ScrollView) {
                    root.postDelayed(() -> ((ScrollView) root).smoothScrollTo(0, v.getBottom() + 200), 200);
                }
            });
        }

        if (getIntent().hasExtra(EXTRA_NOTA)) {
            notaAEditar = (Nota) getIntent().getSerializableExtra(EXTRA_NOTA);
        }

        if (notaAEditar != null) {
            tvFormTitle.setText("Editar Nota");
            etTitulo.setText(notaAEditar.getTitulo());
            etContenido.setText(notaAEditar.getContenido());
            btnEliminar.setVisibility(View.VISIBLE);
        } else {
            tvFormTitle.setText("Nueva Nota");
            btnEliminar.setVisibility(View.GONE);
        }

        btnGuardar.setOnClickListener(v -> guardarNota());
        btnEliminar.setOnClickListener(v -> confirmarEliminarNota());
    }

    private void guardarNota() {
        String titulo = etTitulo.getText() != null ? etTitulo.getText().toString().trim() : "";
        String contenido = etContenido.getText() != null ? etContenido.getText().toString().trim() : "";

        if (TextUtils.isEmpty(titulo)) {
            etTitulo.setError("Ingrese el título de la nota");
            Toast.makeText(this, "El título de la nota es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(contenido)) {
            etContenido.setError("Ingrese el contenido de la nota");
            Toast.makeText(this, "El contenido de la nota es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGuardar.setEnabled(false);
        long timestamp = System.currentTimeMillis();

        Nota nota = new Nota(titulo, contenido, timestamp, currentUserId);

        if (notaAEditar != null && notaAEditar.getId() != null) {
            db.collection("users")
                    .document(currentUserId)
                    .collection("notas")
                    .document(notaAEditar.getId())
                    .set(nota)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Nota actualizada", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnGuardar.setEnabled(true);
                        Toast.makeText(this, "Error al actualizar nota: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            db.collection("users")
                    .document(currentUserId)
                    .collection("notas")
                    .add(nota)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Nota guardada con éxito", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnGuardar.setEnabled(true);
                        Toast.makeText(this, "Error al guardar nota: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void confirmarEliminarNota() {
        if (notaAEditar == null || notaAEditar.getId() == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Eliminar Nota")
                .setMessage("¿Estás seguro de que deseas eliminar la nota '" + notaAEditar.getTitulo() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    db.collection("users")
                            .document(currentUserId)
                            .collection("notas")
                            .document(notaAEditar.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Nota eliminada", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}