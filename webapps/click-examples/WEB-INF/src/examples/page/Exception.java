package examples.page;

import net.sf.click.Page;
import net.sf.click.control.ActionLink;

/**
 * Provides examples of the Click Exception handling which includes a broken
 * Velocity template and a broken event listener.
 *
 * @author Malcolm Edgar
 */
public class Exception extends Page {

    /**
     * @see Page#onInit()
     */
    public void onInit() {
        System.err.println(toString() + " onInit() invoked");

        ActionLink brokenPageLink = new ActionLink("brokenPageLink");
        brokenPageLink.setListener(this, "onBrokenPageClick");
        addControl(brokenPageLink);

        ActionLink exceptionLink = new ActionLink("exceptionLink");
        exceptionLink.setListener(this, "onExceptionClick");
        addControl(exceptionLink);

        ActionLink missingMethodLink = new ActionLink("missingMethodLink");
        missingMethodLink.setListener(this, "missingMethodClick");
        addControl(missingMethodLink);
    }

    public boolean onBrokenPageClick() {
        setPath("broken-page.htm");

        return true;
    }

    public boolean onExceptionClick() {
        throw new NullPointerException("Oh No...");
    }

    /**
     * @see Page#onDestroy()
     */
    public void onDestroy() {
        System.err.println(toString() + " onDestroy() invoked");
    }

}
