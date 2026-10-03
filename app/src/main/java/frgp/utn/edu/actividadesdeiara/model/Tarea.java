package frgp.utn.edu.actividadesdeiara.model;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;

public class Tarea implements Serializable {

    private String id;
    private String nombre;
    private String descripcion;
    private String fecha;
    private String horario;
    private boolean completada;
    private boolean recordatorio;
    private int cantidadRecordatorios = 1; // 1 o 2
    private int minutosAnticipacion = 0; // Para recordatorio 1
    private int minutosAnticipacion2 = 0; // Para recordatorio 2
    private long recordatorioTimeMillis;
    private long recordatorioTimeMillis2;
    private String userId;

    public Tarea() {
        // Constructor vacío requerido por Firestore
    }

    public Tarea(String nombre, String descripcion, String fecha, String horario, boolean completada, boolean recordatorio, int cantidadRecordatorios, int minutosAnticipacion, int minutosAnticipacion2, long recordatorioTimeMillis, long recordatorioTimeMillis2, String userId) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.horario = horario;
        this.completada = completada;
        this.recordatorio = recordatorio;
        this.cantidadRecordatorios = cantidadRecordatorios;
        this.minutosAnticipacion = minutosAnticipacion;
        this.minutosAnticipacion2 = minutosAnticipacion2;
        this.recordatorioTimeMillis = recordatorioTimeMillis;
        this.recordatorioTimeMillis2 = recordatorioTimeMillis2;
        this.userId = userId;
    }

    public Tarea(String nombre, String descripcion, String fecha, String horario, boolean completada, boolean recordatorio, long recordatorioTimeMillis, int minutosAnticipacion, String userId) {
        this(nombre, descripcion, fecha, horario, completada, recordatorio, 1, minutosAnticipacion, 0, recordatorioTimeMillis, 0L, userId);
    }

    public Tarea(String nombre, String descripcion, String fecha, String horario, boolean completada, boolean recordatorio, long recordatorioTimeMillis, String userId) {
        this(nombre, descripcion, fecha, horario, completada, recordatorio, 1, 0, 0, recordatorioTimeMillis, 0L, userId);
    }

    public Tarea(String nombre, String descripcion, String fecha, String horario, boolean completada, String userId) {
        this(nombre, descripcion, fecha, horario, completada, false, 1, 0, 0, 0L, 0L, userId);
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public boolean isCompletada() {
        return completada;
    }

    public void setCompletada(boolean completada) {
        this.completada = completada;
    }

    public boolean isRecordatorio() {
        return recordatorio;
    }

    public void setRecordatorio(boolean recordatorio) {
        this.recordatorio = recordatorio;
    }

    public int getCantidadRecordatorios() {
        return cantidadRecordatorios;
    }

    public void setCantidadRecordatorios(int cantidadRecordatorios) {
        this.cantidadRecordatorios = cantidadRecordatorios;
    }

    public int getMinutosAnticipacion() {
        return minutosAnticipacion;
    }

    public void setMinutosAnticipacion(int minutosAnticipacion) {
        this.minutosAnticipacion = minutosAnticipacion;
    }

    public int getMinutosAnticipacion2() {
        return minutosAnticipacion2;
    }

    public void setMinutosAnticipacion2(int minutosAnticipacion2) {
        this.minutosAnticipacion2 = minutosAnticipacion2;
    }

    public long getRecordatorioTimeMillis() {
        return recordatorioTimeMillis;
    }

    public void setRecordatorioTimeMillis(long recordatorioTimeMillis) {
        this.recordatorioTimeMillis = recordatorioTimeMillis;
    }

    public long getRecordatorioTimeMillis2() {
        return recordatorioTimeMillis2;
    }

    public void setRecordatorioTimeMillis2(long recordatorioTimeMillis2) {
        this.recordatorioTimeMillis2 = recordatorioTimeMillis2;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}