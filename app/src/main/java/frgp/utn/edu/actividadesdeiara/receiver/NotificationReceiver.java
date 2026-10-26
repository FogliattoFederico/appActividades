package frgp.utn.edu.actividadesdeiara.receiver;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;

import frgp.utn.edu.actividadesdeiara.MainActivity;
import frgp.utn.edu.actividadesdeiara.R;
import frgp.utn.edu.actividadesdeiara.ReminderActivity;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "channel_tareas_recordatorios";

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationId = intent.getIntExtra("id", (int) System.currentTimeMillis());
        String t = intent.getStringExtra("titulo");
        String d = intent.getStringExtra("descripcion");

        String titulo = t != null ? t : "Recordatorio de Mi Agendita";
        String descripcion = d != null ? d : "Tienes un evento pendiente para este momento.";

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Recordatorios de Mi Agendita",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificaciones y alarmas emergentes de recordatorios");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 250, 500});
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                notificationId,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent fullScreenIntent = new Intent(context, ReminderActivity.class);
        fullScreenIntent.putExtra("titulo", titulo);
        fullScreenIntent.putExtra("descripcion", descripcion);
        fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                notificationId + 1000,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(titulo)
                .setContentText(descripcion)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(descripcion))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(new long[]{0, 500, 250, 500})
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .addAction(R.drawable.ic_calendar, "Ver Agenda", contentIntent)
                .addAction(R.drawable.ic_notifications, "Aceptar", fullScreenPendingIntent);

        if (notificationManager != null) {
            try {
                notificationManager.notify(notificationId, builder.build());
            } catch (Exception ignored) {
            }
        }

        // Lanza la pantalla emergente de recordatorio (ReminderActivity) sobre cualquier aplicación o pantalla activa
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(context)) {
                context.startActivity(fullScreenIntent);
            } else {
                fullScreenPendingIntent.send();
            }
        } catch (Exception e) {
            try {
                context.startActivity(fullScreenIntent);
            } catch (Exception ignored) {
            }
        }
    }
}