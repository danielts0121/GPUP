package servlets.graphs;

import com.google.gson.Gson;
import dtos.GraphInfoDto;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import target.FileChecker;
import target.TargetGraph;
import target.GraphsManager;
import utils.ServletUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@WebServlet(name = "GraphsServlet", urlPatterns = "/graphs")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class GraphsServlet extends HttpServlet {
    public Gson gson = new Gson();
    public static Path WORKING_DIRECTORY_PATH = Paths.get("C:\\gpup-working-dir");
    private final Map<String, GraphInfoDto> graphInfoDtoMap = new HashMap<>();
    private static final Object creatingDirectory = new Object();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        GraphsManager graphsManager = ServletUtils.getGraphsManager(getServletContext());

        if(req.getParameter("selectedGraphName") != null)
        {
            String graphName = req.getParameter("selectedGraphName");

            if(graphsManager.isGraphExists(graphName))
            {
                GraphInfoDto currDTO = this.graphInfoDtoMap.get(graphName);
                String dtoAsString = this.gson.toJson(currDTO, GraphInfoDto.class);
                resp.getWriter().write(dtoAsString);

                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
            }
            else
            {
                resp.getWriter().println("Graph not exists!");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        else if (req.getParameter("graph") != null)
        {
            String graphName = req.getParameter("graph");

            if(graphsManager.isGraphExists(graphName))
            {
                File graphFile = graphsManager.getGraphFile(graphName);
                String fileAsString = this.gson.toJson(graphFile, File.class);
                resp.getWriter().write(fileAsString);

                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
            }
            else
            {
                resp.getWriter().println("Graph not exists!");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            synchronized (creatingDirectory)
            {
                if(!Files.exists(WORKING_DIRECTORY_PATH))
                    createWorkingDirectory();
            }

            Path filePath = Paths.get(WORKING_DIRECTORY_PATH + "\\CurrentGraph.xml");

            if(Files.exists(filePath))
                Files.delete(filePath);

            Files.createFile(filePath);

            Part filePart = req.getPart("fileToUpload");
            InputStream fileInputStream = filePart.getInputStream();
            Files.copy(fileInputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

            FileChecker fileChecker = new FileChecker();
            TargetGraph graph = fileChecker.createTargetGraphFromXml(filePath);
            GraphsManager graphsManager = ServletUtils.getGraphsManager(getServletContext());


            if(graphsManager.isGraphExists(graph.getGraphName().toLowerCase(Locale.ROOT)))
            {
                resp.addHeader("message", "The graph " + graph.getGraphName() +" already exists in the system!");

                resp.getWriter().println("The graph " + graph.getGraphName() +" already exists in the system!");
                resp.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            }
            else
            {
                graph.setUploaderName(req.getHeader("username"));
                graphsManager.addGraph(graph.getGraphName(), filePath.toFile(), graph);
                this.graphInfoDtoMap.put(graph.getGraphName(), new GraphInfoDto(graph));

                resp.addHeader("message", "The graph " + graph.getGraphName() +" loaded successfully!");
                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
                resp.addHeader("graphname", graph.getGraphName());
            }
        } catch (Exception e) {
            resp.addHeader("message", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void createWorkingDirectory() throws IOException {
        Files.createDirectory(WORKING_DIRECTORY_PATH);
    }
}
