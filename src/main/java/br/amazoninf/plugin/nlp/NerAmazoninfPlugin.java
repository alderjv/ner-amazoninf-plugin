package br.amazoninf.plugin.nlp;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import org.elasticsearch.ingest.Processor;
import org.elasticsearch.plugins.IngestPlugin;
import org.elasticsearch.plugins.Plugin;

public class NerAmazoninfPlugin extends Plugin implements IngestPlugin{
	 static final String NAME = "ner-amazoninf-plugin";
	 @Override
	    public Map<String, Processor.Factory> getProcessors(Processor.Parameters parameters) {
	        Path configDirectory = parameters.env.configFile().resolve(NAME);
	        NerAmazoninfService nerAmazoninfService = new NerAmazoninfService(configDirectory, parameters.env.settings());
	       // nerAmazoninfService.start();

	        return Collections.singletonMap(NerAmazoninfProcessor.TYPE, new NerAmazoninfProcessor.Factory(nerAmazoninfService));
	    }

}
