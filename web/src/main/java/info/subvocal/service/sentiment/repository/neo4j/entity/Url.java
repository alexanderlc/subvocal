package info.subvocal.service.sentiment.repository.neo4j.entity;

import org.springframework.data.neo4j.annotation.GraphId;

/**
 * Created by paul on 08/02/15.
 */
public class Url {

    @GraphId
    private Long nodeId;
    private String id;
    private String url;

    public Url(String url) {
        this.url = url;
    }

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
