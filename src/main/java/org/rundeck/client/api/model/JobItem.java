package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by greg on 3/28/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Root(strict = false)
public class JobItem {
    @Element(required = false)
    private String id;
    @Element
    private String name;
    @Element(required = false)
    private String group;
    @Element
    private String project;
    private String description;
    @Attribute(required = false)
    private String href;
    @Element(required = false)
    private String permalink;

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

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    @Override
    public String toString() {
        return "org.rundeck.client.api.model.JobItem{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", group='" + group + '\'' +
               ", project='" + project + '\'' +
               ", description='" + description + '\'' +
               ", href='" + href + '\'' +
               ", permalink='" + permalink + '\'' +
               '}';
    }

    public String toBasicString() {
        return String.format("[%s] %s%s", id, group != null ? group + "/" : "", name);
    }
}
