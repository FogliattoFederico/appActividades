package frgp.utn.edu.actividadesdeiara.model;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;

public class Actividad implements Serializable {

    private String id;
    private String titulo;
    private String categoria; // "Médico", "Profesional", "Docente", "Actividad Física"
    private String descripcion;
    private String fecha;
    private String hora;
    private String responsable;
    private String rubro;
    private String especialidad;
    private boolean completada;
    private boolean recordatorio;
    private int cantidadRecordatorios = 1; // 1 o 2
    private int minutosAnticipacion = 0; // Para recordatorio 1
    private int minutosAnticipacion2 = 0; // Para recordatorio 2
    private long recordatorioTimeMillis;
    private long recordatorioTimeMillis2;
    private String userId;

    public Actividad() {
        // Required for Firestore
    }

    public Actividad(String titulo, String categoria, String descripcion, String fecha, String hora, String responsable, String rubro, String especialidad, boolean completada, boolean recordatorio, int cantidadRecordatorios, int minutosAnticipacion, int minutosAnticipacion2, long recordatorioTimeMillis, long recordatorioTimeMillis2, String userId) {
        this.titulo = titulo;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.hora = hora;
        this.responsable = responsable;
        this.rubro = rubro;
        this.especialidad = especialidad;
        this.completada = completada;
        this.recordatorio = recordatorio;
        this.cantidadRecordatorios = cantidadRecordatorios;
        this.minutosAnticipacion = minutosAnticipacion;
        this.minutosAnticipacion2 = minutosAnticipacion2;
        this.recordatorioTimeMillis = recordatorioTimeMillis;
        this.recordatorioTimeMillis2 = recordatorioTimeMillis2;
        this.userId = userId;
    }

    public Actividad(String titulo, String categoria, String descripcion, String fecha, String hora, String responsable, String rubro, String especialidad, boolean completada, String userId) {
        this(titulo, categoria, descripcion, fecha, hora, responsable, rubro, especialidad, completada, false, 1, 0, 0, 0L, 0L, userId);
    }

    public Actividad(String titulo, String categoria, String descripcion, String fecha, String hora, String responsable, boolean completada, String userId) {
        this(titulo, categoria, descripcion, fecha, hora, responsable, "", "", completada, false, 1, 0, 0, 0L, 0L, userId);
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
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

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public String getRubro() {
        return rubro;
    }

    public void setRubro(String rubro) {
        this.rubro = rubro;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
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