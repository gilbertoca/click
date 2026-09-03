/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.click.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import java.util.Map;
import junit.framework.TestCase;
import org.apache.click.MockContext;

/**
 * Test Table behavior.
 */
public class TableTest extends TestCase {

    /**
     * Check that Table prints message when no records are set.
     */
    public void testNoRows() {
        MockContext.initContext(Locale.ENGLISH);

        Table table = new Table();
        Column column = new Column("Foo");
        column.setSortable(false);
        table.addColumn(column);

        String header = "<thead>\n<tr>\n<th>Foo</th></tr></thead>\n";
        String body = "<tbody>\n<tr class=\"odd\"><td colspan=\"1\" class=\"error\">No records found.</td></tr>\n</tbody>";
        assertEquals("<table>\n" + header + body + "</table>\n", table.toString());
    }

    /**
     * Check that Column id's are rendered properly.
     */
    public void testTdId() {
        MockContext.initContext(Locale.ENGLISH);

        List<Foo> foos = new ArrayList<Foo>();
        foos.add(new Foo("foo1"));
        foos.add(new Foo("foo2"));

        Table table = new Table();
        table.setRenderId(true);
        table.setName("Foos");
        table.setRowList(foos);
        Column column = new Column("Name");
        column.setSortable(false);
        table.addColumn(column);

        String header = "<thead>\n<tr>\n<th>Name</th></tr></thead>\n";
        String row1 = "<tr class=\"odd\">\n<td id=\"Foos-Name_0\">foo1</td></tr>\n";
        String row2 = "<tr class=\"even\">\n<td id=\"Foos-Name_1\">foo2</td></tr>";
        String body = "<tbody>\n" + row1 + row2 + "</tbody>";
        assertEquals("<table id=\"Foos\">\n" + header + body + "</table>\n", table.toString());
    }

    /**
     * Check Table paging shows correct page.
     */
    public void testPagingCurrentPage() {
        MockContext.initContext(Locale.ENGLISH);

        List<Foo> foos = new ArrayList<Foo>();
        for (int i = 0; i < 1000; i++) {
            foos.add(new Foo("foo" + i));
        }

        Table table = new Table("table");
        table.setRowList(foos);
        table.setPageSize(10);
        table.setPageNumber(0);
        Column column = new Column("name");
        column.setSortable(false);
        table.addColumn(column);

        // Since page number is zero based check that if page number is 0,
        // Page 1 is the current page
        assertTrue(table.toString().indexOf("<strong>1</strong>") > 0);

        table.setPageNumber(99);

        // Check that if page number is 99, Page 100 is the current page
        assertTrue(table.toString().indexOf("<strong>100</strong>") > 0);
    }

    /**
     * Check that table row attributes are set.
     */
    public void testSetRowAttributes() {
        MockContext.initContext(Locale.ENGLISH);

        List<Foo> foos = new ArrayList<Foo>();
        for (int i = 0; i < 3; i++) {
            foos.add(new Foo("foo" + i));
        }

        Table table = new Table("table") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void addRowAttributes(Map attributes, Object row, int rowIndex) {
                Foo foo = (Foo) row;
                attributes.put("id", foo.getName());
                attributes.put("class", "foo bar");
            }
        };
        table.setRowList(foos);
        Column column = new Column("name");
        table.addColumn(column);

        // Check that a row with the id=foo0 is available
        assertTrue(table.toString().indexOf("<tr id=\"foo0\"") > 0);

        // Check that a row with the class=foo bar is available
        assertTrue(table.toString().indexOf("<tr id=\"foo0\" class=\"foo bar") > 0);
    }

    /**
     * Helper class for <code>testRowId</code>.
     */
    public static class Foo {

        private String name;

        public Foo(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Test CLK-673 caption
     */
    public void testCaption() {
        MockContext.initContext(Locale.ENGLISH);

        List<Foo> foos = new ArrayList<Foo>();
        foos.add(new Foo("foo1"));
        foos.add(new Foo("foo2"));

        Table table = new Table("table");
        table.setCaption("caption<code>tt</code>");
        table.setRowList(foos);
        Column column = new Column("name");
        table.addColumn(column);

        assertTrue(table.toString().contains("<caption>caption<code>tt</code></caption>"));
    }

    /**
     * Test that Table.getState contains the table internal state. CLK-715
     */
    public void testGetState() {
        // Setup table

        Table table = new Table("table");
        // Set table state
        int pageNumber = 5;
        boolean ascending = false;
        String sortedColumn = "Dummy";
        String linkValue = "myval";

        table.setPageNumber(pageNumber);
        table.setSortedAscending(ascending);
        table.setSortedColumn(sortedColumn);
        table.getControlLink().setValue(linkValue);

        // Retrieve table state
        Object[] state = (Object[]) table.getState();

        // Perform tests
        assertEquals(state[0], pageNumber);
        assertEquals(state[1], sortedColumn);
        assertEquals(state[2], ascending);

        Map controlLinkParams = (Map) state[3];

        assertEquals(controlLinkParams, table.getControlLink().getParameters());
        assertEquals(controlLinkParams.get("value"), linkValue);
    }

    /**
     * Test that Table.setState set the table internal state.
     *
     * CLK-715
     */
    public void testSetState() {
        // Setup table

        Table table = new Table("table");
        // Set table state
        int pageNumber = 5;
        boolean ascending = false;
        String sortedColumn = "Dummy";
        String linkValue = "myval";

        Object[] state = new Object[4];
        state[0] = Integer.valueOf(pageNumber);
        state[1] = sortedColumn;
        state[2] = Boolean.valueOf(ascending);
        Map controlLinkParams = new HashMap();
        controlLinkParams.put("value", linkValue);
        state[3] = controlLinkParams;

        // Set table state
        table.setState(state);

        // Perform tests
        assertEquals(pageNumber, table.getPageNumber());
        assertEquals(sortedColumn, table.getSortedColumn());
        assertEquals(ascending, table.isSortedAscending());
        assertEquals(controlLinkParams, table.getControlLink().getParameters());
        assertEquals(linkValue, table.getControlLink().getValue());
    }

    /**
     * Test CLK-241. Table Headers use incorrect title attribute
     * (table-last-title) when sortable=true.
     */
    public void testNoTitleOnLinkWhenSorting() {
        MockContext.initContext(Locale.ENGLISH, "/mock.htm");

        List<Foo> foos = new ArrayList<Foo>();
        foos.add(new Foo("foo1"));
        foos.add(new Foo("foo2"));

        Table table = new Table("table");
        table.setPageSize(1);
        table.setPaginatorAttachment(Table.PAGINATOR_ATTACHED);
        table.setBannerPosition(Table.POSITION_TOP);
        table.setSortable(true);
        table.setRowList(foos);
        Column column = new Column("name");
        table.addColumn(column);

        // Test that Name header column does not render a title attribute
        // that was set by the TablePaginator
        assertTrue(table.toString().contains("<th class=\"sortable\"><a href=\"/mock/mock.htm?actionLink=table-controlLink&amp;column=name&amp;page=0\">Name"));
    }

    /**
     * CLK-59 Check that Table correctly extracts column filters from request
     * parameters.
     */
    public void testFilterMapCompilationOnProcess() {
        // 1. Initialize Mock context containing search parameters
        MockContext context = MockContext.initContext();
        context.getMockRequest().setParameter("myTable_filter_customer", "ANA PAULA");
        context.getMockRequest().setParameter("myTable_filter_code", "01540");

        // 2. Setup the table infrastructure
        Table table = new Table("myTable");

        Column customerCol = new Column("customer");
        customerCol.setFilterBy("customer.name");
        table.addColumn(customerCol);

        Column idCol = new Column("code");
        idCol.setFilterBy("customer.code");
        table.addColumn(idCol);

        // Columns without filter targets shouldn't leak parameter values
        Column unfilterableCol = new Column("id");
        table.addColumn(unfilterableCol);

        table.onInit();
        // 3. Process request cycle to trigger parameter parsing
        table.onProcess();

        // 4. Validate gathered state Map against expectation
        Map<String, Object> compiledFilters = table.getFilters();

        assertEquals(2, compiledFilters.size());
        assertEquals("ANA PAULA", compiledFilters.get("customer.name"));
        assertEquals("01540", compiledFilters.get("customer.code"));
        assertNull(compiledFilters.get("id"));
    }

    /**
     * CLK-59 Test that table layout preserves filter state arrays properly.
     */
    public void testFilterStatePreservation() {
        Table table = new Table("myTable");
        Column clientCol = new Column("customer");
        clientCol.setFilterBy("customer.name");
        clientCol.setFilterValue("KLEBER");
        table.addColumn(clientCol);

        // Export State array representation
        Object[] savedState = (Object[]) table.getState();

        // Create clear companion table instance to restore onto
        Table targetTable = new Table("myTable");
        Column targetCol = new Column("customer");
        targetCol.setFilterBy("customer.name");
        targetTable.addColumn(targetCol);

        // Restore state definition array
        targetTable.setState(savedState);

        // Verify values made the round-trip across instance fields safely
        assertEquals("KLEBER", targetTable.getColumn("customer").getFilterValue());
        assertEquals("KLEBER", targetTable.getFilters().get("customer.name"));
    }

    /**
     * CLK-59 Verify that the secondary custom filter row is accurately compiled
     * into HTML.
     */
    public void testFilterRowHtmlRendering() {
        MockContext.initContext(Locale.ENGLISH);
        Table table = new Table("myTable");

        Column nameCol = new Column("name", "Name");
        nameCol.setFilterBy("person.name");
        nameCol.setFilterValue("John");
        nameCol.setSortable(false);
        table.addColumn(nameCol);

        // Run rendering sequence explicitly via the component's string generator
        String htmlOutput = table.toString();

        // Validate row structure and input components are compiled cleanly
        assertTrue(htmlOutput.contains("<tr class=\"filter-row\">"));
        assertTrue(htmlOutput.contains("<input type=\"text\" name=\"myTable_filter_name\" value=\"John\""));
    }

    /**
     * CLK-60 Verifies that adding a filterable column automatically registers
     * an internal AjaxBehavior on the filterLink, and that the rendered input
     * passes the correct link name parameters down to Click.filterTableAjax().
     */
    public void testNativeFilterAjaxBehaviorIntegration() {
        // Initialize the English Mock layout engine first
        MockContext.initContext(Locale.ENGLISH);

        Table ajaxTable = new Table("contractsTable");

        Column clientCol = new Column("cliente", "Cliente Name");
        clientCol.setFilterBy("servidor.pessoa.nome");
        clientCol.setSortable(false);
        ajaxTable.addColumn(clientCol);

        // Manually invoke onInit to simulate framework lifecycle initialization phase
        ajaxTable.onInit();

        ActionLink tableFilterLink = ajaxTable.getFilterLink();
        assertNotNull("Table's internal filterLink must be initialized", tableFilterLink);
        assertFalse("FilterLink should have active behaviors mapped",
                tableFilterLink.getBehaviors().isEmpty());

        String htmlOutput = ajaxTable.toString();

        String expectedLinkName = "contractsTable-filterLink";
        String expectedJsCall = "Click.filterTableAjax(this, 'contractsTable', '" + expectedLinkName + "')";

        assertTrue("Rendered HTML input field must map to the precise JavaScript Ajax helper function",
                htmlOutput.contains(expectedJsCall));
    }

    /**
     * Test that clicking a column sorting header link correctly updates the
     * Table state even when data rows are minimal (short table) and CLK-60 is
     * active.
     */
    public void testShortTableSortingLifecyclePreservation() {
        // 1. Initialize Context simulating a regular header sort link click
        MockContext context = MockContext.initContext();

        Table table = new Table("myTable");
        table.setPageSize(10);
        table.setSortable(true);

        Column nameCol = new Column("name");
        nameCol.setSortable(true);
        table.addColumn(nameCol);

        // 2. Setup standard short mock parameters using the table's controlLink identity
        String controlLinkName = table.getControlLink().getName(); // "myTable-controlLink"
        context.getMockRequest().setParameter("actionLink", controlLinkName);
        context.getMockRequest().setParameter(controlLinkName, "1");
        context.getMockRequest().setParameter(Table.COLUMN, "name");
        context.getMockRequest().setParameter(Table.ASCENDING, "true");
        context.getMockRequest().setParameter(Table.SORT, "true");

        // 3. Setup short data list
        List<Foo> smallList = new ArrayList<Foo>();
        smallList.add(new Foo("Beta Customer"));
        smallList.add(new Foo("Alpha Customer"));
        table.setRowList(smallList);

        // Run core lifecycle targets
        table.onInit();
        table.onProcess();
        table.onRender();

        // 4. Verify that sorting parameter lookups evaluate accurately
        assertEquals("The selected sort column must be mapped correctly", "name", table.getSortedColumn());
        assertFalse("The sort order must be updated to descending (false)", table.isSortedAscending());
    }

    /**
     * CLK-60 / CLK-59 Test that standard sorting and paging links accurately
     * retain active column filtering states during sequential request cycles.
     */
    public void testCombinedSortPagingAndFilterState() {
        // 1. Initialize Mock Context
        MockContext context = MockContext.initContext();

        Table table = new Table("myTable");
        table.setPageSize(2);
        table.setSortable(true);

        Column nameCol = new Column("name");
        nameCol.setFilterBy("person.name");
        table.addColumn(nameCol);

        Column emailCol = new Column("email");
        table.addColumn(emailCol);

        // 2. Target exact framework binding parameters safely
        String controlLinkName = table.getControlLink().getName();
        context.getMockRequest().setParameter("actionLink", controlLinkName);
        context.getMockRequest().setParameter(controlLinkName, "1");
        context.getMockRequest().setParameter(Table.COLUMN, "email");
        context.getMockRequest().setParameter(Table.ASCENDING, "true");
        context.getMockRequest().setParameter(Table.PAGE, "1");

        // Inject the active column input filter mock parameters matching your request parsing rule
        context.getMockRequest().setParameter("myTable_filter_name", "Ana");

        // 3. Populate mock rows to handle out-of-bounds page sanity arrays safely
        List<Foo> rows = new ArrayList<Foo>();
        for (int i = 0; i < 5; i++) {
            rows.add(new Foo("Customer " + i));
        }
        table.setRowList(rows);

        // Run lifecycles
        table.onInit();
        table.onProcess();
        table.onRender();

        // 4. Assertions to confirm all parameters live together happily
        assertEquals("The table should be sorted by email", "email", table.getSortedColumn());
        assertTrue("Sort direction should be ascending", table.isSortedAscending());
        assertEquals("Should look at page index 1", 1, table.getPageNumber());

        // Check that column filters were not overwritten or dropped during lifecycle processing
        assertEquals("The active text field filter must be extracted and preserved",
                "Ana", table.getColumn("name").getFilterValue());
    }
}
