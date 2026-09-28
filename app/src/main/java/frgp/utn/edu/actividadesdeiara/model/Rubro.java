package frgp.utn.edu.actividadesdeiara.model;

public class Rubro {

    private String id;
    private String nombre;

    public Rubro() {
        // Constructor vacío requerido por Firestore
    }

    public Rubro(String nombre) {
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
