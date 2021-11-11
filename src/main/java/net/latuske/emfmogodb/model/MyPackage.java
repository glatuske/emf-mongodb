/**
 */
package net.latuske.emfmogodb.model;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see net.latuske.emfmogodb.model.MyFactory
 * @model kind="package"
 * @generated
 */
public interface MyPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "model";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "https://latuske.net/model";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "model";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	MyPackage eINSTANCE = net.latuske.emfmogodb.model.impl.MyPackageImpl.init();

	/**
	 * The meta object id for the '{@link net.latuske.emfmogodb.model.impl.PersonImpl <em>Person</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see net.latuske.emfmogodb.model.impl.PersonImpl
	 * @see net.latuske.emfmogodb.model.impl.MyPackageImpl#getPerson()
	 * @generated
	 */
	int PERSON = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSON__NAME = 0;

	/**
	 * The feature id for the '<em><b>Address</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSON__ADDRESS = 1;

	/**
	 * The feature id for the '<em><b>Email Addresses</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSON__EMAIL_ADDRESSES = 2;

	/**
	 * The number of structural features of the '<em>Person</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSON_FEATURE_COUNT = 3;

	/**
	 * The number of operations of the '<em>Person</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSON_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link net.latuske.emfmogodb.model.impl.AddressImpl <em>Address</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see net.latuske.emfmogodb.model.impl.AddressImpl
	 * @see net.latuske.emfmogodb.model.impl.MyPackageImpl#getAddress()
	 * @generated
	 */
	int ADDRESS = 1;

	/**
	 * The feature id for the '<em><b>City</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ADDRESS__CITY = 0;

	/**
	 * The number of structural features of the '<em>Address</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ADDRESS_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Address</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ADDRESS_OPERATION_COUNT = 0;


	/**
	 * The meta object id for the '{@link net.latuske.emfmogodb.model.impl.EMailAddressImpl <em>EMail Address</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see net.latuske.emfmogodb.model.impl.EMailAddressImpl
	 * @see net.latuske.emfmogodb.model.impl.MyPackageImpl#getEMailAddress()
	 * @generated
	 */
	int EMAIL_ADDRESS = 2;

	/**
	 * The feature id for the '<em><b>Email</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EMAIL_ADDRESS__EMAIL = 0;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EMAIL_ADDRESS__TYPE = 1;

	/**
	 * The number of structural features of the '<em>EMail Address</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EMAIL_ADDRESS_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>EMail Address</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EMAIL_ADDRESS_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link net.latuske.emfmogodb.model.EMailAddressType <em>EMail Address Type</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see net.latuske.emfmogodb.model.EMailAddressType
	 * @see net.latuske.emfmogodb.model.impl.MyPackageImpl#getEMailAddressType()
	 * @generated
	 */
	int EMAIL_ADDRESS_TYPE = 3;


	/**
	 * Returns the meta object for class '{@link net.latuske.emfmogodb.model.Person <em>Person</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Person</em>'.
	 * @see net.latuske.emfmogodb.model.Person
	 * @generated
	 */
	EClass getPerson();

	/**
	 * Returns the meta object for the attribute '{@link net.latuske.emfmogodb.model.Person#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see net.latuske.emfmogodb.model.Person#getName()
	 * @see #getPerson()
	 * @generated
	 */
	EAttribute getPerson_Name();

	/**
	 * Returns the meta object for the containment reference '{@link net.latuske.emfmogodb.model.Person#getAddress <em>Address</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Address</em>'.
	 * @see net.latuske.emfmogodb.model.Person#getAddress()
	 * @see #getPerson()
	 * @generated
	 */
	EReference getPerson_Address();

	/**
	 * Returns the meta object for the containment reference list '{@link net.latuske.emfmogodb.model.Person#getEmailAddresses <em>Email Addresses</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Email Addresses</em>'.
	 * @see net.latuske.emfmogodb.model.Person#getEmailAddresses()
	 * @see #getPerson()
	 * @generated
	 */
	EReference getPerson_EmailAddresses();

	/**
	 * Returns the meta object for class '{@link net.latuske.emfmogodb.model.Address <em>Address</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Address</em>'.
	 * @see net.latuske.emfmogodb.model.Address
	 * @generated
	 */
	EClass getAddress();

	/**
	 * Returns the meta object for the attribute '{@link net.latuske.emfmogodb.model.Address#getCity <em>City</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>City</em>'.
	 * @see net.latuske.emfmogodb.model.Address#getCity()
	 * @see #getAddress()
	 * @generated
	 */
	EAttribute getAddress_City();

	/**
	 * Returns the meta object for class '{@link net.latuske.emfmogodb.model.EMailAddress <em>EMail Address</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>EMail Address</em>'.
	 * @see net.latuske.emfmogodb.model.EMailAddress
	 * @generated
	 */
	EClass getEMailAddress();

	/**
	 * Returns the meta object for the attribute '{@link net.latuske.emfmogodb.model.EMailAddress#getEmail <em>Email</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Email</em>'.
	 * @see net.latuske.emfmogodb.model.EMailAddress#getEmail()
	 * @see #getEMailAddress()
	 * @generated
	 */
	EAttribute getEMailAddress_Email();

	/**
	 * Returns the meta object for the attribute '{@link net.latuske.emfmogodb.model.EMailAddress#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type</em>'.
	 * @see net.latuske.emfmogodb.model.EMailAddress#getType()
	 * @see #getEMailAddress()
	 * @generated
	 */
	EAttribute getEMailAddress_Type();

	/**
	 * Returns the meta object for enum '{@link net.latuske.emfmogodb.model.EMailAddressType <em>EMail Address Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>EMail Address Type</em>'.
	 * @see net.latuske.emfmogodb.model.EMailAddressType
	 * @generated
	 */
	EEnum getEMailAddressType();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	MyFactory getMyFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link net.latuske.emfmogodb.model.impl.PersonImpl <em>Person</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see net.latuske.emfmogodb.model.impl.PersonImpl
		 * @see net.latuske.emfmogodb.model.impl.MyPackageImpl#getPerson()
		 * @generated
		 */
		EClass PERSON = eINSTANCE.getPerson();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PERSON__NAME = eINSTANCE.getPerson_Name();

		/**
		 * The meta object literal for the '<em><b>Address</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PERSON__ADDRESS = eINSTANCE.getPerson_Address();

		/**
		 * The meta object literal for the '<em><b>Email Addresses</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PERSON__EMAIL_ADDRESSES = eINSTANCE.getPerson_EmailAddresses();

		/**
		 * The meta object literal for the '{@link net.latuske.emfmogodb.model.impl.AddressImpl <em>Address</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see net.latuske.emfmogodb.model.impl.AddressImpl
		 * @see net.latuske.emfmogodb.model.impl.MyPackageImpl#getAddress()
		 * @generated
		 */
		EClass ADDRESS = eINSTANCE.getAddress();

		/**
		 * The meta object literal for the '<em><b>City</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ADDRESS__CITY = eINSTANCE.getAddress_City();

		/**
		 * The meta object literal for the '{@link net.latuske.emfmogodb.model.impl.EMailAddressImpl <em>EMail Address</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see net.latuske.emfmogodb.model.impl.EMailAddressImpl
		 * @see net.latuske.emfmogodb.model.impl.MyPackageImpl#getEMailAddress()
		 * @generated
		 */
		EClass EMAIL_ADDRESS = eINSTANCE.getEMailAddress();

		/**
		 * The meta object literal for the '<em><b>Email</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EMAIL_ADDRESS__EMAIL = eINSTANCE.getEMailAddress_Email();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EMAIL_ADDRESS__TYPE = eINSTANCE.getEMailAddress_Type();

		/**
		 * The meta object literal for the '{@link net.latuske.emfmogodb.model.EMailAddressType <em>EMail Address Type</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see net.latuske.emfmogodb.model.EMailAddressType
		 * @see net.latuske.emfmogodb.model.impl.MyPackageImpl#getEMailAddressType()
		 * @generated
		 */
		EEnum EMAIL_ADDRESS_TYPE = eINSTANCE.getEMailAddressType();

	}

} //MyPackage
