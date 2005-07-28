package examples.page;

import java.io.Serializable;

import net.sf.click.control.Checkbox;
import net.sf.click.control.DateField;
import net.sf.click.control.EmailField;
import net.sf.click.control.Form;
import net.sf.click.control.Label;
import net.sf.click.control.Select;
import net.sf.click.control.Submit;
import net.sf.click.control.TextField;
import examples.control.InvestmentSelect;

/**
 * Provides a example Page to demonstrate Form properties and layout options.
 *
 * @author Malcolm Edgar
 */
public class FormProperties extends BorderedPage {

    /** Form options holder. */
    public static class Options implements Serializable {
        static final long serialVersionUID = 1L;
        String buttonAlign = Form.LEFT;
        int columns = 1;
        boolean disabled = false;
        String errorsAlign = Form.LEFT;
        String errorsPosition = Form.MIDDLE;
        String labelAlign = Form.LEFT;
        String labelsPosition = Form.LEFT;
        boolean readonly = false;
        boolean showBorders = true;
        boolean validate = true;
    }

    /** Form values holder.*/
    public static class Values implements Serializable {
        static final long serialVersionUID = 1L;
        String name = "";
        String email = "";
        String investments = "";
        String dateJoined = "";
    }

    Form form;
    TextField nameField;
    EmailField emailField;
    InvestmentSelect investmentsField;
    DateField dateJoinedField;

    Form optionsForm;
    Select buttonAlignSelect;
    Select errorsAlignSelect;
    Select errorsPositionSelect;
    Select labelAlignSelect;
    Select labelsPositionSelect;
    Select columnsSelect;
    Checkbox showBordersCheckbox;
    Checkbox disabledCheckbox;
    Checkbox readonlyCheckbox;
    Checkbox validateCheckbox;

    public void onInit() {
        //--------------------------
        // Setup demonstration form.
        form = new Form("form", getContext());
        addControl(form);

        nameField = new TextField("Name");
        nameField.setRequired(true);
        nameField.setFocus(true);
        form.add(nameField);

        emailField = new EmailField("Email");
        emailField.setRequired(true);
        form.add(emailField);

        investmentsField = new InvestmentSelect("Investments");
        form.add(investmentsField);

        dateJoinedField = new DateField("Date Joined");
        form.add(dateJoinedField);

        Submit okButton = new Submit("    OK    ");
        okButton.setListener(this, "onOkClick");
        form.add(okButton);

        Submit cancelButton = new Submit(" Cancel ");
        cancelButton.setListener(this, "onCancelClick");
        form.add(cancelButton);

        //-------------------
        // Setup control form
        optionsForm = new Form("optionsForm", getContext());
        optionsForm.setColumns(2);
        optionsForm.setLabelAlign("right");
        optionsForm.setListener(this, "onApplyChanges");
        addControl(optionsForm);

        buttonAlignSelect = new Select("Button Align");
        buttonAlignSelect.addAll(new String[] { "left", "center", "right" });
        buttonAlignSelect.setTitle("Buttons horizontal alignment");
        buttonAlignSelect.setAttribute("onChange", "optionsForm.submit();");
        optionsForm.add(buttonAlignSelect);

        showBordersCheckbox = new Checkbox("Show Borders");
        showBordersCheckbox.setAttribute("onChange", "optionsForm.submit();");
        optionsForm.add(showBordersCheckbox);

        columnsSelect = new Select("Columns");
        columnsSelect.addAll(new String[] { "1", "2", "3", "4" });
        columnsSelect.setTitle("Number of Form table columns");
        columnsSelect.setAttribute("onChange", "optionsForm.submit();");
        optionsForm.add(columnsSelect);

        disabledCheckbox = new Checkbox("Disabled");
        disabledCheckbox.setAttribute("onChange", "optionsForm.submit();");
        optionsForm.add(disabledCheckbox);

        errorsAlignSelect = new Select("Errors Align");
        errorsAlignSelect.addAll(new String[] { "left", "center", "right" });
        errorsAlignSelect.setTitle("Errors block horizontal alignment");
        errorsAlignSelect.setAttribute("onChange", "optionsForm.submit();");
        optionsForm.add(errorsAlignSelect);

        readonlyCheckbox = new Checkbox("Readonly");
        readonlyCheckbox.setAttribute("onChange", "optionsForm.submit();");
        optionsForm.add(readonlyCheckbox);

        errorsPositionSelect = new Select("Errors Position");
        errorsPositionSelect.addAll(new String[] { "top", "middle", "bottom" });
        errorsPositionSelect.setTitle("Form errors position");
        errorsPositionSelect.setAttribute("onChange", "optionsForm.submit();");
        optionsForm.add(errorsPositionSelect);

        validateCheckbox = new Checkbox("Validate");
        validateCheckbox.setAttribute("onChange", "optionsForm.submit();");
        optionsForm.add(validateCheckbox);

        labelAlignSelect = new Select("Label Align");
        labelAlignSelect.addAll(new String[] { "left", "center", "right" });
        labelAlignSelect.setTitle("Field label alignment");
        labelAlignSelect.setAttribute("onChange", "optionsForm.submit();");
        optionsForm.add(labelAlignSelect);

        optionsForm.add(new Label(""));

        labelsPositionSelect = new Select("Labels Position");
        labelsPositionSelect.addAll(new String[] {"left", "top"});
        labelsPositionSelect.setTitle("Form labels position");
        labelsPositionSelect.setAttribute("onChange", "optionsForm.submit();");
        optionsForm.add(labelsPositionSelect);

        Submit resetButton = new Submit("Restore Defaults");
        resetButton.setTitle("Restore default form properties");
        resetButton.setListener(this, "onRestoreDefaults");
        optionsForm.add(resetButton);

        // Setup showBorders checkbox Javascript using HTML head include and
        // setting the body onload function.
        addModel("head-include", "form-head.htm");
        addModel("body-onload", "toggleBorders(document.optionsForm.showBorders);");

        // Apply saved options to the demo form and the optionsForm
        Options options = (Options) getContext().getSessionObject(Options.class);
        applyOptions(options);
    }

    public boolean onOkClick() {
        Values values = (Values) getContext().getSessionObject(Values.class);

        values.name = nameField.getValue();
        values.email = emailField.getValue();
        values.investments = investmentsField.getValue();
        values.dateJoined = dateJoinedField.getValue();

        getContext().setSessionObject(values);

        return true;
    }

    public boolean onCancelClick() {
        setRedirect("index.html");
        return false;
    }

    public boolean onApplyChanges() {
        Options options = new Options();

        options.buttonAlign = buttonAlignSelect.getValue();
        options.columns = Integer.parseInt(columnsSelect.getValue());
        options.errorsAlign = errorsAlignSelect.getValue();
        options.errorsPosition = errorsPositionSelect.getValue();
        options.labelAlign = labelAlignSelect.getValue();
        options.labelsPosition = labelsPositionSelect.getValue();
        options.disabled = disabledCheckbox.isChecked();
        options.readonly = readonlyCheckbox.isChecked();
        options.validate = validateCheckbox.isChecked();
        options.showBorders = showBordersCheckbox.isChecked();

        applyOptions(options);

        getContext().setSessionObject(options);

        // Apply any saved form values to demo form.
        Values values = (Values) getContext().getSessionObject(Values.class);

        nameField.setValue(values.name);
        emailField.setValue(values.email);
        investmentsField.setValue(values.investments);
        dateJoinedField.setValue(values.dateJoined);

        return true;
    }

    public boolean onRestoreDefaults() {
        getContext().removeSessionObject(Options.class);
        getContext().removeSessionObject(Values.class);

        applyOptions(new Options());

        return true;
    }

    private void applyOptions(Options options) {
        form.setButtonAlign(options.buttonAlign);
        form.setColumns(options.columns);
        form.setDisabled(options.disabled);
        form.setErrorsAlign(options.errorsAlign);
        form.setErrorsPosition(options.errorsPosition);
        form.setLabelAlign(options.labelAlign);
        form.setLabelsPosition(options.labelsPosition);
        form.setReadonly(options.readonly);
        form.setValidate(options.validate);

        buttonAlignSelect.setValue(options.buttonAlign);
        columnsSelect.setValue(String.valueOf(options.columns));
        errorsAlignSelect.setValue(options.errorsAlign);
        errorsPositionSelect.setValue(options.errorsPosition);
        labelAlignSelect.setValue(options.labelAlign);
        labelsPositionSelect.setValue(options.labelsPosition);
        showBordersCheckbox.setChecked(options.showBorders);
        readonlyCheckbox.setChecked(options.readonly);
        disabledCheckbox.setChecked(options.disabled);
        validateCheckbox.setChecked(options.validate);
    }
}
