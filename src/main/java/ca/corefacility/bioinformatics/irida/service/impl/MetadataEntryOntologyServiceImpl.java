package ca.corefacility.bioinformatics.irida.service.impl;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.springframework.core.io.ClassPathResource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MetadataEntryOntologyServiceImpl {

	private static final List<String> GENEPIO_FILES = Lists.newArrayList(
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/apollo_sv_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/bccdc_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/bfo.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/brenda_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/chebi_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/chmo_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/doid_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/efo_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/envo_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/eo_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/ero_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/evs_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/fix_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/foodon_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/gazetteer_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/gaz_insdc_mapping.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/genepio.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/geo_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/hardcoded.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/hp_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/ido_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/irida_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/mapping_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/model_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/ncbitaxon_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/ncit_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/ndf-rt_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/oae_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/obi_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/omit_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/ontology-metadata.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/pato_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/pco_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/po_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/public_health_lab_epi_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/ro_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/sio_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/so_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/stato_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/typon_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/uberon_import.owl",
			"/ca/corefacility/bioinformatics/irida/ontology/genepio/uo_import.owl");

	private Model model;
	private Dataset dataset;

	public MetadataEntryOntologyServiceImpl() {
		dataset = DatasetFactory.createMem();

		model = dataset.getDefaultModel();

		GENEPIO_FILES.stream()
				.forEach(f -> {
					ClassPathResource resource = new ClassPathResource(f);
					Path path = Paths.get(resource.getPath());
					RDFDataMgr.read(model, path.toString());
				});
	}

	public List<String> getSymptom(String symptom) {
		// @formatter:off
		String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
				+ "PREFIX obo: <http://purl.obolibrary.org/obo/>\n "
				+" SELECT DISTINCT ?subject ?search\n"
				+ "WHERE { ?pred rdfs:subPropertyOf obo:IAO_0000118.\n"
				+ "  {?subject ?pred ?search} UNION {?subject rdfs:label ?search}.\n"
				+ "  ?subject rdfs:subClassOf* obo:HP_0000118\n"
				+ "  FILTER regex(?search, ?term, 'i').}\n";
		// @formatter:on

		ParameterizedSparqlString query = new ParameterizedSparqlString(queryString);

		query.setLiteral("term", QueryParser.escape(symptom));
		Query q = query.asQuery();
		QueryExecution qexec = QueryExecutionFactory.create(q, dataset);
		ResultSet result = qexec.execSelect();

		List<String> results = new ArrayList<>();
		while (result.hasNext()) {
			QuerySolution next = result.next();
			String search = next.getLiteral("search")
					.getString();

			results.add(search);
		}

		return results;
	}

	public List<String> getBuilding(String symptom) {
		// @formatter:off
		String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
				+ "PREFIX obo: <http://purl.obolibrary.org/obo/>\n "
				+" SELECT DISTINCT ?subject ?search\n"
				+ "WHERE { ?pred rdfs:subPropertyOf obo:IAO_0000118.\n"
				+ "  {?subject ?pred ?search} UNION {?subject rdfs:label ?search}.\n"
				+ "  ?subject rdfs:subClassOf* <http://purl.obolibrary.org/obo/ENVO_00000073>\n"
				+ "  FILTER regex(?search, ?term, 'i').}\n";
		// @formatter:on

		ParameterizedSparqlString query = new ParameterizedSparqlString(queryString);

		query.setLiteral("term", QueryParser.escape(symptom));
		Query q = query.asQuery();
		QueryExecution qexec = QueryExecutionFactory.create(q, dataset);
		ResultSet result = qexec.execSelect();

		List<String> results = new ArrayList<>();
		while (result.hasNext()) {
			QuerySolution next = result.next();
			String search = next.getLiteral("search")
					.getString();

			results.add(search);
		}

		return results;
	}

	public List<String> getBodypart(String symptom) {
		// @formatter:off
		String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
				+ "PREFIX obo: <http://purl.obolibrary.org/obo/>\n "
				+" SELECT DISTINCT ?subject ?search\n"
				+ "WHERE { ?pred rdfs:subPropertyOf obo:IAO_0000118.\n"
				+ "  {?subject ?pred ?search} UNION {?subject rdfs:label ?search}.\n"
				+ "  ?subject rdfs:subClassOf* <http://purl.obolibrary.org/obo/UBERON_0000061>\n"
				+ "  FILTER regex(?search, ?term, 'i').}\n";
		// @formatter:on

		ParameterizedSparqlString query = new ParameterizedSparqlString(queryString);

		query.setLiteral("term", QueryParser.escape(symptom));
		Query q = query.asQuery();
		QueryExecution qexec = QueryExecutionFactory.create(q, dataset);
		ResultSet result = qexec.execSelect();

		List<String> results = new ArrayList<>();
		while (result.hasNext()) {
			QuerySolution next = result.next();
			String search = next.getLiteral("search")
					.getString();

			results.add(search);
		}

		return results;
	}

}
