package co.com.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "temporada")
public class Temporada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "anio", nullable = false, unique = true)
    private Integer anio;

    @OneToMany(mappedBy = "temporada", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Carrera> carreras = new ArrayList<>();

    // Constructores
    public Temporada() {
    }

    public Temporada(Integer anio) {
        this.anio = anio;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public List<Carrera> getCarreras() {
        return carreras;
    }

    public void setCarreras(List<Carrera> carreras) {
        this.carreras = carreras;
    }

    // Métodos de utilidad
    public void addCarrera(Carrera carrera) {
        carreras.add(carrera);
        carrera.setTemporada(this);
    }

    public void removeCarrera(Carrera carrera) {
        carreras.remove(carrera);
        carrera.setTemporada(null);
    }

    @Override
    public String toString() {
        return "Temporada{" +
                "id=" + id +
                ", anio=" + anio +
                '}';
    }
}
