const fs = require('fs');
const path = require('path');
var debug = require('debug')('server');
const { v4: uuidv4 } = require('uuid');
var createError = require('http-errors');

const { hcxInstance } = require('../util/axios');
const { encrypt, decrypt } = require('../util/jose');
const preAuthPayload = require('../resources/jsons/preauth.json');

const privateKey = fs.readFileSync(path.join(__dirname, '..', 'resources', 'keys', 'x509-private-key.pem'), { encoding: 'utf-8' });

const preAuthSubmit = async (req, res, next) => {

    const { name, gender, recipient_code =process.env.recipient_code, error_code, error_code_message, sender_code = process.env.SENDER_CODE, amount } = req.body;
    if (!recipient_code) return next(createError(400, 'Recipient Code is mandatory'));

    const headers = {
        "x-hcx-recipient_code": recipient_code,
        "x-hcx-request_id": "059020c7-ec9a-43c3-88cb-63979db3e58d",
        "x-hcx-timestamp": new Date().toISOString(),
        "x-hcx-sender_code": sender_code,
        "x-hcx-correlation_id": uuidv4(),
        "enc": "A256GCM",
        "x-hcx-workflow_id": "29c06e68-83a9-4340-b002-ba3b8af6ff9f",
        "alg": "RSA-OAEP-256",
        "x-hcx-api_call_id": uuidv4(),
        "x-hcx-status": "request.queued",
        "x-hcx-delay": "2000",
        ...(error_code && {
            "x-hcx-status_test": "response.error",
            "x-hcx-error_details_test": { code: error_code, message: error_code_message || error_code, trace: '' }
        })
    }

    const patientEntry = preAuthPayload.entry.find(e => e.resource.resourceType === 'Patient');
    if (patientEntry) {
        patientEntry.resource.gender = gender;
        const patientName = patientEntry.resource.name;
        if (Array.isArray(patientName) && patientName[0]) {
            patientName[0].text = name;
        }
    }

    const preAuthEntry = preAuthPayload.entry.find(e => e.resource.resourceType === 'Preauth');
    if (preAuthEntry) {
        preAuthEntry.resource.patient.display = name;
        if (amount) {
            preAuthEntry.resource.total.value = amount;
        }
    }

    debug('preAuth-request-payload', JSON.stringify(preAuthPayload));

    const payload = await encrypt({ headers, payload: preAuthPayload, cert: privateKey });
    const data = JSON.stringify({ payload })

    var config = { method: 'post', url: '/' +  process.env.api_version +'/preauth/submit', data };
    debug('preAuth-payload', config);

    try {
        const response = await hcxInstance(config);
        debug('preAuth-success', response?.data);
        return res.json({
            request: preAuthPayload,
            acknowledgement: response?.data
        });
    } catch (error) {
        return next(createError(error?.response?.status || 500, error));
    }
}

const onPreAuthSubmit = async (req, res, next) => {
    let app = require('../app');
    const io = app && app.get('io');
    const requestBody = req.body;
    debug('onpreAuth_submit-req-body', requestBody);

    const { payload } = requestBody;
    debug('onpreAuth_submit-payload', payload);

    try {
        let response = requestBody;

        if (payload) {
            const decryptedPayload = await decrypt({ cert: privateKey, payload });
            response = JSON.parse(Buffer.from(decryptedPayload.plaintext).toString());
            debug('onpreAuth_submit-payload-decrypt', response);
        }

        debug('onpreAuth_submit-success', response);
        io && io.emit('acknowledgement', response);
        return res.json(response);
    } catch (error) {
        debug('onpreAuth_submit-fail', error);
        return next(createError(500, error));
    }
}


module.exports = { preAuthSubmit, onPreAuthSubmit }