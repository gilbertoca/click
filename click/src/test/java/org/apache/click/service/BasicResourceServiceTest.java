package org.apache.click.service;

import java.io.InputStream;
import junit.framework.TestCase;
import org.apache.click.MockContainer;
import org.apache.click.servlet.MockResponse;
import org.apache.click.servlet.MockRequest;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import org.apache.click.servlet.MockServletContext;

/**
 * Verifies BasicResourceService will serve resources packaged on the classpath
 * (META-INF/resources) when the servlet context/webapp does not expose them.
 *
 * This test uses a tiny ServletContext stub that intentionally returns null for
 * getResourceAsStream/getRealPath so BasicResourceService must fall back to the
 * ClassLoader-based lookup (META-INF/resources).
 */
public class BasicResourceServiceTest extends TestCase {

    private MockContainer container;
    private MockServletContext servletContext;
    private BasicResourceService resourceService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // Use a stub servlet context that does NOT expose /click/control.css
        servletContext = new org.apache.click.servlet.MockServletContext() {
            @Override
            public InputStream getResourceAsStream(String path) {
                // Force servlet context to not expose the webapp resource so BasicResourceService falls back
                if ("/click/control.css".equals(path)) {
                    return null;
                }
                return super.getResourceAsStream(path);
            }

            @Override
            public String getRealPath(String path) {
                // Return null to indicate no physical file in webapp
                if ("/click/control.css".equals(path)) {
                    return null;
                }
                return super.getRealPath(path);
            }
        };
        container = new MockContainer("web");
        container.setServletContext(servletContext);
        container.start();
        resourceService = new BasicResourceService();
        resourceService.onInit(servletContext);
    }

    @Override
    protected void tearDown() throws Exception {
        if (container != null) {
            container.stop();
        }
        super.tearDown();
    }

    public void testServesFromClasspathMetaInfResources_andCaches() throws Exception {
        // Create request/response using your MockRequest/MockResponse classes
        MockRequest request = new MockRequest();
        MockResponse response = new MockResponse();

        // Attach our stub context to the MockRequest (MockRequest has a setter in recent changes)
        request.setServletContext(servletContext);

        request.setServletPath("/click/control.css");

        // Clear cache
        Field cacheField = BasicResourceService.class.getDeclaredField("resourceCache");
        cacheField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Map<String, byte[]> cache = (java.util.Map<String, byte[]>) cacheField.get(resourceService);
        cache.clear();

        // Render - should read from classpath (META-INF/resources/click/control.css)
        resourceService.renderResource(request, response);

        byte[] content = response.getBinaryContent();
        assertNotNull("Response content should not be null when resource is on classpath", content);
        String text = new String(content, StandardCharsets.UTF_8);
        assertTrue("Classpath-served CSS should contain expected text", text.contains("The Control CSS styles") || text.contains("input.error"));

        // Now cached
        assertTrue("resourceCache should contain entry for /click/control.css", cache.containsKey("/click/control.css"));
    }
}
