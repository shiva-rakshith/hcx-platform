package io.hcxprotocol.interfaces;


import com.fasterxml.jackson.core.JsonProcessingException;
import io.hcxprotocol.dto.HCXIntegrator;
import io.hcxprotocol.utils.Operations;

import java.util.Map;

/**
 * The <b>Outgoing</b> Interface provide the methods to help in creating the JWE Payload and send the request to the sender system from HCX Gateway.
 * The implementation of this interface process the FHIR object, generates the JWE Payload and call the HCX Gateway API based on operation.
 */
public interface OutgoingInterface {

    /**
     * The outgoing request from participant system to the HCX Gateway generated using FHIR Object.
     * It has the implementation of the below steps to create JWE Payload and send the request.
     * <ul>
     *     <li>Validating the FHIR object using HCX FHIR IG.</li>
     *     <li>Crate HCX Protocol headers based on the request.</li>
     *     <li>Fetch the sender encryption public key from the HCX participant registry.</li>
     *     <li>Encrypt the FHIR object along with HCX Protocol headers using <b>RFC7516</b> to create JWE Payload.</li>
     *     <li>Generate or fetch the authorization token of HCX Gateway.</li>
     *     <li>Trigger HCX Gateway REST API based on operation using the above JWE Payload.</li>
     * </ul>
     * @param fhirPayload The FHIR object created by the participant system.
     * @param operation The HCX operation or action defined by specs to understand the functional behaviour.
     * @param recipientCode The recipient code from HCX Participant registry.
     * @param actionJwe The JWE Payload from the incoming request for which the response JWE Payload created here.
     * @param onActionStatus The HCX Protocol header status (x-hcx-status) value to use while creating the JEW Payload.
     * @param output A wrapper map to collect the outcome (errors or response) of the JWE Payload generation process using FHIR object.
     * <ol>
     *    <li>output -
     *    <pre>
     *    {@code {
     *       "payload":{}, -  jwe payload
     *       "responseObj":{} - success/error response object
     *    }}</pre>
     *    </li>
     *    <li>success response object -
     *    <pre>
     *    {@code {
     *       "timestamp": , - unix timestamp
     *       "correlation_id": "", - fetched from incoming request
     *       "api_call_id": "" - fetched from incoming request
     *    }}</pre>
     *    </li>
     *    <li>error response object -
     *    <pre>
     *    {@code {
     *       "timestamp": , - unix timestamp
     *       "error": {
     *           "code" : "", - error code
     *           "message": "", - error message
     *           "trace":"" - error trace
     *        }
     *    }}</pre>
     *    </li>
     *  </ol>
     * @return It is a boolean value to understand the outgoing request generation is successful or not.
     *
     * <ol>
     *      <li>true - It is successful.</li>
     *      <li>false - It is failure.</li>
     * </ol>
     */
    boolean processFunction(String fhirPayload, Operations operation, String recipientCode, String actionJwe, String onActionStatus, Map<String,Object> output);

    /**
     * The FHIR object resource type validation based on the operation executed here.
     * Also, it uses the HCX FHIR IG to validate the format, structure and minimum required attributes.
     *
     * @param fhirPayload The FHIR object created by the participant system.
     * @param operation The HCX operation or action defined by specs to understand the functional behaviour.
     * @param error A wrapper map to collect the errors from the FHIR Payload validation.
     * <pre>
     *    {@code {
     *       "error_code": "error_message"
     *    }}</pre>
     * @return It is a boolean value to understand the validation status of FHIR Payload.
     *  <ol>
     *      <li>true - Validation is successful.</li>
     *      <li>false - Validation is failure.</li>
     *  </ol>
     */
    boolean validatePayload(String fhirPayload, Operations operation, Map<String,Object> error);

    /**
     * The HCX Protocol Headers created using the input parameters to generate the JWE Payload.
     *
     * @param recipientCode The recipient code from HCX Participant registry.
     * @param actionJwe The JWE Payload from the incoming request for which the response JWE Payload created here.
     * @param onActionStatus The HCX Protocol header status (x-hcx-status) value to use while creating the JEW Payload.
     * @param headers The HCX Protocol headers to create the JWE Payload.
     * @return It is a boolean value to understand the HCX Protocol Headers generation is successful or not.
     * <ol>
     *      <li>true - It is successful.</li>
     *      <li>false - It is failure.</li>
     * </ol>
     */
    boolean createHeader(String recipientCode, String actionJwe, String onActionStatus, Map<String,Object> headers);

    /**
     * It uses the HCX Protocol Headers and FHIR object to generate the JWE Payload using RFC7516.
     *
     * @param headers The HCX Protocol headers to create the JWE Payload.
     * @param fhirPayload The FHIR object created by the participant system.
     * @param output A wrapper map to collect the JWE Payload or the error details in case of failure.
     * <ol>
     *    <li>success output -
     *    <pre>
     *     {@code {
     *       "payload": "" - jwe payload
     *     }}</pre>
     *    </li>
     *    <li>error output -
     *    <pre>
     *    {@code {
     *       "error_code": "error_message"
     *    }}</pre>
     *    </li>
     *  </ol>
     * @return It is a boolean value to understand the encryption of FHIR object is successful or not.
     * <ol>
     *     <li>true - It is successful.</li>
     *     <li>false - It is failure.</li>
     * </ol>
     */
    boolean encryptPayload(Map<String,Object> headers, String fhirPayload, Map<String,Object> output);

    /**
     * Uses the input parameters and the SDK configuration to call HCX Gateway REST API based on the operation.
     *
     * @param jwePayload The JWE Payload created using HCX Protocol Headers and FHIR object.
     * @param operation The HCX operation or action defined by specs to understand the functional behaviour.
     * @param response A wrapper map to collect response of the REST API call.
     * <ol>
     *    <li>success response -
     *    <pre>
     *    {@code {
     *       "responseObj": {
     *       "timestamp": , - unix timestamp
     *       "correlation_id": "", - fetched from incoming request
     *       "api_call_id": "" - fetched from incoming request
     *      }
     *    }}</pre>
     *    </li>
     *    <li>error response -
     *    <pre>
     *    {@code {
     *      "responseObj":{
     *       "timestamp": , - unix timestamp
     *       "error": {
     *           "code" : "", - error code
     *           "message": "", - error message
     *           "trace":"" - error trace
     *        }
     *      }
     *    }}</pre>
     *    </li>
     *  </ol>
     * @return It is a boolean value to understand the REST API call execution is successful or not.
     *
     * @throws JsonProcessingException The exception throws when it is having issues in parsing the JSON object.
     */
    boolean initializeHCXCall(String jwePayload, Operations operation, Map<String,Object> response) throws JsonProcessingException;

}
