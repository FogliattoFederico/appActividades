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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import frgp.utn.edu.actividadesdeiara.model.Actividad;
import frgp.utn.edu.actividadesdeiara.model.Profesional;
import frgp.utn.edu.actividadesdeiara.receiver.NotificationReceiver;

public class AddEditActividadActivity extends AppCompatActivity {

    public static final String EXTRA_ACTIVIDAD = "extra_actividad";

    private static final String[] OPCIONES_CANTIDAD_TEXTO = new String[]{
            "1 recordatorio",
            "2 recordatorios (Doble aviso)"
    };

    private static final String[] OPCIONES_ANTICIPACION_TEXTO = new String[]{
            "En el momento de la actividad",
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

    private static final String[] OPCIONES_FRECUENCIA_TEXTO = new String[]{
            "Evento único (no se repite)",
            "Se repite semanalmente"
    };

    private static final String[] OPCIONES_DURACION_TEXTO = new String[]{
            "Durante 1 mes (4 semanas)",
            "Durante 3 meses (12 semanas)",
            "Durante 6 meses (26 semanas)",
            "Indefinidamente (1 año / 52 semanas)"
    };

    private TextView tvFormTitle, tvAvisoRecordatorio;
    private TextInputEditText etTitulo, etRubro, etEspecialidad, etFecha, etHora, etDescripcion;
    private TextInputLayout tilProfesional, tilFecha, tilHora, tilCantidadRecordatorios, tilAnticipacionRecordatorio1, tilAnticipacionRecordatorio2, tilRepeticion, tilDuracionRepeticion;
    private AutoCompleteTextView actvProfesional, actvCantidadRecordatorios, actvAnticipacionRecordatorio1, actvAnticipacionRecordatorio2, actvRepeticion, actvDuracionRepeticion;
    private View llOpcionesRecordatorio;
    private SwitchMaterial switchRecordatorio;
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

        tilRepeticion = findViewById(R.id.tilRepeticionActividad);
        actvRepeticion = findViewById(R.id.actvRepeticionActividad);
        tilDuracionRepeticion = findViewById(R.id.tilDuracionRepeticionActividad);
        actvDuracionRepeticion = findViewById(R.id.actvDuracionRepeticionActividad);

        etDescripcion = findViewById(R.id.etDescripcion);
        switchRecordatorio = findViewById(R.id.switchRecordatorioActividad);
        llOpcionesRecordatorio = findViewById(R.id.llOpcionesRecordatorioActividad);

        tilCantidadRecordatorios = findViewById(R.id.tilCantidadRecordatoriosActividad);
        actvCantidadRecordatorios = findViewById(R.id.actvCantidadRecordatoriosActividad);

        tilAnticipacionRecordatorio1 = findViewById(R.id.tilAnticipacionRecordatorioActividad1);
        actvAnticipacionRecordatorio1 = findViewById(R.id.actvAnticipacionRecordatorioActividad1);

        tilAnticipacionRecordatorio2 = findViewById(R.id.tilAnticipacionRecordatorioActividad2);
        actvAnticipacionRecordatorio2 = findViewById(R.id.actvAnticipacionRecordatorioActividad2);

        tvAvisoRecordatorio = findViewById(R.id.tvAvisoRecordatorioActividad);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnEliminar = findViewById(R.id.btnEliminar);

        adapterProfesionales = new ArrayAdapter<>(this, R.layout.item_spinner_dropdown, new ArrayList<>());
        actvProfesional.setAdapter(adapterProfesionales);

        bindDropdownClick(actvProfesional, tilProfesional);

        // Populate Repetición Adapters
        ArrayAdapter<String> adapterFrecuencia = new ArrayAdapter<>(this, R.layout.item_spinner_dropdown, OPCIONES_FRECUENCIA_TEXTO);
        actvRepeticion.setAdapter(adapterFrecuencia);
        actvRepeticion.setText(OPCIONES_FRECUENCIA_TEXTO[0], false);

        ArrayAdapter<String> adapterDuracion = new ArrayAdapter<>(this, R.layout.item_spinner_dropdown, OPCIONES_DURACION_TEXTO);
        actvDuracionRepeticion.setAdapter(adapterDuracion);
        actvDuracionRepeticion.setText(OPCIONES_DURACION_TEXTO[1], false);

        bindDropdownClick(actvRepeticion, tilRepeticion);
        bindDropdownClick(actvDuracionRepeticion, tilDuracionRepeticion);

        actvRepeticion.setOnItemClickListener((parent, view, position, id) -> {
            boolean esSemanal = position == 1;
            tilDuracionRepeticion.setVisibility(esSemanal ? View.VISIBLE : View.GONE);
        });

        // Populate Recordatorio Adapters
        ArrayAdapter<String> adapterCantidad = new ArrayAdapter<>(this, R.layout.item_spinner_dropdown, OPCIONES_CANTIDAD_TEXTO);
        actvCantidadRecordatorios.setAdapter(adapterCantidad);
        actvCantidadRecordatorios.setText(OPCIONES_CANTIDAD_TEXTO[0], false);

        ArrayAdapter<String> adapterAnticipacion1 = new ArrayAdapter<>(this, R.layout.item_spinner_dropdown, OPCIONES_ANTICIPACION_TEXTO);
        actvAnticipacionRecordatorio1.setAdapter(adapterAnticipacion1);
        actvAnticipacionRecordatorio1.setText(OPCIONES_ANTICIPACION_TEXTO[0], false);

        ArrayAdapter<String> adapterAnticipacion2 = new ArrayAdapter<>(this, R.layout.item_spinner_dropdown, OPCIONES_ANTICIPACION_TEXTO);
        actvAnticipacionRecordatorio2.setAdapter(adapterAnticipacion2);
        actvAnticipacionRecordatorio2.setText(OPCIONES_ANTICIPACION_TEXTO[1], false);

        bindDropdownClick(actvCantidadRecordatorios, tilCantidadRecordatorios);
        bindDropdownClick(actvAnticipacionRecordatorio1, tilAnticipacionRecordatorio1);
        bindDropdownClick(actvAnticipacionRecordatorio2, tilAnticipacionRecordatorio2);

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

            boolean esSemanal = "Semanal".equalsIgnoreCase(actividadAEditar.getFrecuenciaRepeticion());
            actvRepeticion.setText(OPCIONES_FRECUENCIA_TEXTO[esSemanal ? 1 : 0], false);
            tilDuracionRepeticion.setVisibility(View.GONE);

            switchRecordatorio.setChecked(actividadAEditar.isRecordatorio());
            llOpcionesRecordatorio.setVisibility(actividadAEditar.isRecordatorio() ? View.VISIBLE : View.GONE);

            int cantidad = actividadAEditar.getCantidadRecordatorios() > 0 ? actividadAEditar.getCantidadRecordatorios() : 1;
            actvCantidadRecordatorios.setText(OPCIONES_CANTIDAD_TEXTO[cantidad == 2 ? 1 : 0], false);

            int min1 = actividadAEditar.getMinutosAnticipacion();
            int idx1 = buscarIndiceMinutos(min1);
            actvAnticipacionRecordatorio1.setText(OPCIONES_ANTICIPACION_TEXTO[idx1], false);

            int min2 = actividadAEditar.getMinutosAnticipacion2();
            int idx2 = buscarIndiceMinutos(min2);
            actvAnticipacionRecordatorio2.setText(OPCIONES_ANTICIPACION_TEXTO[idx2], false);

            actualizarVisibilidadYTextoAviso();

            btnEliminar.setVisibility(View.VISIBLE);
        } else {
            tvFormTitle.setText("Nueva Actividad");
            btnEliminar.setVisibility(View.GONE);
        }

        cargarProfesionales();

        btnGuardar.setOnClickListener(v -> guardarActividad());
        btnEliminar.setOnClickListener(v -> confirmarEliminar());
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

    private List<String> generarFechasRepeticion(String fechaInicial, int semanas) {
        List<String> fechas = new ArrayList<>();
        if (fechaInicial == null || fechaInicial.trim().isEmpty()) return fechas;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date dateObj = sdf.parse(fechaInicial.trim());
            if (dateObj == null) return fechas;

            Calendar cal = Calendar.getInstance();
            cal.setTime(dateObj);

            for (int i = 0; i < semanas; i++) {
                fechas.add(sdf.format(cal.getTime()));
                cal.add(Calendar.WEEK_OF_YEAR, 1);
            }
        } catch (Exception ignored) {
        }

        return fechas;
    }

    private int obtenerSemanasDuracionActual() {
        String texto = actvDuracionRepeticion != null && actvDuracionRepeticion.getText() != null ? actvDuracionRepeticion.getText().toString().trim() : "";
        if (texto.contains("1 mes") || texto.contains("4")) return 4;
        if (texto.contains("3 meses") || texto.contains("12")) return 12;
        if (texto.contains("6 meses") || texto.contains("26")) return 26;
        if (texto.contains("1 año") || texto.contains("52") || texto.contains("Indefinidamente")) return 52;
        return 1;
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

        boolean esSemanal = actvRepeticion.getText() != null && actvRepeticion.getText().toString().trim().startsWith("Se repite");
        int semanasDuracion = esSemanal ? obtenerSemanasDuracionActual() : 1;

        List<String> fechasAProgramar = generarFechasRepeticion(fecha, semanasDuracion);
        if (fechasAProgramar.isEmpty()) {
            fechasAProgramar.add(fecha);
        }

        btnGuardar.setEnabled(false);

        // Validar que NO exista ninguna otra actividad en NINGUNA de las fechas/horarios correspondientes
        db.collection("users")
                .document(currentUserId)
                .collection("actividades")
                .get()
                .addOnSuccessListener(snapshot -> {
                    boolean superpuesto = false;
                    String fechaConflicto = "";
                    String tituloConflicto = "";

                    if (snapshot != null) {
                        for (QueryDocumentSnapshot doc : snapshot) {
                            if (actividadAEditar != null && doc.getId().equals(actividadAEditar.getId())) {
                                continue;
                            }

                            String fechaExistente = doc.getString("fecha");
                            String horaExistente = doc.getString("hora");

                            if (fechaExistente != null && horaExistente != null && !horaExistente.trim().isEmpty() && !hora.isEmpty()) {
                                if (fechasAProgramar.contains(fechaExistente.trim()) && horaExistente.trim().equalsIgnoreCase(hora)) {
                                    superpuesto = true;
                                    fechaConflicto = fechaExistente.trim();
                                    tituloConflicto = doc.getString("titulo");
                                    break;
                                }
                            }
                        }
                    }

                    if (superpuesto) {
                        btnGuardar.setEnabled(true);
                        String msj = "No se pueden programar las repeticiones: El día " + fechaConflicto + " a las " + hora + " hs ya existe la actividad '" + (tituloConflicto != null ? tituloConflicto : "") + "'.";
                        new AlertDialog.Builder(this)
                                .setTitle("Horario no disponible")
                                .setMessage(msj)
                                .setPositiveButton("Aceptar", null)
                                .show();
                        return;
                    }

                    ejecutarGuardadoSerie(titulo, rubro, especialidad, fechasAProgramar, hora, responsable, descripcion, esSemanal);
                })
                .addOnFailureListener(e -> {
                    btnGuardar.setEnabled(true);
                    Toast.makeText(this, "Error al validar disponibilidades: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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

    private void programarAlarmaNotificacion(String actividadId, int offset, String titulo, String descripcion, long timeMillis) {
        if (actividadId == null || timeMillis <= System.currentTimeMillis()) return;

        try {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, NotificationReceiver.class);
            int requestCode = Math.abs(actividadId.hashCode()) + offset + 200000;
            intent.putExtra("id", requestCode);
            intent.putExtra("titulo", "Recordatorio de Actividad");
            intent.putExtra("descripcion", !TextUtils.isEmpty(descripcion) ? (titulo + " - " + descripcion) : titulo);

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

    private void cancelarAlarmaNotificacion(String actividadId) {
        if (actividadId == null) return;
        try {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, NotificationReceiver.class);

            int requestCode1 = Math.abs(actividadId.hashCode()) + 200000;
            PendingIntent pendingIntent1 = PendingIntent.getBroadcast(
                    this,
                    requestCode1,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            int requestCode2 = Math.abs(actividadId.hashCode()) + 300000;
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

    private void ejecutarGuardadoSerie(String titulo, String rubro, String especialidad, List<String> fechas, String hora, String responsable, String descripcion, boolean esSemanal) {
        boolean completada = actividadAEditar != null && actividadAEditar.isCompletada();
        String categoria = !rubro.isEmpty() ? rubro : "Profesional";

        boolean recordatorio = switchRecordatorio.isChecked();
        boolean esDoble = actvCantidadRecordatorios.getText() != null && actvCantidadRecordatorios.getText().toString().trim().startsWith("2");
        int cantidadRecordatorios = recordatorio ? (esDoble ? 2 : 1) : 0;

        int minutosAnticipacion1 = recordatorio ? obtenerMinutosDeTexto(actvAnticipacionRecordatorio1) : 0;
        int minutosAnticipacion2 = (recordatorio && esDoble) ? obtenerMinutosDeTexto(actvAnticipacionRecordatorio2) : 0;

        String repeatGroupId = (actividadAEditar != null && actividadAEditar.getRepeatGroupId() != null)
                ? actividadAEditar.getRepeatGroupId()
                : UUID.randomUUID().toString();

        String frecuencia = esSemanal ? "Semanal" : "Único";

        if (actividadAEditar != null && actividadAEditar.getId() != null) {
            String actividadId = actividadAEditar.getId();
            long eventMillis = calcularMillisRecordatorio(fechas.get(0), hora);
            long recordatorioTimeMillis1 = (recordatorio && eventMillis > 0) ? (eventMillis - (minutosAnticipacion1 * 60 * 1000L)) : 0L;
            long recordatorioTimeMillis2 = (recordatorio && esDoble && eventMillis > 0) ? (eventMillis - (minutosAnticipacion2 * 60 * 1000L)) : 0L;

            Actividad actividad = new Actividad(
                    titulo,
                    categoria,
                    descripcion,
                    fechas.get(0),
                    hora,
                    responsable,
                    rubro,
                    especialidad,
                    completada,
                    recordatorio,
                    cantidadRecordatorios,
                    minutosAnticipacion1,
                    minutosAnticipacion2,
                    recordatorioTimeMillis1,
                    recordatorioTimeMillis2,
                    currentUserId
            );
            actividad.setRepeatGroupId(repeatGroupId);
            actividad.setFrecuenciaRepeticion(frecuencia);

            db.collection("users")
                    .document(currentUserId)
                    .collection("actividades")
                    .document(actividadId)
                    .set(actividad)
                    .addOnSuccessListener(aVoid -> {
                        cancelarAlarmaNotificacion(actividadId);
                        if (recordatorio) {
                            if (recordatorioTimeMillis1 > System.currentTimeMillis()) {
                                programarAlarmaNotificacion(actividadId, 0, titulo, descripcion, recordatorioTimeMillis1);
                            }
                            if (esDoble && recordatorioTimeMillis2 > System.currentTimeMillis()) {
                                programarAlarmaNotificacion(actividadId, 100000, titulo, descripcion, recordatorioTimeMillis2);
                            }
                        }
                        Toast.makeText(this, "Actividad actualizada", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnGuardar.setEnabled(true);
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Guardar todas las repeticiones generadas
            int totalGuardados = fechas.size();
            final int[] guardadosExitosos = {0};

            for (String f : fechas) {
                long eventMillis = calcularMillisRecordatorio(f, hora);
                long recordatorioTimeMillis1 = (recordatorio && eventMillis > 0) ? (eventMillis - (minutosAnticipacion1 * 60 * 1000L)) : 0L;
                long recordatorioTimeMillis2 = (recordatorio && esDoble && eventMillis > 0) ? (eventMillis - (minutosAnticipacion2 * 60 * 1000L)) : 0L;

                Actividad actividad = new Actividad(
                        titulo,
                        categoria,
                        descripcion,
                        f,
                        hora,
                        responsable,
                        rubro,
                        especialidad,
                        false,
                        recordatorio,
                        cantidadRecordatorios,
                        minutosAnticipacion1,
                        minutosAnticipacion2,
                        recordatorioTimeMillis1,
                        recordatorioTimeMillis2,
                        currentUserId
                );
                actividad.setRepeatGroupId(repeatGroupId);
                actividad.setFrecuenciaRepeticion(frecuencia);

                db.collection("users")
                        .document(currentUserId)
                        .collection("actividades")
                        .add(actividad)
                        .addOnSuccessListener(documentReference -> {
                            String actividadId = documentReference.getId();
                            if (recordatorio) {
                                if (recordatorioTimeMillis1 > System.currentTimeMillis()) {
                                    programarAlarmaNotificacion(actividadId, 0, titulo, descripcion, recordatorioTimeMillis1);
                                }
                                if (esDoble && recordatorioTimeMillis2 > System.currentTimeMillis()) {
                                    programarAlarmaNotificacion(actividadId, 100000, titulo, descripcion, recordatorioTimeMillis2);
                                }
                            }
                            guardadosExitosos[0]++;
                            if (guardadosExitosos[0] == totalGuardados) {
                                String msj = totalGuardados > 1 ? ("Se agendaron " + totalGuardados + " actividades repetidas con éxito") : "Actividad guardada con éxito";
                                Toast.makeText(this, msj, Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .addOnFailureListener(e -> {
                            btnGuardar.setEnabled(true);
                            Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        }
    }

    private void confirmarEliminar() {
        if (actividadAEditar == null || actividadAEditar.getId() == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Eliminar Actividad")
                .setMessage("¿Estás seguro de que deseas eliminar esta actividad?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    cancelarAlarmaNotificacion(actividadAEditar.getId());
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
