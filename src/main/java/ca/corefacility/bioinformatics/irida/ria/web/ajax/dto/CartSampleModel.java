package ca.corefacility.bioinformatics.irida.ria.web.ajax.dto;

import ca.corefacility.bioinformatics.irida.model.sample.Sample;

/**
 * Used to represent a {@link Sample} on the UI Cart Page.
 * Keeping this as simple as possible as there could be a lot of these asked for.
 */
public class CartSampleModel {
	private final Long id;
	private final String label;

	public CartSampleModel(Sample sample) {
		this.id = sample.getId();
		this.label = sample.getLabel();
	}

	public Long getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}
}
