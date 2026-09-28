package frgp.utn.edu.actividadesdeiara.model;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;

public class Profesional implements Serializable {

    private String id;
    private String nombre;
    private String apellido;
    private String rubro;
    private String especialidad;
    private String telefono;
    private String email;
    private boolean atencionDomicilio;
    private String direccionAtencion;
    private boolean atiendeObraSocial;
    private String honorarios;
    private String cantidadBonos;
    private String adicional;

    public Profesional() {
        // Constructor vacío requerido por Firestore
    }

    public Profesional(String nombre, String apellido, String rubro, String especialidad, String telefono, String email, boolean atencionDomicilio, String direccionAtencion, boolean atiendeObraSocial, String honorarios, String cantidadBonos, String adicional) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.rubro = rubro;
        this.especialidad = especialidad;
        this.telefono = telefono;
        this.email = email;
        this.atencionDomicilio = atencionDomicilio;
        this.direccionAtencion = direccionAtencion;
        this.atiendeObraSocial = atiendeObraSocial;
        this.honorarios = honorarios;
        this.cantidadBonos = cantidadBonos;
        this.adicional = adicional;
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

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAtencionDomicilio() {
        return atencionDomicilio;
    }

    public void setAtencionDomicilio(boolean atencionDomicilio) {
        this.atencionDomicilio = atencionDomicilio;
    }

    public String getDireccionAtencion() {
        return direccionAtencion;
    }

    public void setDireccionAtencion(String direccionAtencion) {
        this.direccionAtencion = direccionAtencion;
    }

    public boolean isAtiendeObraSocial() {
        return atiendeObraSocial;
    }

    public void setAtiendeObraSocial(boolean atiendeObraSocial) {
        this.atiendeObraSocial = atiendeObraSocial;
    }

    public String getHonorarios() {
        return honorarios;
    }

    public void setHonorarios(String honorarios) {
        this.honorarios = honorarios;
    }

    public String getCantidadBonos() {
        return cantidadBonos;
    }

    public void setCantidadBonos(String cantidadBonos) {
        this.cantidadBonos = cantidadBonos;
    }

    public String getAdicional() {
        return adicional;
    }

    public void setAdicional(String adicional) {
        this.adicional = adicional;
    }
}
