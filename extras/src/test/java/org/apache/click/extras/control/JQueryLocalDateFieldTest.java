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
package org.apache.click.extras.control;

import java.time.LocalDate;
import java.util.List;
import junit.framework.TestCase;
import org.apache.click.MockContext;
import org.apache.click.element.Element;
import org.apache.click.servlet.MockRequest;
import org.apache.click.util.HtmlStringBuffer;

/**
 * Provides JQueryLocalDateField JUnit TestCase.
 */
public class JQueryLocalDateFieldTest extends TestCase {

    /**
     * Test that empty, blank, or missing parameters are bound to null.
     */
    public void testNullParameter() {
        MockContext mockContext = MockContext.initContext();
        MockRequest request = mockContext.getMockRequest();

        JQueryLocalDateField jqueryField = new JQueryLocalDateField("jQueryLocalDateField");
        assertEquals("jQueryLocalDateField", jqueryField.getName());

        // Test empty string parameter
        request.getParameterMap().put("jQueryLocalDateField", new String[]{""});
        assertTrue(jqueryField.onProcess());
        assertNull(jqueryField.getLocalDate());

        // Test whitespace string parameter
        request.getParameterMap().put("jQueryLocalDateField", new String[]{" "});
        assertTrue(jqueryField.onProcess());
        assertNull(jqueryField.getLocalDate());

        // Test null parameter mapping
        request.getParameterMap().put("jQueryLocalDateField", null);
        assertTrue(jqueryField.onProcess());
        assertNull(jqueryField.getLocalDate());

        // Test programmatically setting null
        jqueryField.setLocalDate(null);
        assertNull(jqueryField.getLocalDate());
    }

    /**
     * Test successful parameter binding, object caching, and type validation.
     */
    public void testLocalDateCacheValue() {
        MockContext mockContext = MockContext.initContext();
        MockRequest request = mockContext.getMockRequest();

        JQueryLocalDateField jqueryField = new JQueryLocalDateField("jQueryLocalDateField");
        String requestParam = "28/08/2026";
        request.getParameterMap().put("jQueryLocalDateField", new String[]{requestParam});

        assertTrue(jqueryField.onProcess());

        // Check that the standard text value equals the raw request parameter
        assertEquals(requestParam, jqueryField.getValue());

        // Retrieve the LocalDate from the field: checks the binding result
        LocalDate date = jqueryField.getLocalDate();
        assertEquals(LocalDate.of(2026, 8, 28), date);

        // Assert that subsequent retrievals fetch the same instance reference
        assertSame(date, jqueryField.getLocalDate());

        // Check that getValueObject also returns the cached domain value cleanly
        assertSame(date, jqueryField.getValueObject());

        // Programmatically reinforce setter and ensure value synchronization doesn't mutate string state
        jqueryField.setLocalDate(date);
        assertEquals(requestParam, jqueryField.getValue());
    }

    /**
     * Test that invalid HTML5 string payloads flag validation failures cleanly.
     */
    public void testInvalidDateFormat() {
        MockContext mockContext = MockContext.initContext();
        MockRequest request = mockContext.getMockRequest();

        JQueryLocalDateField jqueryField = new JQueryLocalDateField("jQueryLocalDateField");
        request.getParameterMap().put("jQueryLocalDateField", new String[]{"2026/08/28"}); // Invalid date format

        assertTrue(jqueryField.onProcess());
        System.out.println("jQueryLocalDateField: " + jqueryField.getError());
        System.out.println("jQueryLocalDateField: " + jqueryField);
        jqueryField.validate();

        // The layout parser must reject non ISO-8601 formatting sequences
        assertNull(jqueryField.getLocalDate());
        System.out.println("org.apache.click.extra.control.JQueryLocalDateFieldTest.testInvalidDateFormat(): " + jqueryField.getLocalDate());
        assertFalse(jqueryField.isValid());
    }

    /**
     * Test that the component produces valid HTML5 target string render
     * outputs.
     */
    public void testRender() {
        MockContext.initContext();
        JQueryLocalDateField jqueryField = new JQueryLocalDateField("jQueryLocalDateField");
        jqueryField.setLocalDate(LocalDate.of(2026, 8, 28));

        String html = jqueryField.toString();

        // Verify native metadata tags inside component's output
        assertTrue(html.contains("type=\"text\""));
        assertTrue(html.contains("name=\"jQueryLocalDateField\""));
        assertTrue(html.contains("value=\"28/08/2026\""));
    }

    /**
     * Test that the component accurately registers all required jQuery and
     * jQuery UI HTML header asset elements during its lifecycle processing
     * state.
     */
    public void testJQueryAssetsPresence() {
        MockContext.initContext();
        JQueryLocalDateField field = new JQueryLocalDateField("jqueryField");

        // 1. Trigger the framework's head elements registration pipeline
        List<Element> headElements = field.getHeadElements();
        assertNotNull(headElements);
        assertFalse(headElements.isEmpty());

        // 2. Convert the full elements list to a flat string for scannable assertions
        HtmlStringBuffer buffer = new HtmlStringBuffer();
        for (Element element : headElements) {
            element.render(buffer);
        }
        String renderedHeaders = buffer.toString();
        
        //System.out.println("org.apache.click.extras.control.JQueryLocalDateFieldTest.testJQueryAssetsPresence() " + renderedHeaders);
        
        // 3. Verify the correct version-isolated HTML import strings are present
        assertTrue("Missing jQuery Core Import!",
                renderedHeaders.contains("src=\"/mock/click/jquery/3.7.1/jquery.min.js\""));

        assertTrue("Missing jQuery UI JS Import!",
                renderedHeaders.contains("src=\"/mock/click/jquery/ui/1.14.2/jquery-ui.min.js\""));

        assertTrue("Missing jQuery UI CSS Import!",
                renderedHeaders.contains("href=\"/mock/click/jquery/ui/1.14.2/jquery-ui.min.css\""));

        assertTrue("Missing Javascript picker initialization block!",
                renderedHeaders.contains(".datepicker({"));
    }
}
