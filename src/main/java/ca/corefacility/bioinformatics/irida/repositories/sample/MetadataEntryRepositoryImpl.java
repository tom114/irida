package ca.corefacility.bioinformatics.irida.repositories.sample;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.sample.MetadataTemplateField;
import ca.corefacility.bioinformatics.irida.model.sample.metadata.MetadataEntry;

public class MetadataEntryRepositoryImpl implements MetadataEntryRepositoryCustom {
	private DataSource dataSource;

	@Autowired
	public MetadataEntryRepositoryImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public List<MetadataResponse> getMetadataForSampleAndFieldSorted(Project project, List<MetadataTemplateField> fields) {
		NamedParameterJdbcTemplate tmpl = new NamedParameterJdbcTemplate(dataSource);
		MapSqlParameterSource parameters = new MapSqlParameterSource();

		Map<String, MetadataTemplateField> entryMap = new HashMap<>();

		int counter = 0;

		String selectSql = "SELECT s.id as 'sample'";
		String fromSql = " FROM project_sample p INNER JOIN sample s ON s.id=p.sample_id";
		String whereSql = " WHERE p.project_id=:project";

		parameters.addValue("project", project.getId());

		for (MetadataTemplateField field : fields) {
			counter++;
			String fieldCounter = "c" + counter;
			String whereField = "field" + counter;

			entryMap.put(whereField, field);

			String fieldSql =
					" LEFT JOIN " + "(SELECT sample_id, e.value FROM metadata_entry e WHERE field_id=:" + whereField
							+ ")" + fieldCounter + " ON p.sample_id=" + fieldCounter + ".sample_id";

			selectSql = selectSql + ", " + fieldCounter + ".value as '" + whereField + "'";

			fromSql = fromSql + fieldSql;

			parameters.addValue(whereField, field.getId());
		}

		String combinedQuery = selectSql + fromSql + whereSql;

		System.out.println(combinedQuery);

		List<MetadataResponse> metadataResponses = tmpl.query(combinedQuery, parameters, new RowMapper<MetadataResponse>() {
			@Override
			public MetadataResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
				Set<MetadataEntry> entries = new HashSet<>();

				for (String key : entryMap.keySet()) {
					String entryValue = rs.getString(key);

					MetadataEntry entry = new MetadataEntry(entryValue, "text", entryMap.get(key));
					entries.add(entry);
				}

				Long sampleId = rs.getLong("sample");

				return new MetadataResponse(sampleId, entries);
			}
		});

		return metadataResponses;
	}
}
