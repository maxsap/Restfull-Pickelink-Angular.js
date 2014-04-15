package com.gr.project.security.rest;

import com.gr.project.data.PersonDAO;
import com.gr.project.data.PersonListProducer;
import com.gr.project.model.Email;
import com.gr.project.model.Person;
import com.gr.project.security.TokenManager;
import com.gr.project.security.UserLoggedIn;
import com.gr.project.security.credential.Token;
import com.gr.project.security.model.MyUser;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.query.IdentityQuery;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/users")
@Stateless
public class UserRestService {

    @Inject
    @Named("default.return.message.parameter")
    private String MESSAGE_RESPONSE_PARAMETER;

    @Inject
    private TokenManager tokenManager;

    @Inject
    private IdentityManager identityManager;
    
    @Inject
    private PersonListProducer persons;

    @Inject
    @Any
    private Event<Email> event;

    @Inject
    private PersonDAO personDAO;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @UserLoggedIn
    public List<Person> listAllPersons() {
    	return persons.getPersons();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @UserLoggedIn
    public Person lookupPersonById(@PathParam("id") String id) {
    	Person person = personDAO.findById(id);
        if (person == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return person;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createMember(@NotNull RegistrationRequest request) {

        Map<String, Object> response = new HashMap<String, Object>();

        if (!request.getPassword().equals(request.getPasswordConfirmation())) {
            response.put(MESSAGE_RESPONSE_PARAMETER, "Password mismatch.");
        } else {
            try {
                // if there is no user with the provided e-mail, perform registration
                if (getUser(request) == null) {
                    String activationCode = createAccount(request);
                    
                    // XXX handle the path better.
                    Email email = new Email("Please complete the signup", "http://localhost:8080/Project/#/activate/" + activationCode + "?username=" + request
                        .getEmail(), request.getEmail());

                    event.fire(email);

                    return Response.status(Response.Status.OK).entity("ok").type(MediaType.APPLICATION_JSON_TYPE).build();
                } else {
                    response.put(MESSAGE_RESPONSE_PARAMETER, "This username is already in use. Try another one.");
                }
            } catch (IdentityManagementException ime) {
                response.put(MESSAGE_RESPONSE_PARAMETER, "Oops ! Registration failed, try it later.");
            }
        }

        return Response.status(Response.Status.BAD_REQUEST).entity(response).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    private MyUser getUser(RegistrationRequest request) {
        IdentityQuery<MyUser> query = this.identityManager.createIdentityQuery(MyUser.class);

        query.setParameter(MyUser.USER_NAME, request.getEmail());

        List<MyUser> result = query.getResultList();

        if (!result.isEmpty()) {
            return result.get(0);
        }

        return null;
    }

    @POST
    @Path("/activation")
    public Response memberActivation(@NotNull String activationCode) {

        IdentityQuery<MyUser> query = this.identityManager.createIdentityQuery(MyUser.class);

        List<MyUser> result = query
            .setParameter(IdentityType.QUERY_ATTRIBUTE.byName("ActivationCode"), activationCode.replaceAll("\"", ""))
            .getResultList();

        if (result == null || result.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Not Found").type(MediaType.APPLICATION_JSON_TYPE).build();
        }

        MyUser user = result.get(0);
        boolean enabled = user.isEnabled();

        if (enabled) {
//            return Response.status(Response.Status.BAD_REQUEST).entity("User Already Active").type(MediaType.APPLICATION_JSON_TYPE)
//                .build();
        }

        user.setEnabled(true);

        this.identityManager.update(user);

        Token token = this.tokenManager.issue(user);

        this.identityManager.updateCredential(user, token);

        Map<String, String> map = new HashMap<String, String>();

        map.put("token", token.getToken());

        return Response.status(Response.Status.OK).entity(map).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    private String createAccount(RegistrationRequest request) {
        Person person = new Person();

        person.setEmail(request.getEmail());
        person.setFirstName(request.getFirstName());
        person.setLastName(request.getLastName());

        MyUser newUser = new MyUser(request.getEmail());

        newUser.setPerson(person);
        newUser.setEnabled(false); // by default, user is disabled until the account is activated.

        String activationCode = "12345";

        newUser.setAttribute(new Attribute<String>("ActivationCode", activationCode)); // we set an activation code for future use.

        this.identityManager.add(newUser);

        Password password = new Password(request.getPassword());

        this.identityManager.updateCredential(newUser, password);

        return activationCode;
    }
}
