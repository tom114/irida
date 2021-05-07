package ca.corefacility.bioinformatics.irida.ria.web.projects.settings;

import java.security.Principal;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.ria.web.projects.ProjectControllerUtils;
import ca.corefacility.bioinformatics.irida.ria.web.projects.ProjectsController;
import ca.corefacility.bioinformatics.irida.service.AnalysisSubmissionService;
import ca.corefacility.bioinformatics.irida.service.ProjectService;
import ca.corefacility.bioinformatics.irida.service.workflow.IridaWorkflowsService;

/**
 * Handles basic settings pages for a project
 */
@Controller
@RequestMapping("/projects/{projectId}/settings")
public class ProjectSettingsController {
	private final MessageSource messageSource;
	private final ProjectControllerUtils projectControllerUtils;
	private final ProjectService projectService;
	private final AnalysisSubmissionService analysisSubmissionService;
	private final IridaWorkflowsService workflowsService;

	public static final String ACTIVE_NAV_SETTINGS = "settings";

	@Autowired
	public ProjectSettingsController(MessageSource messageSource, ProjectControllerUtils projectControllerUtils,
			ProjectService projectService, AnalysisSubmissionService analysisSubmissionService,
			IridaWorkflowsService workflowsService) {
		this.messageSource = messageSource;
		this.projectControllerUtils = projectControllerUtils;
		this.projectService = projectService;
		this.analysisSubmissionService = analysisSubmissionService;
		this.workflowsService = workflowsService;
	}

	/**
	 * Request for a {@link Project} basic settings page
	 *
	 * @param projectId the ID of the {@link Project} to read
	 * @param model     Model for the view
	 * @param principal Logged in user
	 * @param locale    Locale of the logged in user
	 * @return name of the project settings page
	 */
	@RequestMapping(value = { "", "/**" })
	public String getProjectSettingsBasicPage(@PathVariable Long projectId, final Model model,
			final Principal principal, Locale locale) {
		Project project = projectService.read(projectId);
		model.addAttribute("project", project);
		model.addAttribute(ProjectsController.ACTIVE_NAV, ACTIVE_NAV_SETTINGS);
		model.addAttribute("page", "details");
		projectControllerUtils.getProjectTemplateDetails(model, principal, project);
		return "projects/project_settings";
	}

	/**
	 * Delete a project from the UI. Will redirect to user's projects page on completion.
	 *
	 * @param projectId the {@link Project} id to delete
	 * @param confirm   confirmation checkbox to delete
	 * @return a redirect to the users's project page on completion.
	 */
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	@PreAuthorize("hasPermission(#projectId, 'canManageLocalProjectSettings')")
	public String deleteProject(@PathVariable Long projectId,
			@RequestParam(required = false, defaultValue = "") String confirm) {
		if (confirm.equals("true")) {
			projectService.delete(projectId);

			return "redirect:/projects";
		}

		return "redirect: /projects/" + projectId + "/settings/delete";
	}
}
