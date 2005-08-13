package examples.page;

import java.util.List;

import net.sf.click.extras.panel.BasicPanel;
import net.sf.click.extras.panel.Panel;
import net.sf.click.extras.panel.TabbedPanel;
import examples.domain.CustomerDAO;

/**
 * Provides an TabbedPanel demonstration.
 *
 * @author Phil Barnes
 */
public class TabbedPanelDemo extends BorderedPage {

    public void onInit() {
        List customers = CustomerDAO.getCustomersSortedByName();
        addModel("customers", customers);

        TabbedPanel tabbedPanel = new TabbedPanel("tabbedPanel");
        Panel panel1 = new BasicPanel("panel1", "customersPanel1.htm");
        panel1.setLabel("The First Panel");
        tabbedPanel.addPanel(panel1,true);
        Panel panel2 = new BasicPanel("panel2", "customersPanel2.htm");
        panel2.setLabel("The Second Panel");
        tabbedPanel.addPanel(panel2,false);
        Panel panel3 = new BasicPanel("panel3", "customersPanel3.htm");
        panel3.setLabel("The Third Panel");
        tabbedPanel.addPanel(panel3,false);

        tabbedPanel.setPage(this);
        addModel(tabbedPanel.getName(), tabbedPanel);
    }

}
