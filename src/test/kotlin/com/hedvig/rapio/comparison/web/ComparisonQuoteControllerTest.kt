package com.hedvig.rapio.comparison.web

import com.hedvig.rapio.comparison.ComparisonQuoteController
import com.hedvig.rapio.comparison.QuoteService
import com.hedvig.rapio.comparison.domain.ComparisonQuote
import com.hedvig.rapio.comparison.domain.QuoteData
import com.hedvig.rapio.comparison.web.dto.QuoteResponseDTO
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.hamcrest.Matchers
import org.javamoney.moneta.Money
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.util.*

@WebMvcTest(controllers = [ComparisonQuoteController::class], secure = false)
internal class ComparisonQuoteControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc;

    @MockkBean
    lateinit var quoteService:QuoteService;

    val createRequestJson = """
        {"requestId":"adads",
         "productType": "HOME",
         "quoteData": { 
            "personalNumber": "191212121212",
            "street": "testgatan",
            "zipCode": "12345",
            "city": "Stockholm",
            "livingSpace": 42,
            "householdSize": 2,
            "includeBrfCoverage": false,
            "isStudent": false
         },
         "phoneNumber":"07012123131"
        }
    """.trimIndent()

    @Test
    fun create_quote(){

        val response = QuoteResponseDTO(
                requestId = "adads",
                price = Money.of(123,"SEK"),
                quoteId = UUID.randomUUID(),
                validUntil = Instant.now().epochSecond
                )
        every { quoteService.createQuote(any()) } returns(response)

        val request = post("/v1/quote")
                .with(user("compricer"))
                .content(createRequestJson)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)

        val result = mockMvc.perform(request)

        result
                .andExpect(status().is2xxSuccessful)
                .andExpect(jsonPath("$.id", Matchers.any(String::class.java)))

    }

    val signRequestJson = """
        {
            "requestId": "jl",
            "startsAt": {
                "date": "2019-11-01",
                "timezone": "Europe/Stockholm"
            },
            "email": "some@test.com"
        }
    """.trimIndent()

    @Test
    fun sign_quote(){

        val id = "123"

        val request = post("/v1/quote/$id/sign")
                .with(user("compricer"))
                .content(signRequestJson)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)

        val result = mockMvc.perform(request)

        result
                .andExpect(status().is2xxSuccessful)
                .andExpect(jsonPath("$.id", Matchers.equalTo(id)))

    }

}