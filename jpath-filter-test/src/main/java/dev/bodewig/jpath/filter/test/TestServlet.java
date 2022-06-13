package dev.bodewig.jpath.filter.test;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.minidev.json.JSONObject;

public class TestServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    JSONObject sub = new JSONObject();
    sub.appendField("world", "!");

    JSONObject root = new JSONObject();
    root.appendField("hello", sub);

    PrintWriter writer = response.getWriter();
    writer.append(root.toJSONString());
    writer.flush();
  }
}
