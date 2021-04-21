package br.amazoninf.plugin.nlp;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.SpecialPermission;

import com.microsoft.sqlserver.jdbc.SQLServerDriver;

public class ConnectionFactory {
	private static final Logger logger = LogManager.getLogger(NerAmazoninfService.class);

	private Connection conn = null;

	public Connection getConnection() {

		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			// unprivileged code such as scripts do not have SpecialPermission
			sm.checkPermission(new SpecialPermission());
		}

		AccessController.doPrivileged(new PrivilegedAction<Connection>() {

			@Override
			public Connection run() {
				try {
					DriverManager.registerDriver(new SQLServerDriver());
					conn = DriverManager.getConnection("jdbc:sqlserver://localhost;databaseName=Aizon2", "xx",
							"xx");

				} catch (Exception e) {
					logger.error("Erro ao obter conexao", e);
				}

				return conn;
			}
		});
		
		/*
		try {
			DriverManager.registerDriver(new SQLServerDriver());
			conn = DriverManager.getConnection("jdbc:sqlserver://192.168.1.15;databaseName=Aizon2", "sa",
					"@aizon@1234?");

		} catch (Exception e) {
			logger.error("Erro ao obter conexao", e);
		}
*/
		return conn;
	}

}
