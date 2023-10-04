package servlets.graphs;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import target.GraphsManager;
import utils.ServletUtils;
import java.io.IOException;
import java.util.Set;

@WebServlet(name = "GraphListServlet", urlPatterns = "/graphslist")
public class GraphsListServlet extends HttpServlet {

    private static final Object dummy = new Object();

    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        GraphsManager graphsManager;
        String listAsString;

        synchronized (dummy)
        {
            graphsManager = ServletUtils.getGraphsManager(getServletContext());
        }
        Set<String> graphNamesSet = graphsManager.getGraphNames();

        if(!graphNamesSet.isEmpty())
        {
            listAsString = this.gson.toJson(graphsManager.getGraphNames());

            resp.getWriter().println(listAsString);
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
        else
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
