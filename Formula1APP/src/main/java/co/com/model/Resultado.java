package co.com.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "resultado")
public class Resultado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrera_id")
    private Carrera carrera;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "piloto_id")
    private Piloto piloto;

    @Column(name = "posicion_final")
    private Integer posicionFinal;

    @Column(name = "puntos_obtenidos", precision = 5, scale = 2)
    private BigDecimal puntosObtenidos;

    @Column(name = "vueltas")
    private Integer vueltas;

    @Column(name = "tiempo", length = 100)
    private String tiempo;

    @Column(name = "retirado")
    private Boolean retirado = false;

    @Column(name = "motivo_retiro", length = 200)
    private String motivoRetiro;

    // Constructores
    public Resultado() {
    }

    public Resultado(Integer posicionFinal, BigDecimal puntosObtenidos) {
        this.posicionFinal = posicionFinal;
        this.puntosObtenidos = puntosObtenidos;
        this.retirado = false;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Carrera getCarrera() {
        return carrera;
    }

    public void setCarrera(Carrera carrera) {
        this.carrera = carrera;
    }

    public Piloto getPiloto() {
        return piloto;
    }

    public void setPiloto(Piloto piloto) {
        this.piloto = piloto;
    }

    public Integer getPosicionFinal() {
        return posicionFinal;
    }

    public void setPosicionFinal(Integer posicionFinal) {
        this.posicionFinal = posicionFinal;
    }

    public BigDecimal getPuntosObtenidos() {
        return puntosObtenidos;
    }

    public void setPuntosObtenidos(BigDecimal puntosObtenidos) {
        this.puntosObtenidos = puntosObtenidos;
    }

    public Integer getVueltas() {
        return vueltas;
    }

    public void setVueltas(Integer vueltas) {
        this.vueltas = vueltas;
    }

    public String getTiempo() {
        return tiempo;
    }

    public void setTiempo(String tiempo) {
        this.tiempo = tiempo;
    }

    public Boolean getRetirado() {
        return retirado;
    }

    public void setRetirado(Boolean retirado) {
        this.retirado = retirado;
    }

    public String getMotivoRetiro() {
        return motivoRetiro;
    }

    public void setMotivoRetiro(String motivoRetiro) {
        this.motivoRetiro = motivoRetiro;
    }

    @Override
    public String toString() {
        return "Resultado{" +
                "id=" + id +
                ", posicionFinal=" + posicionFinal +
                ", puntosObtenidos=" + puntosObtenidos +
                ", retirado=" + retirado +
                '}';
    }
}
