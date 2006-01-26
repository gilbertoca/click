/*
 * Copyright 2004-2005 Malcolm A. Edgar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.click.extras.cayenne;

import java.util.Iterator;

import net.sf.click.control.Field;
import net.sf.click.control.Form;
import net.sf.click.control.HiddenField;
import net.sf.click.control.TextArea;
import net.sf.click.control.TextField;
import net.sf.click.util.ClickLogger;
import net.sf.click.util.ClickUtils;

import org.apache.commons.lang.StringUtils;
import org.objectstyle.cayenne.DataObject;
import org.objectstyle.cayenne.DataObjectUtils;
import org.objectstyle.cayenne.PersistenceState;
import org.objectstyle.cayenne.access.DataContext;
import org.objectstyle.cayenne.map.DbAttribute;
import org.objectstyle.cayenne.map.ObjAttribute;
import org.objectstyle.cayenne.map.ObjEntity;
import org.objectstyle.cayenne.validation.ValidationException;
import org.objectstyle.cayenne.validation.ValidationResult;

/**
 * Provides Cayenne data aware Form control: &nbsp; &lt;form method='POST'&gt;.
 *
 * <table class='htmlHeader' cellspacing='10'>
 * <tr>
 * <td>
 *
 * <table class='fields'>
 * <tr>
 * <td align='left'><label>Department Name</label><span class="red">*</span></td>
 * <td align='left'><input type='text' name='name' value='' size='35' /></td>
 * </tr>
 * <tr>
 * <td align='left'><label>Description</label></td>
 * <td align='left'><textarea name='description' cols='35' rows='3'></textarea></td>
 * </tr>
 * </table>
 * <table class="buttons" align='right'>
 * <tr><td>
 * <input type='submit' name='ok' value='  OK  '/>&nbsp;<input type='submit' name='cancel' value='Cancel'/>
 * </td></tr>
 * </table>
 *
 * </td>
 * </tr>
 * </table>
 *
 * <a href="http://objectstyle.org/cayenne/">Cayenne</a> is an Object Relational
 * Mapping (ORM) framework. The CayenneForm supports creating (inserting) and
 * saving (updating) Cayenne <tt>DataObject</tt> instances. This form will
 * automatically apply the given data objects validation constraints to the
 * form fields.
 * <p/>
 * The CayenneForm uses the thread local <tt>DataContext</tt> obtained via
 * <tt>DataContext.getThreadDataContext()</tt> for all object for persistence
 * operations.
 * <p/>
 * The example below provides a <tt>Department</tt> data object creation
 * and editing page. To edit an existing department object, the object is passed
 * to the page as a request parameter. Otherwise a new department object will
 * be created when {@link #saveChanges()} is called.
 *
 * <pre class="codeJava">
 * <span class="kw">public class</span> DepartmentEdit <span class="kw">extends</span> Page {
 *
 *   <span class="kw">private</span> CayenneForm form = <span class="kw">new</span> CayenneForm(<span class="st">"form"</span>, Department.<span class="kw">class</span>);
 *
 *    <span class="kw">public</span> DepartmentEdit() {
 *        form.add(<span class="kw">new</span> TextField(<span class="st">"name"</span>, <span class="st">"Department Name"</span>, 35);
 *        form.add(<span class="kw">new</span> TextArea(<span class="st">"description"</span>, <span class="st">"Description"</span>, 35, 2);
 *
 *        form.add(<span class="kw">new</span> Submit(<span class="st">"ok"</span>, <span class="st">"   OK   "</span>, <span class="kw">this</span>, <span class="st">"onOkClicked"</span>);
 *        form.add(<span class="kw">new</span> Submit(<span class="st">"cancel"</span>, <span class="kw">this</span>, <span class="st">"onCancelClicked"</span>);
 *
 *        form.setButtonAlign(<span class="st">"right"</span>);
 *        addControl(form);
 *    }
 *
 *    <span class="kw">public void</span> onGet() {
 *        Department department = (Department)
 *           getContext().getRequestAttribute(<span class="st">"department"</span>);
 *
 *        <span class="kw">if</span> (department != <span class="kw">null</span>) {
 *            form.setDataObject(department);
 *        }
 *    }
 *
 *    <span class="kw">public boolean</span> onOkClicked() {
 *        <span class="kw">if</span> (form.isValid()) {
 *           <span class="kw">if</span> (form.saveChanges()) {
 *               setRedirect(<span class="st">"departments-view.htm"</span>);
 *           }
 *        }
 *        <span class="kw">return true</span>;
 *    }
 *
 *    <span class="kw">public boolean</span> onCancelClicked() {
 *        setRedirect(<span class="st">"departments-view.htm"</span>);
 *        <span class="kw">return false</span>;
 *    }
 * } </pre>
 *
 * @author Andrus Adamchik
 * @author Malcolm Edgar
 */
public class CayenneForm extends Form {

    private static final long serialVersionUID = 4800858697502035042L;

    private static final String FK_PREFIX = "DOFK_";
    private static final String PROP_PREFIX = "FKPROP_";

    /** The data object primary key hidden field. */
    protected HiddenField pkField = new HiddenField("DOPK", Integer.class);

    /** The data object class name hidden field. */
    protected HiddenField classField = new HiddenField("DOCLASS", String.class);

    /**
     * The flag specifying that object validation meta data has been applied to
     * the form fields.
     */
    protected boolean metaDataApplied = false;

    // ----------------------------------------------------------- Constructors

    /**
     * Create a new CayenneForm with the given form name and <tt>DataObject</tt>
     * class.
     *
     * @param name the form name
     * @param dataClass the <tt>DataObject</tt> class
     */
    public CayenneForm(String name, Class dataClass) {
        super(name);
        add(pkField);
        add(classField);

        if (dataClass == null) {
            throw new IllegalArgumentException("Null dataClass parameter");
        }
        if (!DataObject.class.isAssignableFrom(dataClass)) {
            String msg = "Not a DataObject class: " + dataClass;
            throw new IllegalArgumentException(msg);
        }

        classField.setValue(dataClass.getName());
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Return the thread local <tt>DataContext</tt> obtained via
     * <tt>DataContext.getThreadDataContext()</tt>.
     *
     * @return the thread local <tt>DataContext</tt>
     */
    public DataContext getDataContext() {
        return DataContext.getThreadDataContext();
    }

    /**
     * Return a <tt>DataObject</tt> from the form with the form field values
     * copied into the data object's properties.
     *
     * @return the data object from the form with the form field values applied
     *  to the data object properties.
     */
    public DataObject getDataObject() {
        if (StringUtils.isNotBlank(classField.getValue())) {
            try {
                Class dataClass = Class.forName(classField.getValue());
                DataObject dataObject = null;

                Integer id = (Integer) pkField.getValueObject();
                if (id != null) {
                    dataObject = DataObjectUtils.objectForPK(getDataContext(),
                                                             dataClass,
                                                             id);
                } else {
                    dataObject =
                        getDataContext().createAndRegisterNewObject(dataClass);
                }

                copyTo(dataObject, true);

                // TODO: take care of the diff situations: new object, existing
                // object new FK, existing object old FK, etc.
                copyRelationsTo(dataObject);

                return dataObject;

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /**
     * Set the given <tt>DataObject</tt> in the form, copying the object's
     * properties into the form field values. This method will also set the
     * relationship selection.
     *
     * @param dataObject the <tt>DataObject</tt> to set
     */
    public void setDataObject(DataObject dataObject) {
        if (dataObject != null) {
            if (dataObject.getPersistenceState()
                != PersistenceState.TRANSIENT) {

                int pk = DataObjectUtils.intPKForObject(dataObject);
                pkField.setValueObject(new Integer(pk));
            }

            copyFrom(dataObject);

            copyRelationFrom(dataObject);
        }
    }

    /**
     * Adds and registers a new property relationship with the given
     * property name, foreign <tt>DataObject</tt> class and property field.
     *
     * @param propertyName the form's data object relationship property name
     * @param dataClass the relationship foreign <tt>DataObject</tt> class
     * @param propertyField the property field for the relationship
     */
    public void addRelation(String propertyName, Class dataClass,
            Field propertyField) {

        if (propertyName == null) {
            throw new IllegalArgumentException("Null propertyName parameter");
        }
        if (dataClass == null) {
            throw new IllegalArgumentException("Null dataClass parameter");
        }
        if (!DataObject.class.isAssignableFrom(dataClass)) {
            String msg = "Not a DataObject class: " + dataClass;
            throw new IllegalArgumentException(msg);
        }
        if (propertyField == null) {
            throw new IllegalArgumentException("Null field parameter");
        }

        // Add a hidden field with the Data Object name of this relation.
        // in the same way it was done for the FORM, but here in a generic way
        // cause there might be more than one relation/fk for an entity
        HiddenField fkField =
            new HiddenField(FK_PREFIX + propertyField.getName(), String.class);

        fkField.setValueObject(dataClass.getName());
        add(fkField);

        HiddenField propNameField =
            new HiddenField(PROP_PREFIX + propertyField.getName(),
                            String.class);

        propNameField.setValueObject(propertyName);
        add(propNameField);
    }

    /**
     * Save the object to the database committing all changes in the
     * <tt>DataContext</tt> and return true.
     * If a <tt>ValidationException</tt>
     * occured then all <tt>DataContext</tt> changes will be rolled back,
     * the valiation error will be set as the Form's error and the method will
     * return false.
     * <p/>
     * If no <tt>DataObject</tt> is added to the form using <tt>setDataObject()</tt>
     * then this method will: <ul>
     * <li>create and register a new object instance with the
     *    <tt>DataContext</tt></li>
     * <li>copy the form's field values to the objects properties</li>
     * <li>insert a new object record in the database</li>
     * </ul>
     * <p/>
     * If an existing persistent <tt>DataObject</tt> is added to the form using
     * <tt>setDataObject()</tt> then this method will: <ul>
     * <li>load the persistent object record from the database</li>
     * <li>copy the form's field values to the objects properties</li>
     * <li>update the object record in the database</li>
     * </ul>
     *
     * @return true if the <tt>DataObject</tt> was saved or false otherwise
     */
    public boolean saveChanges() {
        // Load data object into data context
        getDataObject();

        try {
            getDataContext().commitChanges();
            return true;
        }
        catch (ValidationException e) {
            getDataContext().rollbackChanges();

            ValidationResult validation = e.getValidationResult();

            setError(validation != null
                     ? validation.toString()
                     : "Unknown validation error on save.");
            return false;
        }
    }

    /**
     * This method applies the object meta data to the form fields and then
     * invokes the <tt>super.onProcess()</tt> method.
     *
     * @see Form#onProcess()
     *
     * @return true to continue Page event processing or false otherwise
     */
    public boolean onProcess() {
        applyMetaData();
        return super.onProcess();
    }

    /**
     * This method applies the object meta data to the form fields and then
     * invokes the <tt>super.toString()</tt> method.
     *
     * @see Form#toString()
     *
     * @return the HTML string representation of the form
     */
    public String toString() {
        applyMetaData();
        return super.toString();
    }

    // ------------------------------------------------------ Protected Methods

    /**
     * Applies the <tt>DataObject</tt> validation database meta data to the
     * form fields.
     * <p/>
     * The field validation attributes include:
     * <ul>
     * <li>required - is a mandatory field and cannot be null</li>
     * <li>maxLength - the maximum length of the field</li>
     * </ul>
     */
    protected void applyMetaData() {
        if (metaDataApplied) {
            return;
        }

        try {
            Class dataClass = Class.forName(classField.getValue());

            ObjEntity objEntity =
                getDataContext().getEntityResolver().lookupObjEntity(dataClass);

            Iterator attributes = objEntity.getAttributes().iterator();
            while (attributes.hasNext()) {
                ObjAttribute objAttribute = (ObjAttribute) attributes.next();
                DbAttribute dbAttribute = objAttribute.getDbAttribute();

                Field field = getField(objAttribute.getName());
                if (field != null) {
                    field.setRequired(dbAttribute.isMandatory());

                    int maxlength = dbAttribute.getMaxLength();
                    if (maxlength != -1) {
                        if (field instanceof TextField) {
                            ((TextField) field).setMaxLength(maxlength);
                        } else if (field instanceof TextArea) {
                            ((TextArea) field).setMaxLength(maxlength);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        metaDataApplied = true;
    }

    /**
     * Copy the relationships from the give data object to the form.
     * The standard Form <tt>copyFrom()</tt> method cannot handle
     * <tt>RelationSelect</tt> Fields.
     *
     * @param dataObject to copy relationships from
     */
    protected void copyRelationFrom(DataObject dataObject) {
        if (dataObject == null) {
            throw new IllegalArgumentException("Null dataObject parameter");
        }

        log("copyRelationFrom");

        Iterator fieldIterator = ClickUtils.getFormFields(this).iterator();
        while (fieldIterator.hasNext()) {

            String key = ((Field) fieldIterator.next()).getName();

            if (key != null && key.startsWith(FK_PREFIX)) {

                HiddenField hiddenFKField = (HiddenField) getFields().get(key);
                String fkClassName = (String) hiddenFKField.getValueObject();

                String fieldName =
                    PROP_PREFIX + StringUtils.substringAfter(key, FK_PREFIX);
                HiddenField hiddenPropField =
                    (HiddenField) getFields().get(fieldName);

                String propName = (String) hiddenPropField.getValueObject();

                Field fkField = (Field)
                    getFields().get(StringUtils.substringAfter(key, FK_PREFIX));

                log("FKField: " + fkField.getName());
                log("relation: class[" + fkClassName + "], "
                    + "propName[" + propName + "]");

                if (fkClassName != null && propName != null) {
                    DataObject fkDataObject = (DataObject)
                        dataObject.readProperty(propName);

                    if (fkDataObject != null) {

                        int pk4FkData =
                            DataObjectUtils.intPKForObject(fkDataObject);

                        log("relation was set to: " + pk4FkData);
                        fkField.setValueObject(String.valueOf(pk4FkData));

                    } else {
                        log("relation was NOT set");
                    }
                }
            }
        }
    }

    /**
     * Copy the relationships from the form to the given give data object.
     * The standard Form <tt>copyTo()</tt> method cannot handle
     * <tt>RelationSelect</tt> Fields.
     *
     * @param dataObject to copy relationships to
     * @throws ClassNotFoundException if the foreign key class could not be
     * initialized
     */
    protected void copyRelationsTo(DataObject dataObject)
        throws ClassNotFoundException {

        if (dataObject == null) {
            throw new IllegalArgumentException("Null dataObject parameter");
        }

        log("copyRelationsTo");

        Iterator fieldIterator = ClickUtils.getFormFields(this).iterator();
        while (fieldIterator.hasNext()) {

            String key = ((Field) fieldIterator.next()).getName();

            if (key != null && key.startsWith(FK_PREFIX)) {

                HiddenField hiddenFKField = (HiddenField) getFields().get(key);
                String fkClassName = (String) hiddenFKField.getValueObject();

                String fieldName =
                    PROP_PREFIX + StringUtils.substringAfter(key, FK_PREFIX);
                HiddenField hiddenPropField =
                    (HiddenField) getFields().get(fieldName);

                String propName = (String) hiddenPropField.getValueObject();
                Field fkField = (Field)
                    getFields().get(StringUtils.substringAfter(key, FK_PREFIX));

                log("FKField: " + fkField.getName());

                String fkValue = fkField.getValue();
                log("relation: class[" + fkClassName + "], "
                    + "propName[" + propName + "], fk[" + fkValue + "]");

                if (fkClassName != null
                    && propName != null
                    && StringUtils.isNotEmpty(fkValue)) {

                    Class fkClass = Class.forName(fkClassName);
                    DataObject fkDataObject =
                        DataObjectUtils.objectForPK(getDataContext(),
                                                    fkClass,
                                                    Integer.parseInt(fkValue));

                    if (fkDataObject != null) {
                        dataObject.writeProperty(propName, fkDataObject);
                        log("written with: " + fkDataObject);
                    }
                }
            }
        }
    }

    /**
     * Log the given message to the System out console if debug is enabled.
     *
     * @param msg the message to log to System out
     */
    protected void log(Object msg) {
        ClickLogger.getInstance().debug(msg);
    }
}
