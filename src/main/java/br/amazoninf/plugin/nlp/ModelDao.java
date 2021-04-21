package br.amazoninf.plugin.nlp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import br.amazoninf.plugin.nlp.model.IaModel;

public class ModelDao {
	private static final Logger logger = LogManager.getLogger(NerAmazoninfService.class);

	public IaModel getById(String id) throws SQLException {
		IaModel model = null;
		Connection conexao = new ConnectionFactory().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {

			ps = conexao.prepareStatement("select * from Aizon2..IAModel where IAModelID = ?");

			ps.setString(1, id);

			rs = ps.executeQuery();

			if (rs.next()) {
				model = new IaModel();
				model.setIaModelID(rs.getString("IAModelID"));
				model.setMd5Model(rs.getString("MD5Model"));
				model.setModelBin(rs.getBytes("ModelBin"));
				model.setModelName(rs.getString("ModelName"));
			}

		} catch (Exception e) {
			logger.error("Erro ao obter dados do banco ", e);
		} finally {
			rs.close();
			ps.close();
			conexao.close();
		}

		return model;

	}

	public IaModel getResumo(String id) throws SQLException {
		IaModel model = null;
		Connection conexao = new ConnectionFactory().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {

			ps = conexao
					.prepareStatement("select IAModelID,ModelName,MD5Model from Aizon2..IAModel where IAModelID = ?");

			ps.setString(1, id);

			rs = ps.executeQuery();

			if (rs.next()) {
				model = new IaModel();
				model.setIaModelID(rs.getString("IAModelID"));
				model.setModelName(rs.getString("ModelName"));
				model.setMd5Model(rs.getString("MD5Model"));
			}

		} catch (Exception e) {
			logger.error("Erro ao obter resumo", e);
		} finally {
			rs.close();
			ps.close();
			conexao.close();
		}

		return model;

	}

}
