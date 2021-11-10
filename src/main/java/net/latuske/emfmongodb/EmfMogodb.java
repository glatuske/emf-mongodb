package net.latuske.emfmongodb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.emfjson.jackson.resource.JsonResourceFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

import net.latuske.emfmogodb.model.Address;
import net.latuske.emfmogodb.model.MyFactory;
import net.latuske.emfmogodb.model.Person;

public class EmfMogodb {

	public static void main(String[] args) throws IOException {
		MongoClient mongoClient = new MongoClient();
		DB database = mongoClient.getDB("emfmongo-db");
		DBCollection collection = database.getCollection("emfmongo-collection");

		DBObject removeQuery = new BasicDBObject();
		collection.findAndRemove(removeQuery);

		Address address1 = MyFactory.eINSTANCE.createAddress();
		address1.setCity("Kornwestheim");

		Person person1 = MyFactory.eINSTANCE.createPerson();
		person1.setName("Gregor");
		person1.getAddresses().add(address1);

		Address address2 = MyFactory.eINSTANCE.createAddress();
		address2.setCity("Kornwestheim");

		Person person2 = MyFactory.eINSTANCE.createPerson();
		person2.setName("Sabrina");
		person2.getAddresses().add(address2);

		long start = System.currentTimeMillis();
		DBObject object1 = saveToDbObject(person1);
		System.out.println("Save: " + (System.currentTimeMillis() - start));
		start = System.currentTimeMillis();
		collection.insert(object1);
		System.out.println("Insert: " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		DBObject query1 = new BasicDBObject("name", "Gregor");
		DBCursor cursor = collection.find(query1);
		System.out.println(cursor.one());
		System.out.println("Find: " + (System.currentTimeMillis() - start));

		DBObject query2 = new BasicDBObject("name", "Sabrina");
		cursor = collection.find(query2);
		System.out.println(cursor.one());

		DBObject object2 = saveToDbObject(person2);
		collection.insert(object2);

		cursor = collection.find(query2);
		System.out.println(cursor.one());

		collection.findAndRemove(removeQuery);
		cursor = collection.find(query1);
		System.out.println(cursor.one());
	}

	private static DBObject saveToDbObject(Person person1) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("json", new JsonResourceFactory());
		Resource resource = resourceSet.createResource(URI.createFileURI("my.json"));
		resource.getContents().add(person1);
		resource.save(outputStream, Map.of());

		String jsonString = outputStream.toString();
		return (DBObject) JSON.parse(jsonString);
	}

}
