package com.app.sample.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

@RestController
@RequestMapping("api")
public class TestController {

	String yamlSchemaProp = "";
	String yamlSchemaSubProp="";
	static String subPropStr = "";
	static StringBuffer schema = new StringBuffer("\"SCHEMAS\":{");
	static StringBuffer schemaSubProp = new StringBuffer("{");
	boolean flag = false;
	
	@GetMapping(value = "v1/test")
	public String getEmployeeDtls() throws IOException {

		String filePath = "D:\\Project\\JsonObject.txt";

		String jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));

		ObjectMapper objMapper = new ObjectMapper();

		JsonNode json = objMapper.readTree(jsonContent);

//      // Convert JsonNode to YAML string		

//         String yamlString = yamlMapper.writeValueAsString(json);
//         System.out.println("yaml format  : "+yamlString);

		
		StringBuffer schemaProp = new StringBuffer("\"REQUEST\":{\"type\":\"object\",\"properties\":{");
		json.fields().forEachRemaining(entry -> {
			flag = false;
			String fieldName = entry.getKey();
			String type = entry.getValue().getNodeType().toString();
			JsonNode fieldValue = entry.getValue();
			
			if(type.equalsIgnoreCase("OBJECT")) {
				flag = true;
				TestController.generateSubJson(fieldValue,fieldName);
			}
			
            String propStr = "\"" + fieldName + "\":{\"type\":\"" + ( flag ? "array" :type.toLowerCase() ) + "\"," +  ( flag ? "\"items\": $ref :"  : "\"enum\":" )
					+ ( flag ? "\"#/components/schemas/"+fieldName+"Dtls\"" : "["+fieldValue+"]" ) + "}";
            schemaProp.append(propStr + ",");
			
		});
		
		schemaProp.replace(schemaProp.length() - 1, schemaProp.length(), "}}");
		schema.append(schemaProp+"}");
		
		System.out.println(schema);
		
		try {
			ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
			JsonNode jsonschemaProp = objMapper.readTree(schema.toString());
			yamlSchemaProp = yamlMapper.writeValueAsString(jsonschemaProp);
			
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		System.out.println(yamlSchemaSubProp);
		return yamlSchemaProp + " "+ yamlSchemaSubProp ;
	}
	
	public static void generateSubJson(JsonNode fieldValue, String fieldName ) {
		
		StringBuffer schemaProp = new StringBuffer("\""+fieldName+"Dtls\":{\"type\":\"object\",\"properties\":{");
		fieldValue.fields().forEachRemaining(entry2 -> {
    		String fieldName2 = entry2.getKey();
			String type2 = entry2.getValue().getNodeType().toString();
			JsonNode fieldValue2 = entry2.getValue();
			
			String subPropStr = "\"" + fieldName2 + "\":{\"type\":\"" + type2.toLowerCase() + "\"," + "\"enum\":" + "["
					+ fieldValue2 + "]}";
			schemaProp.append(subPropStr + ",");
			
    	});
		schemaProp.replace(schemaProp.length() - 1, schemaProp.length(), "}}");
		schema.append(schemaProp+",");
		System.out.println("sub schema "+schemaProp);
	}
	
}