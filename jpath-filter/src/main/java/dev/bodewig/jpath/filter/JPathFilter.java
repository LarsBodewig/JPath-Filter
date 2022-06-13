package dev.bodewig.jpath.filter;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.util.Optional;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class JPathFilter implements Filter {

  protected static class AnonRespWrapper extends HttpServletResponseWrapper {
    protected final ServletOutputStream outStream;
    protected PrintWriter singleWriter;

    public AnonRespWrapper(HttpServletResponse response, ServletOutputStream outStream) {
      super(response);
      this.outStream = outStream;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
      return this.outStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
      if (this.singleWriter == null) {
        initSingleWriter();
      }
      return this.singleWriter;
    }

    protected synchronized void initSingleWriter() {
      PrintWriter printWriter = new PrintWriter(this.outStream);
      this.singleWriter = printWriter;
    }
  }

  protected static class ServletOutputStreamWrapper extends ServletOutputStream {
    protected final OutputStream outStream;

    public ServletOutputStreamWrapper(OutputStream outStream) {
      this.outStream = outStream;
    }

    @Override
    public void close() throws IOException {
      this.outStream.close();
    }

    @Override
    public void flush() throws IOException {
      this.outStream.flush();
    }

    @Override
    public void write(int b) throws IOException {
      this.outStream.write(b);
    }
  }

  protected static final String PARAMETER_PATH_PARAM = "path-param";
  protected static final String PARAMETER_PROVIDER = "provider-class";

  protected static final String DEFAULT_PATH_PARAM = "p";
  protected static final JsonProvider DEFAULT_PROVIDER = new JsonSmartJsonProvider();

  protected String pathParam;
  protected JsonProvider provider;

  @Override
  public void init(FilterConfig fConfig) throws ServletException {
    String providerParam = fConfig.getInitParameter(PARAMETER_PROVIDER);
    Optional<JsonProvider> provider = getJsonProvider(providerParam);
    if (provider.isPresent()) {
      this.provider = provider.get();
    } else {
      this.provider = new JsonSmartJsonProvider();
    }

    String pathParam = fConfig.getInitParameter(PARAMETER_PATH_PARAM);
    if (pathParam != null && !pathParam.isBlank()) {
      this.pathParam = pathParam;
    } else {
      this.pathParam = DEFAULT_PATH_PARAM;
    }
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    Optional<String> pathParam = getJsonPath(request);

    if (pathParam.isEmpty()) {
      chain.doFilter(request, response);
      return;
    }

    HttpServletResponse httpResp = (HttpServletResponse) response;
    Configuration config = Configuration.defaultConfiguration().jsonProvider(this.provider);

    DocumentContext doc;
    try (PipedInputStream in = new PipedInputStream()) {
      try (PipedOutputStream out = new PipedOutputStream(in);
          ServletOutputStream sos = new ServletOutputStreamWrapper(out)) {
        AnonRespWrapper anonResp = new AnonRespWrapper(httpResp, sos);
        chain.doFilter(request, anonResp);
      }

      doc = JsonPath.parse(in, config);
    }

    try {
      Object subJson = doc.read(pathParam.get());
      String result = this.provider.toJson(subJson);

      try (PrintWriter writer = response.getWriter()) {
        writer.print(result);
      }
    } catch (PathNotFoundException exc) {
      onPathNotFound(request, httpResp, exc);
    }
  }

  protected Optional<String> getJsonPath(ServletRequest request) {
    String pathParam = this.pathParam;
    String path = request.getParameter(pathParam);
    return Optional.ofNullable(path);
  }

  protected Optional<JsonProvider> getJsonProvider(String name) {
    try {
      if (name != null) {
        Class<?> clazz = Class.forName(name);
        Object provider = clazz.getDeclaredConstructor().newInstance();
        return Optional.of((JsonProvider) provider);
      }
    } catch (Exception exc) {
      onProviderNotFound(name, exc);
    }
    return Optional.empty();
  }

  protected void onPathNotFound(
      ServletRequest request, HttpServletResponse response, PathNotFoundException exc) {
    try {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, exc.toString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void onProviderNotFound(String name, Exception exc) {
    // override to add logging
  }

  @Override
  public void destroy() {
    // do nothing
  }
}
