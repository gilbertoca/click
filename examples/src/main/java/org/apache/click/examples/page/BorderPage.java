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
package org.apache.click.examples.page;

import org.apache.click.Page;
import org.apache.click.extras.control.Menu;
import org.apache.click.extras.control.MenuFactory;
import org.apache.click.util.ClickUtils;

/**
 * Provides a page border template. This Page returns the template
 * <code>"border-template.htm"</code>, and sets the Page model values <code>$title</code> and
 * <code>$srcPath</code>.
 * <p/>
 * Please note this page is designed for extending by Page subclasses and will
 * not be auto mapped as the template name <code>"border-template.htm"</code> does
 * not match the Pages class name <code>BorderPage</code>.
 */
public class BorderPage extends Page {

    private static final long serialVersionUID = 1L;

    /**
     * The root menu. Note this transient variable is reinitialized in onInit()
     * to support serialized stateful pages.
     */
    private transient Menu rootMenu;

    // Constructor ------------------------------------------------------------

    /**
     * Create a BorderedPage and set the model attributes <code>$title</code> and
     * <code>$srcPath</code>.
     * <ul>
     * <li><code>$title</code> &nbsp; - &nbsp; the Page title from classname</li>
     * <li><code>$srcPath</code> &nbsp; - &nbsp; the Page Java source path</li>
     * </ul>
     */
    public BorderPage() {
        String className = getClass().getName();

        String shortName = className.substring(className.lastIndexOf('.') + 1);
        String title = ClickUtils.toLabel(shortName);
        addModel("title", title);

        String srcPath = className.replace('.', '/') + ".java";
        addModel("srcPath", srcPath);
    }

    // Event Handlers ---------------------------------------------------------

    /**
     * @see org.apache.click.Page#onInit()
     */
    @Override
    public void onInit() {
        super.onInit();

        MenuFactory menuFactory = new MenuFactory();
        rootMenu = menuFactory.getRootMenu();

        // Add rootMenu to Page control list. Note: rootMenu is removed in Page
        // onDestroy() to ensure rootMenu is not serialized
        addControl(rootMenu);
    }

    /**
     * @see org.apache.click.Page#onDestroy()
     */
    @Override
    public void onDestroy() {
        // Remove menu for when BorderPage is serialized
        if (rootMenu != null) {
            removeControl(rootMenu);
        }
    }

    // Public Methods ---------------------------------------------------------

    /**
     * Returns the name of the border template: &nbsp; <code>"/border-template.htm"</code>
     * <p/>
     * Please note this page is designed for extending by Page subclasses and will
     * not be auto mapped as the template name <code>"border-template.htm"</code> does
     * not match the Pages class name <code>BorderPage</code>.
     *
     * @see org.apache.click.Page#getTemplate()
     */
    @Override
    public String getTemplate() {
        return "/border-template.htm";
    }

}
