package frgp.utn.edu.actividadesdeiara;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
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
    private MaterialButton btnGuardar;

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

        // Click listeners for Date & Time Pickers
        View.OnClickListener listenerFecha = v -> mostrarDatePicker();
        etFecha.setOnClickListener(listenerFecha);
        if (tilFechaTarea != null) {
            tilFechaTarea.setEndIconOnClickListener(listenerFecha);
        }

        View.OnClickListener listenerHora = v -> mostrarTimePicker();
        etHorario.setOnClickListener(listenerHora);
        if (tilHorarioTarea != null) {
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
        } else {
            tvFormTitle.setText("Nueva Tarea");
        }

        btnGuardar.setOnClickListener(v -> guardarTarea());
    }

    private void mostrarDatePicker() {
        Calendar cal = Calendar.getInstance();
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

        TimePickerDialog dialog = new TimePickerDialog(this, (view, h, m) -> {
            String horaSeleccionada = String.format(Locale.getDefault(), "%02d:%02d", h, m);
            etHorario.setText(horaSeleccionada);
        }, hour, minute, true);

        dialog.show();
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
