package net.latuske.emfmongodb;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.io.FileUtils;
import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.ODatabaseType;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.intent.OIntentMassiveInsert;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.ODirection;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

import net.latuske.emfmogodb.model.Address;
import net.latuske.emfmogodb.model.EMailAddress;
import net.latuske.emfmogodb.model.EMailAddressType;
import net.latuske.emfmogodb.model.MyFactory;
import net.latuske.emfmogodb.model.MyPackage;
import net.latuske.emfmogodb.model.Person;

public class EmfOrientDb {

	public static void main(String[] args) throws IOException {
		FileUtils.deleteDirectory(Path.of("orientdb").toFile());

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

		OrientDBConfig config = OrientDBConfig.builder()
				.addConfig(OGlobalConfiguration.CREATE_DEFAULT_USERS, Boolean.TRUE).build();

		try (OrientDB orientDB = new OrientDB("embedded:orientdb", config)) {
			orientDB.createIfNotExists("test", ODatabaseType.PLOCAL);

			try (ODatabaseSession session = orientDB.open("test", "admin", "admin")) {
				insert(session, alice);
				insert(session, bob);

				findByName(session, "Alice");
				findByNamePattern(session, "%li%");

				generateTestData(session, 10_000, 5);

				findByName(session, "Alice");
				findByNamePattern(session, "%li%");
				findByNamePattern(session, "Person%");
			}
		}
	}

	private static void generateTestData(ODatabaseSession session, int numberOfPersons,
			int numberOfEMailAddressPerPerson) {
		long start = System.currentTimeMillis();
		session.declareIntent(new OIntentMassiveInsert());

		for (int i = 0; i < numberOfPersons; i++) {
			Address address = MyFactory.eINSTANCE.createAddress();
			address.setCity("AddressCity" + i);

			Person person = MyFactory.eINSTANCE.createPerson();
			person.setName("PersonName" + i);
			person.setAddress(address);

			for (int j = 0; j < numberOfEMailAddressPerPerson; j++) {
				EMailAddress eMailAddress = MyFactory.eINSTANCE.createEMailAddress();
				eMailAddress.setEmail("PersonName" + i + "@provider" + j + ".com");
				eMailAddress.setType(j % 2 == 0 ? EMailAddressType.OFFICE : EMailAddressType.PRIVATE);
				person.getEmailAddresses().add(eMailAddress);
			}

			createVertex(session, person).save();
		}

		session.declareIntent(null);
		System.out.println("Generate test data took: " + (System.currentTimeMillis() - start));
		System.out.println();
	}

	private static void insert(ODatabaseSession session, Person person) {
		long start = System.currentTimeMillis();

		createVertex(session, person).save();

		System.out.println("Insert took: " + (System.currentTimeMillis() - start));
		System.out.println();
	}

	private static OVertex createVertex(ODatabaseSession session, EObject eObject) {
		EClass eClass = eObject.eClass();
		OClass oClass = session.getClass("vertex_" + eClass.getName());

		if (oClass == null) {
			oClass = session.createVertexClass("vertex_" + eClass.getName());

			oClass.createProperty("emf_package_ns", OType.STRING);
			oClass.createIndex(eClass.getName() + "_emf_package_nsIndex", OClass.INDEX_TYPE.NOTUNIQUE_HASH_INDEX,
					"emf_package_ns");

			oClass.createProperty("emf_type", OType.STRING);
			oClass.createIndex(eClass.getName() + "_emf_typeIndex", OClass.INDEX_TYPE.NOTUNIQUE_HASH_INDEX, "emf_type");

			for (EAttribute eAttribute : eClass.getEAllAttributes()) {
				if (eAttribute.getEType() instanceof EEnum) {
					oClass.createProperty(eAttribute.getName(), OType.STRING);
				} else {
					oClass.createProperty(eAttribute.getName(),
							OType.getTypeByClass(eAttribute.getEType().getInstanceClass()));
				}
				oClass.createIndex(eClass.getName() + '_' + eAttribute.getName() + "Index",
						OClass.INDEX_TYPE.NOTUNIQUE_HASH_INDEX, eAttribute.getName());
			}
		}

		OVertex vertex = session.newVertex(oClass);
		vertex.setProperty("emf_package_ns", eClass.getEPackage().getNsURI());
		vertex.setProperty("emf_type", eClass.getName());

		for (EAttribute eAttribute : eClass.getEAllAttributes()) {
			Object value = eObject.eGet(eAttribute);
			if (eAttribute.getEType() instanceof EEnum) {
				Enumerator enumerator = (Enumerator) value;
				vertex.setProperty(eAttribute.getName(), enumerator.getName());
			} else {
				vertex.setProperty(eAttribute.getName(), value);
			}
		}

		for (EReference eReference : eClass.getEAllContainments()) {
			Object object = eObject.eGet(eReference);

			OClass oClassForEdge = getOrCreateClass(session, eReference);
			if (eReference.isMany()) {
				((List<?>) object).stream().map(EObject.class::cast)
						.map(childEObject -> createVertex(session, childEObject))
						.forEach(otherVetex -> vertex.addEdge(otherVetex, oClassForEdge));
			} else {
				if (object instanceof EObject) {
					vertex.addEdge(createVertex(session, (EObject) object), oClassForEdge);
				} else {
					vertex.setProperty(eReference.getName(), object);
				}
			}
		}

		return vertex;
	}

	private static OClass getOrCreateClass(ODatabaseSession session, EReference eReference) {
		OClass oClass = session.getClass("edge_" + eReference.getName());
		if (oClass == null) {
			oClass = session.createEdgeClass("edge_" + eReference.getName());
		}

		return oClass;
	}

	private static void findByName(ODatabaseSession session, String name) {
		long start = System.currentTimeMillis();
		String statement = "SELECT FROM V WHERE " + MyPackage.Literals.PERSON__NAME.getName() + " = ? and emf_type = ?";
		;
		OResultSet resultSet = session.query(statement, name, MyPackage.Literals.PERSON.getName());
		List<OVertex> result = resultSet.vertexStream().collect(Collectors.toList());
		System.out.println("Find by name took: " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		result.stream().map(EmfOrientDb::createEObject).forEach(System.out::println);
		System.out.println("Convert took: " + (System.currentTimeMillis() - start));
		System.out.println();
	}

	private static void findByNamePattern(ODatabaseSession session, String namePattern) {
		long start = System.currentTimeMillis();
		String statement = "SELECT FROM V WHERE " + MyPackage.Literals.PERSON__NAME.getName()
				+ " like ? and emf_type = ?";
		OResultSet resultSet = session.query(statement, namePattern, MyPackage.Literals.PERSON.getName());
		List<OVertex> result = resultSet.vertexStream().collect(Collectors.toList());
		System.out.println("Find by name pattern took: " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		result.stream().map(EmfOrientDb::createEObject).collect(Collectors.toList());// .forEach(System.out::println);
		System.out.println("Convert took: " + (System.currentTimeMillis() - start));
		System.out.println();
	}

	private static EObject createEObject(OVertex vertex) {
		String emfPackageNs = (String) vertex.getProperty("emf_package_ns");
		String emfType = (String) vertex.getProperty("emf_type");
		EPackage ePackage = EPackage.Registry.INSTANCE.getEPackage(emfPackageNs);
		EClass eClass = (EClass) ePackage.getEClassifier(emfType);
		EObject eObject = MyFactory.eINSTANCE.create(eClass);

		for (EAttribute eAttribute : eClass.getEAllAttributes()) {
			Object value = vertex.getProperty(eAttribute.getName());
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
			List<EObject> relatedEObjects = createEObjectsForEdges(vertex, eReference);

			if (eReference.isMany()) {
				eObject.eSet(eReference, relatedEObjects);
			} else if (!relatedEObjects.isEmpty()) {
				eObject.eSet(eReference, relatedEObjects.get(0));
			}
		}

		return eObject;
	}

	private static List<EObject> createEObjectsForEdges(OVertex vertex, EReference eReference) {
		Iterable<OEdge> edges = vertex.getEdges(ODirection.OUT, "edge_" + eReference.getName());
		return StreamSupport.stream(edges.spliterator(), false).map(edge -> edge.getVertex(ODirection.IN))
				.map(EmfOrientDb::createEObject).collect(Collectors.toList());
	}

}
