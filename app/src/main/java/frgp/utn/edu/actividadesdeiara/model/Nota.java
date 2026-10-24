package frgp.utn.edu.actividadesdeiara.model;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;

public class Nota implements Serializable {

    private String id;
    private String titulo;
    private String contenido;
    private long fechaModificacion;
    private String userId;

    public Nota() {
        // Required for Firestore
    }

    public Nota(String titulo, String contenido, long fechaModificacion, String userId) {
        this.titulo = titulo;
        this.contenido = contenido;
        this.fechaModificacion = fechaModificacion;
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

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public long getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(long fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}