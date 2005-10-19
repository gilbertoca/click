package click.cayenne.entity.auto;

import java.util.List;

/** Class _Person was generated by Cayenne.
  * It is probably a good idea to avoid changing this class manually, 
  * since it may be overwritten next time code is regenerated. 
  * If you need to make any customizations, please use subclass. 
  */
public class _Person extends org.objectstyle.cayenne.CayenneDataObject {

    public static final String BASE_SALARY_PROPERTY = "baseSalary";
    public static final String DATE_HIRED_PROPERTY = "dateHired";
    public static final String FULL_NAME_PROPERTY = "fullName";
    public static final String DEPARTMENT_PROPERTY = "department";
    public static final String MANAGED_PROJECTS_PROPERTY = "managedProjects";
    public static final String PROJECTS_PROPERTY = "projects";

    public static final String PERSON_ID_PK_COLUMN = "person_id";

    public void setBaseSalary(Double baseSalary) {
        writeProperty("baseSalary", baseSalary);
    }
    public Double getBaseSalary() {
        return (Double)readProperty("baseSalary");
    }
    
    
    public void setDateHired(java.util.Date dateHired) {
        writeProperty("dateHired", dateHired);
    }
    public java.util.Date getDateHired() {
        return (java.util.Date)readProperty("dateHired");
    }
    
    
    public void setFullName(String fullName) {
        writeProperty("fullName", fullName);
    }
    public String getFullName() {
        return (String)readProperty("fullName");
    }
    
    
    public void setDepartment(click.cayenne.entity.Department department) {
        setToOneTarget("department", department, true);
    }

    public click.cayenne.entity.Department getDepartment() {
        return (click.cayenne.entity.Department)readProperty("department");
    } 
    
    
    public void addToManagedProjects(click.cayenne.entity.Project obj) {
        addToManyTarget("managedProjects", obj, true);
    }
    public void removeFromManagedProjects(click.cayenne.entity.Project obj) {
        removeToManyTarget("managedProjects", obj, true);
    }
    public List getManagedProjects() {
        return (List)readProperty("managedProjects");
    }
    
    
    public void addToProjects(click.cayenne.entity.Project obj) {
        addToManyTarget("projects", obj, true);
    }
    public void removeFromProjects(click.cayenne.entity.Project obj) {
        removeToManyTarget("projects", obj, true);
    }
    public List getProjects() {
        return (List)readProperty("projects");
    }
    
    
}
