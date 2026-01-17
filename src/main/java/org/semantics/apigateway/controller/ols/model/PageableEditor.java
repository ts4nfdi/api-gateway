package org.semantics.apigateway.controller.ols.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.beans.PropertyEditorSupport;

public class PageableEditor extends PropertyEditorSupport {
  
  private final ObjectMapper mapper = new ObjectMapper();
  
  @Override
  public void setAsText(String text) throws IllegalArgumentException {
    if (StringUtils.isEmpty(text)) {
      setValue(null);
    } else {
      Pageable pageable;
      try {
        pageable = mapper.readValue(text, Pageable.class);
      } catch (JsonProcessingException e) {
        throw new IllegalArgumentException(e);
      }
      setValue(pageable);
    }
  }
}
