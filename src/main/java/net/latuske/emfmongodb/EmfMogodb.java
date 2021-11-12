package net.latuske.emfmongodb;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import net.latuske.emfmogodb.model.Address;
import net.latuske.emfmogodb.model.EMailAddress;
import net.latuske.emfmogodb.model.EMailAddressType;
import net.latuske.emfmogodb.model.MyFactory;
import net.latuske.emfmogodb.model.MyPackage;
import net.latuske.emfmogodb.model.Person;

public class EmfMogodb {

	public static void main(String[] args) throws IOException {
		MongoClient mongoClient = MongoClients.create();
		MongoDatabase database = mongoClient.getDatabase("emfmongo-db");
		MongoCollection<Document> collection = database.getCollection("emfmongo-collection");

		System.out.println(collection.countDocuments());
		collection.deleteMany(new Document());
		System.out.println(collection.countDocuments());

		Address address1 = MyFactory.eINSTANCE.createAddress();
		address1.setCity("Stuttgart");

		EMailAddress eMailAddress1 = MyFactory.eINSTANCE.createEMailAddress();
		eMailAddress1.setEmail("alice@bob.alice");
		eMailAddress1.setType(EMailAddressType.OFFICE);

		Person alice = MyFactory.eINSTANCE.createPerson();
		alice.setName("Alice");
		alice.setAddress(address1);
		alice.getEmailAddresses().add(eMailAddress1);

		Address address2 = MyFactory.eINSTANCE.createAddress();
		address2.setCity("Stuttgart");

		EMailAddress eMailAddress2 = MyFactory.eINSTANCE.createEMailAddress();
		eMailAddress2.setEmail("bob@bob.alice");
		eMailAddress2.setType(EMailAddressType.PRIVATE);

		EMailAddress eMailAddress3 = MyFactory.eINSTANCE.createEMailAddress();
		eMailAddress3.setEmail("bob@bob.bob");
		eMailAddress3.setType(EMailAddressType.OFFICE);

		Person bob = MyFactory.eINSTANCE.createPerson();
		bob.setName("Bob");
		bob.setAddress(address2);
		bob.getEmailAddresses().add(eMailAddress2);
		bob.getEmailAddresses().add(eMailAddress3);

		insert(collection, alice);
		findByName(collection, "Alice");
		findByName(collection, "Bob");

		insert(collection, bob);
		findByName(collection, "Alice");
		findByName(collection, "Bob");
		findByNamePattern(collection, ".*li.*");
		findByCity(collection, "Stuttgart");
		findByEMailAddress(collection, "bob@bob.bob");

		Bson filter = or(eq(MyPackage.Literals.PERSON__NAME.getName(), "Bob"),
				eq(MyPackage.Literals.PERSON__EMAIL_ADDRESSES.getName() + '.' + MyPackage.Literals.EMAIL_ADDRESS__EMAIL.getName(),
						"alice@bob.alice"));
		findByFilter(collection, filter);
	}

	private static void insert(MongoCollection<Document> collection, Person person) {
		long start = System.currentTimeMillis();
		Document document = convertToDocument(person);
		System.out.println("Convert took: " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		collection.insertOne(document);
		System.out.println("Insert took: " + (System.currentTimeMillis() - start));

		System.out.println();
	}

	private static void findByName(MongoCollection<Document> collection, String name) {
		long start = System.currentTimeMillis();
		Bson filter = new Document(MyPackage.Literals.PERSON__NAME.getName(), name);
		FindIterable<Document> cursor = collection.find(filter);
		System.out.println("Find by name took: " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		System.out.println(convertToEObject(cursor.first()));
		System.out.println("Convert took: " + (System.currentTimeMillis() - start));

		System.out.println();
	}

	private static void findByNamePattern(MongoCollection<Document> collection, String namePattern) {
		long start = System.currentTimeMillis();
		Pattern pattern = Pattern.compile(namePattern, Pattern.CASE_INSENSITIVE);
		Bson filter = Filters.regex(MyPackage.Literals.PERSON__NAME.getName(), pattern);
		FindIterable<Document> cursor = collection.find(filter);
		System.out.println("Find by name pattern took: " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		System.out.println(convertToEObject(cursor.first()));
		System.out.println("Convert took: " + (System.currentTimeMillis() - start));

		System.out.println();
	}

	private static void findByCity(MongoCollection<Document> collection, String city) {
		long start = System.currentTimeMillis();
		Bson filter = eq(MyPackage.Literals.PERSON__ADDRESS.getName(),
				new Document().append("emf-type", "Address").append(MyPackage.Literals.ADDRESS__CITY.getName(), city));
		FindIterable<Document> cursor = collection.find(filter);
		System.out.println("Find by city took: " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		cursor.map(EmfMogodb::convertToEObject).forEach(System.out::println);
		System.out.println("Convert took: " + (System.currentTimeMillis() - start));

		System.out.println();

	}

	private static void findByEMailAddress(MongoCollection<Document> collection, String emailAddress) {
		long start = System.currentTimeMillis();
		Bson filter = new Document(MyPackage.Literals.PERSON__EMAIL_ADDRESSES.getName() + '.'
				+ MyPackage.Literals.EMAIL_ADDRESS__EMAIL.getName(), emailAddress);
		FindIterable<Document> cursor = collection.find(filter);
		System.out.println("Find by email address took: " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		System.out.println(convertToEObject(cursor.first()));
		System.out.println("Convert took: " + (System.currentTimeMillis() - start));

		System.out.println();
	}

	private static void findByFilter(MongoCollection<Document> collection, Bson filter) {
		long start = System.currentTimeMillis();
		FindIterable<Document> cursor = collection.find(filter);
		System.out.println("Find by filter took: " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		cursor.map(EmfMogodb::convertToEObject).forEach(System.out::println);
		System.out.println("Convert took: " + (System.currentTimeMillis() - start));

		System.out.println();
	}

	private static Document convertToDocument(EObject eObject) {
		EClass eClass = eObject.eClass();

		Document document = new Document();
		document.put("emf-package-ns", eClass.getEPackage().getNsURI());
		document.put("emf-type", eClass.getName());

		for (EAttribute eAttribute : eClass.getEAllAttributes()) {
			Object value = eObject.eGet(eAttribute);
			if (eAttribute.getEType() instanceof EEnum) {
				Enumerator enumerator = (Enumerator) value;
				document.put(eAttribute.getName(), enumerator.getName());
			} else {
				document.put(eAttribute.getName(), value);
			}
		}

		for (EReference eReference : eClass.getEAllContainments()) {
			Object object = eObject.eGet(eReference);

			if (eReference.isMany()) {
				List<Document> list = ((List<?>) object).stream().map(EObject.class::cast)
						.map(EmfMogodb::convertToDocument).collect(Collectors.toList());
				document.put(eReference.getName(), list);
			} else {
				if (object instanceof EObject) {
					document.put(eReference.getName(), convertToDocument((EObject) object));
				} else {
					document.put(eReference.getName(), object);
				}
			}
		}
		return document;
	}

	private static EObject convertToEObject(Document document) {
		if (document == null) {
			return null;
		}

		String emfPackageNs = (String) document.get("emf-package-ns");
		String emfType = (String) document.get("emf-type");
		EPackage ePackage = EPackage.Registry.INSTANCE.getEPackage(emfPackageNs);
		EClass eClass = (EClass) ePackage.getEClassifier(emfType);
		EObject eObject = MyFactory.eINSTANCE.create(eClass);

		for (EAttribute eAttribute : eClass.getEAllAttributes()) {
			Object value = document.get(eAttribute.getName());
			EClassifier eType = eAttribute.getEType();
			if (eType instanceof EEnum) {
				EEnum eEnum = (EEnum) eType;
				EEnumLiteral eEnumLiteral = eEnum.getEEnumLiteral(value.toString());

				eObject.eSet(eAttribute, eEnumLiteral.getInstance());
			} else {
				eObject.eSet(eAttribute, value);
			}
		}

		for (EReference eReference : eClass.getEAllContainments()) {
			Object object = document.get(eReference.getName());

			if (eReference.isMany()) {
				List<EObject> eList = ((List<?>) object).stream().map(Document.class::cast)
						.map(EmfMogodb::convertToEObject).collect(Collectors.toList());
				eObject.eSet(eReference, eList);
			} else {
				eObject.eSet(eReference, convertToEObject((Document) object));
			}
		}

		return eObject;
	}

//	private static DBObject saveToDbObject(Person person) throws IOException {
//		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//
//		Resource resource = createResource();
//		resource.getContents().add(person);
//		resource.save(outputStream, Map.of());
//
//		String jsonString = outputStream.toString();
//		return (DBObject) JSON.parse(jsonString);
//	}
//
//	private static Person loadFromDbObject(DBObject dbObject) throws IOException {
//		if (dbObject == null) {
//			return null;
//		}
//
//		String jsonString = JSON.serialize(dbObject);
//		ByteArrayInputStream inputStream = new ByteArrayInputStream(jsonString.getBytes());
//
//		Resource resource = createResource();
//		resource.load(inputStream, Map.of());
//
//		return (Person) resource.getContents().get(0);
//	}
//
//	private static Resource createResource() {
//		ResourceSet resourceSet = new ResourceSetImpl();
//		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("json", new JsonResourceFactory());
//		return resourceSet.createResource(URI.createFileURI("my.json"));
//	}

}
