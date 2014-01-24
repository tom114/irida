package ca.corefacility.bioinformatics.irida.config.data;

import java.util.Properties;

import javax.sql.DataSource;

/**
 * Common interface for database configuration files.
 * 
 * @author Franklin Bristow <franklin.bristow@phac-aspc.gc.ca>
 * @author Tom Matthews <thomas.matthews@phac-aspc.gc.ca>
 * 
 */
public interface DataConfig {
	/**
	 * Construct the {@link DataSource} used to connect to a database.
	 * 
	 * @return the {@link DataSource}.
	 */
	public DataSource dataSource();

	/**
	 * Get the Properties to be used by the data source.
	 * 
	 * @return
	 */
	public Properties getJpaProperties();

}
