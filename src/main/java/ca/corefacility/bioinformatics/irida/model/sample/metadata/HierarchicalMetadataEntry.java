package ca.corefacility.bioinformatics.irida.model.sample.metadata;

import ca.corefacility.bioinformatics.irida.model.sample.MetadataTemplateField;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Audited
@Table(name = "hierarchical_metadata_entry")
public class HierarchicalMetadataEntry extends MetadataEntry {

	@OneToMany
	private List<HierarchicalMetadataEntry> children;

	@ManyToOne
	private MetadataTemplateField field;

	public HierarchicalMetadataEntry(String value, String type) {
		super(value, type);
	}

	public List<HierarchicalMetadataEntry> getChildren() {
		return children;
	}

	public void setChildren(List<HierarchicalMetadataEntry> children) {
		this.children = children;
	}

	public MetadataTemplateField getField() {
		return field;
	}

	public void setField(MetadataTemplateField field) {
		this.field = field;
	}
}
