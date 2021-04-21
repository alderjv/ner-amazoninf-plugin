package br.amazoninf.plugin.nlp.verbo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Paradigma {
	private String verbo = "";
	private String radical = "";
	private String sufixo = "";

	private Map<String, String> variacoes;
	private List<String> verbos;

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("paradigma:" + this.verbo + ":" + this.sufixo + "\n");
		Map<String, String> variacoes = new HashMap<>();

		for (String key : this.variacoes.keySet()) {
			if (variacoes.containsKey(this.variacoes.get(key))) {
				String linha = variacoes.get(this.variacoes.get(key));
				linha = String.valueOf(linha) + ":" + key;
				variacoes.put(this.variacoes.get(key), linha);
				continue;
			}
			variacoes.put(this.variacoes.get(key), key);
		}

		for (String key : variacoes.keySet()) {
			String linha = String.valueOf(key) + ":" + (String) variacoes.get(key);
			sb.append(String.valueOf(linha) + "\n");
		}
		return sb.toString();
	}

	public Paradigma() {
		this.variacoes = new HashMap<>();
		this.verbos = new ArrayList<>();
	}

	public String getVerbo() {
		return this.verbo;
	}

	public void setVerbo(String verbo) {
		this.verbo = verbo.trim();
	}

	public String getSufixo() {
		return this.sufixo;
	}

	public void setSufixo(String sufixo) {
		this.sufixo = sufixo.trim();
	}

	public Map<String, String> getVariacoes() {
		return this.variacoes;
	}

	public void setVariacoes(Map<String, String> variacoes) {
		this.variacoes = variacoes;
	}

	public List<String> getVerbos() {
		return this.verbos;
	}

	public void setVerbos(List<String> verbos) {
		this.verbos = verbos;
	}

	public String getRadical() {
		return this.radical;
	}

	public void setRadical(String radical) {
		this.radical = radical.trim();
	}
}
