package frgp.utn.edu.actividadesdeiara;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.Executor;

import frgp.utn.edu.actividadesdeiara.fragment.CalendarioFragment;
import frgp.utn.edu.actividadesdeiara.fragment.ProfesionalesFragment;
import frgp.utn.edu.actividadesdeiara.fragment.RubrosFragment;
import frgp.utn.edu.actividadesdeiara.fragment.TareasFragment;

public class MainActivity extends AppCompatActivity {

    private static final long LOCK_TIMEOUT_MS = 2 * 60 * 1000L; // 2 minutos de inactividad
    private static long backgroundTimestampMs = 0L;

    private FirebaseAuth mAuth;
    private boolean isPromptShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragmentSeleccionado;
            int id = item.getItemId();

            if (id == R.id.nav_profesionales) {
                fragmentSeleccionado = new ProfesionalesFragment();
            } else if (id == R.id.nav_rubros) {
                fragmentSeleccionado = new RubrosFragment();
            } else if (id == R.id.nav_tareas) {
                fragmentSeleccionado = new TareasFragment();
            } else {
                fragmentSeleccionado = new CalendarioFragment();
            }

            mostrarFragment(fragmentSeleccionado);
            return true;
        });

        if (savedInstanceState == null) {
            bottomNavigation.setSelectedItemId(R.id.nav_calendario);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        backgroundTimestampMs = System.currentTimeMillis();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (backgroundTimestampMs > 0L) {
            long elapsed = System.currentTimeMillis() - backgroundTimestampMs;
            if (elapsed >= LOCK_TIMEOUT_MS) {
                backgroundTimestampMs = 0L;
                solicitarDesbloqueoBiometrico();
            }
        }
    }

    private void solicitarDesbloqueoBiometrico() {
        if (isPromptShowing) return;

        BiometricManager biometricManager = BiometricManager.from(this);
        int canAuth = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.BIOMETRIC_WEAK
        );

        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        isPromptShowing = true;
        Executor executor = ContextCompat.getMainExecutor(this);

        BiometricPrompt biometricPrompt = new BiometricPrompt(MainActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                isPromptShowing = false;
                Toast.makeText(MainActivity.this, "¡Aplicación desbloqueada!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                isPromptShowing = false;
                mAuth.signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Mi Agendita Bloqueada")
                .setSubtitle("La aplicación estuvo en segundo plano. Escanea tu huella para continuar.")
                .setNegativeButtonText("Cerrar Sesión")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void mostrarFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}