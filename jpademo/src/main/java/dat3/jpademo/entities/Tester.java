
package dat3.jpademo.entities;

import dto.PersonStyleDTO;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

public class Tester {
    
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("pu");
        EntityManager em = emf.createEntityManager();
        
        Person p1 = new Person("Jønke", 1963);
        Person p2 = new Person("Carsten", 1951);
        
        Address a1 = new Address("Store Torv 1", 2323, "Nr. Snede");
        Address a2 = new Address("Langgade 64", 1212, "Valby");
        
        p1.setAddress(a1);
        p2.setAddress(a2);
        
        Fee f1 = new Fee(100);
        Fee f2 = new Fee(200);
        Fee f3 = new Fee(300);
        
        p1.AddFee(f1);
        p1.AddFee(f3);
        p2.AddFee(f2);
        
        SwimStyle s1 = new SwimStyle("Crawl");
        SwimStyle s2 = new SwimStyle("Butterfly");
        SwimStyle s3 = new SwimStyle("Breast stroke");
        
        p1.AddSwimStyle(s1);
        p1.AddSwimStyle(s3);
        p2.AddSwimStyle(s2);
        
        em.getTransaction().begin();
            //em.persist(a1);
            //em.persist(a2);
            em.persist(p1);
            em.persist(p2);
        em.getTransaction().commit();
        
        em.getTransaction().begin();
            p1.RemoveSwimStyle(s3);
        em.getTransaction().commit();
        
        
        System.out.println("P1: " + p1.getP_id() + ", " + p1.getName());
        System.out.println("P2: " + p2.getP_id() + ", " + p2.getName());
        
        System.out.println("Jønkes gade: " + p1.getAddress().getStreet());
        
        System.out.println("Lad os se om tovejs virker: " + a1.getPerson().getName());
        
        /*
          Nu kan jeg både tage fat i en person og finde frem til deres addresse
          og tage en addresse og finde frem til en person.
        */
        
        System.out.println("Hvem har betalt f2? Det har: " + f2.getPerson().getName());
        
        System.out.println("Hvad er der blevet betalt i alt?");
        
        TypedQuery<Fee> q1 = em.createQuery("SELECT f FROM Fee f", Fee.class);
        List<Fee> fees = q1.getResultList();
        
        for (Fee f: fees){
            System.out.println(f.getPerson().getName() + ": " + f.getAmount() + " " + f.getPayDate());
        }
        
        System.out.println("*-*-*-*-*-*-*-*-*-*-*-* JPQL JOINS *-*-*-*-*-*-*-*-*-*-*-*");
        
        Query q3 = em.createQuery("SELECT new dto.PersonStyleDTO(p.name, p.year, s.styleName) FROM Person p JOIN p.styles s");
        
        List<PersonStyleDTO> personDetails = q3.getResultList();
        
        for (PersonStyleDTO ps: personDetails){
            System.out.println("Navn: " + ps.getName() + ", " + ps.getYear() + ", " + ps.getSwimStyle());
        }
        
        
        
    }
    
    
}
