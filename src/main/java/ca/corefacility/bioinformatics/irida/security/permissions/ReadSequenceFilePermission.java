package ca.corefacility.bioinformatics.irida.security.permissions;

import java.util.List;

import org.springframework.security.core.Authentication;

import ca.corefacility.bioinformatics.irida.model.Project;
import ca.corefacility.bioinformatics.irida.model.Sample;
import ca.corefacility.bioinformatics.irida.model.SequenceFile;
import ca.corefacility.bioinformatics.irida.model.User;
import ca.corefacility.bioinformatics.irida.model.joins.Join;
import ca.corefacility.bioinformatics.irida.repositories.SampleRepository;
import ca.corefacility.bioinformatics.irida.repositories.UserRepository;
import ca.corefacility.bioinformatics.irida.repositories.joins.project.ProjectSampleJoinRepository;
import ca.corefacility.bioinformatics.irida.repositories.joins.project.ProjectUserJoinRepository;

/**
 * Evaluate whether or not an authenticated user can read a sequence file.
 * 
 * @author Franklin Bristow <franklin.bristow@phac-aspc.gc.ca>
 * 
 */
public class ReadSequenceFilePermission extends BasePermission<SequenceFile> {

	private static final String PERMISSION_PROVIDED = "canReadSequenceFile";

	/**
	 * Construct an instance of {@link ReadSequenceFilePermission}.
	 */
	public ReadSequenceFilePermission() {
		super(SequenceFile.class, "sequenceFileRepository");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean customPermissionAllowed(Authentication authentication, SequenceFile sf) {

		// similar to samples, an authenticated user can only read a sequence
		// file if they are participating in the project owning the sample
		// owning the sequence file.
		SampleRepository sampleRepository = getApplicationContext().getBean(SampleRepository.class);
		UserRepository userRepository = getApplicationContext().getBean(UserRepository.class);
		ProjectUserJoinRepository pujRepository = getApplicationContext().getBean(ProjectUserJoinRepository.class);
		ProjectSampleJoinRepository psjRepository = getApplicationContext().getBean(ProjectSampleJoinRepository.class);

		Join<Sample, SequenceFile> sampleSequenceFile = sampleRepository.getSampleForSequenceFile(sf);
		List<Join<Project, Sample>> projectForSample = psjRepository.getProjectForSample(sampleSequenceFile.getSubject());
		for(Join<Project, Sample> projectSample : projectForSample){

			List<Join<Project, User>> projectUsers = pujRepository.getUsersForProject(projectSample.getSubject());
			User u = userRepository.loadUserByUsername(authentication.getName());

			for (Join<Project, User> projectUser : projectUsers) {
				if (u.equals(projectUser.getObject())) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPermissionProvided() {
		return PERMISSION_PROVIDED;
	}

}
