package frgp.utn.edu.actividadesdeiara.receiver;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import frgp.utn.edu.actividadesdeiara.MainActivity;
import frgp.utn.edu.actividadesdeiara.R;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "channel_tareas_recordatorios";

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationId = intent.getIntExtra("id", (int) System.currentTimeMillis());
        String t = intent.getStringExtra("titulo");
        String d = intent.getStringExtra("descripcion");

        String titulo = t != null ? t : "Recordatorio de Tarea";
        String descripcion = d != null ? d : "Tienes una tarea pendiente para ahora.";

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Recordatorios de Tareas",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificaciones para recordatorios de tareas");
            channel.enableVibration(true);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(titulo)
                .setContentText(descripcion)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(descripcion))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (notificationManager != null) {
            try {
                notificationManager.notify(notificationId, builder.build());
            } catch (Exception ignored) {
            }
        }
    }
}