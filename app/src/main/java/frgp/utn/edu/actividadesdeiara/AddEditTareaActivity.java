package frgp.utn.edu.actividadesdeiara;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Locale;

import frgp.utn.edu.actividadesdeiara.model.Tarea;

public class AddEditTareaActivity extends AppCompatActivity {

    public static final String EXTRA_TAREA = "extra_tarea";

    private TextView tvFormTitle;
    private TextInputEditText etNombre, etDescripcion, etFecha, etHorario;
    private TextInputLayout tilFechaTarea, tilHorarioTarea;
    private MaterialButton btnGuardar, btnEliminar;

    private FirebaseFirestore db;
    private String currentUserId;
    private Tarea tareaAEditar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_tarea);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }
        currentUserId = user.getUid();
        db = FirebaseFirestore.getInstance();

        tvFormTitle = findViewById(R.id.tvFormTitleTarea);
        etNombre = findViewById(R.id.etNombreTarea);
        etDescripcion = findViewById(R.id.etDescripcionTarea);
        etFecha = findViewById(R.id.etFechaTarea);
        etHorario = findViewById(R.id.etHorarioTarea);
        tilFechaTarea = findViewById(R.id.tilFechaTarea);
        tilHorarioTarea = findViewById(R.id.tilHorarioTarea);
        btnGuardar = findViewById(R.id.btnGuardarTarea);
        btnEliminar = findViewById(R.id.btnEliminarTarea);

        // Click listeners for Date & Time Pickers
        View.OnClickListener listenerFecha = v -> mostrarDatePicker();
        etFecha.setOnClickListener(listenerFecha);
        etFecha.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick();
                mostrarDatePicker();
                return true;
            }
            return false;
        });
        if (tilFechaTarea != null) {
            tilFechaTarea.setOnClickListener(listenerFecha);
            tilFechaTarea.setEndIconOnClickListener(listenerFecha);
        }

        View.OnClickListener listenerHora = v -> mostrarTimePicker();
        etHorario.setOnClickListener(listenerHora);
        etHorario.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick();
                mostrarTimePicker();
                return true;
            }
            return false;
        });
        if (tilHorarioTarea != null) {
            tilHorarioTarea.setOnClickListener(listenerHora);
            tilHorarioTarea.setEndIconOnClickListener(listenerHora);
        }

        if (getIntent().hasExtra(EXTRA_TAREA)) {
            tareaAEditar = (Tarea) getIntent().getSerializableExtra(EXTRA_TAREA);
        }

        if (tareaAEditar != null) {
            tvFormTitle.setText("Editar Tarea");
            etNombre.setText(tareaAEditar.getNombre());
            etDescripcion.setText(tareaAEditar.getDescripcion());
            etFecha.setText(tareaAEditar.getFecha());
            etHorario.setText(tareaAEditar.getHorario());
            if (btnEliminar != null) {
                btnEliminar.setVisibility(View.VISIBLE);
            }
        } else {
            tvFormTitle.setText("Nueva Tarea");
            if (btnEliminar != null) {
                btnEliminar.setVisibility(View.GONE);
            }
        }

        btnGuardar.setOnClickListener(v -> guardarTarea());
        if (btnEliminar != null) {
            btnEliminar.setOnClickListener(v -> confirmarEliminar());
        }
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

        String textoActual = etHorario != null && etHorario.getText() != null ? etHorario.getText().toString().trim() : "";
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
            etHorario.setText(horaSeleccionada);
        }, hour, minute, true);

        dialog.show();
    }

    private void confirmarEliminar() {
        if (tareaAEditar == null || tareaAEditar.getId() == null) return;

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Eliminar Tarea")
                .setMessage("¿Estás seguro de que deseas eliminar esta tarea?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    db.collection("users")
                            .document(currentUserId)
                            .collection("tareas")
                            .document(tareaAEditar.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Tarea eliminada", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardarTarea() {
        String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
        String descripcion = etDescripcion.getText() != null ? etDescripcion.getText().toString().trim() : "";
        String fecha = etFecha.getText() != null ? etFecha.getText().toString().trim() : "";
        String horario = etHorario.getText() != null ? etHorario.getText().toString().trim() : "";

        if (TextUtils.isEmpty(nombre)) {
            etNombre.setError("Ingrese el nombre de la tarea");
            return;
        }

        btnGuardar.setEnabled(false);

        boolean completada = tareaAEditar != null && tareaAEditar.isCompletada();

        Tarea tarea = new Tarea(
                nombre,
                descripcion,
                fecha,
                horario,
                completada,
                currentUserId
        );

        if (tareaAEditar != null && tareaAEditar.getId() != null) {
            // Actualizar tarea
            db.collection("users")
                    .document(currentUserId)
                    .collection("tareas")
                    .document(tareaAEditar.getId())
                    .set(tarea)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Tarea actualizada", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnGuardar.setEnabled(true);
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Crear nueva tarea
            db.collection("users")
                    .document(currentUserId)
                    .collection("tareas")
                    .add(tarea)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Tarea guardada con éxito", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnGuardar.setEnabled(true);
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
