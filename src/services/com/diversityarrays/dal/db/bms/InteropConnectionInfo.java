package com.diversityarrays.dal.db.bms;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.sqldb.JdbcConnectionParameters;

import org.apache.commons.collections15.Closure;
import org.apache.commons.collections15.ClosureUtils;

import com.diversityarrays.dal.db.DbUtil;

class InteropConnectionInfo {
	
		Connection connection;
				
		private final JdbcConnectionParameters connectionParams;		
		
		@SuppressWarnings("unchecked")
		InteropConnectionInfo(JdbcConnectionParameters connectionParams, Closure<String> progress) throws DalDbException {
			
			if (progress==null) {
				progress = ClosureUtils.nopClosure();
			}
			
			this.connectionParams = connectionParams;
			
			List<SQLException> errors = new ArrayList<SQLException>();
			try {
				if (connectionParams != null) {
					progress.execute("Connecting to " + connectionParams.connectionUrl);
					connection = createConnection();
				}
			}catch(SQLException sqlex){
				throw new DalDbException(sqlex.getMessage());
			}
				
		}

		public Connection createConnection() throws SQLException {
			return DbUtil.createConnection(connectionParams);
		}
		
		public Connection getConnection() {
			return connection;
		}
		
		public void closeConnection() {			
			if (connection != null) {
				try { connection.close(); }
				catch (SQLException ignore) {}
			}
			
		}
		
}
