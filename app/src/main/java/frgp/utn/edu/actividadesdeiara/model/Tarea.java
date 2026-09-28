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
    private String userId;

    public Tarea() {
        // Constructor vacío requerido por Firestore
    }

    public Tarea(String nombre, String descripcion, String fecha, String horario, boolean completada, String userId) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.horario = horario;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
