package servlets.users;

import users.UserManager;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
        String username = request.getParameter("username");
        UserManager userManager = ServletUtils.getUserManager(getServletContext());

        if (username != null) {
            userManager.removeUser(username);
        }
    }
}