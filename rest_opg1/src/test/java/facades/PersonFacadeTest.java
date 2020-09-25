package facades;

import dto.PersonDTO;
import dto.PersonsDTO;
import utils.EMF_Creator;
import entities.Person;
import exceptions.MissingInputException;
import exceptions.PersonNotFoundException;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

//Uncomment the line below, to temporarily disable this test
@Disabled
public class PersonFacadeTest {
    

    private static EntityManagerFactory emf;
    private static PersonFacade facade;
    private static Person p1, p2;

    public PersonFacadeTest() {
    }

    @BeforeAll
    public static void setUpClass() {
       emf = EMF_Creator.createEntityManagerFactoryForTest();
       facade = PersonFacade.getFacadeExample(emf);
    }

    @AfterAll
    public static void tearDownClass() {
//        Clean up database after test is done or use a persistence unit with drop-and-create to start up clean on every test
    }

    // Setup the DataBase in a known state BEFORE EACH TEST
    //TODO -- Make sure to change the script below to use YOUR OWN entity class
    @BeforeEach
    public void setUp() {
        EntityManager em = emf.createEntityManager();
        p1 = new Person("Kurt", "Hummel", "12345678");
        p2 = new Person("Rachel", "Berry", "22222223");
        try {
            em.getTransaction().begin();
            em.createNamedQuery("Person.deleteAllRows").executeUpdate();
                em.persist(p1);
                em.persist(p2);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @AfterEach
    public void tearDown() {
//        Remove any data after each test was run
    }

    
    @Test
    public void testGetPerson() throws PersonNotFoundException {
        System.out.println("Tester getPerson");
        
        Long tempID = p1.getP_id();
        int id = tempID.intValue();
        
        EntityManagerFactory _emf = null;
        PersonFacade pFac = PersonFacade.getFacadeExample(_emf);
        
        PersonDTO expResult = new PersonDTO(p1);
        PersonDTO result = pFac.getPerson(id);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetPersonCount() {
        System.out.println("Tester getPersonCount");
        
        EntityManagerFactory _emf = null;
        PersonFacade pFac = PersonFacade.getFacadeExample(_emf);
        
        Long expResult = 2L;
        Long result = pFac.getPersonCount();
        assertEquals(expResult, result);
    }
    
    @Test
    public void testGetAll() {
        System.out.println("Tester getAll");
        
        EntityManagerFactory _emf = null;
        PersonFacade pFac = PersonFacade.getFacadeExample(_emf);
        
        int expResult = 2;
        PersonsDTO result = pFac.getAllPersons();
        
        assertEquals(expResult, result.getAll().size());
    }
    
    @Test
    public void testGetAllContains() {
        System.out.println("Tester getAll contains");
        
        EntityManagerFactory _emf = null;
        PersonFacade pFac = PersonFacade.getFacadeExample(_emf);
        
        PersonsDTO result = pFac.getAllPersons();
        
        PersonDTO p1DTO = new PersonDTO(p1);
        PersonDTO p2DTO = new PersonDTO(p2);
        
        assertThat(result.getAll(), containsInAnyOrder(p1DTO, p2DTO));
    }
    
    @Test
    public void testAddPerson() throws MissingInputException {
        System.out.println("Tester addPerson");
        
        String firstName = "Karen";
        String lastName = "Poulsen";
        String phone = "20202020202";
        
        EntityManagerFactory _emf = null;
        PersonFacade pFac = PersonFacade.getFacadeExample(_emf);
        
        PersonDTO result = pFac.addPerson(firstName, lastName, phone);
        
        PersonDTO expResult = new PersonDTO(firstName, lastName, phone);
        expResult.setId(expResult.getId());
        assertEquals(expResult.getFirstName(), result.getFirstName());
    }
    
    @Test
    public void testEditPerson() throws PersonNotFoundException, MissingInputException {
        System.out.println("Tester editPerson");
        
        PersonDTO pDto = new PersonDTO(p1);
        
        EntityManagerFactory _emf = null;
        PersonFacade pFac = PersonFacade.getFacadeExample(_emf);
        
        PersonDTO expResult = new PersonDTO(p1);
        expResult.setFirstName("Bo");
        
        pDto.setFirstName("Bo");
        
        PersonDTO result = pFac.editPerson(pDto);
        assertEquals(expResult.getFirstName(), result.getFirstName());
    }

    @Test
    public void testDeletePerson() throws PersonNotFoundException {
        System.out.println("Tester deletePerson");
        
        long id = p2.getP_id();
        
        int p_id = (int) id;
        
        EntityManagerFactory _emf = null;
        PersonFacade pFac = PersonFacade.getFacadeExample(_emf);
        
        PersonDTO expResult = new PersonDTO(p2);
        PersonDTO result = pFac.deletePerson(p_id);
        
        assertEquals(expResult, result);
    }
}
