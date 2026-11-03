package org.tn5250j.mcp;

import java.util.List;

import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.DefaultServerTransportSecurityValidator;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;

/** Streamable HTTP MCP endpoint on the loopback interface only. */
public final class McpHttpServer implements AutoCloseable {

    static final String LOOPBACK_ADDRESS = "127.0.0.1";
    static final String ENDPOINT = "/mcp";

    private static final String SERVER_NAME = "tn5250j-mcp";
    private static final String SERVER_VERSION = "0.1.0";
    private static final String INSTRUCTIONS = "Drives a live IBM i 5250 session that a human is also watching."
            + " Coordinates are 1-based. After sending an AID key ([enter], [pfN], ...) call wait_for_ready.";

    private final Server jetty;
    private final McpSyncServer mcpServer;

    private McpHttpServer(Server jetty, McpSyncServer mcpServer) {
        this.jetty = jetty;
        this.mcpServer = mcpServer;
    }

    public static McpHttpServer start(int port, List<SyncToolSpecification> tools) throws Exception {
        HttpServletStreamableServerTransportProvider transport = HttpServletStreamableServerTransportProvider.builder()
                .mcpEndpoint(ENDPOINT)
                // binding to loopback does not stop a browser page reaching us via DNS rebinding;
                // require a localhost Host header and reject any cross-origin request
                .securityValidator(DefaultServerTransportSecurityValidator.builder()
                        .allowedHost(LOOPBACK_ADDRESS + ":*")
                        .allowedHost("localhost:*")
                        .build())
                .build();

        McpSyncServer mcpServer = McpServer.sync(transport)
                .serverInfo(SERVER_NAME, SERVER_VERSION)
                .instructions(INSTRUCTIONS)
                .capabilities(ServerCapabilities.builder().tools(false).build())
                .tools(tools)
                .build();

        Server jetty = new Server();
        ServerConnector connector = new ServerConnector(jetty);
        connector.setHost(LOOPBACK_ADDRESS);
        connector.setPort(port);
        jetty.addConnector(connector);

        ServletContextHandler context = new ServletContextHandler();
        ServletHolder servlet = new ServletHolder(transport);
        servlet.setAsyncSupported(true);
        context.addServlet(servlet, ENDPOINT);
        jetty.setHandler(context);

        try {
            jetty.start();
        } catch (Exception e) {
            mcpServer.close();
            jetty.stop();
            throw e;
        }
        return new McpHttpServer(jetty, mcpServer);
    }

    @Override
    public void close() throws Exception {
        try {
            mcpServer.closeGracefully();
        } finally {
            jetty.stop();
        }
    }
}
