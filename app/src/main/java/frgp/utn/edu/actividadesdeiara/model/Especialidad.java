package frgp.utn.edu.actividadesdeiara.model;

public class Especialidad {

    private String id;
    private String nombre;

    public Especialidad() {
        // Constructor vacío requerido por Firestore
    }

    public Especialidad(String nombre) {
        this.nombre = nombre;
    }

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
}
