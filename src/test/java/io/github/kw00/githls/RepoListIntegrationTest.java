package io.github.kw00.githls;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.github.tomakehurst.wiremock.WireMockServer;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RepoListIntegrationTest {
    @LocalServerPort
    int port;

    RestTemplate restTemplate = new RestTemplate();
    
    static WireMockServer wireMock;

    @DynamicPropertySource
    static void propertySetup(DynamicPropertyRegistry registry) {
        registry.add("github.api-base", wireMock::baseUrl);
    }

    @BeforeAll
    static void setup() {
        wireMock = new WireMockServer(options().dynamicPort());
        wireMock.start();
    }

    @BeforeEach
    void initWireMock() {
        wireMock.stubFor(
            get(urlPathEqualTo("/users/userA/repos"))
            .atPriority(1)
            .willReturn(okJson(
                """
                [
                    {
                        "name": "repoA",
                        "owner": {
                            "login": "userA"
                        },
                        "fork": false
                    },
                    {
                        "name": "repoB",
                        "owner": {
                            "login": "userB"
                        },
                        "fork": false
                    },
                    {
                        "name": "repoC",
                        "owner": {
                            "login": "userA"
                        },
                        "fork": true
                    }
                ]
                """
            ))
        );
        wireMock.stubFor(
            get(urlPathEqualTo("/repos/userA/repoA/branches"))
            .atPriority(1)
            .willReturn(okJson(
                """
                [
                    {
                        "name": "main",
                        "commit": {
                            "sha": "hashx"
                        }
                    }
                ] 
                """
            ))
        );
        wireMock.stubFor(
            get(urlPathEqualTo("/repos/userB/repoB/branches"))
            .atPriority(1)
            .willReturn(okJson(
                """
                [
                    
                    {
                        "name": "master",
                        "commit": {
                            "sha": "hashx"
                        }
                    },
                    {
                        "name": "feature",
                        "commit": {
                            "sha": "hashx"
                        }
                    }
                ] 
                """
            ))
        );
        wireMock.stubFor(
            get(urlPathMatching("/.*"))
            .atPriority(10)
            .willReturn(aResponse().withStatus(404).withBody(
                """
                {
                    "message": "Not Found",
                    "documentation_url": "https://example.com/rest",
                    "status": 404
                } 
                """
            ))
        );
    }

    @Test
    void shouldReturnReposThatAreNotForks() {

        ResponseEntity<String> response =
            restTemplate.getForEntity(baseUrl() + "/userA", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        var mapper = new ObjectMapper();
        var expectedBody = mapper.readTree(
            """
            [
                {
                    "name": "repoA",
                    "ownerLogin": "userA",
                    "branches": [
                        {
                            "name": "main",
                            "lastCommitSha": "hashx"
                        }
                    ]
                },
                {
                    "name": "repoB",
                    "ownerLogin": "userB",
                    "branches": [
                        {
                            "name": "master",
                            "lastCommitSha": "hashx"
                        },
                        {
                            "name": "feature",
                            "lastCommitSha": "hashx"
                        }
                    ]
                }
            ] 
            """
        );
        var actualBody = mapper.readTree(response.getBody());
        assertEquals(expectedBody, actualBody);
   }

    
    @Test
    void shouldReturnNotFoundErrorResponseIfUserNotFound() {
        try {
            restTemplate.getForEntity(baseUrl() + "/blahblahblah", String.class);
        } catch (HttpClientErrorException e) {
            assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());

            var mapper = new ObjectMapper();
            var bodyAsJsonTree = mapper.readTree(e.getResponseBodyAsString());
            assertTrue(bodyAsJsonTree.get("status").isString());
            assertTrue(bodyAsJsonTree.get("message").isString());
            return;
        }
        throw new RuntimeException("Expected 404 status.");

    }

    @AfterAll
    static void teardown() {
        wireMock.stop();
    }

    String baseUrl() {
        return "http://localhost:" + port;
    }
}
