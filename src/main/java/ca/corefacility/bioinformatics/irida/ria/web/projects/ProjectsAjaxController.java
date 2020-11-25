package ca.corefacility.bioinformatics.irida.ria.web.projects;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ca.corefacility.bioinformatics.irida.ria.web.components.ant.table.TableRequest;
import ca.corefacility.bioinformatics.irida.ria.web.components.ant.table.TableResponse;
import ca.corefacility.bioinformatics.irida.ria.web.projects.dto.ProjectInfoResponse;
import ca.corefacility.bioinformatics.irida.ria.web.projects.settings.dto.Role;
import ca.corefacility.bioinformatics.irida.ria.web.services.UIProjectsService;

/**
 * Controller for handling all ajax requests for Projects.
 */
@RestController
@RequestMapping("/ajax/projects")
public class ProjectsAjaxController {
	private final UIProjectsService UIProjectsService;


	@Autowired
	public ProjectsAjaxController(UIProjectsService UIProjectsService) {
		this.UIProjectsService = UIProjectsService;
	}

	/**
	 * Handle request for get a filtered and sorted list of projects for a user or administrator
	 *
	 * @param tableRequest {@link TableRequest} Details about what is needed in the table (sort, filter, and search).
	 * @param admin           {@link Boolean} Is the user on an administration page.
	 * @return {@link TableResponse}
	 */
	@RequestMapping
	public ResponseEntity<TableResponse> getPagedProjectsForUser(@RequestBody TableRequest tableRequest,
			@RequestParam Boolean admin) {
		return ResponseEntity.ok(UIProjectsService.getPagedProjects(tableRequest, admin));
	}

	/**
	 * Get a list of all roles available on a project
	 *
	 * @param locale - {@link Locale} of the current user
	 * @return list of roles and their internationalized strings
	 */
	@RequestMapping("/roles")
	public ResponseEntity<List<Role>> getProjectRoles(Locale locale) {
		return ResponseEntity.ok(UIProjectsService.getProjectRoles(locale));
	}

	/**
	 * Get project info abd permissions
	 *
	 * @param projectId - the project to get info for
	 * @return {@link ProjectInfoResponse}
	 */
	@RequestMapping("/{projectId}/info")
	public ResponseEntity<ProjectInfoResponse> getProjectInfo(@PathVariable Long projectId) {
		return ResponseEntity.ok(UIProjectsService.getProjectInfo(projectId));
	}
}
