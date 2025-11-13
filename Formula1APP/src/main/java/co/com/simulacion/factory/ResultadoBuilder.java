package co.com.simulacion.factory;

import co.com.model.Carrera;
import co.com.model.Piloto;
import co.com.model.Resultado;

import java.math.BigDecimal;

/**
 * Builder para crear objetos Resultado de forma fluida.
 * Aplica el patrón Builder (creacional).
 */
public class ResultadoBuilder {
    private final Resultado resultado;

    public ResultadoBuilder() {
        this.resultado = new Resultado();
    }

    public ResultadoBuilder carrera(Carrera c) {
        resultado.setCarrera(c);
        return this;
    }

    public ResultadoBuilder piloto(Piloto p) {
        resultado.setPiloto(p);
        return this;
    }

    public ResultadoBuilder posicion(int pos) {
        resultado.setPosicionFinal(pos);
        return this;
    }

    public ResultadoBuilder puntos(BigDecimal puntos) {
        resultado.setPuntosObtenidos(puntos);
        return this;
    }

    public ResultadoBuilder vueltas(int vueltas) {
        resultado.setVueltas(vueltas);
        return this;
    }

    public ResultadoBuilder retirado(boolean retirado, String motivo) {
        resultado.setRetirado(retirado);
        resultado.setMotivoRetiro(motivo);
        return this;
    }
    public ResultadoBuilder tiempo(String tiempo) {
        resultado.setTiempo(tiempo);
        return this;
    }

    public Resultado build() {
        return resultado;
    }
}
