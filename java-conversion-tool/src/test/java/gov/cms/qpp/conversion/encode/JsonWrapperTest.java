package gov.cms.qpp.conversion.encode;

import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class JsonWrapperTest {

	ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
	
	public JsonWrapper<Object> objectObjWrapper;
	public JsonWrapper<String> objectStrWrapper;
	public JsonWrapper<Object> listObjWrapper;
	public JsonWrapper<String> listStrWrapper;
	
	@Before
	public void before() {
		objectObjWrapper = new JsonWrapper<>();
		objectStrWrapper = new JsonWrapper<>();
		listObjWrapper   = new JsonWrapper<>();
		listStrWrapper   = new JsonWrapper<>();
	}

	
	
	@Test
	public void testInitAsList() {
		assertTrue("Object should be null until the first put", listStrWrapper.getObject() == null);
		listStrWrapper.put("name");
		Object list1 = listStrWrapper.getObject();
		assertNotNull("Init should be as a list", list1);
		assertTrue("Init should be as a list", list1 instanceof List);
		listStrWrapper.put("value");
		Object list2 = listStrWrapper.getObject();
		assertEquals("The internal instance should not change upon addition put", list1,list2);
	}
	
	
	@Test
	public void testInitAsObject() {
		assertTrue("Object should be null until the first put", objectStrWrapper.getObject() == null);
		objectStrWrapper.put("name", "value");
		Object obj1 = objectStrWrapper.getObject();
		assertNotNull("Init should be as a map", obj1);
		assertTrue("Init should be as a map", obj1 instanceof Map);
		objectStrWrapper.put("name", "value");
		Object obj2 = objectStrWrapper.getObject();
		assertEquals("The internal instance should not change upon addition put", obj1,obj2);
	}
	
	@Test(expected=IllegalStateException.class)
	public void testCheckState_objectThenList() {
		objectStrWrapper.put("name", "value");
		objectStrWrapper.put("value");
		fail("should not make it here, prior line should throw exception");
	}
	@Test(expected=IllegalStateException.class)
	public void testCheckState_listThenLObject() {
		listStrWrapper.put("value");
		listStrWrapper.put("name", "value");
		fail("should not make it here, prior line should throw exception");
	}
	
	@Test
	public void testGetObject_map() {
		objectStrWrapper.put("name", "value");
		assertTrue("should be as a map", objectStrWrapper.getObject() instanceof Map);
		assertEquals("map should contain put value", 
				"value", ((Map<?,?>)objectStrWrapper.getObject()).get("name"));
		
		Object obj = new Object();
		objectObjWrapper.put("name", obj);
		assertTrue("should be as a map",  objectObjWrapper.getObject() instanceof Map);
		assertEquals("map should contain put value",
				obj, ((Map<?,?>)objectObjWrapper.getObject()).get("name"));
	}	
	
	@Test
	public void testGetObject_list() {
		listStrWrapper.put("name");
		assertTrue("should be as a list", listStrWrapper.getObject() instanceof List);
		assertTrue("lsit should contian put value",
				((List<?>)listStrWrapper.getObject()).contains("name"));
		
		Object obj = new Object();
		listObjWrapper.put(obj);
		assertTrue("should be as a list", listObjWrapper.getObject() instanceof List);
		assertTrue("lsit should contian put value",
				((List<?>)listObjWrapper.getObject()).contains(obj));
	}	
	
	@Test
	public void testIsObject_true() {
		assertFalse("should not be an object container until first put", objectStrWrapper.isObject());
		objectStrWrapper.put("name", "value");
		assertTrue("should be an object container after first put", objectStrWrapper.isObject());
		
		assertFalse("should not be an object container until first map put", objectObjWrapper.isObject());
		objectObjWrapper.put("name", new Object());
		assertTrue("should be an object container after first map put", objectObjWrapper.isObject());
	}
	
	@Test
	public void testIsObject_false() {
		assertFalse("should not be an object container", listStrWrapper.isObject());
		listStrWrapper.put("name");
		assertFalse("should not be an object container after first list put", listStrWrapper.isObject());
		
		assertFalse("should not be an object container", listObjWrapper.isObject());
		listObjWrapper.put(new Object());
		assertFalse("should not be an object container after first list put", listObjWrapper.isObject());
	}
	
	
	@Test
	public void testJackson_simpleObject() throws Exception {
		Map<String, String> map = new LinkedHashMap<>();
		map.put("name1", "value1");
		map.put("name2", "value2");
		
		String json = ow.writeValueAsString(map);
		
		String expect = "{\n  \"name1\" : \"value1\",\n  \"name2\" : \"value2\"\n}";
		assertEquals("expect a simple object of JSON",
				expect, json);
	}

	@Test
	public void testJackson_objectWithArray() throws Exception {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("name1", "value1");
		map.put("name2", new String[] {"A","B","C"});
		map.put("name3", "value3");
		
		String json = ow.writeValueAsString(map);
		
		String expect = "{\n  \"name1\" : \"value1\",\n"+
				"  \"name2\" : [ \"A\", \"B\", \"C\" ],\n" +
				"  \"name3\" : \"value3\"\n}";
		assertEquals("expect array to use [] rather than {} block",
				expect, json);
	}
	
	@Test
	public void testJackson_objectWithList() throws Exception {
		List<String> list = new LinkedList<>();
		list.add("A");
		list.add("B");
		list.add("C");
		
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("name1", "value1");
		map.put("name2", list);
		map.put("name3", "value3");
		
		String json = ow.writeValueAsString(map);
		
		String expect = "{\n  \"name1\" : \"value1\",\n"+
				"  \"name2\" : [ \"A\", \"B\", \"C\" ],\n" +
				"  \"name3\" : \"value3\"\n}";
		assertEquals("expect list to look like array",
				expect, json);
	}
	
	@Test
	public void testJackson_objectWithChild() throws Exception {
		Map<String, Object> obj = new LinkedHashMap<>();
		obj.put("obj1", "A");
		obj.put("obj2", "B");
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("name1", "value1");
		map.put("name2", obj);
		map.put("name3", "value3");
		
		
		
		String json = ow.writeValueAsString(map);
		
		String expect = "{\n  \"name1\" : \"value1\",\n"+
				"  \"name2\" : {\n    \"obj1\" : \"A\",\n    \"obj2\" : \"B\"\n  },\n" +
				"  \"name3\" : \"value3\"\n}";
		assertEquals("expect comma after child and no comma after last value pair",
				expect, json);
	}
	
	
	@Test
	public void testJackson_objectWithChild_commaAndOrder() throws Exception {
		Map<String, Object> obj = new LinkedHashMap<>();
		obj.put("obj1", "A");
		obj.put("obj2", "B");
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("name1", "value1");
		map.put("name3", "value3");
		map.put("name2", obj);
		
		String json = ow.writeValueAsString(map);
		
		String expect = "{\n  \"name1\" : \"value1\",\n"+
				"  \"name3\" : \"value3\",\n" +
				"  \"name2\" : {\n    \"obj1\" : \"A\",\n    \"obj2\" : \"B\"\n  }\n}";
		assertEquals("expect no comma expected after the child and order as inserted",
				expect, json);
	}

	@Test
	public void testToString_simpleObject() throws Exception {
		objectStrWrapper.put("name1", "value1");
		objectStrWrapper.put("name2", "value2");
		
		String json = objectStrWrapper.toString();
		
		String expect = "{\n  \"name1\" : \"value1\",\n  \"name2\" : \"value2\"\n}";
		assertEquals("expect a simple object of JSON",
				expect, json);
	}

	@Test
	public void testToString_objectWithArray() throws Exception {
		objectObjWrapper.put("name1", "value1");
		objectObjWrapper.put("name2", new String[] {"A","B","C"});
		objectObjWrapper.put("name3", "value3");
		
		String json = objectObjWrapper.toString();
		
		String expect = "{\n  \"name1\" : \"value1\",\n"+
				"  \"name2\" : [ \"A\", \"B\", \"C\" ],\n" +
				"  \"name3\" : \"value3\"\n}";
		assertEquals("expect array to use [] rather than {} block",
				expect, json);
	}
	
	@Test
	public void testToString_objectWithList() throws Exception {
		listStrWrapper.put("A");
		listStrWrapper.put("B");
		listStrWrapper.put("C");
		
		objectObjWrapper.put("name1", "value1");
		objectObjWrapper.put("name2", listStrWrapper.getObject());
		objectObjWrapper.put("name3", "value3");
		
		String json = objectObjWrapper.toString();
		
		String expect = "{\n  \"name1\" : \"value1\",\n"+
				"  \"name2\" : [ \"A\", \"B\", \"C\" ],\n" +
				"  \"name3\" : \"value3\"\n}";
		assertEquals("expect list to look like array",
				expect, json);
	}
	
	@Test
	public void testToString_objectWithChild() throws Exception {
		objectStrWrapper.put("obj1", "A");
		objectStrWrapper.put("obj2", "B");
		
		objectObjWrapper.put("name1", "value1");
		objectObjWrapper.put("name2", objectStrWrapper.getObject());
		objectObjWrapper.put("name3", "value3");
		
		String json = objectObjWrapper.toString();
		
		String expect = "{\n  \"name1\" : \"value1\",\n"+
				"  \"name2\" : {\n    \"obj1\" : \"A\",\n    \"obj2\" : \"B\"\n  },\n" +
				"  \"name3\" : \"value3\"\n}";
		assertEquals("expect comma after child and no comma after last value pair",
				expect, json);
	}
	
	
	@Test
	public void testToString_objectWithChild_commaAndOrder() throws Exception {
		objectStrWrapper.put("obj1", "A");
		objectStrWrapper.put("obj2", "B");
		
		objectObjWrapper.put("name1", "value1");
		objectObjWrapper.put("name3", "value3");
		objectObjWrapper.put("name2", objectStrWrapper.getObject());
		
		String json = objectObjWrapper.toString();
		
		String expect = "{\n  \"name1\" : \"value1\",\n"+
				"  \"name3\" : \"value3\",\n" +
				"  \"name2\" : {\n    \"obj1\" : \"A\",\n    \"obj2\" : \"B\"\n  }\n}";
		assertEquals("expect no comma expected after the child and order as inserted",
				expect, json);
	}
	
	@Test(expected=RuntimeException.class)
	public void testToString_exception() {
		objectObjWrapper.put("name", new MockBadJsonTarget());
		objectObjWrapper.toString();
		fail("should not get here, expecting runtime parse exception");
	}
}

class MockBadJsonTarget {
	private String var = "";
	@SuppressWarnings("unused")
	private String getVar() {
		return var;
	}
}