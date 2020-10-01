/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

/**
 *
 * @author miade
 */
@Entity
public class Address implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long a_id;
    private String city;
    private String street;
    private String zip;
    
    // MappedBy er vigtig i bl.a. mange til mange forhold
    // Dette gør at vi fortæller hvis der ejer den entitet. Her siger vi at Person ejer address:
    @OneToMany(mappedBy = "address")
    // Bliver nødt til at lave en liste, da der kan være flere personer som har den samme addresse:
    private List<Person> persons;

    public Address() {
    }

    public Address(String street, String zip, String city) {
        this.city = city;
        this.street = street;
        this.zip = zip;
    }

    public Long getA_id() {
        return a_id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public List<Person> getPersons() {
        return persons;
    }

    public void setPersons(Person person) {
        if (person != null){
            persons.add(person);
        }
    }

    
    
    

}
