package org.apache.click.service;

import java.io.InputStream;
import junit.framework.TestCase;
import org.apache.click.MockContainer;
import org.apache.click.servlet.MockResponse;
import org.apache.click.servlet.MockRequest;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import org.apache.click.servlet.MockServletContext;
import org.apache.click.util.ClickUtils;

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
        // 1. Create your custom stub servlet context
        servletContext = new org.apache.click.servlet.MockServletContext() {
            @Override
            public InputStream getResourceAsStream(String path) {
                if ("/click/control.css".equals(path)) {
                    return null; // Force classpath fallback
                }
                return super.getResourceAsStream(path);
            }

            @Override
            public String getRealPath(String path) {
                if ("/click/control.css".equals(path)) {
                    return null; // Indicate no physical file in webapp
                }
                return super.getRealPath(path);
            }
        };

        // 2. Setup the container with the stubbed context
        container = new MockContainer("web");
        container.setServletContext(servletContext);
        container.start();

        // 3. Initialize your target service under test
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

        // Force GET method so the ResourceService processes and caches the asset
        request.setMethod("GET");
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
        ConfigService config = ClickUtils.getConfigService(container.getServletContext());
        // Assert caching behaviors based on the active mode logic
        if (config.isProductionMode() || config.isProfileMode()) {
            assertTrue("Cache should hold entry in production/profile modes", cache.containsKey("/click/control.css"));
        } else {
            assertFalse("Cache must remain empty during debug/trace modes", cache.containsKey("/click/control.css"));
        }
    }
}
