package net.sf.click.examples.page.panel;

import java.util.Date;
import java.util.List;

import net.sf.click.control.Column;
import net.sf.click.control.Table;
import net.sf.click.examples.control.FilterPanel;
import net.sf.click.examples.page.BorderPage;

/**
 * Provides example usage of a custom date range FilterPanel control.
 *
 * @author Malcolm Edgar
 */
public class FilterPanelDemo extends BorderPage {

    public FilterPanel filterPanel = new FilterPanel();
    public Table table = new Table();

    public FilterPanelDemo() {
        // Setup customers table
        table.setAttribute("class", "isi");
        table.setAttribute("width", "550px");

        table.addColumn(new Column("name"));

        Column column = new Column("age");
        column.setAttribute("style", "{text-align:center;}");
        table.addColumn(column);

        table.addColumn(new Column("investments"));

        column = new Column("holdings");
        column.setFormat("${0,number,#,##0.00}");
        column.setAttribute("style", "{text-align:right;}");
        table.addColumn(column);

        column = new Column("dateJoined");
        column.setAttribute("style", "{text-align:right;}");
        column.setFormat("{0, date,dd MMM yyyy}");
        table.addColumn(column);
    }

    /**
     * @see net.sf.click.Page#onRender()
     */
    public void onRender() {
        Date from = filterPanel.getStartDate();
        Date to = filterPanel.getEndDate();

        List customers = getCustomerService().getCustomers(from, to);

        table.setRowList(customers);
    }
}
