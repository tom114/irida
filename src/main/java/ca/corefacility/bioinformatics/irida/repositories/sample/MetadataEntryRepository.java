package ca.corefacility.bioinformatics.irida.repositories.sample;

import ca.corefacility.bioinformatics.irida.model.sample.MetadataTemplateField;
import ca.corefacility.bioinformatics.irida.model.sample.metadata.HierarchicalMetadataEntry;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MetadataEntryRepository {

	@Query("FROM HierarchicalMetadataEntry e WHERE e.header=?1")
	public List<HierarchicalMetadataEntry> getEntiresForHeader(MetadataTemplateField field);
}
