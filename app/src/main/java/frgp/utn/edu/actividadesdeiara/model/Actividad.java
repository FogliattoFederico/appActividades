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
    private boolean completada;
    private String userId;

    public Actividad() {
        // Required for Firestore
    }

    public Actividad(String titulo, String categoria, String descripcion, String fecha, String hora, String responsable, boolean completada, String userId) {
        this.titulo = titulo;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.hora = hora;
        this.responsable = responsable;
        this.completada = completada;
        this.userId = userId;
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

    public boolean isCompletada() {
        return completada;
    }

    public void setCompletada(boolean completada) {
        this.completada = completada;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
