package ca.corefacility.bioinformatics.irida.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import ca.corefacility.bioinformatics.irida.model.enums.ExportUploadState;
import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SequenceFile;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SequenceFilePair;

/**
 * Class storing a request to upload sequence data to NCBI.
 */
@Entity
@Table(name = "ncbi_export_submission")
@EntityListeners(AuditingEntityListener.class)
public class NcbiExportSubmission implements IridaThing {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "project_id")
	private Project project;

	@Column(name = "bio_project_id", nullable = false)
	private String bioProjectId;

	@Column(name = "namespace", nullable = false)
	private String ncbiNamespace;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "ncbi_export_submission_single_files", inverseJoinColumns = @JoinColumn(name = "sequence_file_id") )
	private List<SequenceFile> singleFiles;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "ncbi_export_submission_pair_files", inverseJoinColumns = @JoinColumn(name = "pair_file_id") )
	private List<SequenceFilePair> pairFiles;

	@Column(name = "created_date", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate;

	@LastModifiedDate
	@Column(name = "modified_date")
	@Temporal(TemporalType.TIMESTAMP)
	private Date modifiedDate;

	@Column(name = "upload_state", nullable = false)
	@Enumerated(EnumType.STRING)
	private ExportUploadState uploadState;

	public NcbiExportSubmission() {
		uploadState = ExportUploadState.NEW;
		createdDate = new Date();
	}

	public NcbiExportSubmission(Project project, List<SequenceFile> singleFiles, List<SequenceFilePair> pairFiles) {
		this();
		this.project = project;
		this.singleFiles = singleFiles;
		this.pairFiles = pairFiles;
	}

	public NcbiExportSubmission(Project project, String bioProjectId, String ncbiNamespace,
			List<SequenceFile> singleFiles, List<SequenceFilePair> pairFiles) {
		this();
		this.project = project;
		this.bioProjectId = bioProjectId;
		this.ncbiNamespace = ncbiNamespace;
		this.singleFiles = singleFiles;
		this.pairFiles = pairFiles;
	}

	@Override
	public Date getCreatedDate() {
		return createdDate;
	}

	@Override
	public Date getModifiedDate() {
		return modifiedDate;
	}

	@Override
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	@Override
	public String getLabel() {
		return "NCBI Submission Project " + project.getLabel() + " " + createdDate;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public Project getProject() {
		return project;
	}

	public List<SequenceFilePair> getPairFiles() {
		return pairFiles;
	}

	public List<SequenceFile> getSingleFiles() {
		return singleFiles;
	}

	public void setUploadState(ExportUploadState uploadState) {
		this.uploadState = uploadState;
	}

	public ExportUploadState getUploadState() {
		return uploadState;
	}

	public String getNcbiNamespace() {
		return ncbiNamespace;
	}

	public void setNcbiNamespace(String ncbiNamespace) {
		this.ncbiNamespace = ncbiNamespace;
	}

	public String getBioProjectId() {
		return bioProjectId;
	}

	public void setBioProjectId(String bioProjectId) {
		this.bioProjectId = bioProjectId;
	}
}
