package com.fincons.parkingsystem.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
//@TestInstance(Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class AbstractIntegrationTest {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:13.3")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }



    @Autowired
    protected MockMvc mockMvc;
    protected final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules().configure(
                    SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // POST
    protected <T> T performPostRequest(String path, Object body, Class<T> responseType, ResultMatcher expectedStatus) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body));
        return performRequest(requestBuilder, responseType, expectedStatus);
    }

    protected <T> T performPostRequestExpectedSuccess(String path, Object body, Class<T> responseType) throws Exception {
        return performPostRequest(path, body, responseType, status().is2xxSuccessful());
    }

    // GET
    protected <T> T performGetRequest(String path, Class<T> responseType, ResultMatcher expectedStatus) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(path);
        return performRequest(requestBuilder, responseType, expectedStatus);
    }

    // DELETE
    protected <T> T performDeleteRequest(String path, Class<T> responseType, ResultMatcher expectedStatus) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.delete(path);
        return performRequest(requestBuilder, responseType, expectedStatus);
    }

    // PATCH
    protected <T> T performPatchRequest(String path, Object body, Class<T> responseType, ResultMatcher expectedStatus) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.patch(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body));
        return performRequest(requestBuilder, responseType, expectedStatus);
    }

    // Generic request performer
    private <T> T performRequest(MockHttpServletRequestBuilder requestBuilder, Class<T> responseType, ResultMatcher expectedStatus) throws Exception {
        MvcResult mvcResult = mockMvc.perform(requestBuilder)
                .andExpect(expectedStatus)
                .andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        if (jsonResponse.isEmpty() || responseType == Void.class) {
            return null;
        }
        return convertStringToClass(jsonResponse, responseType);
    }

    private <T> T convertStringToClass(String jsonString, Class<T> responseType) throws JsonProcessingException {
        return mapper.readValue(jsonString, responseType);
    }
}
