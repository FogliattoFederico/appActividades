package frgp.utn.edu.actividadesdeiara;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import java.util.List;

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
    private Spinner spinnerRubro, spinnerEspecialidad;
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        MaterialToolbar toolbar = findViewById(R.id.toolbarProfesional);
        toolbar.setNavigationOnClickListener(v -> finish());

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

        spinnerRubro = findViewById(R.id.spinnerRubro);
        spinnerEspecialidad = findViewById(R.id.spinnerEspecialidad);
        btnGuardar = findViewById(R.id.btnGuardarProfesional);

        adapterRubros = new ArrayAdapter<>(this, R.layout.item_spinner, new ArrayList<>());
        adapterRubros.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerRubro.setAdapter(adapterRubros);

        adapterEspecialidades = new ArrayAdapter<>(this, R.layout.item_spinner, new ArrayList<>());
        adapterEspecialidades.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerEspecialidad.setAdapter(adapterEspecialidades);

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
            toolbar.setTitle("Editar profesional");
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
            toolbar.setTitle("Nuevo profesional");
        }

        spinnerRubro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < listaRubros.size()) {
                    cargarEspecialidadesDeRubro(listaRubros.get(position).getId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        cargarRubros();

        btnGuardar.setOnClickListener(v -> guardarProfesional());
    }

    private void cargarRubros() {
        db.collection("users")
                .document(currentUserId)
                .collection("rubros")
                .get()
                .addOnSuccessListener(snapshot -> {
                    listaRubros.clear();
                    List<String> nombres = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        Rubro rubro = doc.toObject(Rubro.class);
                        rubro.setId(doc.getId());
                        listaRubros.add(rubro);
                        nombres.add(rubro.getNombre());
                    }

                    adapterRubros.clear();
                    adapterRubros.addAll(nombres);
                    adapterRubros.notifyDataSetChanged();

                    if (profesionalEnEdicion != null) {
                        for (int i = 0; i < listaRubros.size(); i++) {
                            if (listaRubros.get(i).getNombre().equals(profesionalEnEdicion.getRubro())) {
                                spinnerRubro.setSelection(i);
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

                    adapterEspecialidades.clear();
                    adapterEspecialidades.addAll(nombres);
                    adapterEspecialidades.notifyDataSetChanged();

                    if (especialidadPendienteAlEditar != null) {
                        int indice = nombres.indexOf(especialidadPendienteAlEditar);
                        if (indice >= 0) {
                            spinnerEspecialidad.setSelection(indice);
                        }
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

        String rubro = spinnerRubro.getSelectedItem() != null ? spinnerRubro.getSelectedItem().toString() : "";
        String especialidad = spinnerEspecialidad.getSelectedItem() != null ? spinnerEspecialidad.getSelectedItem().toString() : "";

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
