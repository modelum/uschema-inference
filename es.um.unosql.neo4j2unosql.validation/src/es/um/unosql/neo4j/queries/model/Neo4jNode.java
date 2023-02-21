package es.um.unosql.neo4j.queries.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import es.um.unosql.neo4j.queries.utils.StringNameUtils;

public class Neo4jNode
{
	private String nodeId;
	private String name;
	private Map<String, Object> properties;
	private List<Neo4jReference> relationships;
	private List<String> labels;


	public Neo4jNode(String name, Map<String, Object> properties)
	{
		this.name = name;

		this.properties = new HashMap<String, Object>(properties);
		this.relationships = new LinkedList<Neo4jReference>();
	}
	
	public Neo4jNode(String nodeId, String name, Map<String, Object> properties)
	{
		this(name, properties);
		this.nodeId = nodeId;
	}
	
	public Neo4jNode(String nodeId, String name, Map<String, Object> properties,
			List<Map<String, Object>> referencesProperties, List<String> referenceType, 
			List<String> targetLabels, List<Long> targetNodeIds, List<String> originLabels, List<Long> relsId)
	{
		this(nodeId, name, properties);
		
		this.labels = originLabels;
		
		for (int i = 0; i < referenceType.size(); i++)
		{
			Neo4jReference neo4jReference = new Neo4jReference(
					Long.toString(relsId.get(i)),
					nodeId,
					referencesProperties.get(i), 
					referenceType.get(i), 
					Long.toString(targetNodeIds.get(i)),
					StringNameUtils.targetLabels(originLabels),
					StringNameUtils.targetLabels(targetLabels.get(i))
			);
			relationships.add(neo4jReference);
		}
	}

	public String getId()
	{
		return nodeId;
	}
	
	public String getName()
	{
		return name;
	}

	public Map<String, Object> getProperties()
	{
		return properties;
	}
	
	public List<Neo4jReference> getRelationships()
	{
		return relationships;
	}

	public Map<String, Object> getPropertiesCopy()
	{
		return new HashMap<String, Object>(properties);
	}

	@Override
	public String toString()
	{
		return "Neo4jNode [nodeId=" + nodeId + ", name=" + name + ", properties=" + properties + ", relationships="
				+ relationships + ", labels=" + labels + "]";
	}

	
}
