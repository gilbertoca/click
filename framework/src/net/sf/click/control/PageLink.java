/*
 * Copyright 2004-2006 Malcolm A. Edgar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.click.control;

import java.util.Iterator;
import java.util.Map;

import net.sf.click.util.HtmlStringBuffer;

public class PageLink extends AbstractLink {

    private static final long serialVersionUID = 1L;

    // ----------------------------------------------------- Instance Variables

    /** The target page class. */
    protected Class pageClass;

    // ----------------------------------------------------------- Constructors

    /**
     * Create an PageLink for the given name.
     *
     * @param name the page link name
     * @throws IllegalArgumentException if the name is null
     */
    public PageLink(String name) {
        setName(name);
    }

    /**
     * Create an PageLink for the given name and target Page class.
     *
     * @param name the page link name
     * @param targetPage the target page class
     * @throws IllegalArgumentException if the name is null
     */
    public PageLink(String name, Class targetPage) {
        setName(name);
        if (targetPage == null) {
            throw new IllegalArgumentException("Null targetPage parameter");
        }
        pageClass = targetPage;
    }

    /**
     * Create an PageLink with no name defined, <b>please note</b> the
     * control's name must be defined before it is valid.
     * <p/>
     * <div style="border: 1px solid red;padding:0.5em;">
     * No-args constructors are provided for Java Bean tools support and are not
     * intended for general use. If you create a control instance using a
     * no-args constructor you must define its name before adding it to its
     * parent. </div>
     */
    public PageLink() {
    }

    // ------------------------------------------------------ Public Attributes

    /**
     * Return the PageLink anchor &lt;a&gt; tag href attribute.
     * This method will encode the URL with the session ID
     * if required using <tt>HttpServletResponse.encodeURL()</tt>.
     *
     * @return the PageLink HTML href attribute
     */
    public String getHref() {
        if (getPageClass() == null) {
            throw new IllegalStateException("target pageClass is not defined");
        }

        HtmlStringBuffer buffer = new HtmlStringBuffer();

        buffer.append(getContext().getRequest().getContextPath());

        String pagePage = getContext().getPagePath(getPageClass());

        buffer.append(pagePage);

        if (hasParameters()) {
            buffer.append("?");

            Iterator i = getParameters().entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry entry = (Map.Entry) i.next();
                String name = entry.getKey().toString();
                String value = entry.getValue().toString();

                buffer.append(name);
                buffer.append("=");
                buffer.append(value);
                if (i.hasNext()) {
                    buffer.append("&");
                }
            }
        }

        return getContext().getResponse().encodeURL(buffer.toString());
    }

    /**
     * This method does nothing.
     *
     * @see net.sf.click.Control#setListener(Object, String)
     *
     * @param listener the listener object with the named method to invoke
     * @param method the name of the method to invoke
     */
    public void setListener(Object listener, String method) {
    }

    public Class getPageClass() {
        return pageClass;
    }

    public void setPageClass(Class targetPage) {
        pageClass = targetPage;
    }

    // --------------------------------------------------------- Public Methods

    /**
     * This method will return true.
     *
     * @see net.sf.click.Control#onProcess()
     *
     * @return true
     */
    public boolean onProcess() {
        return true;
    }

}
