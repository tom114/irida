package ca.corefacility.bioinformatics.irida.repositories.sample;

import java.util.Set;

import ca.corefacility.bioinformatics.irida.model.sample.metadata.MetadataEntry;

public class MetadataResponse {

	long sampleId;
	Set<MetadataEntry> entries;

	public MetadataResponse(long sampleId, Set<MetadataEntry> entries) {
		this.sampleId = sampleId;
		this.entries = entries;
	}

	public long getSampleId() {
		return sampleId;
	}

	public Set<MetadataEntry> getEntries() {
		return entries;
	}
}
