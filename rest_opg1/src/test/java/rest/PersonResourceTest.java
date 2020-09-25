package rest;

import dto.PersonDTO;
import entities.Person;
import utils.EMF_Creator;
import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import java.net.URI;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
//Uncomment the line below, to temporarily disable this test
@Disabled
public class PersonResourceTest {
    

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_URL = "http://localhost/api";
    private static Person p1,p2,p3;
    
    static final URI BASE_URI = UriBuilder.fromUri(SERVER_URL).port(SERVER_PORT).build();
    private static HttpServer httpServer;
    private static EntityManagerFactory emf;

    static HttpServer startServer() {
        ResourceConfig rc = ResourceConfig.forApplication(new ApplicationConfig());
        return GrizzlyHttpServerFactory.createHttpServer(BASE_URI, rc);
    }

    @BeforeAll
    public static void setUpClass() {
        //This method must be called before you request the EntityManagerFactory
        EMF_Creator.startREST_TestWithDB();
        emf = EMF_Creator.createEntityManagerFactoryForTest();
        
        httpServer = startServer();
        //Setup RestAssured
        RestAssured.baseURI = SERVER_URL;
        RestAssured.port = SERVER_PORT;
        RestAssured.defaultParser = Parser.JSON;
    }
    
    @AfterAll
    public static void closeTestServer(){
        //System.in.read();
         //Don't forget this, if you called its counterpart in @BeforeAll
         EMF_Creator.endREST_TestWithDB();
         httpServer.shutdownNow();
    }
    
    // Setup the DataBase (used by the test-server and this test) in a known state BEFORE EACH TEST
    //TODO -- Make sure to change the EntityClass used below to use YOUR OWN (renamed) Entity class
    @BeforeEach
    public void setUp() {
        EntityManager em = emf.createEntityManager();
        p1 = new Person("Kurt", "Hummel", "12345678");
        p2 = new Person("Rachel", "Berry", "22222223");
        p3 = new Person("Santana", "Something", "666");
        try {
            em.getTransaction().begin();
            em.createNamedQuery("Person.deleteAllRows").executeUpdate();
            em.createNativeQuery("alter table PERSON AUTO_INCREMENT = 1").executeUpdate();
                em.persist(p1);
                em.persist(p2); 
                em.persist(p3);
            em.getTransaction().commit();
        } finally { 
            em.close();
        }
    }
    
    @Test
    public void testServerIsUp() {
        System.out.println("Testing is server UP");
        given().when().get("/person").then().statusCode(200);
    }
   
    //This test assumes the database contains two rows
    @Test
    public void testDummyMsg() throws Exception {
        given()
        .contentType("application/json")
        .get("/person/").then()
        .assertThat()
        .statusCode(HttpStatus.OK_200.getStatusCode())
        .body("msg", equalTo("Hello World"));   
    }
    
    /*
        getPerson
        getAll
        addPerson
        editPerson
        deletePerson
    */
    
    @Test
    public void testGetPerson() throws Exception {
        System.out.println("Testing getting Person");
        
        given()
            .contentType("application/json")
            .get("/person/id/" + p1.getP_id())
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK_200.getStatusCode())
            .body("firstName", is("Kurt"))
            .and()
            .body("lastName", is("Hummel"));
    }
    
    
    @Test
    public void getAllPersons(){
        System.out.println("Testing getting all");
        
            List<PersonDTO> personsDTOs;
        
            personsDTOs = given()
                .contentType("application/json")
                .when()
                .get("/person/all")
                .then()
                .extract().body().jsonPath().getList("all", PersonDTO.class);
                    
            PersonDTO p1DTO = new PersonDTO(p1);
            PersonDTO p2DTO = new PersonDTO(p2);
            PersonDTO p3DTO = new PersonDTO(p3);
            
            assertThat(personsDTOs, containsInAnyOrder(p1DTO, p2DTO, p3DTO));
    }
    @Test
    public void testAddPerson(){
        given()
            .contentType(ContentType.JSON)
            .body(new PersonDTO("Emil", "Andersen", "2112211", "SomeRoad", "000", "Nothingham"))
            .when()
            .post("person")
            .then()
            .body("firstName", equalTo("Emil"))
            .body("lastName", equalTo("Andersen"))
            .body("phone", equalTo("2112211"))
            .body("id", notNullValue());
    }
    
    
    @Test
    public void testEditPerson(){
        PersonDTO person = new PersonDTO(p1);
        person.setFirstName("Cool-BoB");
 
        given()
            .contentType(ContentType.JSON)
            .body(person)
            .when()
            .put("person/"+ person.getId())
            .then()
            .body("firstName", equalTo("Cool-BoB"))
            .body("lastName", equalTo("Hummel"))
            .body("phone", equalTo("12345678"))
            .body("id", equalTo((int)person.getId()));
    }

    @Test
      public void testDelete() throws Exception {
        
        PersonDTO person = new PersonDTO(p1);
        
        given()
            .contentType("application/json")
            .delete("/person/" + person.getId())
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK_200.getStatusCode());
        
        List<PersonDTO> personsDTOs;
        
        personsDTOs = given()
                .contentType("application/json")
                .when()
                .get("/person/all")
                .then()
                .extract().body().jsonPath().getList("all", PersonDTO.class);

     

        PersonDTO p2DTO = new PersonDTO(p2);
        PersonDTO p3DTO = new PersonDTO(p3);

        assertThat(personsDTOs, containsInAnyOrder(p2DTO, p3DTO));
            
    }
      
      // Exceptions:
      @Test
        public void testGetPersonException() throws Exception {
            System.out.println("Testing getting Person exception");

            given()
                .contentType("application/json")
                .get("/person/id/" + 4)
                .then()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND_404.getStatusCode())
                .body("message", equalTo("No person with provided id found"));
        }
        
        @Test
        public void testGetPersonException2() throws Exception {
            System.out.println("Testing getting Person exception 2");

            given()
                .contentType("application/json")
                .get("/person/id/" + 13)
                .then()
                .assertThat()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR_500.getStatusCode())
                .body("message", equalTo("Internal Server Error"));
        }
      
      @Test
        public void testEditPersonException() throws Exception {
            System.out.println("Testing edit person exception");

            PersonDTO person = new PersonDTO(p1);
 
            given()
                .contentType(ContentType.JSON)
                .body(person)
                .when()
                .put("person/"+ 600)
                .then()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND_404.getStatusCode())
                .body("message", equalTo("No person with provided id found"));
        }
      
        @Test
        public void testEditPersonException2() throws Exception {
            System.out.println("Testing edit Person exception 2");

            PersonDTO person = new PersonDTO(p1);
            person.setFirstName("");
 
            given()
                .contentType(ContentType.JSON)
                .body(person)
                .when()
                .put("person/"+ person.getId())
                .then()
                .assertThat()
                .statusCode(HttpStatus.BAD_REQUEST_400.getStatusCode())
                .body("message", equalTo("First Name and/or Last Name is missing"));
        }
        
      @Test
        public void testDeletePersonException() throws Exception {
            System.out.println("Testing deleting Person exception");
        
            given()
                .contentType("application/json")
                .delete("/person/" + 300)
                .then()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND_404.getStatusCode())
                .body("message", equalTo("Person with id: (300) not found"));
        }
        
        @Test
        public void testAddPersonException() throws Exception {
            System.out.println("Testing adding Person exception");

            given()
            .contentType(ContentType.JSON)
            .body(new PersonDTO("", "Andersen", "2112211", "SomeRoad", "000", "Nothingham"))
            .when()
            .post("person")
            .then()
            .assertThat()
            .statusCode(HttpStatus.BAD_REQUEST_400.getStatusCode())
            .body("message", equalTo("First Name and/or Last Name is missing"));
        }
        
        
}























