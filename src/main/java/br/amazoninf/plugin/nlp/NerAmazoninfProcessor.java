package br.amazoninf.plugin.nlp;

import static org.elasticsearch.ingest.ConfigurationUtils.readOptionalList;
import static org.elasticsearch.ingest.ConfigurationUtils.readOptionalStringProperty;
import static org.elasticsearch.ingest.ConfigurationUtils.readStringProperty;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.Strings;
import org.elasticsearch.ingest.AbstractProcessor;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;

import net.sf.json.JSONObject;

public class NerAmazoninfProcessor extends AbstractProcessor {

	private static final Logger logger = LogManager.getLogger(NerAmazoninfService.class);

	static final String TYPE = "nerprocessador";

	private final NerAmazoninfService nerAmazoninfService;
	private final String sourceField;
	private final List<String> fields;
	private static final String CAMPOS_NER = "n";

	NerAmazoninfProcessor(NerAmazoninfService nerAmazoninfService, String tag, String sourceField, String targetField,
			String annotatedTextField, List<String> fields) {
		super(tag);
		this.nerAmazoninfService = nerAmazoninfService;
		this.sourceField = sourceField;
		this.fields = fields;
		logger.info(" ############################### NerAmazoninfProcessor construtor ");
	}

	public static final class Factory implements Processor.Factory {

		private NerAmazoninfService nerAmazoninfService;

		Factory(NerAmazoninfService nerAmazoninfService) {
			this.nerAmazoninfService = nerAmazoninfService;
		}

		@Override
		public NerAmazoninfProcessor create(Map<String, Processor.Factory> registry, String processorTag,
				Map<String, Object> config) {
			logger.info(" ############################### NerAmazoninfProcessor create ");
			String field = readStringProperty(TYPE, processorTag, config, "field");
			String targetField = readStringProperty(TYPE, processorTag, config, "target_field", "entities");
			String annotatedTextField = readOptionalStringProperty(TYPE, processorTag, config, "annotated_text_field");
			List<String> fields = readOptionalList(TYPE, processorTag, config, "fields");

			return new NerAmazoninfProcessor(nerAmazoninfService, processorTag, field, targetField, annotatedTextField,
					fields);
		}
	}

	@Override
	public IngestDocument execute(IngestDocument ingestDocument) throws Exception {
		// logger.info(" ############################### NerAmazoninfProcessor execute
		// ");
		String conteudo = ingestDocument.getFieldValue(sourceField, String.class);

		if (Strings.hasLength(conteudo)) {

			Map<String, Set<String>> entities = new HashMap<>();
			// mergeExisting(entities, ingestDocument, targetField);
			// conteudo = conteudo.replaceAll(
			// "(\\.|:|\\(|\\)|!|\\?|,|-|\"|'|�|�| the | The | Abstract | � | � | �s | �s |
			// numa | numas | duma | dumas | num | nuns | no | nos | na | nas | aos | ao |
			// por | um | uma | umas | uns | pela | pelas | pelo | pelos | ora | pare | para
			// | como | do | da | dos | das | de | a | o | e | em | as | os | se | )",
			// "");

			try {
				JSONObject camposNer = nerAmazoninfService.find(conteudo, fields);

				@SuppressWarnings("unchecked")
				Iterator<String> iCampos = camposNer.keys();

				/*
				 * while (iCampos.hasNext()) { String key = iCampos.next();
				 * ingestDocument.setFieldValue(key, camposNer.getString(key)); }
				 */
				ingestDocument.setFieldValue(CAMPOS_NER, camposNer);
			} catch (Exception e) {
				logger.error(" Erro na chamada NerAmazoninfService.find() ", e);
			}

		}

		return ingestDocument;
	}

	@Override
	public String getType() {
		return TYPE;
	}
}
