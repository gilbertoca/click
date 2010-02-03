package net.sf.click.examples.page.control;

import java.util.Iterator;
import java.util.List;

import net.sf.click.control.Checkbox;
import net.sf.click.control.Field;
import net.sf.click.control.FieldSet;
import net.sf.click.control.Form;
import net.sf.click.control.Submit;
import net.sf.click.control.TextArea;
import net.sf.click.control.TextField;
import net.sf.click.examples.control.PackagingRadioGroup;
import net.sf.click.examples.page.BorderPage;
import net.sf.click.examples.page.HomePage;
import net.sf.click.extras.control.CreditCardField;
import net.sf.click.extras.control.DateField;
import net.sf.click.extras.control.IntegerField;
import net.sf.click.extras.control.PageSubmit;
import net.sf.click.util.ClickUtils;

/**
 * Provides a form FieldSet example.
 *
 * @author Malcolm Edgar
 */
public class FieldSetDemo extends BorderPage {

    public Form form = new Form();

    public FieldSetDemo() {
        form.setLabelAlign(Form.ALIGN_RIGHT);
        form.setFieldStyle("width:22em;");

        // Delivery fieldset

        FieldSet deliveryFieldSet = new FieldSet("deliveryDetails");
        form.add(deliveryFieldSet);

        TextField addressToField = new TextField("addressedTo", true);
        addressToField.setSize(30);
        deliveryFieldSet.add(addressToField);

        TextArea textArea = new TextArea("deliveryAddress", true);
        textArea.setCols(30);
        textArea.setRows(3);
        deliveryFieldSet.add(textArea);

        DateField dateField = new DateField("deliveryDate");
        deliveryFieldSet.add(dateField);

        PackagingRadioGroup radioGroup = new PackagingRadioGroup("packaging");
        radioGroup.setValue("STD");
        radioGroup.setVerticalLayout(true);
        deliveryFieldSet.add(radioGroup);

        deliveryFieldSet.add(new Checkbox("telephoneOnDelivery"));

        // Payment fieldset

        FieldSet paymentFieldSet = new FieldSet("paymentDetails");
        form.add(paymentFieldSet);

        paymentFieldSet.add(new TextField("cardName"));
        paymentFieldSet.add(new CreditCardField("cardNumber"));
        IntegerField expiryField = new IntegerField("expiry");
        expiryField.setSize(4);
        expiryField.setMaxLength(4);
        paymentFieldSet.add(expiryField);

        form.add(new Submit("ok", "  OK  ",  this, "onOkClick"));
        form.add(new PageSubmit("cancel", HomePage.class));
    }

    public boolean onOkClick() {
        if (form.isValid()) {
            List fieldList = ClickUtils.getFormFields(form);
            for (Iterator i = fieldList.iterator(); i.hasNext(); ) {
                Field field = (Field) i.next();
                System.out.println(field.getName() + "=" + field.getValue());
            }
        }
        return true;
    }
}