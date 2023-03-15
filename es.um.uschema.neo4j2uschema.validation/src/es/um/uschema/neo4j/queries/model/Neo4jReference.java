package es.um.uschema.neo4j.queries.model;

import java.util.HashMap;
import java.util.Map;

public class Neo4jReference
{
	private String relId;
	private String originId;
	private Map<String, Object> properties;
	private String type;
	private String targetNodeId;
	private String targetLabels;
	private String originLabels;

	public Neo4jReference(String relId, String originId, Map<String, Object> properties, String type, String targetNodeId,
			String originLabels, String targetLabels)
	{
		this(originId, properties, type, targetNodeId);
		this.relId = relId;
		this.targetLabels = targetLabels;
		this.originLabels = originLabels;
	}

	public Neo4jReference(String originId, Map<String, Object> properties, String type, String targetNodeId)
	{
		this.originId = originId;
		this.properties = properties;
		this.type = type;
		this.targetNodeId = targetNodeId;
	}

	public String getRelId()
	{
		return relId;
	}

	public String getOriginId()
	{
		return originId;
	}

	public Map<String, Object> getProperties()
	{
		return properties;
	}

	public Map<String, Object> getPropertiesCopy()
	{
		return new HashMap<String, Object>(properties);
	}

	public void setRelId(String relId)
	{
		this.relId = relId;
	}

	public String getType()
	{
		return type;
	}

	public String getTargetNodeId()
	{
		return targetNodeId;
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

	@Override
	public String toString()
	{
		return "Neo4jReference [relId=" + relId + ", originId=" + originId + ", properties=" + properties + ", type="
				+ type + ", targetNodeId=" + targetNodeId + ", targetLabels=" + targetLabels + ", originLabels="
				+ originLabels + "]";
	}

}
