package servlets.tasks.worker;

import com.google.gson.Gson;
import dtos.CompilationTaskDto;
import dtos.GPUPTaskDto;
import dtos.SimulationTaskDto;
import dtos.WorkerDetailsDto;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import target.Target;
import target.TargetForWorker;
import task.TasksManager;
import users.UserManager;
import utils.ServletUtils;
import worker.taskmanagment.TaskHistoryDto;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

@WebServlet(name = "WorkerTaskServlet", urlPatterns = "/worker/task")
public class WorkerTaskServlet extends HttpServlet {
    public Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        TasksManager tasksManager = ServletUtils.getTasksManager(getServletContext());
        UserManager userManager = ServletUtils.getUserManager(getServletContext());

        PrintWriter out = response.getWriter();
        response.setContentType("application/json");

        if(request.getParameter("getTaskToDo") != null)
        {
            Set<String> signedToTasks = gson.fromJson(request.getReader(), Set.class);
            GPUPTaskDto gpupTaskDto = tasksManager.pollTaskReadyForWorker(signedToTasks);
            String gpupTaskJson = "";
            if(gpupTaskDto != null) {
                if (gpupTaskDto.getTaskType().equalsIgnoreCase("simulation")) {
                    gpupTaskJson = gson.toJson(gpupTaskDto, SimulationTaskDto.class);
                    response.addHeader("taskType", "simulation");
                }
                if (gpupTaskDto.getTaskType().equalsIgnoreCase("compilation")) {
                    gpupTaskJson = gson.toJson(gpupTaskDto, CompilationTaskDto.class);
                    response.addHeader("taskType", "compilation");
                }
            }
            out.write(gpupTaskJson);
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
        }

        if(request.getHeader("updateStatus") != null) {
            TargetForWorker targetForWorker = gson.fromJson(request.getReader(), TargetForWorker.class);
            String workerName = request.getHeader("workerName").toLowerCase();
            tasksManager.updateTargetsStatusAndResult(targetForWorker);
            if(targetForWorker.getTargetStatus() == Target.Status.FINISHED) {
                String taskName = targetForWorker.getTaskName();
                TaskHistoryDto taskH = tasksManager.getTaskHistoryForWorker(workerName, taskName);
                userManager.getWorkerDetailsDto(workerName).addCredits(targetForWorker.getPricing());
                taskH.addTargetDone();
                taskH.addCredits(targetForWorker.getPricing());
            }
            tasksManager.addTargetToWorkerMap(workerName,targetForWorker);
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
        else if(request.getParameter("registerToTask") != null) {
            WorkerDetailsDto workerDetailsDto = userManager.getWorkerDetailsDto(request.getParameter("workerName").toLowerCase());
            String taskName = request.getParameter("taskName");
            if(workerDetailsDto.getRegisteredTasks().contains(taskName))
            {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.addHeader("message","Already registered to task " + taskName);
            }
            else {
                response.setStatus(HttpServletResponse.SC_OK);
                tasksManager.getTaskForServerSide(taskName).addWorker();
                tasksManager.getTaskDetailsDTO(taskName).addWorker();
                workerDetailsDto.registerToTask(taskName.toLowerCase());
                tasksManager.createTaskHistoryForWorker(workerDetailsDto.getUserName(),taskName);
            }
        }
        else if(request.getParameter("unregisterFromTask") != null) {
            String taskName = request.getParameter("taskName");
            tasksManager.getTaskForServerSide(taskName).removeWorker();
            tasksManager.getTaskDetailsDTO(taskName).removeWorker();
            userManager.getWorkerDetailsDto(request.getParameter("workerName").toLowerCase()).unregisterFromTask(taskName.toLowerCase());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        TasksManager tasksManager = ServletUtils.getTasksManager(getServletContext());
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");

        if(request.getParameter("targetId") != null) {
            String targetId = request.getParameter("targetId");
            String workerName = request.getParameter("workerName");

            TargetForWorker targetForWorker = tasksManager.getTargetFromWorkerMap(workerName,targetId);
            String targetForWorkerJson = gson.toJson(targetForWorker,TargetForWorker.class);
            out.write(targetForWorkerJson);
        }
        else if(request.getParameter("workerName") != null){
            String workerName = request.getParameter("workerName");
            Set<String> targetForWorkerSet = tasksManager.getWorkerTargetSet(workerName);
            String targetForWorkerSetJson = gson.toJson(targetForWorkerSet);
            out.write(targetForWorkerSetJson);
        }
    }
}
