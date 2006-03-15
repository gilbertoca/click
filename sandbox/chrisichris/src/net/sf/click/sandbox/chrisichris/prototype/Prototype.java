package net.sf.click.sandbox.chrisichris.prototype;

import net.sf.click.util.HtmlStringBuffer;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Helper which creates commonly used JS code for prototype. This class has no state. 
 * Because of velocity the mothods are not static but you can use 
 * Prototype.INSTANCE to get a Singleton.
 * @author chris
 *
 */
public class Prototype implements Cloneable{

    public static final Prototype INSTANCE = new Prototype();
    
    public static final String INSERTION_BEFORE = "Insertion.Before";
    public static final String INSERTION_TOP = "Insertion.Top";
    public static final String INSERTION_BOTTOM = "Insertion.Bottom";
    public static final String INSERTION_AFTER = "Insertion.After";
    
    public Prototype() {
        super();
        // TODO Auto-generated constructor stub
    }

    public String javascriptTag(String content){
        if(content == null)
            content = "";
        HtmlStringBuffer stB = new HtmlStringBuffer(50+content.length());
        stB.elementStart("script");
        stB.appendAttribute("type", "text/javascript");
        stB.appendAttribute("language","javascript");
        stB.closeTag();
        
        //use cdata for xhtml and hide it from jscript through jscript comments
        stB.append("\n// <![CDATA{ \n");
        stB.append(content);
        stB.append("\n// ]]>\n");
        stB.elementEnd("script");
        return stB.toString();
    }
    
    public String updateElementJS(String elementId, String content,String position){
        if(elementId == null) {
            throw new NullPointerException("param elementId");
        }
        
        if(content == null){
            content = "";
        } else {
            content = StringEscapeUtils.escapeJavaScript(content);
        }
        
        final String ret;
        if(position != null){
            ret = "new Insertion."+StringUtils.capitalize(position)+"('"+elementId+"','"+content+"');\n";
        } else {
            ret = "$('"+elementId+").innerHTML = '"+content+"';\n";
        }
        
        return ret;
    }
    
    /**
     * Returns JS which makes the html element with the given id empty. Can be ie used in PrototypeAjax.onComplete
     * @param elementId
     * @return
     */
    public String clearElementJS(String elementId){
        if(elementId == null)
            throw new NullPointerException("param elementId");
        return "$('"+elementId+"').innerHTML = '';\n";
    }
    
    /**
     * Returns JS which remove the html element with the given id. Can be ie used in PrototypeAjax.onComplete
     * @param elementId
     * @return
     */
    public String removeElementJS(String elementId){
        if(elementId == null)
            throw new NullPointerException("param elementId");
        return "Element.remove('"+elementId+"');\n";
    }
    
    /**
     * Return the given in parameter + ";return false;"
     * @param js to execute
     * @return the js parameter + ";return false;"
     */
    public String onClick(String js) {
        return js+"; return false;";
    }
    
}
