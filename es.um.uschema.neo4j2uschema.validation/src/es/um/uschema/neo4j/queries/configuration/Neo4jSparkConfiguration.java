package es.um.uschema.neo4j.queries.configuration;

public class Neo4jSparkConfiguration
{

	private String appName;
	private String master;
	private String user;
	private String password;
	private String url;

	public Neo4jSparkConfiguration(String appName)
	{
		this.appName = appName;
	}

	public Neo4jSparkConfiguration master(String master)
	{
		this.master = master;

		return this;
	}

	public Neo4jSparkConfiguration local(int threads)
	{
		this.master = "local[" + threads + "]";

		return this;
	}

	public Neo4jSparkConfiguration user(String user)
	{
		this.user = user;

		return this;
	}

	public Neo4jSparkConfiguration password(String password)
	{
		this.password = password;

		return this;
	}

	public Neo4jSparkConfiguration url(String url)
	{
		this.url = url;

		return this;
	}

	public String getAppName()
	{
		return appName;
	}

	public String getMaster()
	{
		return master;
	}

	public String getUser()
	{
		return user;
	}

	public String getPassword()
	{
		return password;
	}

	public String getUrl()
	{
		return url;
	}

}
