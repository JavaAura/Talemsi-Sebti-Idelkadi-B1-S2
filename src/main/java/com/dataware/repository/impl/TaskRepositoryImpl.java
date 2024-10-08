package com.dataware.repository.impl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.dataware.database.DatabaseConnection;
import com.dataware.model.Member;
import com.dataware.model.Project;
import com.dataware.model.Task;
import com.dataware.model.enums.TaskPriority;
import com.dataware.model.enums.TaskStatus;
import com.dataware.repository.TaskRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskRepositoryImpl implements TaskRepository {

	private Connection conn;
	private static final Logger logger = LoggerFactory.getLogger(TaskRepositoryImpl.class);

	public TaskRepositoryImpl() {
		conn = DatabaseConnection.getInstance().getConnection();
	}

	public TaskRepositoryImpl(Connection connection) {
		this.conn = connection;
	}

	@Override
	public boolean addTask(Task task) {

		String query = " INSERT INTO `task`(`title`, `description`, `priority`, `status`, `creation_date`, `due_date`, `project_id`, `member_id`) VALUES (? , ? , ? , ? , ? , ? , ? , ?)";

		try (PreparedStatement pstmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
			pstmt.setString(1, task.getTitle());
			pstmt.setString(2, task.getDescription());
			pstmt.setString(3, task.getPriority().toString());
			pstmt.setString(4, task.getStatus().toString());
			pstmt.setDate(5, Date.valueOf(task.getCreationDate()));
			pstmt.setDate(6, Date.valueOf(task.getDueDate()));
			pstmt.setInt(7, task.getProject().getId());
			pstmt.setInt(8, task.getMember().getId());

			boolean result = pstmt.executeUpdate() > 0;
			logger.info("Task added successfully: {}", task.getTitle());

			return result;
		} catch (SQLException e) {
			logger.error("Error adding task: {}", task.getTitle(), e);
		}
		return false;
	}

	@Override
	public boolean updateTask(Task task) {
		String query = "UPDATE `task` SET `title`=? ,`description`= ? ,`priority`= ? ,`status`= ? ,`creation_date`= ? ,`due_date`= ? ,`member_id`= ?  WHERE id = ?";

		try (PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setString(1, task.getTitle());
			pstmt.setString(2, task.getDescription());
			pstmt.setString(3, task.getPriority().toString());
			pstmt.setString(4, task.getStatus().toString());
			pstmt.setDate(5, Date.valueOf(task.getCreationDate()));
			pstmt.setDate(6, Date.valueOf(task.getDueDate()));
			pstmt.setInt(7, task.getMember().getId());

			pstmt.setInt(8, task.getId());

			boolean result = pstmt.executeUpdate() > 0;
			logger.info("Task updated successfully: {}", task.getTitle());
			return result;

		} catch (SQLException e) {
			logger.error("Error updating task: {}", task.getTitle(), e);
		}
		return false;
	}

	@Override
	public boolean deleteTask(int id) {
		String query = "DELETE FROM `task` WHERE id =?";
		try (PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setInt(1, id);
			boolean result = pstmt.executeUpdate() > 0;
			logger.info("Task deleted successfully: ID {}", id);
			return result;
		} catch (SQLException e) {
			logger.error("Error deleting task with ID: {}", id, e);
		}
		return false;
	}

	@Override
	public Optional<List<Task>> displayAll(int pageNumber, int pageSize) {

		int offset = (pageNumber - 1) * pageSize;

		String query = "SELECT t.id as `task_id`, t.title, t.description as `task_description`, t.priority, t.status as `task_status`, "
				+ "t.creation_date, t.due_date, p.id as `project_id`, p.name, p.description as `project_description`, "
				+ "p.start_date, p.end_date, p.status as `project_status`, m.id as `member_id`, m.first_name, m.last_name, "
				+ "m.email, m.role " + "FROM `task` t " + "JOIN `project` p ON p.id = t.project_id "
				+ "JOIN `member` m ON m.id = t.member_id " + "LIMIT ? OFFSET ?;";

		List<Task> tasks = new ArrayList<>();

		try (PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setInt(1, pageSize);
			pstmt.setInt(2, offset);

			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				Project project = new Project();
				project.setId(rs.getInt("project_id"));
				project.setName(rs.getString("name"));
				project.setDescription(rs.getString("project_description"));

				Member member = new Member();
				member.setId(rs.getInt("member_id"));
				member.setFirstName(rs.getString("first_name"));
				member.setLastName(rs.getString("last_name"));
				member.setEmail(rs.getString("email"));

				Task task = new Task();
				task.setId(rs.getInt("task_id"));
				task.setTitle(rs.getString("title"));
				task.setDescription(rs.getString("task_description"));
				task.setPriority(TaskPriority.fromString(rs.getString("priority")));
				task.setStatus(TaskStatus.fromString(rs.getString("task_status")));
//				task.setStatus(TaskStatus.valueOf());
				task.setCreationDate(rs.getDate("creation_date").toLocalDate());
				task.setDueDate(rs.getDate("due_date").toLocalDate());
				task.setProject(project);
				task.setMember(member);

				tasks.add(task);
				logger.info("Displayed {} tasks from page {}.", tasks.size(), pageNumber);
			}

		} catch (SQLException e) {
			logger.error("Error displaying tasks: ", e);
		}

		return tasks.isEmpty() ? Optional.empty() : Optional.of(tasks);
	}

	public int getTotalTasks() {
		String query = "SELECT COUNT(*) FROM task";

		try (PreparedStatement pstmt = conn.prepareStatement(query); ResultSet rs = pstmt.executeQuery()) {
			if (rs.next()) {
				int count = rs.getInt(1);
				logger.info("Total tasks: {}", count);
				return count;
			}
		} catch (SQLException e) {
			logger.error("Error counting total tasks: ", e);
		}
		return 0;
	}

	@Override
	public Optional<Task> getTaskById(int id) {
		String query = "SELECT t.id as `task_id`, t.title, t.description as `task_description`, t.priority, t.status as `task_status`, "
				+ "t.creation_date, t.due_date, p.id as `project_id`, p.name, p.description as `project_description`, "
				+ "p.start_date, p.end_date, p.status as `project_status`, m.id as `member_id`, m.first_name, m.last_name, "
				+ "m.email, m.role FROM `task` t JOIN `project` p ON p.id = t.project_id JOIN `member` m ON m.id = t.member_id WHERE t.id = ?";

		try (PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setInt(1, id);

			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				Project project = new Project();
				project.setId(rs.getInt("project_id"));
				project.setName(rs.getString("name"));
				project.setDescription(rs.getString("project_description"));

				Member member = new Member();
				member.setId(rs.getInt("member_id"));
				member.setFirstName(rs.getString("first_name"));
				member.setLastName(rs.getString("last_name"));
				member.setEmail(rs.getString("email"));

				Task task = new Task();
				task.setId(rs.getInt("task_id"));
				task.setTitle(rs.getString("title"));
				task.setDescription(rs.getString("task_description"));
				task.setPriority(TaskPriority.fromString(rs.getString("priority")));
				task.setStatus(TaskStatus.fromString(rs.getString("task_status")));
				task.setCreationDate(rs.getDate("creation_date").toLocalDate());
				task.setDueDate(rs.getDate("due_date").toLocalDate());
				task.setProject(project);
				task.setMember(member);

				return Optional.of(task);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

	@Override
	public int getLastInsertedId() {
		String query = "SELECT LAST_INSERT_ID()"; // Utilisez cette requête pour obtenir le dernier ID inséré.

		try (PreparedStatement pstmt = conn.prepareStatement(query); ResultSet rs = pstmt.executeQuery()) {
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

}
