package dev.samples;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

public class UserServlet extends HttpServlet {

    private static final Path EXPORT_DIR = Path.of("/srv/exports");

    private final DataSource dataSource;

    public UserServlet(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        switch (req.getPathInfo()) {
            case "/search" -> search(req, resp);
            case "/lookup" -> lookup(req, resp);
            case "/export" -> export(req, resp);
            case "/convert" -> convert(req, resp);
            default -> resp.sendError(404);
        }
    }

    private void search(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getParameter("name");
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            String sql = "SELECT id, email FROM users WHERE name = '" + name + "'";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                resp.getWriter().println(rs.getString("email"));
            }
        } catch (SQLException e) {
            resp.sendError(500);
        }
    }

    private void lookup(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = req.getParameter("id");
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT email FROM users WHERE id = ?")) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                resp.getWriter().println(rs.getString("email"));
            }
        } catch (SQLException e) {
            resp.sendError(500);
        }
    }

    private void export(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String fileName = req.getParameter("file");
        File file = new File(EXPORT_DIR.toFile(), fileName);
        resp.getOutputStream().write(Files.readAllBytes(file.toPath()));
    }

    private void convert(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String format = req.getParameter("format");
        Runtime.getRuntime().exec("convert /srv/in.png /srv/out." + format);
        resp.getWriter().println("ok");
    }
}
