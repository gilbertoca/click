package net.sf.click.extras.validator;

import net.sf.click.control.TextField;

/**
 * The implementation of {@link net.sf.click.extras.validator.Validator}
 * for {@link net.sf.click.control.TextField}.
 * 
 * @author Naoki Takezoe
 */
public class TextFieldValidator implements Validator {
	
	private TextField field;
	
	public TextFieldValidator(TextField field){
		this.field = field;
	}
	
	public String getName(){
		return field.getName();
	}
	
	public String getClientValidationScript(){
		
    	StringBuffer sb = new StringBuffer();
    	
    	sb.append(JavaScriptUtils.createValidationFunction(field) + "{\n");
    	if(field.isRequired()){
	    	sb.append("if(").append(JavaScriptUtils.getFieldValue(field)).append("==''){\n");
	    	sb.append(JavaScriptUtils.fieldAlert("field-required-error", field));
    		sb.append(JavaScriptUtils.focusField(field));
	    	sb.append("return false;\n");
	    	sb.append("}\n");
    	}
    	if(field.getMinLength() > 0){
    		sb.append("if(").append(JavaScriptUtils.getFieldValue(field)).append(".length <").append(field.getMinLength()).append("){\n");
    		sb.append(JavaScriptUtils.fieldAlert("field-minlength-error", field, new Integer(field.getMinLength())));
    		sb.append(JavaScriptUtils.focusField(field));
	    	sb.append("return false;\n");
	    	sb.append("}\n");
    	}
    	if(field.getMaxLength() > 0){
    		sb.append("if(").append(JavaScriptUtils.getFieldValue(field)).append(".length >").append(field.getMaxLength()).append("){\n");
    		sb.append(JavaScriptUtils.fieldAlert("field-maxlength-error", field, new Integer(field.getMaxLength())));
    		sb.append(JavaScriptUtils.focusField(field));
	    	sb.append("return false;\n");
	    	sb.append("}\n");
    	}
    	sb.append("return true;\n");
    	sb.append("}\n");
    	
    	return sb.toString();
	}
	
}
