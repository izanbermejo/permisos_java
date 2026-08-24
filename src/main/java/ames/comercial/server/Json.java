package ames.comercial.server;

import ames.comercial.server.exception.AppException;
import ames.comercial.server.exception.UtilsException.ParsedJson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Json {

	private ObjectMapper mapper;
	
	public Json (ObjectMapper mapper) {
		this.mapper = mapper;
	}
	
	public JsonNode serialize (Object value) {
		return (value == null) ? null : mapper.valueToTree(value);
	}

	public JsonNode serialize (String value) {
		try {
			return mapper.readTree(value);
		} catch (Exception error) {
			throw new ParsedJson(error);
		} 
	}
	
	public <T> T deserialize (String json, TypeReference<T> destination) {
		try {
			return mapper.readValue(json, destination);
		} catch (JsonProcessingException procErr) {
			throw new AppException(procErr);
		} 
	}	

	public <T> T deserialize (String json, Class<T> destination) {
		try {
			return (json == null) ? null : mapper.readValue(json, destination);
		} catch (JsonProcessingException procErr) {
			throw new AppException(procErr);
		} 
	}

}
