package frgp.utn.edu.actividadesdeiara;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private ProgressBar pbRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        try {
            mAuth = FirebaseAuth.getInstance();
        } catch (Exception ignored) {
        }

        etEmail = findViewById(R.id.etRegisterEmail);
        etPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etRegisterConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        TextView tvLoginLink = findViewById(R.id.tvLoginLink);
        pbRegister = findViewById(R.id.pbRegister);

        btnRegister.setOnClickListener(v -> registerUser());
        tvLoginLink.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Ingrese su correo electrónico");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Ingrese su contraseña");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            return;
        }

        pbRegister.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        try {
            if (mAuth == null) {
                mAuth = FirebaseAuth.getInstance();
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        pbRegister.setVisibility(View.GONE);
                        btnRegister.setEnabled(true);

                        if (task.isSuccessful()) {
                            getSharedPreferences("user_prefs", MODE_PRIVATE)
                                    .edit()
                                    .putString("saved_email", email)
                                    .putString("saved_password", password)
                                    .apply();

                            Toast.makeText(RegisterActivity.this, "¡Cuenta creada exitosamente!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            handleRegisterError(task.getException());
                        }
                    });
        } catch (Exception e) {
            pbRegister.setVisibility(View.GONE);
            btnRegister.setEnabled(true);
            mostrarDialogoConfiguracionFirebase(e.getLocalizedMessage());
        }
    }

    private void handleRegisterError(Exception exception) {
        if (exception == null) {
            Toast.makeText(this, "Error desconocido al registrar usuario", Toast.LENGTH_LONG).show();
            return;
        }

        String msg = exception.getLocalizedMessage() != null ? exception.getLocalizedMessage() : "";

        if (exception instanceof FirebaseAuthException) {
            String errorCode = ((FirebaseAuthException) exception).getErrorCode();
            switch (errorCode) {
                case "ERROR_EMAIL_ALREADY_IN_USE":
                    Toast.makeText(this, "Este correo electrónico ya está registrado.", Toast.LENGTH_LONG).show();
                    return;
                case "ERROR_INVALID_EMAIL":
                    etEmail.setError("El correo no tiene un formato válido");
                    return;
                case "ERROR_WEAK_PASSWORD":
                    etPassword.setError("La contraseña debe tener al menos 6 caracteres");
                    return;
                case "ERROR_OPERATION_NOT_ALLOWED":
                    mostrarDialogoConfiguracionFirebase("El proveedor 'Correo electrónico/Contraseña' no está habilitado en la consola de Firebase.");
                    return;
            }
        }

        if (msg.contains("API key") || msg.contains("API_KEY") || msg.contains("not valid") || msg.contains("invalid") || msg.contains("project")) {
            mostrarDialogoConfiguracionFirebase(msg);
        } else {
            Toast.makeText(this, "Error: " + msg, Toast.LENGTH_LONG).show();
        }
    }

    private void mostrarDialogoConfiguracionFirebase(String detalleError) {
        new AlertDialog.Builder(this)
                .setTitle("Configuración de Firebase Requerida")
                .setMessage("Para registrar usuarios en Firebase debes configurar tu proyecto:\n\n" +
                        "1. Ve a Firebase Console (console.firebase.google.com)\n" +
                        "2. En Authentication -> 'Método de acceso', habilita 'Correo electrónico/Contraseña'.\n" +
                        "3. Descarga tu archivo 'google-services.json' real y colócalo en la carpeta 'app/' del proyecto.\n\n" +
                        "Detalle técnico:\n" + detalleError)
                .setPositiveButton("Entendido", null)
                .show();
    }
}
