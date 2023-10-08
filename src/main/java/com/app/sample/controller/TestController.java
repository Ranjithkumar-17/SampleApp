package com.app.sample.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

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
	String yamlSchemaSubProp = "";
	static String subPropStr = "";
	static StringBuffer schema = new StringBuffer("{\"SCHEMAS\":{");
	static StringBuffer schemaSubProp = new StringBuffer("{");
	static boolean flag = false;
//	static boolean flag2 = false;
	static Map<String,Boolean> flag2 = new HashMap<String,Boolean>();

	@GetMapping(value = "v1/test")
	public String getEmployeeDtls() throws IOException {

		String filePath = "C:\\Ranjith\\Projects\\samplse.json";
		String jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));

		// Object Mapper
		ObjectMapper objMapper = new ObjectMapper();
		JsonNode json = objMapper.readTree(jsonContent);

		StringBuffer ReqResStr = new StringBuffer("\"REQUEST\":{\"type\":\"object\",\"properties\":{");

		// main iteration for request response json
		json.fields().forEachRemaining(key -> {

			flag = false;
			String keyName = key.getKey();
			String keyType = key.getValue().getNodeType().toString();
			JsonNode keyValue = key.getValue();

			if (keyType.equalsIgnoreCase("OBJECT")) {
				flag = true;
				TestController.generateSubJson(keyValue, keyName);
			}

			String str = "\"" + keyName + "\":{\"type\":\"" + (flag ? "array" : "string") + "\","
					+ (flag ? "\"items\": {\"$ref\":" : "\"enum\":")
					+ (flag ? "\"#/components/schemas/" + keyName + "Dtls\"}" : "[" + keyValue + "]") + "}";

			ReqResStr.append(str + ",");

		});

		// Replace the last comma with close brace
		ReqResStr.replace(ReqResStr.length() - 1, ReqResStr.length(), "}}");

		// Adding object to schema
		schema.append(ReqResStr + "}}");

		System.out.println(schema);

		try {
			ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
			JsonNode jsonschemaProp = objMapper.readTree(schema.toString());
			yamlSchemaProp = yamlMapper.writeValueAsString(jsonschemaProp);

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return yamlSchemaProp + " " + yamlSchemaSubProp;
	}

	public static void generateSubJson(JsonNode jsonObject, String jsonKey) {

		StringBuffer ReqResStr = new StringBuffer("\"" + jsonKey + "Dtls\":{\"type\":\"object\",\"properties\":{");

		jsonObject.fields().forEachRemaining(key -> {
			
			String keyName = key.getKey();
			String keyType = key.getValue().getNodeType().toString();
			JsonNode keyValue = key.getValue();
			flag2.put(keyName,false);
			
			if (keyType.equalsIgnoreCase("OBJECT")) {
				
				flag2.replace(keyName, true);
				
				TestController.generateSubJson(keyValue, keyName);
			}

			String str = "\"" + keyName + "\":{\"type\":\"" + (Boolean.valueOf(flag2.get(keyName)) ? "array" : "string") + "\","
					+ (Boolean.valueOf(flag2.get(keyName)) ? "\"items\": {\"$ref\":" : "\"enum\":")
					+ (Boolean.valueOf(flag2.get(keyName)) ? "\"#/components/schemas/" + keyName + "Dtls\"}" : "[" + keyValue + "]") + "}";

			ReqResStr.append(str + ",");

		});

		ReqResStr.replace(ReqResStr.length() - 1, ReqResStr.length(), "}}");
		schema.append(ReqResStr + ",");
	}

}