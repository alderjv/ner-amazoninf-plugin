package br.amazoninf.plugin.nlp.model;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import opennlp.tools.util.TrainingParameters;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class IaModel implements Serializable {

	private static final long serialVersionUID = -5754550900916562914L;

	private String iaModelID;
	
	private String analysisID;
	
	private Date modifiedDate;

	private String dictionaryItensNotTraining;

	private String modelName;

	private String md5Model;

	private byte[] modelBin;

	private Boolean lemmatizeBeforeApplyingModel;
		
	private TrainingParameters trainingParameters;
}
