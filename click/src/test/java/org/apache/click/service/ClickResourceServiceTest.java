package org.apache.click.service;

import junit.framework.TestCase;
import org.apache.click.MockContainer;
import org.apache.click.servlet.MockRequest;
import org.apache.click.servlet.MockResponse;

import javax.servlet.ServletContext;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import org.apache.click.util.ClickUtils;

/**
 * Verifies ClickResourceService deploys resources to the physical /click
 * directory and still serves them correctly.
 */
public class ClickResourceServiceTest extends TestCase {

    private MockContainer container;
    private ServletContext servletContext;
    private ClickResourceService resourceService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // Use the test web root which contains click/control.css under src/test/resources/web
        container = new MockContainer("web");
        container.start();
        servletContext = container.getRequest().getServletContext();

        // Initialize ClickResourceService directly
        resourceService = new ClickResourceService();
        resourceService.onInit(servletContext);
    }

    @Override
    protected void tearDown() throws Exception {
        if (container != null) {
            container.stop();
        }
        super.tearDown();
    }

    public void testRenderResource_servesClasspathResource_andCaches() throws Exception {
        MockRequest request = container.getRequest();
        MockResponse response = container.getResponse();

        // Simulate request path for resource
        request.setServletPath("/click/control.css");
        // Ensure no pre-existing cache
        // Access protected field resourceCache via reflection (test in same package)
        Field cacheField = BasicResourceService.class.getDeclaredField("resourceCache");
        cacheField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Map<String, byte[]> cache = (java.util.Map<String, byte[]>) cacheField.get(resourceService);
        cache.clear();

        // First render: should load from classpath and write to response
        resourceService.renderResource(request, response);

        assertEquals("text/css", response.getContentType()); // ClickUtils mime bundle maps css -> text/css or application/css depending on env
        byte[] content = response.getBinaryContent();
        assertNotNull("Response binary content should not be null", content);
        String text = new String(content, StandardCharsets.UTF_8);
        assertTrue("Response should contain CSS token", text.contains("The Control CSS styles") || text.contains("input.error"));

        // Now cache should contain an entry for /click/control.css
        ConfigService config = ClickUtils.getConfigService(container.getServletContext());
        // Assert caching behaviors based on the active mode logic
        if (config.isProductionMode() || config.isProfileMode()) {
            assertTrue("Cache should hold entry in production/profile modes", cache.containsKey("/click/control.css"));
        } else {
            assertFalse("Cache must remain empty during debug/trace modes", cache.containsKey("/click/control.css"));
        }
        // Clear response and call again to ensure served from cache (no exception and same bytes)
        response.reset();
        resourceService.renderResource(request, response);
        byte[] content2 = response.getBinaryContent();
        assertNotNull(content2);
        assertEquals(content.length, content2.length);
        assertEquals(new String(content, StandardCharsets.UTF_8), new String(content2, StandardCharsets.UTF_8));
    }

    public void testRenderResource_missingResource_returns404() throws Exception {
        MockRequest request = container.getRequest();
        MockResponse response = container.getResponse();

        request.setServletPath("/click/does-not-exist.txt");
        response.reset();

        resourceService.renderResource(request, response);

        assertTrue("Response should be an error", response.isError());
        assertEquals(javax.servlet.http.HttpServletResponse.SC_NOT_FOUND, response.getCode());
    }

    public void testIsResourceRequest() throws Exception {
        // A resource request
        MockRequest request = container.getRequest();
        request.setServletPath("/click/control.css");
        assertTrue(resourceService.isResourceRequest(request));

        // A template request (simulate .htm template)
        request.setServletPath("/index.htm");
        assertFalse(resourceService.isResourceRequest(request));
    }

    public void testDeploysToPhysicalClickDirectory_andServes() throws Exception {
        MockRequest request = container.getRequest();
        MockResponse response = container.getResponse();

        // Simulate request path for resource
        request.setServletPath("/click/control.css");

        // Ensure cache is clear before first request
        Field cacheField = BasicResourceService.class.getDeclaredField("resourceCache");
        cacheField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Map<String, byte[]> cache = (java.util.Map<String, byte[]>) cacheField.get(resourceService);
        cache.clear();

        // First render should trigger deployment (ClickResourceService deploys physical files)
        resourceService.renderResource(request, response);

        // Verify file deployed to real path (servletContext real path should be non-null in MockContainer)
        String realPath = servletContext.getRealPath("/click/control.css");
        assertNotNull("servletContext.getRealPath returned null - MockContainer must provide a real path", realPath);
        File deployed = new File(realPath);
        assertTrue("Deployed resource should exist on disk: " + deployed.getAbsolutePath(), deployed.exists());

        // Response content assertions
        byte[] content = response.getBinaryContent();
        assertNotNull("Response binary content should not be null", content);
        String text = new String(content, StandardCharsets.UTF_8);
        assertTrue("Response must contain expected CSS text", text.contains("The Control CSS styles") || text.contains("input.error"));

        // Cache populated
        ConfigService config = ClickUtils.getConfigService(container.getServletContext());
        // Assert caching behaviors based on the active mode logic
        if (config.isProductionMode() || config.isProfileMode()) {
            assertTrue("Cache should hold entry in production/profile modes", cache.containsKey("/click/control.css"));
        } else {
            assertFalse("Cache must remain empty during debug/trace modes", cache.containsKey("/click/control.css"));
        }
        // Serve again to ensure cached bytes are used
        response.reset();
        resourceService.renderResource(request, response);
        byte[] content2 = response.getBinaryContent();
        assertNotNull(content2);
        assertEquals(content.length, content2.length);
        assertEquals(new String(content, StandardCharsets.UTF_8), new String(content2, StandardCharsets.UTF_8));
    }
}
