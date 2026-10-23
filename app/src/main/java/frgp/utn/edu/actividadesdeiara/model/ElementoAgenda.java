package frgp.utn.edu.actividadesdeiara.model;

import java.io.Serializable;

public class ElementoAgenda implements Serializable {

    public static final int TIPO_ACTIVIDAD = 1;
    public static final int TIPO_TAREA = 2;

    private final int tipo;
    private Actividad actividad;
    private Tarea tarea;

    public ElementoAgenda(Actividad actividad) {
        this.tipo = TIPO_ACTIVIDAD;
        this.actividad = actividad;
    }

    public ElementoAgenda(Tarea tarea) {
        this.tipo = TIPO_TAREA;
        this.tarea = tarea;
    }

    public int getTipo() {
        return tipo;
    }

    public Actividad getActividad() {
        return actividad;
    }

    public Tarea getTarea() {
        return tarea;
    }

    public String getHora() {
        if (tipo == TIPO_ACTIVIDAD && actividad != null) {
            return actividad.getHora() != null ? actividad.getHora() : "";
        } else if (tipo == TIPO_TAREA && tarea != null) {
            return tarea.getHorario() != null ? tarea.getHorario() : "";
        }
        return "";
    }

    public String getFecha() {
        if (tipo == TIPO_ACTIVIDAD && actividad != null) {
            return actividad.getFecha() != null ? actividad.getFecha() : "";
        } else if (tipo == TIPO_TAREA && tarea != null) {
            return tarea.getFecha() != null ? tarea.getFecha() : "";
        }
        return "";
    }
}