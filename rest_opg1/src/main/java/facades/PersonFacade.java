package facades;

import dto.PersonDTO;
import dto.PersonsDTO;
import entities.Address;
import entities.Person;
import exceptions.MissingInputException;
import exceptions.PersonNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 *
 * Rename Class to a relevant name Add add relevant facade methods
 */
public class PersonFacade implements IPersonFacade{

    private static PersonFacade instance;
    private static EntityManagerFactory emf;
    
    //Private Constructor to ensure Singleton
    private PersonFacade() {}
    
    
    /**
     * 
     * @param _emf
     * @return an instance of this facade class.
     */
    public static PersonFacade getFacadeExample(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new PersonFacade();
        }
        return instance;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
    
    public long getPersonCount(){
        EntityManager em = emf.createEntityManager();
        try{
            long personCount = (long)em.createQuery("SELECT COUNT(p) FROM Person p").getSingleResult();
            return personCount;
        }finally{  
            em.close();
        }
        
    }
    
    // GET metoder:
    @Override
    public PersonDTO getPerson(int id) throws PersonNotFoundException {
        long p_id = id;
       EntityManager em = getEntityManager();
       try {
           Person person = em.find(Person.class, p_id);
         // Fremprovokerer en 500 server fejl:
            if (p_id == 13){
                System.out.println(1/0);
            }
           
           if (person == null) {
                throw new PersonNotFoundException(String.format("No person with provided id found", p_id));
            } else {
                return new PersonDTO(person);
           }
       } finally {
           em.close();
       }
    }

    @Override
    public PersonsDTO getAllPersons() {
        EntityManager em = getEntityManager();
        try {
            return new PersonsDTO(em.createNamedQuery("Person.getAllRows").getResultList());
        } finally{  
            em.close();
        }   
    }
    
    // OPG-5:
    @Override
    public PersonDTO addPerson(String firstName, String lastName, String phone, String street, String zip, String city) throws MissingInputException{
        if ((firstName.length() == 0) || (lastName.length() == 0)){
           throw new MissingInputException("First Name and/or Last Name is missing"); 
        }
        EntityManager em = getEntityManager();
        Person person = new Person(firstName, lastName, phone);
        
        try {
            em.getTransaction().begin();
                Query q = em.createQuery("SELECT a FROM Address a WHERE a.street= :street AND a.zip= :zip AND a.city= :city");
                q.setParameter("street", street);
                q.setParameter("zip", zip);
                q.setParameter("city", city);
                
                // Laver en liste med alle adresser:
                List<Address> addresses = q.getResultList();
                // Hvis addressen allerede findes i db:
                if (addresses.size() > 0){
                    person.setAddress(addresses.get(0));
                } else {
                    person.setAddress(new Address(street, zip, city));
                }
                em.persist(person);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return new PersonDTO(person);
    }

    // OPG-6:
    @Override
    public PersonDTO editPerson(PersonDTO p) throws PersonNotFoundException, MissingInputException {
        if ((p.getFirstName().length() == 0) || (p.getLastName().length() == 0)){
           throw new MissingInputException("First Name and/or Last Name is missing"); 
        } 
        EntityManager em = getEntityManager();
        Person person = em.find(Person.class, p.getId());
        if (person == null) {
                throw new PersonNotFoundException(String.format("No person with provided id found", p.getId()));
        } else {
            person.setFirstName(p.getFirstName());
            person.setLastName(p.getLastName());
            person.setPhone(p.getPhone());
            person.setLastEdited();
            person.setAddress(new Address(p.getStreet(), p.getZip(), p.getCity()));
            try {
                em.getTransaction().begin();
                    em.merge(person);
                em.getTransaction().commit();

                    return new PersonDTO(person);
               
            } finally {  
            em.close();
          }
        }    
    }
    
    // OPG-7:
    @Override
    public PersonDTO deletePerson(int id) throws PersonNotFoundException {
         EntityManager em = getEntityManager();
         
         Long p_id = Long.valueOf(id);
         
          Person person = em.find(Person.class, p_id);
          Address address = person.getAddress();
          
          if (person == null) {
            throw new PersonNotFoundException(String.format("Person with id: (%d) not found", id));
          } else {
                try {
                    em.getTransaction().begin();
                        em.remove(person);
                     // Hvis flere har den samme addresse skal addressen ikke slettes
                        address.getPersons().remove(person);
                        if (address.getPersons().size() < 1){
                            em.remove(address);
                        }
                    em.getTransaction().commit();
                } finally {
                    em.close();
            }
            return new PersonDTO(person);
          }
    }
    
    

}
