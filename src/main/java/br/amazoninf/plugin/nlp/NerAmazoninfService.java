package br.amazoninf.plugin.nlp;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.RandomDocumentPicks;

import br.amazoninf.plugin.nlp.model.IaModel;
import br.amazoninf.plugin.nlp.verbo.Conjugue;
import br.amazoninf.plugin.nlp.verbo.Paradigma;
import net.sf.json.JSONObject;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

public class NerAmazoninfService {

	// private static final long serialVersionUID = 3344128184022281250L;

	private static final Logger logger = LogManager.getLogger(NerAmazoninfService.class);

	private SentenceDetectorME detector;
	private TokenizerME tokenizer;
	private List<NameFinderME> listNF;
	private static final String ESPACO_BRANCO = " ";
	private static final String CAMPO_VERBOS = "VERBOS";
	private static final String CAMPO_VERBO = "VERBO";
	private static final String CAMPO_RADICAL = "RADICAL";
	private static final String CAMPO_CONJUGACAO = "CONJUGACAO";
	private static final String TERMOS_INVALIDOS = "\\b(\\.|:|\\(|\\)|!|\\?|,|-|\"|'|�|�|the|abstract|�|�|�s|�s|numa|numas|duma|dumas|num|nuns|no|nos|na|nas|aos|ao|por|um|uma|umas|uns|pela|pelas|pelo|pelos|ora|pare|para|como|do|da|dos|das|de|a|o|e|em|as|os|se|the|on|of|in|and|abstract|objective|plataforma|volume|portal|anexo)\\b";
	private static final String MD5 = "MD5";
	private static final String BINARIO_MODELO = "BINARIO_MODELO";

	// Metodo main para teste da classe
	public static void main(String[] args) throws IOException {

		String input = "O Jos� Carlos de Ara�jo filho de seu pai Andr� Marques de Ara�jo, residente � Rua Olivio Dultra Melo na cidade de Azedo a 500km de Belo Horizonte - MG foi envolvido em um acidente com Maria de Nazar� Melo no bairro de Santo Andr� na cidade de Monte Carmelo, a pessoa acusada de cometer o acidente foi Camila Pitanga, o valor estimado dos preju�zos foi de R$ 50.000,00 (cinq�enta mil reais) e acabou com o roubo, assalto e assasinato muito bem planejados";

		try {
			// System.out.println(new TesteNLP().getNomesProprios(input));

			System.out.println(new NerAmazoninfService(null, null).find(input, null));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public NerAmazoninfService(Path configDirectory, Settings settings) {
		// logger.info(" ############################### NerAmazoninfService Construtor
		// ");

		try {
			detector = getDetector();
			tokenizer = getTokenizer();
			listNF = getListNF();

		} catch (Exception e) {
			logger.error("Erro ao obter processadores NLP ", e);
		}

	}

	public synchronized JSONObject find(String conteudo, List<String> fields) {
		// logger.info(" ############################### NerAmazoninfService find ");

		String sentences[] = detector.sentDetect(conteudo);
		JSONObject jsonObject = new JSONObject();

		StringBuilder valorCampo = new StringBuilder();

		for (String sentence : sentences) {
			if (Strings.hasLength(sentence)) {
				try {

					String tokens[] = tokenizer.tokenize(sentence);

					// jsonObject = conjugarVerbos(tokens, jsonObject);

					for (NameFinderME nameFinder : listNF) {
						Span[] nameSpans = nameFinder.find(tokens);

						for (Span campo : nameSpans) {
							valorCampo.append(spanToString(campo, tokens));
							if (!seTermosInvalidos(valorCampo)) {
								jsonObject.accumulate(campo.getType(), valorCampo.toString());
							}
							valorCampo.delete(0, valorCampo.length());
						}
					}
				} catch (Exception e) {
					logger.error("Erro ao processar metodo find(). Tamanho da sentenca " + sentence.length()
							+ " \nsentenca: " + sentence + " \nconteudo: " + conteudo, e);
				}
			}
		}

		return jsonObject;
	}

	private List<NameFinderME> getListNF() {

		IaModel iaModelDefault = new IaModel();
		IaModel iaModelSisp = new IaModel();
		InputStream isNerDefault = null;
		InputStream isNerSisp = null;

		iaModelDefault.setIaModelID("D95484C5-B6EB-4CEB-ABBC-8BAF8D8F83C2");
		iaModelSisp.setIaModelID("46C65FE3-CE76-4398-A0BD-5E6902BA0766");

		ModelDao modelDao = new ModelDao();

		try {
			iaModelDefault = modelDao.getResumo(iaModelDefault.getIaModelID());
			iaModelSisp = modelDao.getResumo(iaModelSisp.getIaModelID());
		} catch (SQLException e1) {
			logger.error("Erro no banco de dados ", e1);
		}

		isNerDefault = getStream(iaModelDefault, modelDao);
		isNerSisp = getStream(iaModelSisp, modelDao);

		/*
		 * //File file = new File(
		 * "C:/Users/Administrator/AppData/Local/Temp/2/D95484C5-B6EB-4CEB-ABBC-8BAF8D8F83C2.bin"
		 * ); File file = new File(iaModelDefault.getModelName());
		 * 
		 * try { isNerDefault = new FileInputStream(file);
		 * md5Valido(iaModelDefault.getMd5Model(), isNerDefault); } catch (Exception e1)
		 * { e1.printStackTrace(); }
		 */

		// InputStream isNerSisp =
		// this.getClass().getClassLoader().getResourceAsStream("ner/ner-aizon-sisp.bin");
		// InputStream isNerSisp =
		// this.getClass().getClassLoader().getResourceAsStream("ner/ner-teste-relato.bin");

		TokenNameFinderModel modelNerDefault = null;
		TokenNameFinderModel modelNerSisp = null;

		try {
			modelNerDefault = new TokenNameFinderModel(isNerDefault);
			modelNerSisp = new TokenNameFinderModel(isNerSisp);
		} catch (Exception e) {
			logger.error("Erro ao obter NER", e);
		}

		List<NameFinderME> listNF = new ArrayList<NameFinderME>();
		listNF.add(new NameFinderME(modelNerDefault));
		listNF.add(new NameFinderME(modelNerSisp));

		return listNF;
	}

	private InputStream getStream(IaModel iaModel, ModelDao modelDao) {
		InputStream isNer = null;
		
		//TODO: Verificar como recuperar um indice existente
		/*
		 * IngestDocument ingestDocument = new IngestDocument("modelo", null,
		 * iaModel.getMd5Model(), null, null, null, null);
		 * 
		 * if (ingestDocument.hasField(MD5)) { logger.info("Encontrou o campo MD5");
		 * InputStream conteudo = ingestDocument.getFieldValue(BINARIO_MODELO,
		 * InputStream.class);
		 * 
		 * logger.info("Recuperou o binario"); return conteudo; }
		 */

		try {

			iaModel = modelDao.getById(iaModel.getIaModelID());
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(iaModel.getModelBin());
			isNer = byteArrayInputStream;

			// TODO: Gravar no indice os dados do modelo
			// ingestDocument.setFieldValue(BINARIO_MODELO, isNer);
			// ingestDocument.setFieldValue(MD5, iaModel.getMd5Model());

		} catch (Exception e) {
			logger.error("Erro ao obter stream ", e);
		}

		return isNer;
	}

	private boolean md5Valido(String md5File, File fileTmp) {
		String md5FileTmp = StringUtils.EMPTY;
		try {
			md5FileTmp = DigestUtils.md5Hex(new FileInputStream(fileTmp));
		} catch (Exception e) {
			logger.error("Erro ao validar md5", e);
		}
		return md5File.equals(md5FileTmp);
	}

	private TokenizerME getTokenizer() {
		TokenizerModel tokenModel = null;
		InputStream inputStreamTokenizer = this.getClass().getClassLoader().getResourceAsStream("ner/pt-token.bin");
		try {
			tokenModel = new TokenizerModel(inputStreamTokenizer);
		} catch (Exception e) {
			logger.error("Erro ao obter tokenizador", e);
		}
		TokenizerME tokenizer = new TokenizerME(tokenModel);

		return tokenizer;
	}

	private SentenceDetectorME getDetector() {
		SentenceModel sentModel = null;
		// this.getClass().getClassLoader().getResourceAsStream("ner/pt-sent_customizado.bin");
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("ner/pt-sent_customizado.bin");
		try {
			sentModel = new SentenceModel(inputStream);
		} catch (Exception e) {
			logger.error("Erro ao obter modelo de sentencas", e);
		}
		SentenceDetectorME detector = new SentenceDetectorME(sentModel);

		return detector;
	}

	private boolean seTermosInvalidos(StringBuilder valorCampo) {
		return valorCampo.toString().toLowerCase().trim().matches(TERMOS_INVALIDOS);
	}

	private JSONObject conjugarVerbos(String[] tokens, JSONObject json) {
		try {
			for (String token : tokens) {
				Paradigma p = Conjugue.getInstance().conjugue(token.toLowerCase().trim());
				if (p != null) {

					JSONObject jsonVerbo = new JSONObject();
					jsonVerbo.accumulate(CAMPO_VERBO, p.getVerbo());

					jsonVerbo.element(CAMPO_VERBO, p.getVerbo()).accumulate(CAMPO_RADICAL, p.getRadical());

					p.getVariacoes().forEach((k, v) -> {
						jsonVerbo.element(CAMPO_VERBO, p.getVerbo()).accumulate(CAMPO_CONJUGACAO, k);
					});

					if (!json.containsValue(jsonVerbo)) {
						json.accumulate(CAMPO_VERBOS, jsonVerbo);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Erro ao conjugar verbos", e);
		}
		return json;
	}

	private String spanToString(Span span, String[] tokens) {

		StringBuilder valorCampo = new StringBuilder();

		valorCampo.setLength(0);
		for (int ti = span.getStart(); ti < span.getEnd(); ti++) {
			valorCampo.append(tokens[ti]).append(ESPACO_BRANCO);
		}

		return valorCampo.toString();
	}

}
