package br.amazoninf.plugin.nlp.verbo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Conjugue {
	private static Conjugue instance;

	public static Conjugue getInstance() throws IOException {
		if (instance == null) {
			instance = new Conjugue();
		}
		return instance;
	}

	private HashMap<String, String> verbos = new HashMap<>();
	private HashMap<String, Paradigma> paradigmas = new HashMap<>();
	private HashMap<String, Paradigma> paradigmas_regulares = new HashMap<>();

	public Conjugue() throws IOException {
		InputStream examplex = Conjugue.class.getResourceAsStream("/verbos2.properties");
		BufferedReader in = new BufferedReader(new InputStreamReader(examplex));

		Paradigma paradigma = null;
		boolean geral = false;
		String str;
		while ((str = in.readLine()) != null) {
			if (str.trim().isEmpty())
				continue;
			if (str.trim().toCharArray()[0] == '#')
				continue;
			String[] dados = str.replaceAll("::", ": :").split(":");

			if (dados[0].equals("paradigma")) {
				if (str.trim().equals("paradigma:")) {
					geral = true;

					continue;
				}
				String sufixo = "";
				if (dados.length == 2) {
					sufixo = dados[1].substring(dados[1].length() - 2);
				} else {
					sufixo = dados[2];
				}
				if (paradigma != null) {
					this.paradigmas.put(paradigma.getVerbo(), paradigma);
					if (!this.paradigmas_regulares.containsKey(paradigma.getSufixo())) {
						this.paradigmas_regulares.put(paradigma.getSufixo(), paradigma);
					}
				}
				paradigma = new Paradigma();
				paradigma.setVerbo(dados[1]);
				if (dados[1].length() - sufixo.length() - 1 > 0) {
					paradigma.setRadical(dados[1].substring(0, dados[1].length() - sufixo.length()));
				} else {
					paradigma.setRadical("");
				}
				paradigma.setSufixo(sufixo);
				this.verbos.put(dados[1].toLowerCase().trim(), paradigma.getVerbo().toLowerCase().trim());
				continue;
			}
			if (dados.length > 1) {
				for (int i = 1; i < dados.length; i++) {
					paradigma.getVariacoes().put(dados[i], dados[0]);
					this.verbos.put(dados[i].toLowerCase().trim(), paradigma.getVerbo().toLowerCase().trim());
				}
				continue;
			}
			if (geral) {
				String sufixo = dados[0].trim().substring(dados[0].length() - 2);
				paradigma = this.paradigmas_regulares.get(sufixo);
				Paradigma paradigma_v = geraParadigma(paradigma, dados[0].trim());
				this.paradigmas.put(dados[0].trim(), paradigma_v);
				continue;
			}
			Paradigma paradigma_v = geraParadigma(paradigma, dados[0]);
			this.paradigmas.put(paradigma_v.getVerbo(), paradigma_v);
		}

		this.paradigmas.put(paradigma.getVerbo(), paradigma);
		if (!this.paradigmas_regulares.containsKey(paradigma.getSufixo())) {
			this.paradigmas_regulares.put(paradigma.getSufixo(), paradigma);
		}
	}

	private Paradigma geraParadigma(Paradigma paradigma, String verbo) {
		Paradigma paradigma_v = new Paradigma();
		paradigma_v.setVerbo(verbo);
		if (paradigma.getRadical().length() > 0) {
			if (paradigma_v.getVerbo().length() - paradigma.getSufixo().length() > 0) {
				paradigma_v.setRadical(paradigma_v.getVerbo().substring(0,
						paradigma_v.getVerbo().length() - paradigma.getSufixo().length()));
			}
		} else if (verbo.length() - paradigma.getSufixo().length() > 0) {
			paradigma_v.setRadical(verbo.substring(0, verbo.length() - paradigma.getSufixo().length()));
		} else {
			paradigma_v.setRadical("");
		}
		paradigma_v.setSufixo(paradigma.getSufixo());
		paradigma.getVerbos().add(verbo.trim());
		this.verbos.put(verbo.toLowerCase().trim(), paradigma_v.getVerbo().toLowerCase().trim());

		for (String variacao : paradigma.getVariacoes().keySet()) {
			if (!variacao.trim().isEmpty()) {
				String sufixo2 = "";
				try {
					sufixo2 = variacao.trim().substring(paradigma.getRadical().length());
				} catch (Exception e) {
					System.out.println("Erro:" + variacao + "\t" + paradigma.getRadical());
					e.printStackTrace();
					return null;
				}
				Map<String, String> regras = new HashMap<>();
				String radical0 = paradigma_v.getRadical();
				String radical1 = paradigma.getRadical();
				String radical2 = variacao.substring(0, variacao.length() - sufixo2.length());
				for (int i = 0; i < radical1.length(); i++) {
					if (radical1.toCharArray()[i] != radical2.toCharArray()[i]) {
						regras.put(String.valueOf(radical1.toCharArray()[i]),
								String.valueOf(radical2.toCharArray()[i]));
					}
				}
				String radical3 = "";
				for (int i = 0; i < (radical0.toCharArray()).length; i++) {
					String letra = String.valueOf(radical0.toCharArray()[i]);
					String letra2 = regras.get(letra);
					if (letra2 == null) {
						radical3 = String.valueOf(radical3) + letra;
					} else {
						radical3 = String.valueOf(radical3) + letra2;
					}
				}
				String variacao2 = String.valueOf(radical3) + sufixo2.trim();
				paradigma_v.getVariacoes().put(variacao2.trim(), paradigma.getVariacoes().get(variacao));
				this.verbos.put(variacao2.toLowerCase().trim(), paradigma_v.getVerbo().toLowerCase().trim());
			}
		}
		return paradigma_v;
	}

	public Paradigma conjugue(String verbo) {
		if (this.verbos.containsKey(verbo.toLowerCase().trim())) {
			String paradigmaname = this.verbos.get(verbo.toLowerCase().trim());
			Paradigma paradigma = this.paradigmas.get(paradigmaname);
			return paradigma;
		}

		return null;
	}

	
	public void testName() throws Exception {
		String[] verbos2 = { "subscrever" };
		byte b;
		int i;
		String[] arrayOfString;
		for (i = (arrayOfString = verbos2).length, b = 0; b < i;) {
			String string = arrayOfString[b];
			Paradigma p = conjugue(string);
			if (p != null) {

				System.out.println(p.getVerbo());
				System.out.println("---------//-----------");
				System.out.println(p.toString());
				System.out.println("-----------------------");
			}
			b++;
		}
		for (String k : this.verbos.keySet()) {
			if (((String) this.verbos.get(k)).toLowerCase().equals("subscrever"))
				System.out.println(String.valueOf(this.verbos.get(k)) + ":" + k);
		}
	}
}
