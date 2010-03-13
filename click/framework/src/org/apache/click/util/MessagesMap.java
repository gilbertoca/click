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
package org.apache.click.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.ServletContext;
import org.apache.commons.lang.Validate;

import org.apache.click.Context;
import org.apache.click.service.ConfigService;

/**
 * Provides a localized read only messages Map for Page and Control classes.
 * <p/>
 * A MessagesMap instance is available in each Velocity page using the name
 * "<span class="blue">messages</span>".
 * <p/>
 * For example suppose you have a localized page title, which is stored in the
 * Page's properties file. You can access page "title" message in your page
 * template via:
 *
 * <pre class="codeHtml">
 * <span class="blue">$messages.title</span> </pre>
 *
 * This is roughly equivalent to making the call:
 *
 * <pre class="codeJava">
 * <span class="kw">public void</span> onInit() {
 *    ..
 *    addModel(<span class="st">"title"</span>, getMessage(<span class="st">"title"</span>);
 * } </pre>
 *
 * Please note if the specified message does not exist in your Page's
 * properties file, or if the Page does not have a properties file, then
 * a <tt>MissingResourceException</tt> will be thrown.
 * <p/>
 * The ClickServlet adds a MessagesMap instance to the Velocity Context before
 * it is merged with the page template.
 */
public class MessagesMap implements Map<String, String> {

    /** Cache of resource bundle and locales which were not found. */
    protected static final Set NOT_FOUND_CACHE =
        Collections.synchronizedSet(new HashSet());

    /** Cache of messages keyed by bundleName + Locale name. */
    protected static final Map MESSAGES_CACHE = new HashMap();

    /** The cache key set load lock. */
    protected static final Object CACHE_LOAD_LOCK = new Object();

    // ----------------------------------------------------- Instance Variables

    /** The base class. */
    protected final Class baseClass;

    /** The class global resource bundle base name. */
    protected final String globalBaseName;

    /** The map of localized messages. */
    protected Map messages;

    /** The resource bundle locale. */
    protected final Locale locale;

    // ----------------------------------------------------------- Constructors

    /**
     * Create a resource bundle messages <tt>Map</tt> adaptor for the given
     * object's class resource bundle, the global resource bundle and
     * <tt>Context</tt>.
     * <p/>
     * Messages located in the object's resource bundle will override any
     * messages defined in the global resource bundle.
     *
     * @param baseClass the target class
     * @param globalResource the global resource bundle name
     */
    public MessagesMap(Class baseClass, String globalResource) {
        Validate.notNull(baseClass, "Null object parameter");

        this.baseClass = baseClass;
        this.locale = Context.getThreadLocalContext().getLocale();

        this.globalBaseName = globalResource;
    }

    // --------------------------------------------------------- Public Methods

    /**
     * @see java.util.Map#size()
     */
    public int size() {
        ensureInitialized();
        return messages.size();
    }

    /**
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        ensureInitialized();
        return messages.isEmpty();
    }

    /**
     * @see java.util.Map#containsKey(Object)
     */
    public boolean containsKey(Object key) {
        if (key != null) {
            ensureInitialized();
            return messages.containsKey(key);
        }
        return false;
    }

    /**
     * @see java.util.Map#containsValue(Object)
     */
    public boolean containsValue(Object value) {
        ensureInitialized();
        return messages.containsValue(value);

    }

    /**
     * Return localized resource message for the given key. If the message is
     * not found a <tt>MissingResourceException</tt> will be thrown.
     *
     * @see java.util.Map#get(Object)
     * @throws MissingResourceException if the given key was not found
     */
    public String get(Object key) {
        if (containsKey(key)) {
            return (String) messages.get(key);

        } else {
            String msg =
                "Message \"{0}\" not found in bundle \"{1}\" "
                + "for locale \"{2}\"";
            String keyStr = (key != null) ? key.toString() : null;
            Object[] args = { keyStr, baseClass.getName(), locale };
            msg = MessageFormat.format(msg, args);
            throw new MissingResourceException(msg, baseClass.getName(), keyStr);
        }
    }

    /**
     * This method is not supported and will throw
     * <tt>UnsupportedOperationException</tt> if invoked.
     *
     * @see java.util.Map#put(Object, Object)
     */
    public String put(String key, String value) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported and will throw
     * <tt>UnsupportedOperationException</tt> if invoked.
     *
     * @see java.util.Map#remove(Object)
     */
    public String remove(Object key) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported and will throw
     * <tt>UnsupportedOperationException</tt> if invoked.
     *
     * @see java.util.Map#putAll(Map)
     */
    public void putAll(Map map) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported and will throw
     * <tt>UnsupportedOperationException</tt> if invoked.
     *
     * @see java.util.Map#clear()
     */
    public void clear() {
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.util.Map#keySet()
     */
    public Set keySet() {
        ensureInitialized();
        return messages.keySet();
    }

    /**
     * @see java.util.Map#values()
     */
    public Collection values() {
        ensureInitialized();
        return messages.values();
    }

    /**
     * @see java.util.Map#entrySet()
     */
    public Set entrySet() {
        ensureInitialized();
        return messages.entrySet();
    }

    /**
     * @see #toString()
     */
    public String toString() {
        ensureInitialized();
        return messages.toString();
    }

    // ------------------------------------------------------ Protected Methods

    private void ensureInitialized() {
        if (messages == null) {

            CacheKey resourceKey = new CacheKey(globalBaseName,
                baseClass.getName(), locale.toString());

            messages = (Map) MESSAGES_CACHE.get(resourceKey);

            if (messages != null) {
                return;
            }

            messages = new HashMap();

            synchronized (CACHE_LOAD_LOCK) {

                loadResourceValuesIntoMap(globalBaseName, messages);

                List classnameList = new ArrayList();

                // Build class list
                Class aClass = baseClass;
                while (!aClass.getName().equals("java.lang.Object")) {
                    classnameList.add(aClass.getName());
                    aClass = aClass.getSuperclass();
                }

                // Load messages from parent to child order, so that child
                // class messages override parent messages.
                for (int i = classnameList.size() - 1; i >= 0; i--) {
                    String className = (String) classnameList.get(i);
                    loadResourceValuesIntoMap(className, messages);
                }

                messages = Collections.unmodifiableMap(messages);

                ServletContext servletContext = Context.getThreadLocalContext().getServletContext();
                ConfigService configService = ClickUtils.getConfigService(servletContext);
                if (configService.isProductionMode() || configService.isProfileMode()) {
                    MESSAGES_CACHE.put(resourceKey, messages);
                }
            }
        }
    }

    private void loadResourceValuesIntoMap(String resourceName, Map map) {
        if (resourceName == null) {
            return;
        }

        String resourceKey = resourceName + locale.toString();

        if (!NOT_FOUND_CACHE.contains(resourceKey)) {
            try {
                ResourceBundle resources = ClickUtils.getBundle(resourceName, locale);

                Enumeration e = resources.getKeys();
                while (e.hasMoreElements()) {
                    String name = e.nextElement().toString();
                    String value = resources.getString(name);
                    map.put(name, value);
                }

            } catch (MissingResourceException mre) {
                NOT_FOUND_CACHE.add(resourceKey);
            }
        }
    }

    /**
     * See DRY Performance article by Kirk Pepperdine.
     * <p/>
     * http://www.javaspecialists.eu/archive/Issue134.html
     */
    private static class CacheKey {

        /** Global base name to encapsulate in cache key. */
        private final String globalBaseName;

        /** Base class name to encapsulate in cache key. */
        private final String baseClass;

        /** Locale to encapsulate in cache key. */
        private final String locale;

        /**
         * Constructs a new CacheKey for the given baseName, baseClass and
         * locale.
         *
         * @param globalBaseName the base name to build the cache key for
         * @param baseClass the base class name to build the cache key for
         * @param locale the request locale to build the cache key for
         */
        public CacheKey(String globalBaseName, String baseClass, String locale) {
            if (globalBaseName == null) {
                throw new IllegalArgumentException("Null globalBaseName parameter");
            }
            if (baseClass == null) {
                throw new IllegalArgumentException("Null baseClass parameter");
            }
            if (locale == null) {
                throw new IllegalArgumentException("Null locale parameter");
            }
            this.globalBaseName = globalBaseName;
            this.baseClass = baseClass;
            this.locale = locale;
        }

        /**
         * @see Object#equals(Object)
         *
         * @param o the object with which to compare this instance with
         * @return true if the specified object is the same as this object
         */
        @Override
        public final boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            CacheKey that = (CacheKey) o;

            if (!globalBaseName.equals(that.globalBaseName)) {
                return false;
            }

            if (!baseClass.equals(that.baseClass)) {
                return false;
            }

            if (!locale.equals(that.locale)) {
                return false;
            }

            return true;
        }

        /**
         * @see Object#hashCode()
         *
         * @return a hash code value for this object.
         */
        @Override
        public final int hashCode() {
            return globalBaseName.hashCode()
                * 31 + baseClass.hashCode()
                * 31 + locale.hashCode();
        }
    }
}
