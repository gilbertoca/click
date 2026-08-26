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

import java.time.LocalDate;
import junit.framework.TestCase;
import org.apache.click.MockContext;
import org.apache.click.servlet.MockRequest;

/**
 * Provides LocalDateField JUnit TestCase.
 */
public class LocalDateFieldTest extends TestCase {

    /**
     * Test that empty, blank, or missing parameters are bound to null.
     */
    public void testNullParameter() {
        MockContext mockContext = MockContext.initContext();
        MockRequest request = mockContext.getMockRequest();

        LocalDateField localDateField = new LocalDateField("localDateField");
        assertEquals("localDateField", localDateField.getName());

        // Test empty string parameter
        request.getParameterMap().put("localDateField", new String[]{""});
        assertTrue(localDateField.onProcess());
        assertNull(localDateField.getLocalDate());

        // Test whitespace string parameter
        request.getParameterMap().put("localDateField", new String[]{" "});
        assertTrue(localDateField.onProcess());
        assertNull(localDateField.getLocalDate());

        // Test null parameter mapping
        request.getParameterMap().put("localDateField", null);
        assertTrue(localDateField.onProcess());
        assertNull(localDateField.getLocalDate());

        // Test programmatically setting null
        localDateField.setLocalDate(null);
        assertNull(localDateField.getLocalDate());
    }

    /**
     * Test successful parameter binding, object caching, and type validation.
     */
    public void testLocalDateCacheValue() {
        MockContext mockContext = MockContext.initContext();
        MockRequest request = mockContext.getMockRequest();

        LocalDateField localDateField = new LocalDateField("localDateField");
        String requestParam = "2026-08-26";
        request.getParameterMap().put("localDateField", new String[]{requestParam});

        assertTrue(localDateField.onProcess());

        // Check that the standard text value equals the raw request parameter
        assertEquals(requestParam, localDateField.getValue());

        // Retrieve the LocalDate from the field: checks the binding result
        LocalDate date = localDateField.getLocalDate();
        assertEquals(LocalDate.of(2026, 8, 26), date);

        // Assert that subsequent retrievals fetch the same instance reference
        assertSame(date, localDateField.getLocalDate());

        // Check that getValueObject also returns the cached domain value cleanly
        assertSame(date, localDateField.getValueObject());

        // Programmatically reinforce setter and ensure value synchronization doesn't mutate string state
        localDateField.setLocalDate(date);
        assertEquals(requestParam, localDateField.getValue());
    }

    /**
     * Test that invalid HTML5 string payloads flag validation failures cleanly.
     */
    public void testInvalidDateFormat() {
        MockContext mockContext = MockContext.initContext();
        MockRequest request = mockContext.getMockRequest();

        LocalDateField localDateField = new LocalDateField("localDateField");
        request.getParameterMap().put("localDateField", new String[]{"2026/08/26"}); // Invalid ISO sequence

        assertTrue(localDateField.onProcess());
        System.out.println("LocalDateField: " + localDateField.getError());
        System.out.println("LocalDateField: " + localDateField.getErrorLabel());
        System.out.println("LocalDateField: " + localDateField);
        localDateField.validate();

        // The layout parser must reject non ISO-8601 formatting sequences
        assertNull(localDateField.getLocalDate());
        System.out.println("org.apache.click.control.LocalDateFieldTest.testInvalidDateFormat(): " + localDateField.getLocalDate());
        assertFalse(localDateField.isValid());
    }

    /**
     * Test that the component produces valid HTML5 target string render
     * outputs.
     */
    public void testRender() {
        MockContext.initContext();
        LocalDateField localDateField = new LocalDateField("localDateField");
        localDateField.setLocalDate(LocalDate.of(2026, 8, 26));

        String html = localDateField.toString();

        // Verify native metadata tags inside component's output
        assertTrue(html.contains("type=\"date\""));
        assertTrue(html.contains("name=\"localDateField\""));
        assertTrue(html.contains("value=\"2026-08-26\""));
    }
}
