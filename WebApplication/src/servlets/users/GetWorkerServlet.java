package servlets.users;

import com.google.gson.Gson;
import dtos.WorkerDetailsDto;
import dtos.WorkerTaskDetailsDto;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import task.TaskForServerSide;
import task.TasksManager;
import users.UserManager;
import utils.ServletUtils;
import worker.taskmanagment.TaskHistoryDto;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

@WebServlet(name = "GetWorkerServlet", urlPatterns = "/getWorker")
public class GetWorkerServlet extends HttpServlet {
    private Gson gson = new Gson();;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        TasksManager tasksManager = ServletUtils.getTasksManager(getServletContext());
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");

        if(request.getParameter("getWorkerDto") != null) {
            String workerName = request.getParameter("workerName");
            WorkerDetailsDto workerDetailsDto = userManager.getWorkerDetailsDto(workerName);
            String workerDetailsDtoJson = gson.toJson(workerDetailsDto, WorkerDetailsDto.class);
            out.write(workerDetailsDtoJson);
        }
        else if(request.getParameter("getWorkerTasks") != null) {
            String workerName = request.getParameter("workerName");
            WorkerDetailsDto workerDetailsDto = userManager.getWorkerDetailsDto(workerName);
            Set<String> workersOnlineTasks = new HashSet<>();
            for (String taskName : workerDetailsDto.getRegisteredTasks() ) {
                if(!tasksManager.getTaskForServerSide(taskName).getTaskStatus().equalsIgnoreCase("finished") &&
                !tasksManager.getTaskForServerSide(taskName).getTaskStatus().equalsIgnoreCase("stopped"))
                {
                    workersOnlineTasks.add(taskName);
                }
            }
            String workersOnlineTasksJson = gson.toJson(workersOnlineTasks);
            out.write(workersOnlineTasksJson);
        }
        else if(request.getParameter("getWorkerTask") != null) {
            String workerName = request.getParameter("workerName");
            String taskName = request.getParameter("taskName");
            TaskHistoryDto workerTaskHistory = tasksManager.getTaskHistoryForWorker(workerName,taskName);
            TaskForServerSide taskForServerSide = tasksManager.getTaskForServerSide(taskName);
            WorkerTaskDetailsDto workerTaskDetailsDto =
                    new WorkerTaskDetailsDto(taskName,taskForServerSide.getTaskStatus(),
                            taskForServerSide.getAmountOfWorkers(), workerTaskHistory.getTargetsDoneByWorker()
                                , workerTaskHistory.getCreditsEarned());
            String workerTaskDetailsDtoJson = gson.toJson(workerTaskDetailsDto,WorkerTaskDetailsDto.class);
            out.write(workerTaskDetailsDtoJson);
        }
    }
}