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

        // Assures the default internal property layout value is "table"
        assertEquals(Form.LAYOUT_TABLE, fieldSet.getLayout());

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
    public void testHtml5DivLayoutRendering() {
        // Setup parent Form to avoid NullPointerExceptions during message bundle resolutions
        Form form = new Form("mainForm");
        FieldSet fieldSet = new FieldSet("identityFs", "Identity Info");
        form.add(fieldSet);

        // Activates the newly introduced Fork layout capability
        fieldSet.setLayout(Form.LAYOUT_DIV);
        assertEquals(Form.LAYOUT_DIV, fieldSet.getLayout());

        TextField cpfField = new TextField("cpf", "CPF Document");
        fieldSet.add(cpfField);

        HtmlStringBuffer buffer = new HtmlStringBuffer();
        fieldSet.render(buffer);
        String htmlResult = buffer.toString();

        // 1. Guard Rails: Assures no legacy table elements leaked into the HTML5 buffer pipeline
        assertFalse("DIV mode must not output inner table elements", htmlResult.contains("<table"));
        assertFalse("DIV mode must not output table tr rows", htmlResult.contains("<tr"));
        assertFalse("DIV mode must not output table td cells", htmlResult.contains("<td"));

        // 2. Structural Asserts: Verifies presence of modern semantic layout block containers
        assertTrue("Must contain the custom fieldset content wrapper division", htmlResult.contains("class=\"fieldset-content\""));
        assertTrue("Must contain the outer field group layer", htmlResult.contains("class=\"form-field-group\""));
        assertTrue("Must contain the element input controller wrapper", htmlResult.contains("class=\"form-control-wrapper\""));

        // 3. Control Delegation Asserts: Verifies that inner fields are rendered cleanly
        assertTrue("Must output native clean field identifiers", htmlResult.contains("name=\"cpf\""));
    }
}
