# Projeto ner-amazoninf-plugin
Plugin do elastic que processa o texto do campo texto_processamento e cria as tags de acordo com a aplicação dos modelos NER cadastrados.

## Geração do plugin
A execução do pom.xml gera o arquivo /target/release/ner-amazoninf-plugin-0.0.2-SNAPSHOT.zip.

## Instalação do plugin
1) Parar o elasticsearch e apagar o diretório <elastic_home>\plugins

2) Copiar o plugin para um diretório temporário e entrar no diretório <elastic_home>\bin e executar o comando de instalação

```
.\elasticsearch-plugin.bat install file:///C:/tmp/ner-amazoninf-plugin-0.0.2-SNAPSHOT.zip
```

Confirmar a instalação, esta confirmação é necessária pois existem diretivas de segurança necessárias no arquivo [plugin-security.policy](https://github.com/alderjv/ner-amazoninf-plugin/blob/main/src/main/resources/plugin-security.policy)

Após a isnstalação inicializar o elasticsearch
