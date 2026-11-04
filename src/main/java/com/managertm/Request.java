
package com.managertm;


public class Request {
    public String Controller;
    public String Tabla;
    public String Accion;
    public String Parametro1;
    public String Parametro2;
    public String Parametro3;
    public String Parametro4;

    public Request() {
    }

    public String getController() {
        return Controller;
    }

    public void setController(String Controller) {
        this.Controller = Controller;
    }

    public String getTabla() {
        return Tabla;
    }

    public void setTabla(String Tabla) {
        this.Tabla = Tabla;
    }

    public String getAccion() {
        return Accion;
    }

    public void setAccion(String Accion) {
        this.Accion = Accion;
    }

    public String getParametro1() {
        return Parametro1;
    }

    public void setParametro1(String Parametro1) {
        this.Parametro1 = Parametro1;
    }

    public String getParametro2() {
        return Parametro2;
    }

    public void setParametro2(String Parametro2) {
        this.Parametro2 = Parametro2;
    }

    public String getParametro3() {
        return Parametro3;
    }

    public void setParametro3(String Parametro3) {
        this.Parametro3 = Parametro3;
    }

    public String getParametro4() {
        return Parametro4;
    }

    public void setParametro4(String Parametro4) {
        this.Parametro4 = Parametro4;
    }
    
}
