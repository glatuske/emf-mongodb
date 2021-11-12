package net.latuske.emfmongodb;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
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

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

import net.latuske.emfmogodb.model.Address;
import net.latuske.emfmogodb.model.EMailAddress;
import net.latuske.emfmogodb.model.EMailAddressType;
import net.latuske.emfmogodb.model.MyFactory;
import net.latuske.emfmogodb.model.MyPackage;
import net.latuske.emfmogodb.model.Person;

public class EmfOrientDb {

	public static void main(String[] args) throws IOException {
		Path path = Path.of("orientdb");
		FileUtils.deleteDirectory(path.toFile());

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

		OrientGraph graph = new OrientGraph("plocal:orientdb");
		try {
			insert(graph, alice);
			insert(graph, bob);

			graph.createKeyIndex("emf-package-ns", Vertex.class);
			graph.createKeyIndex("emf-type", Vertex.class);

			MyPackage.eINSTANCE.getEClassifiers().stream().filter(EClass.class::isInstance).map(EClass.class::cast)
					.map(EClass::getEAllAttributes).flatMap(Collection::stream)
					.forEach(eAttribute -> graph.createKeyIndex(eAttribute.getName(), Vertex.class));

			findByName(graph, "Alice");
		} finally {
			graph.shutdown();
		}
	}

	private static void insert(OrientGraph graph, Person person) {
		long start = System.currentTimeMillis();

		try {
			createVertex(graph, person);
			graph.commit();
		} catch (Exception e) {
			graph.rollback();
		}

		System.out.println("Insert took: " + (System.currentTimeMillis() - start));
		System.out.println();
	}

	private static Vertex createVertex(OrientGraph graph, EObject eObject) {
		EClass eClass = eObject.eClass();

		Vertex vertex = graph.addVertex(null);
		vertex.setProperty("emf-package-ns", eClass.getEPackage().getNsURI());
		vertex.setProperty("emf-type", eClass.getName());

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

			if (eReference.isMany()) {
				((List<?>) object).stream().map(EObject.class::cast)
						.map(childEObject -> createVertex(graph, childEObject))
						.forEach(otherVetex -> vertex.addEdge(eReference.getName(), otherVetex));
			} else {
				if (object instanceof EObject) {
					vertex.addEdge(eReference.getName(), createVertex(graph, (EObject) object));
				} else {
					vertex.setProperty(eReference.getName(), object);
				}
			}
		}

		return vertex;
	}

	private static EObject createEObject(Vertex vertex) {
		String emfPackageNs = (String) vertex.getProperty("emf-package-ns");
		String emfType = (String) vertex.getProperty("emf-type");
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

	private static List<EObject> createEObjectsForEdges(Vertex vertex, EReference eReference) {
		Iterable<Edge> edges = vertex.getEdges(Direction.OUT, eReference.getName());
		return StreamSupport.stream(edges.spliterator(), false).map(edge -> edge.getVertex(Direction.OUT))
				.map(EmfOrientDb::createEObject).collect(Collectors.toList());
	}

	private static void findByName(OrientGraph graph, String name) {
		long start = System.currentTimeMillis();
		Iterable<Vertex> result = graph.query().has(MyPackage.Literals.PERSON.getName(), name)
				.has("emf-type", MyPackage.Literals.PERSON.getName()).vertices();
		System.out.println("Find by name took: " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		for (Vertex vertex : result) {
			System.out.println(createEObject(vertex));
		}

		System.out.println("Convert took: " + (System.currentTimeMillis() - start));
	}

}
