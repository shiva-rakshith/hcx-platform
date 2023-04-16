import { useState, } from 'react'
import { Button, Form, Segment, Grid, Image, Loader, Message } from 'semantic-ui-react'
import { get, post } from '../service/APIService';
import { useForm } from "react-hook-form";
import { ToastContainer, toast } from 'react-toastify';
import * as _ from 'lodash'
import { useHistory } from "react-router-dom";
import { useDispatch, useSelector } from 'react-redux';
import { updateForm } from '../store/store';
import { showError } from '../utils/Error';

export const Home = () => {

    const dispatch = useDispatch()
    const { register, handleSubmit, watch, formState: { errors }, reset } = useForm();
    const [existingUser, setExistingUser] = useState(false)
    const [loader, setLoader] = useState(false)
    const [participantCode, setParticipantCode] = useState(false)
    let history = useHistory();
    let state = 0;
    const formStore = useSelector((state) => state)

    const apiVersion = process.env.REACT_APP_PARTICIPANT_API_VERSION;

    const getParticipantDetails = () => {
        setLoader(true)
        const reqBody = { filters: { participant_code: { 'eq': participantCode}}};
        post("/applicant/search/?fields=communication,sponsors", reqBody)
            .then((function (data) {
                const participant = _.get(data, 'data.participants')[0] || {}
                console.log(Object.keys(participant).length)
                if (Object.keys(participant).length !== 0) {
                    const communicationStatus = _.get(participant, 'communication.status') || 'pending'
                    dispatch(updateForm({ participant: participant, participant_code: participantCode, identity_verification: _.get(participant, 'sponsors') ? participant.sponsors[0].status : 'pending', communicationStatus: communicationStatus }))
                    if (participant.status === 'Active') {
                        history.push("/onboarding/success");
                    } else {
                        history.push("/onboarding/process?state=1");
                    }
                } else {
                    throw "Invalid Participant Code";
                }
            })).catch((function (err) {
                console.error(err)
                toast.error(_.get(err, 'response.data.error.message') || err, {
                    position: toast.POSITION.TOP_CENTER, autoClose: 2000
                });
            }))
            .finally(() => {
                setLoader(false)
            })
    }

    const newUser = () => {
        history.push("/onboarding/process");
    }

    return <>
        <ToastContainer autoClose={false} />
        <Grid centered>
            <Grid.Row columns="1">
                <div className='banner' style={{ width: '45%', marginTop: '30px' }}>
                    <Grid.Column>
                        <Image src='favicon.ico' style={{ width: '50px', marginRight: '20px' }} />
                    </Grid.Column>
                    <Grid.Column>
                        <p style={{ color: 'white', fontSize: '30px' }}><b>HCX Onboarding</b></p>
                    </Grid.Column>
                </div>
            </Grid.Row>
            <Grid.Row columns="1">
                <Segment raised padded style={{ width: '45%' }}>
                    {loader && <Loader active />}
                    <Message>
                        <Message.Header>Welcome to HCX Onboarding!</Message.Header><br />
                        <Message.Content style={{ textAlign: 'left' }}>Following is the onboarding process for new and existing users:</Message.Content>
                        <Message.Content style={{ textAlign: 'left' }}><b>New User:</b> There are 3 steps in onboarding process:</Message.Content>
                        <Message.List>
                            <Message.Item><b>Basic Details</b></Message.Item>
                            <Message.Item><b>Set Password</b></Message.Item>
                            <Message.Item><b>Update Complete Details</b></Message.Item>
                        </Message.List><br />
                        <Message.Content style={{ textAlign: 'left' }}><b>Existing User:</b> If the onboarding process has started and exited the form before completion. Please select <b>existing user</b> and enter your <b>participant code</b>. Form will take you to the stage from where you have exited.</Message.Content>
                    </Message>
                    <Form>
                        <Grid centered>
                            {existingUser ? null :
                                <Grid.Row>
                                    <button style={{ width: '36%' }} floated='right' class="ui primary button button-color" onClick={newUser}>New User</button>
                                </Grid.Row>}
                            {existingUser ? null :
                                <Grid.Row>
                                    <button style={{ width: '36%' }} class="ui primary button button-color" onClick={e => setExistingUser(true)}> Existing User</button>
                                </Grid.Row>}
                            {existingUser ?
                                <Grid.Row>
                                    <Form.Field style={{ width: '40%' }} disabled={loader}>
                                        <input placeholder='Enter Participant Code' onInput={e => setParticipantCode(e.target.value)} {...register("participant_code", { required: true })} />
                                    </Form.Field>
                                </Grid.Row> : null}
                            {existingUser ?
                                <Grid.Row>
                                    <Button disabled={loader} onClick={getParticipantDetails} className="primary center-element button-color">
                                        {loader ? "Submitting" : "Submit"}</Button>
                                </Grid.Row> : null}
                        </Grid>
                    </Form>
                </Segment>
            </Grid.Row>

        </Grid>
    </>

}