package frgp.utn.edu.actividadesdeiara;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class ReminderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Turn screen on and show over lockscreen like an Alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            );
        }

        setContentView(R.layout.activity_reminder);

        actualizarCampos(getIntent());

        MaterialButton btnVerAgenda = findViewById(R.id.btnVerAgenda);
        MaterialButton btnEntendido = findViewById(R.id.btnEntendido);

        btnVerAgenda.setOnClickListener(v -> {
            Intent intent = new Intent(ReminderActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        btnEntendido.setOnClickListener(v -> finish());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        actualizarCampos(intent);
    }

    private void actualizarCampos(Intent intent) {
        if (intent == null) return;
        String titulo = intent.getStringExtra("titulo");
        String descripcion = intent.getStringExtra("descripcion");

        TextView tvTitle = findViewById(R.id.tvReminderTitle);
        TextView tvDesc = findViewById(R.id.tvReminderDescription);

        if (tvTitle != null && titulo != null && !titulo.trim().isEmpty()) {
            tvTitle.setText(titulo);
        }
        if (tvDesc != null && descripcion != null && !descripcion.trim().isEmpty()) {
            tvDesc.setText(descripcion);
        }
    }
}