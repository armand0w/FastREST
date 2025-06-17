package com.armandow.rest;

import com.armandow.rest.enums.CharsetType;
import com.armandow.rest.enums.ContentType;
import com.armandow.rest.enums.MethodType;
import com.armandow.rest.exceptions.AuthorizationException;
import com.armandow.rest.exceptions.CustomException;
import com.armandow.rest.exceptions.OperationExecutionException;
import com.armandow.rest.exceptions.OperationNotFoundException;
import com.armandow.rest.http.RESTClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RESTMethodsTest {
    private static Tomcat tomcat;
    private static final int PORT = 9999;
    private static final String APP = "fastRestApp";
    private static final String LOCALHOST = "http://localhost";
    private static String base;

    @BeforeAll
    static void beforeAll() throws LifecycleException, IOException {
        var tempDir = Files.createTempDirectory("tomcat-temp");

        tomcat = new Tomcat();
        tomcat.setPort(PORT);
        tomcat.setBaseDir(tempDir.toString());
        tomcat.getConnector();

        var ctx = tomcat.addContext("/" + APP, new File(".").getAbsolutePath());
        Tomcat.addServlet(ctx, "RESTApp", new RESTApplication());
        ctx.addServletMappingDecoded("/*", "RESTApp");

        tomcat.start();
        base = LOCALHOST + ":" + PORT + "/" + APP;
        log.info("Test start FastREST: {}", base);
    }

    @AfterAll
    static void afterAll() throws LifecycleException {
        tomcat.stop();
        tomcat.destroy();
        log.info("End test FastREST");
    }

    @Test
    @Order(1)
    void testRestClientHttps() throws OperationNotFoundException, AuthorizationException, OperationExecutionException, CustomException, IOException, InterruptedException {
        var headers = new HashMap<String, String>();
        headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:108.0) Gecko/20100101 Firefox/140.0");

        var resp = RESTClient.execute("https://api.ipify.org?format=json", MethodType.GET, ContentType.TEXT_PLAIN, CharsetType.UTF_8, null, headers, ContentType.APPLICATION_JSON);
        log.trace(resp.toString());

        assertNotNull(resp);
    }

    @Test
    @Order(2)
    void testGET() throws AuthorizationException, OperationExecutionException, OperationNotFoundException, CustomException, IOException, InterruptedException {
        var resp = RESTClient.execute(base + "/methods/get", MethodType.GET, ContentType.TEXT_PLAIN, CharsetType.UTF_8, null, null, ContentType.TEXT_PLAIN);

        assertNotNull(resp);
        assertEquals("ola ke ase!", resp);
    }

    @Test
    @Order(3)
    void testPOST() throws AuthorizationException, OperationExecutionException, OperationNotFoundException, CustomException, IOException, InterruptedException {
        var resp = (JSONObject) RESTClient.execute(base + "/methods/post", MethodType.POST, ContentType.APPLICATION_JSON, CharsetType.UTF_8, "{}", null, ContentType.APPLICATION_JSON);

        assertNotNull(resp);
        assertEquals(new JSONObject().toString(), resp.toString());
    }

    @Test
    @Order(4)
    void testPUT() throws AuthorizationException, OperationExecutionException, OperationNotFoundException, CustomException, IOException, InterruptedException {
        var resp = (JSONObject) RESTClient.execute(base + "/methods/put", MethodType.PUT, ContentType.APPLICATION_JSON, CharsetType.UTF_8, "{}", null, ContentType.APPLICATION_JSON);

        assertNotNull(resp);
        assertEquals(new JSONObject().toString(), resp.toString());
    }

    @Test
    @Order(5)
    void testPathParam() throws OperationNotFoundException, AuthorizationException, OperationExecutionException, CustomException, IOException, InterruptedException {
        var resp = (byte[]) RESTClient.execute(base + "/params/txt/test.txt", MethodType.GET, ContentType.TEXT_PLAIN, CharsetType.UTF_8, null, null, ContentType.APPLICATION_OCTET_STREAM);

        assertNotNull(resp);
        assertEquals("ola ke ase!!!", new String(resp).trim());
    }

    @Test
    @Order(6)
    void testContentParamJSON() throws Exception {
        var resp = (JSONObject) RESTClient.execute(base + "/params/contentParam", MethodType.POST, ContentType.APPLICATION_JSON, CharsetType.UTF_8, new JSONObject().put("saludo", "ola ke ase!!!"), null, ContentType.APPLICATION_JSON);

        assertNotNull(resp);
        var value = resp.optQuery("/saludo");
        assertNotNull(value);
        assertNotNull(value.toString());
        assertInstanceOf(String.class, value);
        assertEquals("ola ke ase!!!", value.toString());

        var exception = assertThrows(OperationExecutionException.class, () -> RESTClient.execute(base + "/params/contentParam", MethodType.POST, ContentType.APPLICATION_JSON, CharsetType.UTF_8, "No JSON", null, ContentType.APPLICATION_JSON));

        assertNotNull(exception);
        assertEquals(409, exception.getHTTPStatus());
        assertTrue(exception.getMessage().startsWith("Bad request"));
        assertTrue(exception.getData().toString().startsWith("A JSONObject text must begin with '{'"));
    }

    @Test
    @Order(7)
    void testContentParamText() throws Exception {
        var resp = RESTClient.execute(base + "/params/contentParamText", MethodType.POST, ContentType.TEXT_PLAIN, CharsetType.UTF_8, "ola ke ase!!!", null, ContentType.TEXT_PLAIN);

        assertNotNull(resp);
        assertEquals("ola ke ase!!!", resp);
    }

    @Test
    @Order(8)
    void testAuthException() {
        var exception = assertThrows(AuthorizationException.class, () -> RESTClient.execute(base + "/exceptions/auth", MethodType.POST, ContentType.APPLICATION_JSON, CharsetType.UTF_8, "{}", null, ContentType.APPLICATION_XML));

        assertNotNull(exception);
        assertEquals(401, exception.getHTTPStatus());
        assertEquals("Test AuthorizationException", exception.getData());
    }

    @Test
    @Order(9)
    void testOperationNotFound() {
        var exception = assertThrows(OperationNotFoundException.class, () -> RESTClient.execute(base + "/exceptions/384765834765", MethodType.POST, ContentType.APPLICATION_ZIP, CharsetType.UTF_8, "{}", null, ContentType.APPLICATION_XML));

        assertNotNull(exception);
        assertEquals(405, exception.getHTTPStatus());
        assertTrue(exception.getMessage().startsWith("Method Not Allowed"));
        assertTrue(exception.getData().toString().startsWith("Operation not allowed"));
    }

    @Test
    @Order(10)
    void testContentException() {
        var exception = assertThrows(OperationExecutionException.class, () -> RESTClient.execute(base + "/exceptions/content", MethodType.POST, ContentType.APPLICATION_JSON, CharsetType.UTF_8, "{}", null, ContentType.APPLICATION_JSON));

        assertNotNull(exception);
        assertEquals(409, exception.getHTTPStatus());
        assertEquals(new JSONObject().toString(), exception.getData().toString());
    }

    @Test
    @Order(11)
    void testServerParameters() throws OperationNotFoundException, AuthorizationException, OperationExecutionException, CustomException, IOException, InterruptedException {
        var resp = (JSONObject) RESTClient.execute(base + "/params/server", MethodType.POST, ContentType.APPLICATION_JSON, CharsetType.UTF_8, "{}", null, ContentType.APPLICATION_JSON);

        assertNotNull(resp);

        var server = resp.optQuery("/serverName");
        var port = resp.optQuery("/serverPort");
        var context = resp.optQuery("/context");

        assertNotNull(server);
        assertNotNull(server.toString());
        assertTrue(LOCALHOST.contains(server.toString()));

        assertNotNull(port);
        assertNotNull(port.toString());
        assertEquals(String.valueOf(PORT), port.toString());

        assertNotNull(context);
        assertNotNull(context.toString());
        assertTrue(context.toString().contains(APP));
    }
}
