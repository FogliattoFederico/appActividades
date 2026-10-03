package frgp.utn.edu.actividadesdeiara;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import frgp.utn.edu.actividadesdeiara.model.Actividad;
import frgp.utn.edu.actividadesdeiara.model.Profesional;

public class AddEditActividadActivity extends AppCompatActivity {

    public static final String EXTRA_ACTIVIDAD = "extra_actividad";

    private TextView tvFormTitle;
    private TextInputEditText etTitulo, etRubro, etEspecialidad, etFecha, etHora, etDescripcion;
    private TextInputLayout tilProfesional, tilFecha, tilHora;
    private AutoCompleteTextView actvProfesional;
    private MaterialButton btnGuardar, btnEliminar;

    private FirebaseFirestore db;
    private String currentUserId;
    private Actividad actividadAEditar;

    private final List<Profesional> listaProfesionales = new ArrayList<>();
    private ArrayAdapter<String> adapterProfesionales;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_actividad);

        View root = findViewById(R.id.rootViewActividad);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
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

        tvFormTitle = findViewById(R.id.tvFormTitle);
        etTitulo = findViewById(R.id.etTitulo);
        actvProfesional = findViewById(R.id.actvProfesional);
        tilProfesional = findViewById(R.id.tilProfesional);
        etRubro = findViewById(R.id.etRubro);
        etEspecialidad = findViewById(R.id.etEspecialidad);
        etFecha = findViewById(R.id.etFecha);
        etHora = findViewById(R.id.etHora);
        tilFecha = findViewById(R.id.tilFecha);
        tilHora = findViewById(R.id.tilHora);
        etDescripcion = findViewById(R.id.etDescripcion);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnEliminar = findViewById(R.id.btnEliminar);

        adapterProfesionales = new ArrayAdapter<>(this, R.layout.item_spinner_dropdown, new ArrayList<>());
        actvProfesional.setAdapter(adapterProfesionales);

        View.OnClickListener listenerProfesional = v -> actvProfesional.showDropDown();
        actvProfesional.setOnClickListener(listenerProfesional);
        if (tilProfesional != null) {
            tilProfesional.setOnClickListener(listenerProfesional);
            tilProfesional.setEndIconOnClickListener(listenerProfesional);
        }

        actvProfesional.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < listaProfesionales.size()) {
                Profesional p = listaProfesionales.get(position);
                etRubro.setText(p.getRubro() != null ? p.getRubro() : "");
                etEspecialidad.setText(p.getEspecialidad() != null ? p.getEspecialidad() : "");
            }
        });

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
            actvProfesional.setText(actividadAEditar.getResponsable() != null ? actividadAEditar.getResponsable() : "", false);
            etRubro.setText(actividadAEditar.getRubro() != null ? actividadAEditar.getRubro() : "");
            etEspecialidad.setText(actividadAEditar.getEspecialidad() != null ? actividadAEditar.getEspecialidad() : "");
            etFecha.setText(actividadAEditar.getFecha());
            etHora.setText(actividadAEditar.getHora());
            etDescripcion.setText(actividadAEditar.getDescripcion());
            btnEliminar.setVisibility(View.VISIBLE);
        } else {
            tvFormTitle.setText("Nueva Actividad");
            btnEliminar.setVisibility(View.GONE);
        }

        cargarProfesionales();

        btnGuardar.setOnClickListener(v -> guardarActividad());
        btnEliminar.setOnClickListener(v -> confirmarEliminar());
    }

    private void cargarProfesionales() {
        if (currentUserId == null) return;

        db.collection("users")
                .document(currentUserId)
                .collection("profesionales")
                .get()
                .addOnSuccessListener(snapshot -> {
                    listaProfesionales.clear();
                    List<String> nombres = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        Profesional p = doc.toObject(Profesional.class);
                        p.setId(doc.getId());
                        listaProfesionales.add(p);

                        String apellido = p.getApellido() != null ? p.getApellido().trim() : "";
                        String nombre = p.getNombre() != null ? p.getNombre().trim() : "";
                        String nombreCompleto = (!apellido.isEmpty() && !nombre.isEmpty()) ? (apellido + ", " + nombre) : (!apellido.isEmpty() ? apellido : nombre);
                        nombres.add(nombreCompleto);
                    }

                    adapterProfesionales = new ArrayAdapter<>(this, R.layout.item_spinner_dropdown, nombres);
                    actvProfesional.setAdapter(adapterProfesionales);

                    if (listaProfesionales.isEmpty()) {
                        Toast.makeText(this, "No tienes profesionales registrados. Primero registra un profesional.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar profesionales: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
        String responsable = actvProfesional.getText() != null ? actvProfesional.getText().toString().trim() : "";
        String rubro = etRubro.getText() != null ? etRubro.getText().toString().trim() : "";
        String especialidad = etEspecialidad.getText() != null ? etEspecialidad.getText().toString().trim() : "";
        String fecha = etFecha.getText() != null ? etFecha.getText().toString().trim() : "";
        String hora = etHora.getText() != null ? etHora.getText().toString().trim() : "";
        String descripcion = etDescripcion.getText() != null ? etDescripcion.getText().toString().trim() : "";

        if (TextUtils.isEmpty(titulo)) {
            etTitulo.setError("Ingrese el título de la actividad");
            return;
        }

        if (TextUtils.isEmpty(responsable)) {
            Toast.makeText(this, "Seleccione un profesional previamente registrado", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(fecha)) {
            etFecha.setError("Seleccione la fecha");
            return;
        }

        btnGuardar.setEnabled(false);

        // Validar superposición de fecha y hora
        db.collection("users")
                .document(currentUserId)
                .collection("actividades")
                .whereEqualTo("fecha", fecha)
                .get()
                .addOnSuccessListener(snapshot -> {
                    boolean superpuesto = false;
                    String tituloExistente = "";

                    if (snapshot != null) {
                        for (QueryDocumentSnapshot doc : snapshot) {
                            if (actividadAEditar != null && doc.getId().equals(actividadAEditar.getId())) {
                                continue;
                            }

                            String horaExistente = doc.getString("hora");
                            if (horaExistente != null && !horaExistente.trim().isEmpty() && !hora.isEmpty()) {
                                if (horaExistente.trim().equalsIgnoreCase(hora)) {
                                    superpuesto = true;
                                    tituloExistente = doc.getString("titulo");
                                    break;
                                }
                            }
                        }
                    }

                    if (superpuesto) {
                        btnGuardar.setEnabled(true);
                        String msj = "Ya existe una actividad (" + (tituloExistente != null ? tituloExistente : "") + ") programada para el día " + fecha + " a las " + hora + " hs.";
                        new AlertDialog.Builder(this)
                                .setTitle("Horario no disponible")
                                .setMessage(msj)
                                .setPositiveButton("Aceptar", null)
                                .show();
                        return;
                    }

                    ejecutarGuardado(titulo, rubro, especialidad, fecha, hora, responsable, descripcion);
                })
                .addOnFailureListener(e -> {
                    btnGuardar.setEnabled(true);
                    Toast.makeText(this, "Error al validar horario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void ejecutarGuardado(String titulo, String rubro, String especialidad, String fecha, String hora, String responsable, String descripcion) {
        boolean completada = actividadAEditar != null && actividadAEditar.isCompletada();
        String categoria = !rubro.isEmpty() ? rubro : "Profesional";

        Actividad actividad = new Actividad(
                titulo,
                categoria,
                descripcion,
                fecha,
                hora,
                responsable,
                rubro,
                especialidad,
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
