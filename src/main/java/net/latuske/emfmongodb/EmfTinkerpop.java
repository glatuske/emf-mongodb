package net.latuske.emfmongodb;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.tinkerpop.gremlin.process.traversal.TextP;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import net.latuske.emfmogodb.model.Address;
import net.latuske.emfmogodb.model.EMailAddress;
import net.latuske.emfmogodb.model.EMailAddressType;
import net.latuske.emfmogodb.model.MyFactory;
import net.latuske.emfmogodb.model.MyPackage;
import net.latuske.emfmogodb.model.Person;

public class EmfTinkerpop {

	public static void main(String[] args) throws Exception {
		FileUtils.deleteDirectory(Path.of("tinkerpop").toFile());

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

		long start = System.currentTimeMillis();
		Map<String, Object> configuration = Map.of("gremlin.tinkergraph.graphLocation", "tinkerpop/data",
				"gremlin.tinkergraph.graphFormat", "gryo");

		try (Graph graph = TinkerGraph.open(new MapConfiguration(configuration))) {
			System.out.println("Open took: " + (System.currentTimeMillis() - start));

			insert(graph, alice);
			insert(graph, bob);

			findByName(graph, "Alice");
			findByNamePattern(graph, "li");

			generateTestData(graph, 10_000, 5);

			findByName(graph, "Alice");
			findByNamePattern(graph, "li");
			findByNamePattern(graph, "Person");

			start = System.currentTimeMillis();
		}

		System.out.println("Close took: " + (System.currentTimeMillis() - start));

		reOpen(configuration);
		reOpen(configuration);
		reOpen(configuration);
	}

	private static void reOpen(Map<String, Object> configuration) throws Exception {
		long start = System.currentTimeMillis();

		try (Graph graph = TinkerGraph.open(new MapConfiguration(configuration))) {
		}

		System.out.println("Re-Open took: " + (System.currentTimeMillis() - start));
	}

	private static void generateTestData(Graph graph, int numberOfPersons, int numberOfEMailAddressPerPerson) {
		long start = System.currentTimeMillis();

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

			createVertex(graph, person);
		}

		System.out.println("Generate test data took: " + (System.currentTimeMillis() - start));
		System.out.println();
	}

	private static void insert(Graph graph, Person person) {
		long start = System.currentTimeMillis();

		createVertex(graph, person);

		System.out.println("Insert took: " + (System.currentTimeMillis() - start));
		System.out.println();
	}

	private static Vertex createVertex(Graph graph, EObject eObject) {
		EClass eClass = eObject.eClass();

		Vertex vertex = graph.addVertex(eClass.getName());
		vertex.property("emf_package_ns", eClass.getEPackage().getNsURI());
		vertex.property("emf_type", eClass.getName());

		for (EAttribute eAttribute : eClass.getEAllAttributes()) {
			Object value = eObject.eGet(eAttribute);
			if (eAttribute.getEType() instanceof EEnum) {
				Enumerator enumerator = (Enumerator) value;
				vertex.property(eAttribute.getName(), enumerator.getName());
			} else {
				vertex.property(eAttribute.getName(), value);
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
					vertex.property(eReference.getName(), object);
				}
			}
		}

		return vertex;
	}

	private static void findByName(Graph graph, String name) {
		long start = System.currentTimeMillis();
		List<Vertex> result = graph.traversal().V().has(MyPackage.Literals.PERSON__NAME.getName(), name)
				.has("emf_type", MyPackage.Literals.PERSON.getName()).toList();
		System.out.println("Find by name took: " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		result.stream().map(EmfTinkerpop::createEObject).forEach(System.out::println);
		System.out.println("Convert took: " + (System.currentTimeMillis() - start));
		System.out.println();
	}

	private static void findByNamePattern(Graph graph, String namePattern) {
		long start = System.currentTimeMillis();
		List<Vertex> result = graph.traversal().V()
				.has(MyPackage.Literals.PERSON__NAME.getName(), TextP.containing(namePattern))
				.has("emf_type", MyPackage.Literals.PERSON.getName()).toList();
		System.out.println("Find by name pattern took: " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		System.out.println(result.stream().map(EmfTinkerpop::createEObject).count());
		System.out.println("Convert took: " + (System.currentTimeMillis() - start));
		System.out.println();
	}

	private static EObject createEObject(Vertex vertex) {
		String emfPackageNs = (String) vertex.property("emf_package_ns").value();
		String emfType = (String) vertex.property("emf_type").value();
		EPackage ePackage = EPackage.Registry.INSTANCE.getEPackage(emfPackageNs);
		EClass eClass = (EClass) ePackage.getEClassifier(emfType);
		EObject eObject = MyFactory.eINSTANCE.create(eClass);

		for (EAttribute eAttribute : eClass.getEAllAttributes()) {
			Object value = vertex.property(eAttribute.getName()).value();
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
		Iterator<Edge> edges = vertex.edges(Direction.OUT, eReference.getName());
		Iterable<Edge> iterable = () -> edges;

		return StreamSupport.stream(iterable.spliterator(), false).map(Edge::inVertex).map(EmfTinkerpop::createEObject)
				.collect(Collectors.toList());
	}

}
