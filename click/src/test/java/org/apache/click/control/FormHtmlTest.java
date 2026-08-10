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
        
        // Assures the default internal property layout value is "table"
        assertEquals(Form.LAYOUT_TABLE, form.getLayout());
        
        form.add(new TextField("username", "User"));
        
        HtmlStringBuffer buffer = new HtmlStringBuffer();
        form.render(buffer);
        String htmlResult = buffer.toString();
        
        // Assures the historical table markup is outputted correctly
        assertTrue("Default layout must contain a table element", htmlResult.contains("<table class=\"form\""));
        assertTrue("Default layout must contain tr table rows", htmlResult.contains("<tr"));
        assertTrue("Default layout must contain td table cells", htmlResult.contains("<td"));
    }

    /**
     * Verifies that explicitly switching to DIV layout strategy dynamically updates
     * the DOM tree into clean block elements, completely eliminating any legacy table artifacts.
     */
    public void testHtml5DivLayoutRendering() {
        // Initializing the context to populate ThreadLocal mock servlet stacks
        MockContext.initContext("test-form.htm");
        
        Form form = new Form("modernForm");
        
        // Activates the newly introduced Fork layout capability
        form.setLayout(Form.LAYOUT_DIV);
        assertEquals(Form.LAYOUT_DIV, form.getLayout());
        
        TextField nameField = new TextField("name", "Full Name");
        nameField.setRequired(true); // Triggers the input required verification badge
        form.add(nameField);
        
        HtmlStringBuffer buffer = new HtmlStringBuffer();
        form.render(buffer);
        String htmlResult = buffer.toString();
        
        // 1. Guard Rails: Assures no legacy table elements leaked into the HTML5 buffer pipeline
        assertFalse("DIV mode must not output table opening elements", htmlResult.contains("<table"));
        assertFalse("DIV mode must not output table tr rows", htmlResult.contains("<tr"));
        assertFalse("DIV mode must not output table td cells", htmlResult.contains("<td"));
        
        // 2. Structural Asserts: Verifies presence of modern semantic layout block containers
        assertTrue("Must contain the primary container block division", htmlResult.contains("class=\"click-form\""));
        assertTrue("Must contain the specialized outer field group division", htmlResult.contains("class=\"form-field-group\""));
        assertTrue("Must contain the internal input wrapper structural division", htmlResult.contains("class=\"form-control-wrapper\""));
        
        // 3. Accessibility Asserts: Verifies W3C label compliance ('for' attribute matching element ID)
        assertTrue("Label element must map cleanly to the targeted field ID", htmlResult.contains("for=\"modernForm_name\""));
        
        // 4. Localization Asserts: Assures required marker token uses localized asset property maps (*)
        assertTrue("Must render the required form badge structure", htmlResult.contains("class=\"form-required-marker\""));
    }

    /**
     * Verifies that errors and buttons are correctly mapped to modern block layouts,
     * maintaining javascript-focus accessibility hooks without leaking legacy tables.
     */
    public void testHtml5ErrorsAndButtonsRendering() {
        // Initializing context with a specific virtual page layout matching Click target bounds
        MockContext context = MockContext.initContext("test-form.htm");
        MockRequest request = context.getMockRequest();
        
        // Simulates a real POST submission targeting the form container
        request.setMethod("POST");
        request.setParameter(Form.FORM_NAME, "modernForm");
        
        Form form = new Form("modernForm");
        form.setLayout(Form.LAYOUT_DIV);
        
        TextField emailField = new TextField("email", "Email");
        emailField.setRequired(true); 
        form.add(emailField);
        
        Submit submitButton = new Submit("save", "Save");
        form.add(submitButton);
        
        // Binds the active button token sequence to mock execution pass
        request.setParameter("modernForm_save", "Save");
        
        // CRITICAL FIX: Based on TextFieldTest.java, Apache Click requires the bound field key 
        // to be present as an explicit parameter (even if empty) to trigger validation processing rules.
        request.setParameter("email", "");
        
        // Process lifecycle execution to trigger field required validation evaluation
        form.onProcess();
        
        // The mock request parameters are now fully active, forcing the form state to fail validation
        assertFalse("Form validation state must be invalid due to missing required field data", form.isValid());
        
        HtmlStringBuffer buffer = new HtmlStringBuffer();
        form.render(buffer);
        String htmlResult = buffer.toString();
        
        // 1. Errors Verification
        assertFalse("Errors block must not wrap in tables", htmlResult.contains("<table class=\"errors\""));
        assertTrue("Errors block must use the new HTML5 container structure", htmlResult.contains("class=\"form-errors-container\""));
        assertTrue("Must maintain accessibility anchors pointing to fields focus routines", htmlResult.contains("href=\"javascript:"));
        assertTrue("Must display field-level validation tags adjacent to control-wrappers", htmlResult.contains("class=\"form-field-error\""));
        
        // 2. Buttons Verification
        assertTrue("Actions area must render using clean button bars", htmlResult.contains("class=\"form-actions-bar\""));
        assertTrue("Must output native clean submit tags", htmlResult.contains("type=\"submit\" name=\"save\""));
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
        form.setLayout(Form.LAYOUT_DIV);
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
