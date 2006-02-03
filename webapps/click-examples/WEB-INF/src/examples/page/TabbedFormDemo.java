package examples.page;

import java.util.Iterator;
import java.util.List;

import net.sf.click.control.Checkbox;
import net.sf.click.control.Field;
import net.sf.click.control.FieldSet;
import net.sf.click.control.Radio;
import net.sf.click.control.RadioGroup;
import net.sf.click.control.Submit;
import net.sf.click.control.TextArea;
import net.sf.click.control.TextField;
import net.sf.click.extras.control.CreditCardField;
import net.sf.click.extras.control.DateField;
import net.sf.click.extras.control.EmailField;
import net.sf.click.extras.control.IntegerField;
import net.sf.click.extras.control.TabbedForm;
import net.sf.click.util.ClickUtils;
import examples.control.PackagingRadioGroup;
import examples.control.TitleSelect;

public class TabbedFormDemo extends BorderedPage {

    private TabbedForm form = new TabbedForm("form");
    private RadioGroup paymentGroup = new RadioGroup("paymentOption", true);
    private TextField cardName = new TextField("cardName");
    private CreditCardField cardNumber = new CreditCardField("cardNumber");
    private IntegerField expiry = new IntegerField("expiry");

    public TabbedFormDemo() {
        form.setBackgroundColor("#F7FFAF");
        form.setTabHeight("210px");
        form.setTabWidth("420px");

        // Contact tab sheet

        FieldSet contactTabSheet = new FieldSet("contactDetails");
        form.addTabSheet(contactTabSheet);

        contactTabSheet.add(new TitleSelect("title"));

        contactTabSheet.add(new TextField("firstName"));

        contactTabSheet.add(new TextField("middleNames"));

        contactTabSheet.add(new TextField("surname", true));

        contactTabSheet.add(new TextField("contactNumber"));

        contactTabSheet.add(new EmailField("email"));

        // Delivery tab sheet

        FieldSet deliveryTabSheet = new FieldSet("deliveryDetails");
        form.addTabSheet(deliveryTabSheet);

        TextArea textArea = new TextArea("deliveryAddress", true);
        textArea.setCols(30);
        textArea.setRows(3);
        deliveryTabSheet.add(textArea);

        deliveryTabSheet.add(new DateField("deliveryDate"));

        PackagingRadioGroup packaging = new PackagingRadioGroup("packaging");
        packaging.setValue("STD");
        deliveryTabSheet.add(packaging);

        deliveryTabSheet.add(new Checkbox("telephoneOnDelivery"));

        // Payment tab sheet

        FieldSet paymentTabSheet = new FieldSet("paymentDetails");
        form.addTabSheet(paymentTabSheet);

        paymentGroup.add(new Radio("cod", "Cash On Delivery "));
        paymentGroup.add(new Radio("credit", "Credit Card "));
        paymentGroup.setVerticalLayout(false);
        paymentTabSheet.add(paymentGroup);

        paymentTabSheet.add(cardName);
        paymentTabSheet.add(cardNumber);
        paymentTabSheet.add(expiry);
        expiry.setSize(4);
        expiry.setMaxLength(4);

        // Buttons

        form.add(new Submit("ok", "   OK   ",  this, "onOkClick"));
        form.add(new Submit("cancel", this, "onCancelClick"));

        addControl(form);
    }

    public boolean onOkClick() {
        if (form.isValid()) {

            if (paymentGroup.getValue().equals("credit")) {
                cardName.setRequired(true);
                cardName.validate();

                cardNumber.setRequired(true);
                cardNumber.validate();

                expiry.setRequired(true);
                expiry.validate();

                if (form.isValid()) {
                    processDelivery();
                }

            } else {
                processDelivery();
            }

        }
        return true;
    }

    public boolean onCancelClick() {
        setRedirect("index.html");
        return false;
    }

    private void processDelivery() {
        List fieldList = ClickUtils.getFormFields(form);
        for (Iterator i = fieldList.iterator(); i.hasNext(); ) {
            Field field = (Field) i.next();
            System.out.println(field.getName() + "=" + field.getValue());
        }
    }
}
