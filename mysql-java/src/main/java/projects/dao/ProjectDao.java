package projects.dao;

import projects.entity.Project;
import provided.util.DaoBase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import projects.exception.DbException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Step;

//This class will read and write to the MySQL database and performs CRUD (Create, Read, Update and Delete)

public class ProjectDao extends DaoBase {
	private static final String CATEGORY_TABLE = "category";
	private static final String MATERIAL_TABLE = "material";
	private static final String PROJECT_TABLE = "project";
	private static final String PROJECT_CATEGORY_TABLE = "project_category";
	private static final String STEP_TABLE = "step";

	// Inserts a row into the project table
	public Project insertProject(Project project) {
		// @formatter:off
		//The sql String is used to create a SQL statement
		String sql = ""
			+ "INSERT INTO " + PROJECT_TABLE + " "
			+ "(project_name, estimated_hours, actual_hours, difficulty, notes)"
			+ "VALUES "
			+ "(?, ?, ?, ?, ?)";
		//formatter:on
		
		//Commits or rolls back the transaction
		try (Connection conn = DbConnection.getConnection()) {	
			startTransaction(conn);
			
			//Creates a SQL statement using the sql String and sets the parameters to the values input by the users 
			try (PreparedStatement statement = conn.prepareStatement(sql)) {	
				setParameter(statement, 1, project.getProjectName(), String.class);
				setParameter(statement, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(statement, 3, project.getActualHours(), BigDecimal.class);
				setParameter(statement, 4, project.getDifficulty(), Integer.class);
				setParameter(statement, 5, project.getNotes(), String.class);
				
				statement.executeUpdate();			
				Integer projectId = getLastInsertId(conn, PROJECT_TABLE);
				
				commitTransaction(conn);
				
				project.setProjectId(projectId);
				
				return project;
			} catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
			
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}

	// retrieve a project row and all associated child rows
	//Obtain a Connection, Start a transaction inside the try-with-resource statement 
	//catch block to handle Exception
	public List<Project> fetchAllProjects() {
		String sql = "SELECT * FROM " 
					 + PROJECT_TABLE 
					 + " ORDER BY project_name"; 
		
		//Creates a connection with the database
		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			try (PreparedStatement statement = conn.prepareStatement(sql)) {
				
			//Fetches the projects when it executes the query and returns the result set
			  try (ResultSet resultSet = statement.executeQuery()) {
					List<Project> projects = new LinkedList<>();
				//New while loop
				//Loops through the result set. Creates and assign each result row to a new Project object.
				// Adds the Project object to the List of Projects
				while (resultSet.next()) {
						projects.add(extract(resultSet, Project.class)); //Adds each object to the projects list
					}
					
				return projects;
			
				}
			} catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}		
	}

	//Fetches the selected project from the database using the project ID
	public Optional<Project> fetchProjectById(Integer projectId) {
		String sql = "SELECT * FROM "
					+ PROJECT_TABLE 
					+ " WHERE project_id = ?";
		
		//Creates a connection with the database
		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			
			try {
				Project project = null;
				
				//Sets the parameter on the statement
				try (PreparedStatement statement = conn.prepareStatement(sql)) {
					setParameter(statement, 1, projectId, Integer.class);
					
					//Sets the values of the fields of the Project object using the data retrieved from the result set
					try (ResultSet resultSet = statement.executeQuery()) {
						if (resultSet.next()) {
							project = extract(resultSet, Project.class);
									
						}
					}
				}
				
				//the methods that will return materials, steps, and categories as Lists
				if (Objects.nonNull(project)) {
					project.getMaterials().addAll(fetchMaterialsForProject(conn, projectId));
					project.getSteps().addAll(fetchStepsForProject(conn, projectId));
					project.getCategories().addAll(fetchCategoriesForProject(conn, projectId));
				}
				
				commitTransaction(conn);
				
				return Optional.ofNullable(project);

			} catch (Exception e){
				rollbackTransaction(conn);
				throw new DbException(e);
			}
			
		} catch (SQLException e) {
			throw new DbException(e);
		}
	
	}

	//Fetches the category data of a project from the project category table 
	private List<Category> fetchCategoriesForProject(Connection conn, Integer projectId) throws SQLException {
		// @formatter:off
		String sql = "SELECT c.* FROM " + CATEGORY_TABLE + " c " 
					+ "JOIN " + PROJECT_CATEGORY_TABLE + " pc USING (category_id) "
					+ "WHERE project_id = ?";
		// @formatter:on 

		try (PreparedStatement statement = conn.prepareStatement(sql)) {
			setParameter(statement, 1, projectId, Integer.class);

			try (ResultSet resultSet = statement.executeQuery()) {
				List<Category> categories = new LinkedList<>();

				// Sets the fields of the Category objects using the data retrieved from the
				// result set. It adds the objects to the categories list.
				while (resultSet.next()) {
					categories.add(extract(resultSet, Category.class));
				}

				return categories;
			}
		}
	}

	// Fetches the steps of a project from the step table
	private List<Step> fetchStepsForProject(Connection conn, Integer projectId) throws SQLException {
		// @formatter:off
		String sql = "SELECT * FROM " + STEP_TABLE
				   + " WHERE project_id = ?";
		// @formatter:on 

		try (PreparedStatement statement = conn.prepareStatement(sql)) {
			setParameter(statement, 1, projectId, Integer.class);

			try (ResultSet resultSet = statement.executeQuery()) {
				List<Step> steps = new LinkedList<>();

				// Sets the fields of the Step objects using the data retrieved from the result
				// set. It adds the objects to the steps list.
				while (resultSet.next()) {
					steps.add(extract(resultSet, Step.class));
				}

				return steps;
			}
		}
	}

	// Fetches the materials used for a project from the material table
	private List<Material> fetchMaterialsForProject(Connection conn, Integer projectId) throws SQLException {
		// @formatter:off
		String sql = "SELECT * FROM " + MATERIAL_TABLE
				   + " WHERE project_id = ?";
		// @formatter:on 

		try (PreparedStatement statement = conn.prepareStatement(sql)) {
			setParameter(statement, 1, projectId, Integer.class);

			try (ResultSet resultSet = statement.executeQuery()) {
				List<Material> materials = new LinkedList<>();

				// Sets the fields of the Material objects using the data retrieved from the
				// result set. It adds the objects to the materials list.
				while (resultSet.next()) {
					materials.add(extract(resultSet, Material.class));
				}

				return materials;
			}
		}
	}
	// method to use SQL to update project details

	public boolean modifyProjectDetails(Project project) {
		// @formatter:off
		String sql = ""
			+ "UPDATE " + PROJECT_TABLE + " SET "
			+ "project_name = ?, "
			+ "estimated_hours = ?, "
			+ "actual_hours = ?, "
			+ "difficulty = ?, "
			+ "notes = ?, "
			+ "WHERE project_id = ?";	
		// @formatter:on
		// Obtain the Connection and PreparedStatement using the appropriate
		// try-with-resource and catch blocks
		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			// Set all parameters
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				setParameter(stmt, 1, project.getProjectName(), String.class);
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
				setParameter(stmt, 4, project.getDifficulty(), Integer.class);
				setParameter(stmt, 5, project.getNotes(), String.class);
				setParameter(stmt, 6, project.getProjectId(), Integer.class);

				// commit updated transaction.

				boolean modified = stmt.executeUpdate() == 1;
				commitTransaction(conn);
				// The executeUpdate() method returns the number of rows affected by the UPDATE
				// operation
				return modified;
			} catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}

	}

	public boolean deleteProject(Integer projectId) {
		String sql = "DELETE FROM " + PROJECT_TABLE + " WHERE project_id = ?";

		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);

			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				setParameter(stmt, 1, projectId, Integer.class);

				boolean deleted = stmt.executeUpdate() == 1;

				commitTransaction(conn);
				return deleted;
			} catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}

}