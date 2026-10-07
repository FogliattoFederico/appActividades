package frgp.utn.edu.actividadesdeiara;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import frgp.utn.edu.actividadesdeiara.model.Profesional;
import frgp.utn.edu.actividadesdeiara.model.Rubro;

public class AddEditProfesionalActivity extends AppCompatActivity {

    public static final String EXTRA_PROFESIONAL = "extra_profesional";

    private FirebaseFirestore db;
    private String currentUserId;

    private TextInputEditText etNombre, etApellido, etTelefono, etEmail, etSesionesSemanales;
    private TextInputEditText etDireccionAtencion, etHonorarios, etCantidadBonos, etAdicional;
    private TextInputLayout tilDireccionAtencion, tilHonorarios;
    private SwitchMaterial switchAtencionDomicilio, switchAtiendeObraSocial;
    private LinearLayout llObraSocialCampos;
    private AutoCompleteTextView actvRubro, actvEspecialidad;
    private MaterialButton btnGuardar;

    private final List<Rubro> listaRubros = new ArrayList<>();
    private ArrayAdapter<String> adapterRubros;
    private ArrayAdapter<String> adapterEspecialidades;

    private Profesional profesionalEnEdicion;
    private String especialidadPendienteAlEditar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_profesional);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        MaterialToolbar toolbar = findViewById(R.id.toolbarProfesional);
        if (toolbar != null) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(0, systemBars.top, 0, 0);
                return insets;
            });
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        etTelefono = findViewById(R.id.etTelefono);
        etEmail = findViewById(R.id.etEmail);
        etSesionesSemanales = findViewById(R.id.etSesionesSemanales);
        etDireccionAtencion = findViewById(R.id.etDireccionAtencion);
        etHonorarios = findViewById(R.id.etHonorarios);
        etCantidadBonos = findViewById(R.id.etCantidadBonos);
        etAdicional = findViewById(R.id.etAdicional);

        tilDireccionAtencion = findViewById(R.id.tilDireccionAtencion);
        tilHonorarios = findViewById(R.id.tilHonorarios);

        switchAtencionDomicilio = findViewById(R.id.switchAtencionDomicilio);
        switchAtiendeObraSocial = findViewById(R.id.switchAtiendeObraSocial);
        llObraSocialCampos = findViewById(R.id.llObraSocialCampos);

        actvRubro = findViewById(R.id.actvRubro);
        actvEspecialidad = findViewById(R.id.actvEspecialidad);
        TextInputLayout tilRubro = findViewById(R.id.tilRubro);
        TextInputLayout tilEspecialidad = findViewById(R.id.tilEspecialidad);
        btnGuardar = findViewById(R.id.btnGuardarProfesional);

        adapterRubros = new ArrayAdapter<>(this, R.layout.item_spinner_dropdown, new ArrayList<>());
        actvRubro.setAdapter(adapterRubros);

        adapterEspecialidades = new ArrayAdapter<>(this, R.layout.item_spinner_dropdown, new ArrayList<>());
        actvEspecialidad.setAdapter(adapterEspecialidades);

        View.OnClickListener listenerRubro = v -> actvRubro.showDropDown();
        actvRubro.setOnClickListener(listenerRubro);
        if (tilRubro != null) {
            tilRubro.setOnClickListener(listenerRubro);
            tilRubro.setEndIconOnClickListener(listenerRubro);
        }

        View.OnClickListener listenerEspecialidad = v -> actvEspecialidad.showDropDown();
        actvEspecialidad.setOnClickListener(listenerEspecialidad);
        if (tilEspecialidad != null) {
            tilEspecialidad.setOnClickListener(listenerEspecialidad);
            tilEspecialidad.setEndIconOnClickListener(listenerEspecialidad);
        }

        // Dynamic visibility logic on switches
        switchAtencionDomicilio.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tilDireccionAtencion.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        });

        switchAtiendeObraSocial.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tilHonorarios.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            llObraSocialCampos.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        profesionalEnEdicion = (Profesional) getIntent().getSerializableExtra(EXTRA_PROFESIONAL);
        if (profesionalEnEdicion != null) {
            if (toolbar != null) toolbar.setTitle("Editar profesional");
            etNombre.setText(profesionalEnEdicion.getNombre());
            etApellido.setText(profesionalEnEdicion.getApellido());
            etTelefono.setText(profesionalEnEdicion.getTelefono());
            etEmail.setText(profesionalEnEdicion.getEmail());
            etSesionesSemanales.setText(profesionalEnEdicion.getSesionesSemanales());

            switchAtencionDomicilio.setChecked(profesionalEnEdicion.isAtencionDomicilio());
            tilDireccionAtencion.setVisibility(profesionalEnEdicion.isAtencionDomicilio() ? View.GONE : View.VISIBLE);
            etDireccionAtencion.setText(profesionalEnEdicion.getDireccionAtencion());

            switchAtiendeObraSocial.setChecked(profesionalEnEdicion.isAtiendeObraSocial());
            tilHonorarios.setVisibility(profesionalEnEdicion.isAtiendeObraSocial() ? View.GONE : View.VISIBLE);
            llObraSocialCampos.setVisibility(profesionalEnEdicion.isAtiendeObraSocial() ? View.VISIBLE : View.GONE);

            etHonorarios.setText(profesionalEnEdicion.getHonorarios());
            etCantidadBonos.setText(profesionalEnEdicion.getCantidadBonos());
            etAdicional.setText(profesionalEnEdicion.getAdicional());

            especialidadPendienteAlEditar = profesionalEnEdicion.getEspecialidad();
        } else {
            if (toolbar != null) toolbar.setTitle("Nuevo profesional");
        }

        actvRubro.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < listaRubros.size()) {
                cargarEspecialidadesDeRubro(listaRubros.get(position).getId());
                actvEspecialidad.setText("", false);
            }
        });

        cargarRubros();

        btnGuardar.setOnClickListener(v -> guardarProfesional());
    }

    private void cargarRubros() {
        if (currentUserId == null) return;

        db.collection("users")
                .document(currentUserId)
                .collection("rubros")
                .get()
                .addOnSuccessListener(snapshot -> {
                    listaRubros.clear();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        Rubro rubro = doc.toObject(Rubro.class);
                        rubro.setId(doc.getId());
                        listaRubros.add(rubro);
                    }

                    Collections.sort(listaRubros, (r1, r2) -> {
                        String n1 = r1.getNombre() != null ? r1.getNombre().toLowerCase(Locale.getDefault()) : "";
                        String n2 = r2.getNombre() != null ? r2.getNombre().toLowerCase(Locale.getDefault()) : "";
                        return n1.compareTo(n2);
                    });

                    List<String> nombres = new ArrayList<>();
                    for (Rubro r : listaRubros) {
                        nombres.add(r.getNombre());
                    }

                    adapterRubros = new ArrayAdapter<>(this, R.layout.item_spinner_dropdown, nombres);
                    actvRubro.setAdapter(adapterRubros);

                    if (profesionalEnEdicion != null && profesionalEnEdicion.getRubro() != null) {
                        actvRubro.setText(profesionalEnEdicion.getRubro(), false);
                        for (Rubro r : listaRubros) {
                            if (r.getNombre().equals(profesionalEnEdicion.getRubro())) {
                                cargarEspecialidadesDeRubro(r.getId());
                                break;
                            }
                        }
                    }

                    if (listaRubros.isEmpty()) {
                        Toast.makeText(this, "Aún no has creado ningún rubro. Ve a la sección Rubros primero.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar rubros: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void cargarEspecialidadesDeRubro(String rubroId) {
        if (currentUserId == null) return;

        db.collection("users")
                .document(currentUserId)
                .collection("rubros")
                .document(rubroId)
                .collection("especialidades")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> nombres = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String nombre = doc.getString("nombre");
                        if (nombre != null) nombres.add(nombre);
                    }

                    Collections.sort(nombres, (s1, s2) -> s1.toLowerCase(Locale.getDefault()).compareTo(s2.toLowerCase(Locale.getDefault())));

                    adapterEspecialidades = new ArrayAdapter<>(this, R.layout.item_spinner_dropdown, nombres);
                    actvEspecialidad.setAdapter(adapterEspecialidades);

                    if (especialidadPendienteAlEditar != null) {
                        actvEspecialidad.setText(especialidadPendienteAlEditar, false);
                        especialidadPendienteAlEditar = null;
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar especialidades: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void guardarProfesional() {
        String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
        String apellido = etApellido.getText() != null ? etApellido.getText().toString().trim() : "";
        String telefono = etTelefono.getText() != null ? etTelefono.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String sesionesSemanales = etSesionesSemanales.getText() != null ? etSesionesSemanales.getText().toString().trim() : "";

        boolean atencionDomicilio = switchAtencionDomicilio.isChecked();
        String direccionAtencion = etDireccionAtencion.getText() != null ? etDireccionAtencion.getText().toString().trim() : "";

        boolean atiendeObraSocial = switchAtiendeObraSocial.isChecked();
        String honorarios = etHonorarios.getText() != null ? etHonorarios.getText().toString().trim() : "";
        String cantidadBonos = etCantidadBonos.getText() != null ? etCantidadBonos.getText().toString().trim() : "";
        String adicional = etAdicional.getText() != null ? etAdicional.getText().toString().trim() : "";

        String rubro = actvRubro.getText() != null ? actvRubro.getText().toString().trim() : "";
        String especialidad = actvEspecialidad.getText() != null ? actvEspecialidad.getText().toString().trim() : "";

        if (TextUtils.isEmpty(apellido)) {
            etApellido.setError("Ingrese el apellido");
            return;
        }

        if (TextUtils.isEmpty(nombre)) {
            etNombre.setError("Ingrese el nombre");
            return;
        }

        if (TextUtils.isEmpty(rubro)) {
            Toast.makeText(this, "Seleccione un rubro para el profesional", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(telefono)) {
            etTelefono.setError("Ingrese el número de teléfono");
            return;
        }

        if (!atencionDomicilio && TextUtils.isEmpty(direccionAtencion)) {
            etDireccionAtencion.setError("Ingrese la dirección de atención del profesional");
            return;
        }

        if (!atiendeObraSocial && TextUtils.isEmpty(honorarios)) {
            etHonorarios.setError("Ingrese el valor de los honorarios");
            return;
        }

        if (atiendeObraSocial && TextUtils.isEmpty(cantidadBonos)) {
            etCantidadBonos.setError("Ingrese la cantidad de bonos a presentar");
            return;
        }

        btnGuardar.setEnabled(false);

        Profesional profesional = new Profesional(
                nombre,
                apellido,
                rubro,
                especialidad,
                telefono,
                email,
                atencionDomicilio,
                direccionAtencion,
                atiendeObraSocial,
                honorarios,
                cantidadBonos,
                adicional,
                sesionesSemanales
        );

        if (profesionalEnEdicion != null && profesionalEnEdicion.getId() != null) {
            db.collection("users")
                    .document(currentUserId)
                    .collection("profesionales")
                    .document(profesionalEnEdicion.getId())
                    .set(profesional)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Profesional actualizado", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnGuardar.setEnabled(true);
                        Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            db.collection("users")
                    .document(currentUserId)
                    .collection("profesionales")
                    .add(profesional)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Profesional registrado con éxito", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnGuardar.setEnabled(true);
                        Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
