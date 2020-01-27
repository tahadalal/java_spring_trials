package com.example.demo;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

import com.example.demo.ClassWithKeysInEnum.Properties;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

public class PreDefinedMapApplication {

	public static void main(String[] args) {
		System.out.println(
			ClassWithKeysInEnum
				.builder()
					.property(Properties.prop1, "value1")
					.property(Properties.prop2, "value2")
				.build()
				.getProperties()
		);
		
		BuilderExample2 builder = BuilderExample2.builder()
			.prop1("value3")
			.prop2("value4")
			.build();
		
		ObjectMapper mapper = new ObjectMapper();
		System.out.println(mapper.convertValue(builder, Map.class));
		
		
	}
	
	

}

@Builder
class ClassWithKeysInEnum {
	
	public enum Properties {
		prop1,
		prop2
	}
	
	@Singular
	private Map<Properties,String> properties;
	
	public Map<String, String> getProperties(){
		
		Map<String, String> stringProperties = new HashMap<String, String>();
		
		this.properties.forEach((key,value) -> {
			stringProperties.put(key.toString(), value);
		});
		
		return stringProperties;
	}
	
	
	
}


@Builder
@Getter
class BuilderExample2 {
	
	private String prop1;
	private String prop2;
	
}

