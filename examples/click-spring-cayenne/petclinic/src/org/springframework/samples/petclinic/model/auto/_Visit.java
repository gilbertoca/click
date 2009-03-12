package org.springframework.samples.petclinic.model.auto;

/** Class _Visit was generated by Cayenne.
  * It is probably a good idea to avoid changing this class manually, 
  * since it may be overwritten next time code is regenerated. 
  * If you need to make any customizations, please use subclass. 
  */
public class _Visit extends org.springframework.samples.petclinic.model.Entity {

    public static final String DATE_PROPERTY = "date";
    public static final String DESCRIPTION_PROPERTY = "description";
    public static final String PET_PROPERTY = "pet";

    public static final String ID_PK_COLUMN = "ID";

    public void setDate(java.util.Date date) {
        writeProperty("date", date);
    }
    public java.util.Date getDate() {
        return (java.util.Date)readProperty("date");
    }
    
    
    public void setDescription(String description) {
        writeProperty("description", description);
    }
    public String getDescription() {
        return (String)readProperty("description");
    }
    
    
    public void setPet(org.springframework.samples.petclinic.model.Pet pet) {
        setToOneTarget("pet", pet, true);
    }

    public org.springframework.samples.petclinic.model.Pet getPet() {
        return (org.springframework.samples.petclinic.model.Pet)readProperty("pet");
    } 
    
    
}