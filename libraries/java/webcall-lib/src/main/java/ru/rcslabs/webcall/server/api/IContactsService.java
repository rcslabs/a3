package ru.rcslabs.webcall.server.api;

public interface IContactsService {
	
	/**
	 * Returns contacts in JSON format:<br/>
	 * <pre>
	 * {
	 *     "id":"33963930",
	 *     "lastModified":"2011-04-20T08:29:51.234Z",
	 *     "role":"",
	 *     "note":"",
	 *     "metaData":null,
	 *     "givenName":"Joe",
	 *     "msisdnList":["792811111111"],
	 *     "familyName":"Cocker",
	 *     "nickname":"",
	 *     "middleName":"",
	 *     "birthday":null,
	 *     "createdDate":"2011-04-20T08:29:51.232Z",
	 *     "organizationName":"",
	 *     "prefixName":"",
	 *     "suffixName":"",
	 *     "title":"",
	 *     "gender":null,
	 *     "addresses":[],
	 *     "variableProperties":null,
	 *     "urls":[],
	 *     "telephoneNumbers":[
	 *     {
	 *         "metaData":null,
	 *         "telephoneNumber":"792811111111",
	 *         "contactPropertyTypes":["TYPE_CELL"]
	 *     }],
	 *     "emailAddresses":[],
	 *     "hasContactPhoto":false,
	 *     "contactSource":"MEMORYBANK",
	 *     "editable":true
	 * }
	 * </pre>
	 * @param uid
	 * @return contacts in JSON format
	 */
	String getContacts(String uid);

	String getContactDetails(String uid, String phoneNumber);
	String getUserProfiles(String uid, String phoneNumber);
	
	String addContact(String uid, String phoneNumber);
	void deleteContact(String uid, String phoneNumber);
	String searchContacts(String uid, String searchString);
	
	
}
