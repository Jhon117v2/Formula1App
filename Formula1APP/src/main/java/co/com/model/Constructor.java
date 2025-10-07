package co.com.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "constructor")
public class Constructor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "nacionalidad", length = 100)
    private String nacionalidad;

    @OneToMany(mappedBy = "constructor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Piloto> pilotos = new ArrayList<>();

    // Constructores
    public Constructor() {
    }

    public Constructor(String nombre, String nacionalidad) {
        this.nombre = nombre;
        this.nacionalidad = nacionalidad;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNacionalidad() {
        return nacionalidad;
    }

    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }

    public List<Piloto> getPilotos() {
        return pilotos;
    }

    public void setPilotos(List<Piloto> pilotos) {
        this.pilotos = pilotos;
    }

    // Métodos de utilidad
    public void addPiloto(Piloto piloto) {
        pilotos.add(piloto);
        piloto.setConstructor(this);
    }

    public void removePiloto(Piloto piloto) {
        pilotos.remove(piloto);
        piloto.setConstructor(null);
    }

    @Override
    public String toString() {
        return "Constructor{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", nacionalidad='" + nacionalidad + '\'' +
                '}';
    }
}