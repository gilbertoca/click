package examples.page;

import net.sf.click.control.FileField;
import net.sf.click.control.Form;
import net.sf.click.control.Submit;
import net.sf.click.control.TextField;

/**
 * Provides File Upload example using the FileField control.
 *
 * @author Malcolm Edgar
 */
public class FileUpload extends BorderedPage {

    Form form;
    FileField fileField;
    TextField descField;

    public void onInit() {
        form = new Form("form", getContext());
        form.setLabelsPosition("top");
        addControl(form);

        fileField = new FileField("Select File");
        fileField.setRequired(true);
        fileField.setSize(40);
        form.add(fileField);

        descField = new TextField("Description");
        form.add(descField);

        Submit okButton = new Submit("    OK    ");
        okButton.setListener(this, "onOkClick");
        form.add(okButton);

        Submit cancelButton = new Submit(" Cancel ");
        cancelButton.setListener(this, "onCancelClick");
        form.add(cancelButton);
    }

    public boolean onOkClick() {
        if (form.isValid()) {
            addModel("fileItem", fileField.getFileItem());
            addModel("fileDesc", descField.getValue());
        }
        return true;
    }

    public boolean onCancelClick() {
        setRedirect("index.html");
        return false;
    }
}
 
