package es.um.unosql.neo4j.queries.model;

import java.util.HashMap;
import java.util.Map;

public class Neo4jReferenceToNewObject
{
	private String originId;
	private Map<String, Object> properties;
	private String type;
	private String targetLabels;
	private String originLabels;
	private Neo4jNode targetNode;

	public Neo4jReferenceToNewObject(String originId, String originLabels, HashMap<String, Object> properties,
			String type, Neo4jNode targetNode)
	{
		this.originId = originId;
		this.originLabels = originLabels;
		this.properties = properties;
		this.type = type;
		this.targetNode = targetNode;
	}

	public String getOriginId()
	{
		return originId;
	}

	public void setOriginId(String originId)
	{
		this.originId = originId;
	}

	public Map<String, Object> getProperties()
	{
		return properties;
	}

	public void setProperties(Map<String, Object> properties)
	{
		this.properties = properties;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getTargetLabels()
	{
		return targetLabels;
	}

	public void setTargetLabels(String targetLabels)
	{
		this.targetLabels = targetLabels;
	}

	public String getOriginLabels()
	{
		return originLabels;
	}

	public void setOriginLabels(String originLabels)
	{
		this.originLabels = originLabels;
	}

	public Neo4jNode getTargetNode()
	{
		return targetNode;
	}

	public void setTargetNode(Neo4jNode targetNode)
	{
		this.targetNode = targetNode;
	}

	@Override
	public String toString()
	{
		return "Neo4jReferenceToNewObject [originId=" + originId + ", properties=" + properties + ", type=" + type
				+ ", targetLabels=" + targetLabels + ", originLabels=" + originLabels + ", targetNode=" + targetNode
				+ "]";
	}

}
