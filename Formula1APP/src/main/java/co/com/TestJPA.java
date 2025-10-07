package co.com;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class TestJPA {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("Formula1APP");
        EntityManager em = emf.createEntityManager();

        System.out.println("Conexión establecida correctamente 🚀");

        em.close();
        emf.close();
    }
}
