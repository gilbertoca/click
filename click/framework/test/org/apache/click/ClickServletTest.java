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
package org.apache.click;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.click.control.Form;
import org.apache.click.pages.ListenerPage;

/**
 * Test ClickServlet behavior.
 * 
 * @author Bob Schellink
 */
public class ClickServletTest extends TestCase {

    /**
     * Assert the ListenerPage that the Submit button listener is invoked *after*
     * the TextField value was bound to the request value.
     *
     * CLK-365.
     */
    public void testRegisterListener() {
        MockContainer container = new MockContainer("web");
        container.start();
        container.setParameter(Form.FORM_NAME, "form"); // Simulate form submitted
        container.setParameter("submit", "submit"); // Simulate Submit button clicked
        container.setParameter("field", "one"); // Simulate TextField value set

        ListenerPage page = (ListenerPage) container.testPage(ListenerPage.class);

        // assert that the Page did successfully execute
        Assert.assertTrue(page.success);
        container.stop();
    }
}