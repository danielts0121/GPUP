package servlets.tasks;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import task.TasksManager;
import utils.ServletUtils;
import java.io.IOException;
import java.util.Set;

@WebServlet(name = "TaskListServlet", urlPatterns = "/tasks/list")
public class TaskListServlet extends HttpServlet {

    private static final Object dummy = new Object();
    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        TasksManager tasksManager;
        String listAsString;

        synchronized (dummy)
        {
            tasksManager = ServletUtils.getTasksManager(getServletContext());
        }
        if(req.getParameter("allTasksList") == null && req.getParameter("onlineTasksList") == null)
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        else
        {
            Set<String> tasksList;
            if(req.getParameter("allTasksList") != null)
                tasksList = tasksManager.getAllTaskList();
            else
                tasksList = tasksManager.getOnlineTaskList();

            listAsString = gson.toJson(tasksList);

            resp.getWriter().println(listAsString);
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
    }
}
