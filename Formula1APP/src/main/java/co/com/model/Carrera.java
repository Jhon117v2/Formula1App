package co.com.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carrera")
public class Carrera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_gp", length = 200)
    private String nombreGp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "circuito_id")
    private Circuito circuito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "temporada_id")
    private Temporada temporada;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "gp_numero")
    private Integer gpNumero;

    @OneToMany(mappedBy = "carrera", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Resultado> resultados = new ArrayList<>();

    // Constructores
    public Carrera() {
    }

    public Carrera(String nombreGp, LocalDate fecha, Integer gpNumero) {
        this.nombreGp = nombreGp;
        this.fecha = fecha;
        this.gpNumero = gpNumero;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreGp() {
        return nombreGp;
    }

    public void setNombreGp(String nombreGp) {
        this.nombreGp = nombreGp;
    }

    public Circuito getCircuito() {
        return circuito;
    }

    public void setCircuito(Circuito circuito) {
        this.circuito = circuito;
    }

    public Temporada getTemporada() {
        return temporada;
    }

    public void setTemporada(Temporada temporada) {
        this.temporada = temporada;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Integer getGpNumero() {
        return gpNumero;
    }

    public void setGpNumero(Integer gpNumero) {
        this.gpNumero = gpNumero;
    }

    public List<Resultado> getResultados() {
        return resultados;
    }

    public void setResultados(List<Resultado> resultados) {
        this.resultados = resultados;
    }

    // Métodos de utilidad
    public void addResultado(Resultado resultado) {
        resultados.add(resultado);
        resultado.setCarrera(this);
    }

    public void removeResultado(Resultado resultado) {
        resultados.remove(resultado);
        resultado.setCarrera(null);
    }

    @Override
    public String toString() {
        return "Carrera{" +
                "id=" + id +
                ", nombreGp='" + nombreGp + '\'' +
                ", fecha=" + fecha +
                ", gpNumero=" + gpNumero +
                '}';
    }
}
