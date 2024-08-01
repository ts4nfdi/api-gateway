import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.semantics.apigateway.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class GatewayControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private SearchService searchService;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private JsonNode mockedOlsSearch;
	private JsonNode mockedOntoportalSearch;
	private JsonNode mockerSkosmosSearch;

	@BeforeEach
	public void setUp() throws IOException {
		mockedOlsSearch = objectMapper.readTree(new ClassPathResource("search/ols.json").getFile());
		mockedOntoportalSearch = objectMapper.readTree(new ClassPathResource("search/ontoportal.json").getFile());
		mockerSkosmosSearch = objectMapper.readTree(new ClassPathResource("search/skosmos.json").getFile());
	}

	@Test
	public void testSearch() throws Exception {
		// Arrange
		String userId = "user123";
		String orderId = "order123";
		Mockito.when(searchService.getUserDetails(userId)).thenReturn(mockedUserDetails);
		Mockito.when(orderService.getOrderDetails(orderId)).thenReturn(mockedOrderDetails);

		// Act & Assert
		mockMvc.perform(get("/details")
						.param("userId", userId)
						.param("orderId", orderId))
				.andExpect(status().isOk())
				.andExpect(content().string("mockedUserDetails mockedOrderDetails"));
	}
}
