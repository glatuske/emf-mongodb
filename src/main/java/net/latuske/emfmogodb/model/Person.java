/**
 */
package net.latuske.emfmogodb.model;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Person</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link net.latuske.emfmogodb.model.Person#getName <em>Name</em>}</li>
 *   <li>{@link net.latuske.emfmogodb.model.Person#getAddress <em>Address</em>}</li>
 *   <li>{@link net.latuske.emfmogodb.model.Person#getEmailAddresses <em>Email Addresses</em>}</li>
 * </ul>
 *
 * @see net.latuske.emfmogodb.model.MyPackage#getPerson()
 * @model
 * @generated
 */
public interface Person extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see net.latuske.emfmogodb.model.MyPackage#getPerson_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link net.latuske.emfmogodb.model.Person#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Address</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Address</em>' containment reference.
	 * @see #setAddress(Address)
	 * @see net.latuske.emfmogodb.model.MyPackage#getPerson_Address()
	 * @model containment="true"
	 * @generated
	 */
	Address getAddress();

	/**
	 * Sets the value of the '{@link net.latuske.emfmogodb.model.Person#getAddress <em>Address</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Address</em>' containment reference.
	 * @see #getAddress()
	 * @generated
	 */
	void setAddress(Address value);

	/**
	 * Returns the value of the '<em><b>Email Addresses</b></em>' containment reference list.
	 * The list contents are of type {@link net.latuske.emfmogodb.model.EMailAddress}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Email Addresses</em>' containment reference list.
	 * @see net.latuske.emfmogodb.model.MyPackage#getPerson_EmailAddresses()
	 * @model containment="true"
	 * @generated
	 */
	EList<EMailAddress> getEmailAddresses();

} // Person
