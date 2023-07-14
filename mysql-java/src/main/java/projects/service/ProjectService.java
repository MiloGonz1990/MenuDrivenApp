package projects.service;

import projects.dao.ProjectDao;
import projects.entity.Project;
import java.util.List;
import java.util.NoSuchElementException;
import projects.exception.DbException;

public class ProjectService {

	private ProjectDao projectDao = new ProjectDao();

	// method calls the DAO class to insert a project row
	public Project addProject(Project project) {
		return projectDao.insertProject(project);
	}

	// created two new methods
	// retrieving only the id and the name of the projects
	public List<Project> fetchAllProjects() {
		return projectDao.fetchAllProjects();
	}

	// It returns a Project object and takes an Integer projectId as a parameter.
	// It throws an exception if the project ID input by the user doesn't exist in
	// the database.
	public Project fetchProjectById(Integer projectId) {
		return projectDao.fetchProjectById(projectId).orElseThrow(
				() -> new NoSuchElementException("Project with project ID=" + projectId + " does not exist."));
	}

	// The service is responsible for calling the DAO to update the project details
	// and to return those details to the caller
	// The service method is called by the menu application class, and results are
	// returned to that class.
	// The DAO method returns a boolean that indicates whether the UPDATE operation
	// was successful.
	public void modifyProjectDetails(Project project) {
		if (!projectDao.modifyProjectDetails(project)) {
			throw new DbException("Project with ID=" + project.getProjectId() + " does not exist");
		}

	}

	public void deleteProject(Integer projectId) {
		if (!projectDao.deleteProject(projectId)) {
			throw new DbException("Project with ID= " + projectId + " does not exist.");
		}

	}
}