package frgp.utn.edu.actividadesdeiara;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Locale;

import frgp.utn.edu.actividadesdeiara.model.Actividad;

public class AddEditActividadActivity extends AppCompatActivity {

    public static final String EXTRA_ACTIVIDAD = "extra_actividad";

    private TextView tvFormTitle;
    private TextInputEditText etTitulo, etResponsable, etFecha, etHora, etDescripcion;
    private TextInputLayout tilFecha, tilHora;
    private AutoCompleteTextView actvCategoria;
    private MaterialButton btnGuardar, btnEliminar;

    private FirebaseFirestore db;
    private String currentUserId;
    private Actividad actividadAEditar;

    private static final String[] CATEGORIAS = new String[]{
            "Médico",
            "Profesional",
            "Docente",
            "Actividad Física"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_actividad);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }
        currentUserId = user.getUid();
        db = FirebaseFirestore.getInstance();

        tvFormTitle = findViewById(R.id.tvFormTitle);
        etTitulo = findViewById(R.id.etTitulo);
        actvCategoria = findViewById(R.id.actvCategoria);
        etResponsable = findViewById(R.id.etResponsable);
        etFecha = findViewById(R.id.etFecha);
        etHora = findViewById(R.id.etHora);
        tilFecha = findViewById(R.id.tilFecha);
        tilHora = findViewById(R.id.tilHora);
        etDescripcion = findViewById(R.id.etDescripcion);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnEliminar = findViewById(R.id.btnEliminar);

        // Setup Dropdown Adapter
        ArrayAdapter<String> adapterCategory = new ArrayAdapter<>(
                this,
                R.layout.item_spinner_dropdown,
                CATEGORIAS
        );
        actvCategoria.setAdapter(adapterCategory);

        // Date & Time Picker listeners
        View.OnClickListener listenerFecha = v -> mostrarDatePicker();
        etFecha.setOnClickListener(listenerFecha);
        if (tilFecha != null) {
            tilFecha.setOnClickListener(listenerFecha);
            tilFecha.setEndIconOnClickListener(listenerFecha);
        }

        View.OnClickListener listenerHora = v -> mostrarTimePicker();
        etHora.setOnClickListener(listenerHora);
        if (tilHora != null) {
            tilHora.setOnClickListener(listenerHora);
            tilHora.setEndIconOnClickListener(listenerHora);
        }

        // Check if edit mode
        if (getIntent().hasExtra(EXTRA_ACTIVIDAD)) {
            actividadAEditar = (Actividad) getIntent().getSerializableExtra(EXTRA_ACTIVIDAD);
        }

        if (actividadAEditar != null) {
            tvFormTitle.setText("Editar Actividad");
            etTitulo.setText(actividadAEditar.getTitulo());
            actvCategoria.setText(actividadAEditar.getCategoria(), false);
            etResponsable.setText(actividadAEditar.getResponsable());
            etFecha.setText(actividadAEditar.getFecha());
            etHora.setText(actividadAEditar.getHora());
            etDescripcion.setText(actividadAEditar.getDescripcion());
            btnEliminar.setVisibility(View.VISIBLE);
        } else {
            tvFormTitle.setText("Nueva Actividad");
            actvCategoria.setText(CATEGORIAS[0], false);
            btnEliminar.setVisibility(View.GONE);
        }

        btnGuardar.setOnClickListener(v -> guardarActividad());
        btnEliminar.setOnClickListener(v -> confirmarEliminar());
    }

    private void mostrarDatePicker() {
        Calendar cal = Calendar.getInstance();
        String textoFecha = etFecha != null && etFecha.getText() != null ? etFecha.getText().toString().trim() : "";
        if (textoFecha.contains("/")) {
            try {
                String[] partes = textoFecha.split("/");
                if (partes.length == 3) {
                    int d = Integer.parseInt(partes[0].trim());
                    int m = Integer.parseInt(partes[1].trim()) - 1;
                    int y = Integer.parseInt(partes[2].trim());
                    cal.set(Calendar.YEAR, y);
                    cal.set(Calendar.MONTH, m);
                    cal.set(Calendar.DAY_OF_MONTH, d);
                }
            } catch (Exception ignored) {
            }
        }
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
            String fechaSeleccionada = String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, y);
            etFecha.setText(fechaSeleccionada);
        }, year, month, day);

        dialog.show();
    }

    private void mostrarTimePicker() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        String textoActual = etHora != null && etHora.getText() != null ? etHora.getText().toString().trim() : "";
        if (textoActual.contains(":")) {
            try {
                String[] partes = textoActual.split(":");
                if (partes.length >= 2) {
                    hour = Integer.parseInt(partes[0].trim());
                    minute = Integer.parseInt(partes[1].trim());
                }
            } catch (Exception ignored) {
            }
        }

        TimePickerDialog dialog = new TimePickerDialog(this, (view, h, m) -> {
            String horaSeleccionada = String.format(Locale.getDefault(), "%02d:%02d", h, m);
            etHora.setText(horaSeleccionada);
        }, hour, minute, true);

        dialog.show();
    }

    private void guardarActividad() {
        String titulo = etTitulo.getText() != null ? etTitulo.getText().toString().trim() : "";
        String categoria = actvCategoria.getText() != null ? actvCategoria.getText().toString().trim() : CATEGORIAS[0];
        String responsable = etResponsable.getText() != null ? etResponsable.getText().toString().trim() : "";
        String fecha = etFecha.getText() != null ? etFecha.getText().toString().trim() : "";
        String hora = etHora.getText() != null ? etHora.getText().toString().trim() : "";
        String descripcion = etDescripcion.getText() != null ? etDescripcion.getText().toString().trim() : "";

        if (TextUtils.isEmpty(titulo)) {
            etTitulo.setError("Ingrese el título de la actividad");
            return;
        }

        if (TextUtils.isEmpty(fecha)) {
            etFecha.setError("Seleccione la fecha");
            return;
        }

        btnGuardar.setEnabled(false);

        boolean completada = actividadAEditar != null && actividadAEditar.isCompletada();

        Actividad actividad = new Actividad(
                titulo,
                categoria,
                descripcion,
                fecha,
                hora,
                responsable,
                completada,
                currentUserId
        );

        if (actividadAEditar != null && actividadAEditar.getId() != null) {
            // Update existing
            db.collection("users")
                    .document(currentUserId)
                    .collection("actividades")
                    .document(actividadAEditar.getId())
                    .set(actividad)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Actividad actualizada", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnGuardar.setEnabled(true);
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Create new
            db.collection("users")
                    .document(currentUserId)
                    .collection("actividades")
                    .add(actividad)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Actividad guardada con éxito", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnGuardar.setEnabled(true);
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void confirmarEliminar() {
        if (actividadAEditar == null || actividadAEditar.getId() == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Eliminar Actividad")
                .setMessage("¿Estás seguro de que deseas eliminar esta actividad?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    db.collection("users")
                            .document(currentUserId)
                            .collection("actividades")
                            .document(actividadAEditar.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Actividad eliminada", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
