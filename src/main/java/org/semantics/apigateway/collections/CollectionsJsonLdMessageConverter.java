package org.semantics.apigateway.collections;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import ioinformarics.oss.jackson.module.jsonld.JsonldModule;
import org.semantics.apigateway.collections.models.TerminologyCollectionDto;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

@Component
public class CollectionsJsonLdMessageConverter implements HttpMessageConverter<Collection<TerminologyCollectionDto>> {
  
  public static final String MEDIA_TYPE_APPLICATION_LD_JSON_VALUE = "application/ld+json";
  public static final MediaType MEDIA_TYPE_APPLICATION_LD_JSON = MediaType.valueOf(MEDIA_TYPE_APPLICATION_LD_JSON_VALUE);
  
  private static final ObjectMapper customObjectMapper;
  
  static {
    customObjectMapper = new ObjectMapper();
    customObjectMapper.registerModule(new JsonldModule());
    customObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
  }
  
  
  @Override
  public boolean canRead(Class<?> clazz, @Nullable MediaType mediaType) {
    return false;
  }
  
  @Override
  public boolean canWrite(Class<?> clazz, @Nullable MediaType mediaType) {
    return Collection.class.isAssignableFrom(clazz) && MEDIA_TYPE_APPLICATION_LD_JSON.equals(mediaType);
  }
  
  @Override
  public List<MediaType> getSupportedMediaTypes() {
    return List.of(MEDIA_TYPE_APPLICATION_LD_JSON);
  }
  
  @Override
  public Collection<TerminologyCollectionDto> read(Class<? extends Collection<TerminologyCollectionDto>> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
    return List.of();
  }
  
  @Override
  public void write(Collection<TerminologyCollectionDto> terminologyCollectionDtos, @Nullable MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    outputMessage.getHeaders().setContentType(MEDIA_TYPE_APPLICATION_LD_JSON);
    try (OutputStream out = outputMessage.getBody()) {
      customObjectMapper.writeValue(out, terminologyCollectionDtos);
    }
  }
}
