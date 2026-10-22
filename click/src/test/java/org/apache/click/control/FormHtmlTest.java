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
import org.apache.click.servlet.MockRequest;
import org.apache.click.util.HtmlStringBuffer;

/**
 * Dedicated test suite validating semantic HTML/DOM output specifications
 * for the Form component introduced in the JDK 17 Fork.
 */
public class FormHtmlTest extends TestCase {

    /**
     * Validates that the default Form behavior remains strictly table-based,
     * guaranteeing total backward compatibility for existing applications.
     */
    public void testDefaultLayoutTableRendering() {
        // Initializing the context to populate ThreadLocal mock servlet stacks
        MockContext.initContext("test-form.htm");
        
        Form form = new Form("legacyForm");
        
        // Assures the default internal property layout value is false "table layout"
        assertEquals(form.isNoLayout(), false);
        
        form.add(new TextField("username", "User"));
        
        HtmlStringBuffer buffer = new HtmlStringBuffer();
        form.render(buffer);
        String htmlResult = buffer.toString();
        System.out.println("testDefaultLayoutTableRendering() "  + htmlResult);        
        // Assures the historical table markup is outputted correctly
        assertTrue("Default layout must contain a table element", htmlResult.contains("<table class=\"form\""));
        assertTrue("Default layout must contain tr table rows", htmlResult.contains("<tr"));
        assertTrue("Default layout must contain td table cells", htmlResult.contains("<td"));
    }

    /**
     * Verifies that explicitly activating setNoLayout(true) outputs flat, native 
     * semantic HTML5 markup completely free of forced wrapper classes or tables.
     */
    public void testHtml5NoLayoutRendering() {
        MockContext.initContext("test-form.htm");
        Form form = new Form("modernForm");
        
        // 1. Update flag to verify your new WHATWG zero-layout toggle
        form.setNoLayout(true);
        assertTrue(form.isNoLayout());
        
        TextField nameField = new TextField("name", "Full Name");
        nameField.setRequired(true); 
        form.add(nameField);
        
        HtmlStringBuffer buffer = new HtmlStringBuffer();
        form.render(buffer);
        String htmlResult = buffer.toString();
        
        // Guard Rails: Assures NO structural wrappers pollute the stream
        assertFalse("No layout mode must not output tables", htmlResult.contains("<table"));
        assertFalse("No layout mode must not output custom div rows", htmlResult.contains("class=\"form-field-group\""));
        assertFalse("No layout mode must not output wrapper divs", htmlResult.contains("class=\"form-control-wrapper\""));
        
        System.out.println("testHtml5NoLayoutRendering() "  + htmlResult);
        // Verification of Pure, Flat Elements
        assertTrue("Must contain a raw semantic label", htmlResult.contains("<label for=\"modernForm_name\""));
        assertTrue("Must contain the native input field directly", htmlResult.contains("name=\"name\""));
        assertTrue("Must use the baseline required suffix class", htmlResult.contains("class=\"required\""));
    }

    /**
     * Validates that field validation errors append raw baseline error blocks
     * directly beneath fields without leaking table or division structures.
     */
    public void testHtml5ErrorsAndButtonsFlatRendering() {
        MockContext context = MockContext.initContext("test-form.htm");
        MockRequest request = context.getMockRequest();
        
        request.setMethod("POST");
        request.setParameter(Form.FORM_NAME, "modernForm");
        
        Form form = new Form("modernForm");
        form.setNoLayout(true); // Active the new flat execution engine
        
        TextField emailField = new TextField("email", "Email");
        emailField.setRequired(true);
        form.add(emailField);
        
        Submit submitButton = new Submit("save", "Save");
        form.add(submitButton);
        
        request.setParameter("modernForm_save", "Save");
        request.setParameter("email", ""); // Emits blank parameter to trigger field required validation
        
        form.onProcess();
        assertFalse(form.isValid());
        
        HtmlStringBuffer buffer = new HtmlStringBuffer();
        form.render(buffer);
        String htmlResult = buffer.toString();
        System.out.println("testHtml5ErrorsAndButtonsFlatRendering() "  + htmlResult);
        // Assert that the global/field error blocks leverage native tags and baseline classes
        assertFalse(htmlResult.contains("class=\"form-errors-container\""));
        assertTrue("Form error must use native span error class", htmlResult.contains("<span class=\"error\""));
        assertTrue("Field error must match baseline inline format", htmlResult.contains("<span class=\"error\">"));
        assertTrue("Accessibility anchors must remain active", htmlResult.contains("href=\"javascript:"));
        
        // Assert that button bars render sequentially without structural containers
        assertFalse(htmlResult.contains("class=\"form-actions-bar\""));
        assertTrue("Must output clean native submit tag", htmlResult.contains("type=\"submit\" name=\"save\""));
    }

    /**
     * Memory Leak & Thread-Local Concurrency Check.
     * Guarantees that dynamically swapping layout architectures or executing continuous render
     * passes does not cause component accumulation, tracking pollution, or internal state drift.
     */
    public void testLayoutStateIsolation() {
        // Initializing the context to populate ThreadLocal mock servlet stacks
        MockContext.initContext("test-form.htm");

        Form form = new Form("stateForm");
        form.add(new TextField("data"));
        
        // Execution Pass A: Forces classic table rendering structure
        HtmlStringBuffer legacyBuffer = new HtmlStringBuffer();
        form.render(legacyBuffer);
        int totalControlsLegacy = form.getControls().size();
        
        // Execution Pass B: Shifts layout strategy to DIV and renders instantly on the same thread
        form.setNoLayout(true);
        HtmlStringBuffer modernBuffer = new HtmlStringBuffer();
        form.render(modernBuffer);
        int totalControlsModern = form.getControls().size();
        
        // Internal collection trackers must stay immutable under rendering side-effects
        assertEquals("Rendering multiple layout sequences must not artificially inflate internal tracking lists",
                totalControlsLegacy, totalControlsModern);
        
        // The form must hold exactly 2 elements: 1 Default HiddenField (FORM_NAME tracking key) + 1 TextField
        assertEquals(2, form.getControls().size());
    }
}
