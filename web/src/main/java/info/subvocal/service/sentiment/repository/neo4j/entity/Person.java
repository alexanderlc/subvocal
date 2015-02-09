package info.subvocal.service.sentiment.repository.neo4j.entity;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import java.util.Set;

/**
 *
 */
@NodeEntity
public class Person {

    @GraphId
    private Long nodeId;
    private String id;
    private String name;

    public Person(String name) {
        this.name = name;
    }

    @RelatedTo(type = "DISAGREE")
    Set<Url> disagreeWith;

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Url> getDisagreeWith() {
        return disagreeWith;
    }

    public void setDisagreeWith(Set<Url> disagreeWith) {
        this.disagreeWith = disagreeWith;
    }
}
