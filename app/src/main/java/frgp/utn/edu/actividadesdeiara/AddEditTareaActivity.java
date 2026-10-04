package frgp.utn.edu.actividadesdeiara;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Locale;

import frgp.utn.edu.actividadesdeiara.model.Tarea;
import frgp.utn.edu.actividadesdeiara.receiver.NotificationReceiver;

public class AddEditTareaActivity extends AppCompatActivity {

    public static final String EXTRA_TAREA = "extra_tarea";

    private static final String[] OPCIONES_CANTIDAD_TEXTO = new String[]{
            "1 recordatorio",
            "2 recordatorios (Doble aviso)"
    };

    private static final String[] OPCIONES_ANTICIPACION_TEXTO = new String[]{
            "En el momento de la tarea",
            "10 minutos antes",
            "30 minutos antes",
            "1 hora antes",
            "2 horas antes",
            "1 día antes"
    };

    private static final int[] OPCIONES_ANTICIPACION_MINUTOS = new int[]{
            0,
            10,
            30,
            60,
            120,
            1440
    };

    private TextView tvFormTitle, tvAvisoRecordatorio;
    private TextInputEditText etNombre, etDescripcion, etFecha, etHorario;
    private TextInputLayout tilFechaTarea, tilHorarioTarea, tilCantidadRecordatorios, tilAnticipacionRecordatorio1, tilAnticipacionRecordatorio2;
    private AutoCompleteTextView actvCantidadRecordatorios, actvAnticipacionRecordatorio1, actvAnticipacionRecordatorio2;
    private View llOpcionesRecordatorio;
    private SwitchMaterial switchRecordatorio;
    private MaterialButton btnGuardar, btnEliminar;

    private FirebaseFirestore db;
    private String currentUserId;
    private Tarea tareaAEditar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_tarea);

        View root = findViewById(R.id.rootViewTarea);
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

        tvFormTitle = findViewById(R.id.tvFormTitleTarea);
        etNombre = findViewById(R.id.etNombreTarea);
        etDescripcion = findViewById(R.id.etDescripcionTarea);
        etFecha = findViewById(R.id.etFechaTarea);
        etHorario = findViewById(R.id.etHorarioTarea);
        tilFechaTarea = findViewById(R.id.tilFechaTarea);
        tilHorarioTarea = findViewById(R.id.tilHorarioTarea);
        switchRecordatorio = findViewById(R.id.switchRecordatorioTarea);
        llOpcionesRecordatorio = findViewById(R.id.llOpcionesRecordatorioTarea);

        tilCantidadRecordatorios = findViewById(R.id.tilCantidadRecordatoriosTarea);
        actvCantidadRecordatorios = findViewById(R.id.actvCantidadRecordatoriosTarea);

        tilAnticipacionRecordatorio1 = findViewById(R.id.tilAnticipacionRecordatorioTarea);
        actvAnticipacionRecordatorio1 = findViewById(R.id.actvAnticipacionRecordatorioTarea);

        tilAnticipacionRecordatorio2 = findViewById(R.id.tilAnticipacionRecordatorioTarea2);
        actvAnticipacionRecordatorio2 = findViewById(R.id.actvAnticipacionRecordatorioTarea2);

        tvAvisoRecordatorio = findViewById(R.id.tvAvisoRecordatorioTarea);
        btnGuardar = findViewById(R.id.btnGuardarTarea);
        btnEliminar = findViewById(R.id.btnEliminarTarea);

        // Populate Adapters
        ArrayAdapter<String> adapterCantidad = new ArrayAdapter<>(this, R.layout.item_spinner_dropdown, OPCIONES_CANTIDAD_TEXTO);
        actvCantidadRecordatorios.setAdapter(adapterCantidad);
        actvCantidadRecordatorios.setText(OPCIONES_CANTIDAD_TEXTO[0], false);

        ArrayAdapter<String> adapterAnticipacion1 = new ArrayAdapter<>(this, R.layout.item_spinner_dropdown, OPCIONES_ANTICIPACION_TEXTO);
        actvAnticipacionRecordatorio1.setAdapter(adapterAnticipacion1);
        actvAnticipacionRecordatorio1.setText(OPCIONES_ANTICIPACION_TEXTO[0], false);

        ArrayAdapter<String> adapterAnticipacion2 = new ArrayAdapter<>(this, R.layout.item_spinner_dropdown, OPCIONES_ANTICIPACION_TEXTO);
        actvAnticipacionRecordatorio2.setAdapter(adapterAnticipacion2);
        actvAnticipacionRecordatorio2.setText(OPCIONES_ANTICIPACION_TEXTO[1], false);

        // Click listeners for Dropdowns
        bindDropdownClick(actvCantidadRecordatorios, tilCantidadRecordatorios);
        bindDropdownClick(actvAnticipacionRecordatorio1, tilAnticipacionRecordatorio1);
        bindDropdownClick(actvAnticipacionRecordatorio2, tilAnticipacionRecordatorio2);

        // Click listeners for Date & Time Pickers
        View.OnClickListener listenerFecha = v -> mostrarDatePicker();
        etFecha.setOnClickListener(listenerFecha);
        if (tilFechaTarea != null) {
            tilFechaTarea.setOnClickListener(listenerFecha);
            tilFechaTarea.setEndIconOnClickListener(listenerFecha);
        }

        View.OnClickListener listenerHora = v -> mostrarTimePicker();
        etHorario.setOnClickListener(listenerHora);
        if (tilHorarioTarea != null) {
            tilHorarioTarea.setOnClickListener(listenerHora);
            tilHorarioTarea.setEndIconOnClickListener(listenerHora);
        }

        switchRecordatorio.setOnCheckedChangeListener((buttonView, isChecked) -> {
            llOpcionesRecordatorio.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (isChecked) {
                solicitarPermisoNotificaciones();
                actualizarVisibilidadYTextoAviso();
            }
        });

        actvCantidadRecordatorios.setOnItemClickListener((parent, view, position, id) -> actualizarVisibilidadYTextoAviso());
        actvAnticipacionRecordatorio1.setOnItemClickListener((parent, view, position, id) -> actualizarVisibilidadYTextoAviso());
        actvAnticipacionRecordatorio2.setOnItemClickListener((parent, view, position, id) -> actualizarVisibilidadYTextoAviso());

        if (etDescripcion != null) {
            etDescripcion.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus && root instanceof ScrollView) {
                    root.postDelayed(() -> ((ScrollView) root).smoothScrollTo(0, v.getBottom() + 200), 200);
                }
            });
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
            switchRecordatorio.setChecked(tareaAEditar.isRecordatorio());
            llOpcionesRecordatorio.setVisibility(tareaAEditar.isRecordatorio() ? View.VISIBLE : View.GONE);

            int cantidad = tareaAEditar.getCantidadRecordatorios() > 0 ? tareaAEditar.getCantidadRecordatorios() : 1;
            actvCantidadRecordatorios.setText(OPCIONES_CANTIDAD_TEXTO[cantidad == 2 ? 1 : 0], false);

            int min1 = tareaAEditar.getMinutosAnticipacion();
            int idx1 = buscarIndiceMinutos(min1);
            actvAnticipacionRecordatorio1.setText(OPCIONES_ANTICIPACION_TEXTO[idx1], false);

            int min2 = tareaAEditar.getMinutosAnticipacion2();
            int idx2 = buscarIndiceMinutos(min2);
            actvAnticipacionRecordatorio2.setText(OPCIONES_ANTICIPACION_TEXTO[idx2], false);

            actualizarVisibilidadYTextoAviso();

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

    private void bindDropdownClick(AutoCompleteTextView actv, TextInputLayout til) {
        View.OnClickListener listener = v -> actv.showDropDown();
        actv.setOnClickListener(listener);
        if (til != null) {
            til.setOnClickListener(listener);
            til.setEndIconOnClickListener(listener);
        }
    }

    private int buscarIndiceMinutos(int minutos) {
        for (int i = 0; i < OPCIONES_ANTICIPACION_MINUTOS.length; i++) {
            if (OPCIONES_ANTICIPACION_MINUTOS[i] == minutos) {
                return i;
            }
        }
        return 0;
    }

    private int obtenerMinutosDeTexto(AutoCompleteTextView actv) {
        String seleccion = actv.getText() != null ? actv.getText().toString().trim() : "";
        for (int i = 0; i < OPCIONES_ANTICIPACION_TEXTO.length; i++) {
            if (OPCIONES_ANTICIPACION_TEXTO[i].equalsIgnoreCase(seleccion)) {
                return OPCIONES_ANTICIPACION_MINUTOS[i];
            }
        }
        return 0;
    }

    private void actualizarVisibilidadYTextoAviso() {
        boolean esDoble = actvCantidadRecordatorios.getText() != null && actvCantidadRecordatorios.getText().toString().trim().startsWith("2");
        tilAnticipacionRecordatorio2.setVisibility(esDoble ? View.VISIBLE : View.GONE);

        String texto1 = actvAnticipacionRecordatorio1.getText() != null ? actvAnticipacionRecordatorio1.getText().toString().trim() : "";
        if (!esDoble) {
            if (texto1.equals(OPCIONES_ANTICIPACION_TEXTO[0])) {
                tvAvisoRecordatorio.setText("Se enviará 1 notificación en la fecha y hora indicadas.");
            } else {
                tvAvisoRecordatorio.setText("Se enviará 1 notificación " + texto1.toLowerCase(Locale.getDefault()) + " del horario agendado.");
            }
        } else {
            String texto2 = actvAnticipacionRecordatorio2.getText() != null ? actvAnticipacionRecordatorio2.getText().toString().trim() : "";
            tvAvisoRecordatorio.setText("Se enviarán 2 notificaciones: la primera " + texto1.toLowerCase(Locale.getDefault()) + " y la segunda " + texto2.toLowerCase(Locale.getDefault()) + ".");
        }
    }

    private void solicitarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
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

        new AlertDialog.Builder(this)
                .setTitle("Eliminar Tarea")
                .setMessage("¿Estás seguro de que deseas eliminar esta tarea?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    cancelarAlarmaNotificacion(tareaAEditar.getId());
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

    private long calcularMillisRecordatorio(String fecha, String horario) {
        if (TextUtils.isEmpty(fecha)) return 0L;

        try {
            Calendar cal = Calendar.getInstance();
            String[] partesFecha = fecha.split("/");
            if (partesFecha.length == 3) {
                int d = Integer.parseInt(partesFecha[0].trim());
                int m = Integer.parseInt(partesFecha[1].trim()) - 1;
                int y = Integer.parseInt(partesFecha[2].trim());
                cal.set(Calendar.YEAR, y);
                cal.set(Calendar.MONTH, m);
                cal.set(Calendar.DAY_OF_MONTH, d);
            }

            int hour = 9;
            int minute = 0;
            if (!TextUtils.isEmpty(horario) && horario.contains(":")) {
                String[] partesHora = horario.split(":");
                if (partesHora.length >= 2) {
                    hour = Integer.parseInt(partesHora[0].trim());
                    minute = Integer.parseInt(partesHora[1].trim());
                }
            }
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            return cal.getTimeInMillis();
        } catch (Exception e) {
            return 0L;
        }
    }

    private void programarAlarmaNotificacion(String tareaId, int offset, String nombre, String descripcion, long timeMillis) {
        if (tareaId == null || timeMillis <= System.currentTimeMillis()) return;

        try {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, NotificationReceiver.class);
            int requestCode = Math.abs(tareaId.hashCode()) + offset;
            intent.putExtra("id", requestCode);
            intent.putExtra("titulo", "Recordatorio de Tarea");
            intent.putExtra("descripcion", !TextUtils.isEmpty(descripcion) ? (nombre + " - " + descripcion) : nombre);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (alarmManager != null) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent);
            }
        } catch (Exception ignored) {
        }
    }

    private void cancelarAlarmaNotificacion(String tareaId) {
        if (tareaId == null) return;
        try {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, NotificationReceiver.class);

            int requestCode1 = Math.abs(tareaId.hashCode());
            PendingIntent pendingIntent1 = PendingIntent.getBroadcast(
                    this,
                    requestCode1,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            int requestCode2 = Math.abs(tareaId.hashCode()) + 100000;
            PendingIntent pendingIntent2 = PendingIntent.getBroadcast(
                    this,
                    requestCode2,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent1);
                alarmManager.cancel(pendingIntent2);
            }
        } catch (Exception ignored) {
        }
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
        boolean recordatorio = switchRecordatorio.isChecked();

        boolean esDoble = actvCantidadRecordatorios.getText() != null && actvCantidadRecordatorios.getText().toString().trim().startsWith("2");
        int cantidadRecordatorios = recordatorio ? (esDoble ? 2 : 1) : 0;

        int minutosAnticipacion1 = recordatorio ? obtenerMinutosDeTexto(actvAnticipacionRecordatorio1) : 0;
        int minutosAnticipacion2 = (recordatorio && esDoble) ? obtenerMinutosDeTexto(actvAnticipacionRecordatorio2) : 0;

        long eventMillis = calcularMillisRecordatorio(fecha, horario);

        long recordatorioTimeMillis1 = (recordatorio && eventMillis > 0) ? (eventMillis - (minutosAnticipacion1 * 60 * 1000L)) : 0L;
        long recordatorioTimeMillis2 = (recordatorio && esDoble && eventMillis > 0) ? (eventMillis - (minutosAnticipacion2 * 60 * 1000L)) : 0L;

        Tarea tarea = new Tarea(
                nombre,
                descripcion,
                fecha,
                horario,
                completada,
                recordatorio,
                cantidadRecordatorios,
                minutosAnticipacion1,
                minutosAnticipacion2,
                recordatorioTimeMillis1,
                recordatorioTimeMillis2,
                currentUserId
        );

        if (tareaAEditar != null && tareaAEditar.getId() != null) {
            String tareaId = tareaAEditar.getId();
            db.collection("users")
                    .document(currentUserId)
                    .collection("tareas")
                    .document(tareaId)
                    .set(tarea)
                    .addOnSuccessListener(aVoid -> {
                        cancelarAlarmaNotificacion(tareaId);
                        if (recordatorio) {
                            if (recordatorioTimeMillis1 > System.currentTimeMillis()) {
                                programarAlarmaNotificacion(tareaId, 0, nombre, descripcion, recordatorioTimeMillis1);
                            }
                            if (esDoble && recordatorioTimeMillis2 > System.currentTimeMillis()) {
                                programarAlarmaNotificacion(tareaId, 100000, nombre, descripcion, recordatorioTimeMillis2);
                            }
                        }
                        Toast.makeText(this, "Tarea actualizada", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnGuardar.setEnabled(true);
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            db.collection("users")
                    .document(currentUserId)
                    .collection("tareas")
                    .add(tarea)
                    .addOnSuccessListener(documentReference -> {
                        String tareaId = documentReference.getId();
                        if (recordatorio) {
                            if (recordatorioTimeMillis1 > System.currentTimeMillis()) {
                                programarAlarmaNotificacion(tareaId, 0, nombre, descripcion, recordatorioTimeMillis1);
                            }
                            if (esDoble && recordatorioTimeMillis2 > System.currentTimeMillis()) {
                                programarAlarmaNotificacion(tareaId, 100000, nombre, descripcion, recordatorioTimeMillis2);
                            }
                        }
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
