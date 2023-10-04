package servlets.tasks.updates;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import target.Target;
import target.TargetForWorker;
import task.TaskForServerSide;
import task.TasksManager;
import utils.ServletUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

@WebServlet(name = "GetTargetsServlet", urlPatterns = "/tasks/getTargets")
public class GetTargetsServlet extends HttpServlet {
    Gson gson = new Gson();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        TasksManager tasksManager = ServletUtils.getTasksManager(getServletContext());
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");

        if(request.getParameter("taskName")!= null) {
            String taskName = request.getParameter("taskName");
            Set<TargetForWorker>  targetDtoSet = new HashSet<>();
            for (Target target:tasksManager.getTaskForServerSide(taskName).getTargetGraph().getAllChosenTargets()) {
                TaskForServerSide taskForServerSide = tasksManager.getTaskForServerSide(taskName);
                targetDtoSet.add(Target.extractTargetForWorkerFromTarget(target,taskName,taskForServerSide.getTaskType().name()
                        ,taskForServerSide.getTargetGraph().getTaskPricing().get(taskForServerSide.getTaskType())));
            }

            String targetDtoSetJson = gson.toJson(targetDtoSet, new TypeToken<Set<TargetForWorker>>(){}.getType());

            out.write(targetDtoSetJson);
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
    }
}

