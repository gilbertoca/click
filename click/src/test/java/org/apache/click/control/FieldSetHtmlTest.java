/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://apache.org
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.click.control;

import junit.framework.TestCase;
import org.apache.click.MockContext;
import org.apache.click.util.HtmlStringBuffer;

/**
 * Dedicated test suite validating semantic HTML/DOM output specifications for
 * the FieldSet component introduced in the JDK 17 Fork.
 */
public class FieldSetHtmlTest extends TestCase {

    @Override
    public void setUp() {
        // Initializes the MockContext to populate ThreadLocal mock servlet stacks
        MockContext.initContext("test-form.htm");
    }

    /**
     * Validates that the default FieldSet behavior remains strictly
     * table-based, guaranteeing total backward compatibility for existing
     * enterprise applications.
     */
    public void testDefaultLayoutTableRendering() {
        Form form = new Form("mainForm");
        FieldSet fieldSet = new FieldSet("addressFs", "Address Details");
        form.add(fieldSet);

        // Assures the default internal property layout value is false "table layout"
        assertEquals(form.isNoLayout(), false);

        fieldSet.add(new TextField("street", "Street"));

        HtmlStringBuffer buffer = new HtmlStringBuffer();
        fieldSet.render(buffer);
        String htmlResult = buffer.toString();

        // Assures the historical table markup is outputted correctly
        assertTrue("Default layout must contain an outer fieldset tag", htmlResult.contains("<fieldset"));
        assertTrue("Default layout must contain a table element", htmlResult.contains("<table class=\"fields\""));
        assertTrue("Default layout must contain tr rows", htmlResult.contains("<tr"));
    }

    /**
     * Verifies that explicitly switching to DIV layout strategy dynamically
     * updates the DOM tree into clean block elements, completely eliminating
     * any legacy table artifacts.
     */
    public void testHtml5NoLayoutRendering() {
        MockContext.initContext("test-form.htm");
        Form form = new Form("modernForm");

        FieldSet fieldSet = new FieldSet("identityFs", "Identity Info");
        form.add(fieldSet);

        // Update flag to verify your new WHATWG zero-layout toggle
        form.setNoLayout(true);
        assertTrue(form.isNoLayout());

        TextField idField = new TextField("id", "Customer ID");
        idField.setRequired(true); 
        fieldSet.add(idField);

        HtmlStringBuffer buffer = new HtmlStringBuffer();
        fieldSet.render(buffer);
        String htmlResult = buffer.toString();
        System.out.println("testHtml5NoLayoutRendering() "  + htmlResult);
        // Guard Rails: Assures NO structural wrappers pollute the stream
        assertFalse("No layout mode must not output tables", htmlResult.contains("<table"));
        assertFalse("No layout mode must not output custom div rows", htmlResult.contains("class=\"form-field-group\""));
        assertFalse("No layout mode must not output wrapper divs", htmlResult.contains("class=\"form-control-wrapper\""));
        
        // Verification of Pure, Flat Elements
        assertTrue("Must contain a raw semantic label", htmlResult.contains("<label for=\"modernForm_id\""));
        assertTrue("Must contain the native input field directly", htmlResult.contains("name=\"id\""));
        assertTrue("Must use the baseline required suffix class", htmlResult.contains("class=\"required\""));
        
        // Control Delegation Asserts: Verifies that inner fields are rendered cleanly
        assertTrue("Must output native clean field identifiers", htmlResult.contains("name=\"id\""));
    }
}
