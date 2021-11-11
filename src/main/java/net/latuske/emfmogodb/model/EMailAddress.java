/**
 */
package net.latuske.emfmogodb.model;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>EMail Address</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link net.latuske.emfmogodb.model.EMailAddress#getEmail <em>Email</em>}</li>
 *   <li>{@link net.latuske.emfmogodb.model.EMailAddress#getType <em>Type</em>}</li>
 * </ul>
 *
 * @see net.latuske.emfmogodb.model.MyPackage#getEMailAddress()
 * @model
 * @generated
 */
public interface EMailAddress extends EObject {
	/**
	 * Returns the value of the '<em><b>Email</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Email</em>' attribute.
	 * @see #setEmail(String)
	 * @see net.latuske.emfmogodb.model.MyPackage#getEMailAddress_Email()
	 * @model
	 * @generated
	 */
	String getEmail();

	/**
	 * Sets the value of the '{@link net.latuske.emfmogodb.model.EMailAddress#getEmail <em>Email</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Email</em>' attribute.
	 * @see #getEmail()
	 * @generated
	 */
	void setEmail(String value);

	/**
	 * Returns the value of the '<em><b>Type</b></em>' attribute.
	 * The literals are from the enumeration {@link net.latuske.emfmogodb.model.EMailAddressType}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' attribute.
	 * @see net.latuske.emfmogodb.model.EMailAddressType
	 * @see #setType(EMailAddressType)
	 * @see net.latuske.emfmogodb.model.MyPackage#getEMailAddress_Type()
	 * @model
	 * @generated
	 */
	EMailAddressType getType();

	/**
	 * Sets the value of the '{@link net.latuske.emfmogodb.model.EMailAddress#getType <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' attribute.
	 * @see net.latuske.emfmogodb.model.EMailAddressType
	 * @see #getType()
	 * @generated
	 */
	void setType(EMailAddressType value);

} // EMailAddress
