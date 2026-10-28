package frgp.utn.edu.actividadesdeiara.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import frgp.utn.edu.actividadesdeiara.model.Actividad;
import frgp.utn.edu.actividadesdeiara.model.Tarea;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
            "android.intent.action.LOCKED_BOOT_COMPLETED".equals(action) ||
            Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {

            reprogramarAlarmasDespuesDeReinicio(context);
        }
    }

    private void reprogramarAlarmasDespuesDeReinicio(Context context) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String userId = user.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        long now = System.currentTimeMillis();

        // 1. Reprogramar Tareas futuras
        db.collection("users")
                .document(userId)
                .collection("tareas")
                .whereEqualTo("recordatorio", true)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot != null) {
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Tarea tarea = doc.toObject(Tarea.class);
                            if (tarea != null) {
                                String tareaId = doc.getId();
                                if (tarea.getRecordatorioTimeMillis() > now) {
                                    programarAlarma(context, tareaId, 0, "Recordatorio de Tarea",
                                            !TextUtils.isEmpty(tarea.getDescripcion()) ? (tarea.getNombre() + " - " + tarea.getDescripcion()) : tarea.getNombre(),
                                            tarea.getRecordatorioTimeMillis());
                                }
                                if (tarea.getCantidadRecordatorios() == 2 && tarea.getRecordatorioTimeMillis2() > now) {
                                    programarAlarma(context, tareaId, 100000, "Recordatorio de Tarea",
                                            !TextUtils.isEmpty(tarea.getDescripcion()) ? (tarea.getNombre() + " - " + tarea.getDescripcion()) : tarea.getNombre(),
                                            tarea.getRecordatorioTimeMillis2());
                                }
                            }
                        }
                    }
                });

        // 2. Reprogramar Actividades futuras
        db.collection("users")
                .document(userId)
                .collection("actividades")
                .whereEqualTo("recordatorio", true)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot != null) {
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Actividad act = doc.toObject(Actividad.class);
                            if (act != null) {
                                String actId = doc.getId();
                                if (act.getRecordatorioTimeMillis() > now) {
                                    programarAlarma(context, actId, 200000, "Recordatorio de Actividad",
                                            !TextUtils.isEmpty(act.getDescripcion()) ? (act.getTitulo() + " - " + act.getDescripcion()) : act.getTitulo(),
                                            act.getRecordatorioTimeMillis());
                                }
                                if (act.getCantidadRecordatorios() == 2 && act.getRecordatorioTimeMillis2() > now) {
                                    programarAlarma(context, actId, 300000, "Recordatorio de Actividad",
                                            !TextUtils.isEmpty(act.getDescripcion()) ? (act.getTitulo() + " - " + act.getDescripcion()) : act.getTitulo(),
                                            act.getRecordatorioTimeMillis2());
                                }
                            }
                        }
                    }
                });
    }

    private void programarAlarma(Context context, String idStr, int offset, String titulo, String descripcion, long timeMillis) {
        if (idStr == null || timeMillis <= System.currentTimeMillis()) return;

        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            Intent intent = new Intent(context, NotificationReceiver.class);
            int requestCode = Math.abs(idStr.hashCode()) + offset;
            intent.putExtra("id", requestCode);
            intent.putExtra("titulo", titulo);
            intent.putExtra("descripcion", descripcion);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent);
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent);
            }
        } catch (Exception ignored) {
        }
    }
}