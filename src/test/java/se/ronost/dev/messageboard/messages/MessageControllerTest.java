package se.ronost.dev.messageboard.messages;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import se.ronost.dev.messageboard.MessageboardApplication;
import se.ronost.dev.messageboard.users.User;
import se.ronost.dev.messageboard.users.UserRepository;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MessageboardApplication.class)
@WebAppConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)

public class MessageControllerTest {


    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    private MockMvc mockMvc;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream()
            .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
            .findAny()
            .orElse(null);

        assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }

    private User mrPink, niceGuyEddie;

    @Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();

        this.messageRepository.deleteAllInBatch();
        this.userRepository.deleteAllInBatch();

        this.mrPink = new User("Mr.Pink");
        this.niceGuyEddie = new User("Nice Guy Eddie");
        this.userRepository.save(mrPink);
        this.userRepository.save(niceGuyEddie);

        this.messageRepository.save(new Message("C'mon, throw in a buck!", this.niceGuyEddie));
        this.messageRepository.save(new Message("Uh-uh, I don't tip.", this.mrPink));
        this.messageRepository.save(new Message("You don't tip?", this.niceGuyEddie));
        this.messageRepository.save(new Message("Nah, I don't believe in it.", this.mrPink));
        this.messageRepository.save(new Message("You don't believe in tipping?", this.niceGuyEddie));
    }

    @Test
    public void shouldListMessages() throws Exception {
        mockMvc.perform(get("/messages"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].message", is("C'mon, throw in a buck!")))
                .andExpect(jsonPath("$[0].id", isA(int.class)))
                .andExpect(jsonPath("$[0].created", is(notNullValue())))
                .andExpect(jsonPath("$[0].user.id", isA(int.class)))
                .andExpect(jsonPath("$[0].user.username", is(this.niceGuyEddie.getUsername())))
                .andExpect(jsonPath("$[1].message", is("Uh-uh, I don't tip.")))
                .andExpect(jsonPath("$[1].id", isA(int.class)))
                .andExpect(jsonPath("$[1].created", is(notNullValue())))
                .andExpect(jsonPath("$[1].user.id", isA(int.class)))
                .andExpect(jsonPath("$[1].user.username", is(this.mrPink.getUsername())))
                .andExpect(jsonPath("$[2].message", is("You don't tip?")))
                .andExpect(jsonPath("$[2].id", isA(int.class)))
                .andExpect(jsonPath("$[2].created", is(notNullValue())))
                .andExpect(jsonPath("$[2].user.id", isA(int.class)))
                .andExpect(jsonPath("$[2].user.username", is(this.niceGuyEddie.getUsername())))
                .andExpect(jsonPath("$[3].message", is("Nah, I don't believe in it.")))
                .andExpect(jsonPath("$[3].id", isA(int.class)))
                .andExpect(jsonPath("$[3].created", is(notNullValue())))
                .andExpect(jsonPath("$[3].user.id", isA(int.class)))
                .andExpect(jsonPath("$[3].user.username", is(this.mrPink.getUsername())))
                .andExpect(jsonPath("$[4].message", is("You don't believe in tipping?")))
                .andExpect(jsonPath("$[4].id", isA(int.class)))
                .andExpect(jsonPath("$[4].created", is(notNullValue())))
                .andExpect(jsonPath("$[4].user.id", isA(int.class)))
                .andExpect(jsonPath("$[4].user.username", is(this.niceGuyEddie.getUsername())));
    }

    @Test
    public void shouldAddMessage() throws Exception {
        String messageJson = json(new Message("You know what this is? The world's smallest violin playing just for the waitresses.", this.mrPink));

        this.mockMvc.perform(post("/messages")
                .header("X-AUTH-USER-HEADER", this.mrPink.getUsername())
                .contentType(contentType)
                .content(messageJson))
                .andExpect(status().isCreated())
                .andReturn();

        this.mockMvc.perform(get("/messages"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[5].message", is("You know what this is? The world's smallest violin playing just for the waitresses.")))
                .andExpect(jsonPath("$[5].id", isA(int.class)))
                .andExpect(jsonPath("$[5].created", is(notNullValue())))
                .andExpect(jsonPath("$[5].user.id", isA(int.class)))
                .andExpect(jsonPath("$[5].user.username", is(this.mrPink.getUsername())));
    }

    @Test
    public void shouldFailToAddMessageWhenAuthenticatedUserIsUnknown() throws Exception {
        String messageJson = json(new Message("Some message", this.mrPink));

        this.mockMvc.perform(post("/messages")
                .header("X-AUTH-USER-HEADER", "Some random dude")
                .contentType(contentType)
                .content(messageJson))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }

    @Test
    public void shouldEditMessage() throws Exception {
        String messageJson = json(new Message("Message!!", this.mrPink));

        this.mockMvc.perform(post("/messages")
                .header("X-AUTH-USER-HEADER", this.mrPink.getUsername())
                .contentType(contentType)
                .content(messageJson))
                .andExpect(status().isCreated())
                .andReturn();

        this.mockMvc.perform(get("/messages"))
                .andExpect(jsonPath("$[5].message", is("Message!!")))
                .andExpect(jsonPath("$[5].user.username", is(this.mrPink.getUsername())));

        messageJson = json(new Message("Edited message!!", this.mrPink));

        Long latestId = this.getLatestId();

        this.mockMvc.perform(put("/messages/" + latestId.toString())
                .header("X-AUTH-USER-HEADER", "Mr.Pink")
                .contentType(contentType)
                .content(messageJson))
                .andExpect(status().isOk())
                .andReturn();

        this.mockMvc.perform(get("/messages"))
                .andExpect(jsonPath("$[5].message", is("Edited message!!")))
                .andExpect(jsonPath("$[5].user.username", is(this.mrPink.getUsername())));
    }

    @Test
    public void shouldFailToEditMessageWhenWrongUser() throws Exception {
        String messageJson = json(new Message("Message!!", this.mrPink));
        
        this.mockMvc.perform(post("/messages")
        .header("X-AUTH-USER-HEADER", this.mrPink.getUsername())
        .contentType(contentType)
        .content(messageJson))
        .andExpect(status().isCreated())
        .andReturn();
        
        Long latestId = this.getLatestId();

        this.mockMvc.perform(put("/messages/" + latestId.toString())
        .header("X-AUTH-USER-HEADER", this.niceGuyEddie.getUsername())
        .contentType(contentType)
        .content(messageJson))
        .andExpect(status().isForbidden())
        .andReturn();

        this.mockMvc.perform(put("/messages/" + latestId.toString())
        .header("X-AUTH-USER-HEADER", "Some random dude")
        .contentType(contentType)
        .content(messageJson))
        .andExpect(status().isUnauthorized())
        .andReturn();
    }

    @Test
    public void shouldDeleteMessage() throws Exception {
        String messageJson = json(new Message("Message to be deleted!!", this.mrPink));

        this.mockMvc.perform(post("/messages")
                .header("X-AUTH-USER-HEADER", this.mrPink.getUsername())
                .contentType(contentType)
                .content(messageJson))
                .andExpect(status().isCreated())
                .andReturn();

        this.mockMvc.perform(get("/messages"))
                .andExpect(jsonPath("$[5].message", is("Message to be deleted!!")))
                .andExpect(jsonPath("$[5].user.username", is("Mr.Pink")));

        Long latestId = this.getLatestId();                ;

        this.mockMvc.perform(delete("/messages/" + latestId.toString())
                .header("X-AUTH-USER-HEADER", this.mrPink.getUsername())
                .contentType(contentType))
                .andExpect(status().isOk());

        this.mockMvc.perform(delete("/messages/" + latestId.toString())
                .header("X-AUTH-USER-HEADER", this.mrPink.getUsername())
                .contentType(contentType))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shoulFailTodDeleteMessageWhenWrongUser() throws Exception {
        String messageJson = json(new Message("Message to be deleted!!", this.mrPink));

        this.mockMvc.perform(post("/messages")
                .header("X-AUTH-USER-HEADER", this.mrPink.getUsername())
                .contentType(contentType)
                .content(messageJson))
                .andExpect(status().isCreated())
                .andReturn();

        this.mockMvc.perform(get("/messages"))
                .andExpect(jsonPath("$[5].message", is("Message to be deleted!!")))
                .andExpect(jsonPath("$[5].user.username", is("Mr.Pink")));

        Long latestId = this.getLatestId();                ;

        this.mockMvc.perform(delete("/messages/" + latestId.toString())
                .header("X-AUTH-USER-HEADER", this.niceGuyEddie.getUsername())
                .contentType(contentType))
                .andExpect(status().isForbidden());

        this.mockMvc.perform(delete("/messages/" + latestId.toString())
                .header("X-AUTH-USER-HEADER", "Some random dude")
                .contentType(contentType))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldFailWhenInvalidUser() throws Exception {
        String messageJson = json(new Message("You know what this is? The world's smallest violin playing just for the waitresses.", this.mrPink));
        Long latestId = this.getLatestId();

        this.mockMvc.perform(post("/messages")
                .header("X-AUTH-USER-HEADER", "")
                .contentType(contentType)
                .content(messageJson))
                .andExpect(status().isBadRequest())
                .andReturn();
        
        this.mockMvc.perform(post("/messages")
                .contentType(contentType)
                .content(messageJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        this.mockMvc.perform(put("/messages/" + latestId.toString())
                .header("X-AUTH-USER-HEADER", "")
                .contentType(contentType)
                .content(messageJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        this.mockMvc.perform(put("/messages/" + latestId.toString())
                .contentType(contentType)
                .content(messageJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        this.mockMvc.perform(delete("/messages/" + latestId.toString())
                .header("X-AUTH-USER-HEADER", "")
                .contentType(contentType))
                .andExpect(status().isBadRequest());
        
        this.mockMvc.perform(delete("/messages/" + latestId.toString())
                .contentType(contentType))
                .andExpect(status().isBadRequest());
    }

    protected String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }

    private Long getLatestId() {
        return this.messageRepository.findAll(new Sort(Sort.Direction.DESC, "id")).get(0).getId();
    }
}
