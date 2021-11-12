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
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import net.latuske.emfmogodb.model.Address;
import net.latuske.emfmogodb.model.EMailAddress;
import net.latuske.emfmogodb.model.EMailAddressType;
import net.latuske.emfmogodb.model.MyFactory;
import net.latuske.emfmogodb.model.MyPackage;
import net.latuske.emfmogodb.model.Person;

public class EmfNeo4j {

	public static void main(String[] args) throws IOException {
		Path path = Path.of("neo4j");
		FileUtils.deleteDirectory(path.toFile());

		DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(path).build();
		GraphDatabaseService graphDb = managementService.database(GraphDatabaseSettings.DEFAULT_DATABASE_NAME);

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

		insert(graphDb, alice);
		insert(graphDb, bob);

		findByName(graphDb, "Alice");

		try (Transaction tx = graphDb.beginTx()) {
			// Result execute = tx.execute(null);
			Node findNode = tx.findNode(Label.label(MyPackage.Literals.PERSON.getName()),
					MyPackage.Literals.PERSON__NAME.getName(), "Alice");
			EObject eObject = createEObject(findNode);
			System.out.println(eObject);
		}
	}

	private static void insert(GraphDatabaseService graphDb, Person person) {
		long start = System.currentTimeMillis();

		try (Transaction tx = graphDb.beginTx()) {
			createNode(tx, person);
			tx.commit();
		}

		System.out.println("Insert took: " + (System.currentTimeMillis() - start));
		System.out.println();
	}

	private static Node createNode(Transaction tx, EObject eObject) {
		EClass eClass = eObject.eClass();

		Node node = tx.createNode();
		node.addLabel(Label.label(eClass.getName()));
		node.setProperty("emf-package-ns", eClass.getEPackage().getNsURI());
		node.setProperty("emf-type", eClass.getName());

		for (EAttribute eAttribute : eClass.getEAllAttributes()) {
			Object value = eObject.eGet(eAttribute);
			if (eAttribute.getEType() instanceof EEnum) {
				Enumerator enumerator = (Enumerator) value;
				node.setProperty(eAttribute.getName(), enumerator.getName());
			} else {
				node.setProperty(eAttribute.getName(), value);
			}
		}

		for (EReference eReference : eClass.getEAllContainments()) {
			Object object = eObject.eGet(eReference);
			RelationshipType rsType = RelationshipType.withName(eReference.getName());

			if (eReference.isMany()) {
				((List<?>) object).stream().map(EObject.class::cast).map(childEObject -> createNode(tx, childEObject))
						.forEach(childNode -> node.createRelationshipTo(childNode, rsType));
			} else {
				if (object instanceof EObject) {
					node.createRelationshipTo(createNode(tx, (EObject) object), rsType);
				} else {
					node.setProperty(eReference.getName(), object);
				}
			}
		}

		return node;
	}

	private static EObject createEObject(Node node) {
		String emfPackageNs = (String) node.getProperty("emf-package-ns");
		String emfType = (String) node.getProperty("emf-type");
		EPackage ePackage = EPackage.Registry.INSTANCE.getEPackage(emfPackageNs);
		EClass eClass = (EClass) ePackage.getEClassifier(emfType);
		EObject eObject = MyFactory.eINSTANCE.create(eClass);

		for (EAttribute eAttribute : eClass.getEAllAttributes()) {
			Object value = node.getProperty(eAttribute.getName());
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
			List<EObject> relatedEObjects = createEObjectsForRelationships(node, eReference);

			if (eReference.isMany()) {
				eObject.eSet(eReference, relatedEObjects);
			} else if (!relatedEObjects.isEmpty()) {
				eObject.eSet(eReference, relatedEObjects.get(0));
			}
		}

		return eObject;
	}

	private static List<EObject> createEObjectsForRelationships(Node node, EReference eReference) {
		Iterable<Relationship> relationships = node.getRelationships(RelationshipType.withName(eReference.getName()));
		return StreamSupport.stream(relationships.spliterator(), false)
				.map(relationship -> relationship.getOtherNode(node)).map(EmfNeo4j::createEObject)
				.collect(Collectors.toList());
	}

	private static void findByName(GraphDatabaseService graphDb, String name) {
		try (Transaction tx = graphDb.beginTx()) {
			long start = System.currentTimeMillis();
			Node findNode = tx.findNode(Label.label(MyPackage.Literals.PERSON.getName()),
					MyPackage.Literals.PERSON__NAME.getName(), name);
			System.out.println("Find by name took: " + (System.currentTimeMillis() - start));

			EObject eObject = createEObject(findNode);
			System.out.println(eObject);
			System.out.println("Convert took: " + (System.currentTimeMillis() - start));

			System.out.println();
		}
	}

}
