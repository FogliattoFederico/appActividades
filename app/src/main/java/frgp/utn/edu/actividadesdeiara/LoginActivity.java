package frgp.utn.edu.actividadesdeiara;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_EMAIL = "saved_email";
    private static final String KEY_PASSWORD = "saved_password";

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private MaterialCardView cvBiometric;
    private ProgressBar pbLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            mAuth = FirebaseAuth.getInstance();
        } catch (Exception ignored) {
        }

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        cvBiometric = findViewById(R.id.cvBiometric);
        TextView tvRegister = findViewById(R.id.tvRegister);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        pbLogin = findViewById(R.id.pbLogin);

        // Pre-fill email if saved
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedEmail = prefs.getString(KEY_EMAIL, "");
        if (!TextUtils.isEmpty(savedEmail)) {
            etEmail.setText(savedEmail);
        }

        btnLogin.setOnClickListener(v -> loginUser());
        tvRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
        tvForgotPassword.setOnClickListener(v -> mostrarDialogoRecuperarPassword());
        cvBiometric.setOnClickListener(v -> autenticarConHuella());

        // Automatically trigger fingerprint if credentials exist
        if (tieneCredencialesGuardadas()) {
            cvBiometric.post(this::autenticarConHuella);
        }
    }

    private boolean tieneCredencialesGuardadas() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String email = prefs.getString(KEY_EMAIL, null);
        String password = prefs.getString(KEY_PASSWORD, null);
        return !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password);
    }

    private void autenticarConHuella() {
        if (!tieneCredencialesGuardadas() && (mAuth == null || mAuth.getCurrentUser() == null)) {
            Toast.makeText(this, "Ingresa primero con tu correo y contraseña para vincular tu huella digital.", Toast.LENGTH_LONG).show();
            return;
        }

        BiometricManager biometricManager = BiometricManager.from(this);
        int canAuth = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.BIOMETRIC_WEAK
        );

        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            switch (canAuth) {
                case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                    Toast.makeText(this, "Este dispositivo no cuenta con sensor de huella digital.", Toast.LENGTH_LONG).show();
                    break;
                case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                    Toast.makeText(this, "El sensor biométrico no está disponible por el momento.", Toast.LENGTH_LONG).show();
                    break;
                case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                    Toast.makeText(this, "Debes registrar al menos una huella en los Ajustes de tu teléfono.", Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(this, "La autenticación biométrica no está disponible.", Toast.LENGTH_LONG).show();
                    break;
            }
            return;
        }

        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(LoginActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                if (mAuth != null && mAuth.getCurrentUser() != null) {
                    Toast.makeText(LoginActivity.this, "¡Acceso con huella verificado!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                    return;
                }

                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                String savedEmail = prefs.getString(KEY_EMAIL, "");
                String savedPassword = prefs.getString(KEY_PASSWORD, "");

                if (!TextUtils.isEmpty(savedEmail) && !TextUtils.isEmpty(savedPassword)) {
                    pbLogin.setVisibility(View.VISIBLE);
                    btnLogin.setEnabled(false);

                    mAuth.signInWithEmailAndPassword(savedEmail, savedPassword)
                            .addOnCompleteListener(LoginActivity.this, task -> {
                                pbLogin.setVisibility(View.GONE);
                                btnLogin.setEnabled(true);

                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "¡Ingreso con huella exitoso!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                } else {
                                    handleLoginError(task.getException());
                                }
                            });
                } else {
                    Toast.makeText(LoginActivity.this, "Inicia sesión con correo primero para activar la huella.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(LoginActivity.this, "Huella no reconocida. Intenta nuevamente.", Toast.LENGTH_SHORT).show();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Acceso Biométrico")
                .setSubtitle("Escanea tu huella digital para ingresar")
                .setNegativeButtonText("Usar Contraseña")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void loginUser() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Ingrese su correo electrónico");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Ingrese su contraseña");
            return;
        }

        pbLogin.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        try {
            if (mAuth == null) {
                mAuth = FirebaseAuth.getInstance();
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        pbLogin.setVisibility(View.GONE);
                        btnLogin.setEnabled(true);

                        if (task.isSuccessful()) {
                            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                                    .edit()
                                    .putString(KEY_EMAIL, email)
                                    .putString(KEY_PASSWORD, password)
                                    .apply();

                            Toast.makeText(LoginActivity.this, "¡Inicio de sesión exitoso!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            handleLoginError(task.getException());
                        }
                    });
        } catch (Exception e) {
            pbLogin.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
            mostrarDialogoConfiguracionFirebase(e.getLocalizedMessage());
        }
    }

    private void handleLoginError(Exception exception) {
        if (exception == null) {
            Toast.makeText(this, "Error desconocido al iniciar sesión", Toast.LENGTH_LONG).show();
            return;
        }

        String msg = exception.getLocalizedMessage() != null ? exception.getLocalizedMessage() : "";

        if (exception instanceof FirebaseAuthException) {
            String errorCode = ((FirebaseAuthException) exception).getErrorCode();
            switch (errorCode) {
                case "ERROR_USER_NOT_FOUND":
                case "ERROR_WRONG_PASSWORD":
                case "ERROR_INVALID_CREDENTIAL":
                    Toast.makeText(this, "Correo o contraseña incorrectos.", Toast.LENGTH_LONG).show();
                    return;
                case "ERROR_INVALID_EMAIL":
                    etEmail.setError("El correo no tiene un formato válido");
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
                .setMessage("El archivo 'google-services.json' actual contiene credenciales temporales de ejemplo.\n\n" +
                        "Para conectar la app a tu propia base de datos:\n\n" +
                        "1. Ve a Firebase Console (console.firebase.google.com)\n" +
                        "2. Crea un proyecto e integra una App Android con el paquete: frgp.utn.edu.actividadesdeiara\n" +
                        "3. En Authentication -> 'Método de acceso', habilita 'Correo electrónico/Contraseña'.\n" +
                        "4. Descarga tu archivo 'google-services.json' real y reemplázalo en la carpeta 'app/' del proyecto.\n\n" +
                        "Detalle del error:\n" + detalleError)
                .setPositiveButton("Entendido", null)
                .show();
    }

    private void mostrarDialogoRecuperarPassword() {
        String emailActual = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";

        TextInputLayout tilModal = new TextInputLayout(
                new ContextThemeWrapper(this, com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox)
        );
        tilModal.setHint("Correo electrónico registrado");
        int paddingPx = (int) (16 * getResources().getDisplayMetrics().density);
        tilModal.setPadding(paddingPx, paddingPx, paddingPx, 0);

        TextInputEditText etModalEmail = new TextInputEditText(tilModal.getContext());
        etModalEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        etModalEmail.setText(emailActual);
        etModalEmail.setTextColor(ContextCompat.getColor(this, R.color.black));
        tilModal.addView(etModalEmail);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Restablecer Contraseña")
                .setMessage("Ingresa tu correo electrónico y te enviaremos un enlace con las instrucciones para crear una nueva contraseña:")
                .setView(tilModal)
                .setPositiveButton("Enviar Correo", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String emailRecupero = etModalEmail.getText() != null ? etModalEmail.getText().toString().trim() : "";

            if (TextUtils.isEmpty(emailRecupero) || !Patterns.EMAIL_ADDRESS.matcher(emailRecupero).matches()) {
                tilModal.setError("Ingresa un correo electrónico válido");
                return;
            }

            tilModal.setError(null);
            dialog.dismiss();

            enviarCorreoRecuperacion(emailRecupero);
        });
    }

    private void enviarCorreoRecuperacion(String email) {
        pbLogin.setVisibility(View.VISIBLE);

        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    pbLogin.setVisibility(View.GONE);
                    new AlertDialog.Builder(LoginActivity.this)
                            .setTitle("Correo Enviado")
                            .setMessage("Se ha enviado un correo electrónico a " + email + " con las instrucciones para restablecer tu contraseña.\n\nPor favor, revisa tu bandeja de entrada o carpeta de correo no deseado (Spam).")
                            .setPositiveButton("Entendido", null)
                            .show();
                })
                .addOnFailureListener(e -> {
                    pbLogin.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Error al enviar correo: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
