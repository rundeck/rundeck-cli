package org.rundeck.client.api.model.scheduler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TakeoverServerItem {

  public String  uuid;
  public Boolean all;

  public String getUuid() {
    return uuid;
  }

  public TakeoverServerItem setUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  public Boolean getAll() {
    return all;
  }

  public TakeoverServerItem setAll(Boolean all) {
    this.all = all;
    return this;
  }

  @Override
  public String toString() {
    return "{" +
        "uuid='" + uuid + '\'' +
        ", all=" + all +
        '}';
  }
}
