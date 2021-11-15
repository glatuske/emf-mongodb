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
import com.orientechnologies.orient.core.metadata.schema.OClass;
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
			}
		}

	}

	private static void insert(ODatabaseSession session, Person person) {
		long start = System.currentTimeMillis();

		createVertex(session, person).save();

		System.out.println("Insert took: " + (System.currentTimeMillis() - start));
		System.out.println();
	}

	private static OVertex createVertex(ODatabaseSession session, EObject eObject) {
		EClass eClass = eObject.eClass();

		OVertex vertex = session.newVertex();
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

			OClass oClass = getOrCreateClass(session, eReference);
			if (eReference.isMany()) {
				((List<?>) object).stream().map(EObject.class::cast)
						.map(childEObject -> createVertex(session, childEObject))
						.forEach(otherVetex -> vertex.addEdge(otherVetex, oClass));
			} else {
				if (object instanceof EObject) {
					vertex.addEdge(createVertex(session, (EObject) object), oClass);
				} else {
					vertex.setProperty(eReference.getName(), object);
				}
			}
		}

		return vertex;
	}

	private static OClass getOrCreateClass(ODatabaseSession session, EReference eReference) {
		OClass oClass = session.getClass(eReference.getName());
		if (oClass == null) {
			oClass = session.createClass(eReference.getName(), "E");
		}

		return oClass;
	}

	private static void findByName(ODatabaseSession session, String name) {
		long start = System.currentTimeMillis();
		String statement = "SELECT FROM V WHERE " + MyPackage.Literals.PERSON__NAME.getName() + " = ? and emf_type = ?";
		OResultSet resultSet = session.query(statement, name, MyPackage.Literals.PERSON.getName());
		System.out.println("Find by name took: " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		resultSet.vertexStream().map(EmfOrientDb::createEObject).forEach(System.out::println);
		System.out.println("Convert took: " + (System.currentTimeMillis() - start));
		System.out.println();
	}

	private static void findByNamePattern(ODatabaseSession session, String namePattern) {
		long start = System.currentTimeMillis();
		String statement = "SELECT FROM V WHERE " + MyPackage.Literals.PERSON__NAME.getName()
				+ " like ? and emf_type = ?";
		OResultSet resultSet = session.query(statement, namePattern, MyPackage.Literals.PERSON.getName());
		System.out.println("Find by name pattern took: " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		resultSet.vertexStream().map(EmfOrientDb::createEObject).forEach(System.out::println);
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
		Iterable<OEdge> edges = vertex.getEdges(ODirection.OUT, eReference.getName());
		return StreamSupport.stream(edges.spliterator(), false).map(edge -> edge.getVertex(ODirection.IN))
				.map(EmfOrientDb::createEObject).collect(Collectors.toList());
	}

}
