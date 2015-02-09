package info.subvocal.service.sentiment.repository.neo4j.entity;

import org.springframework.data.neo4j.annotation.NodeEntity;

/**
 *
 */
@NodeEntity
public class Domain {

    private String url;

    public Domain(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
