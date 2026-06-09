package io.nh_backend.rest_quest.item.controller;

import io.nh_backend.rest_quest.common.constant.ErrorCode;
import io.nh_backend.rest_quest.common.constant.SuccessCode;
import io.nh_backend.rest_quest.common.exception.BusinessException;
import io.nh_backend.rest_quest.item.dto.ItemResponse;
import io.nh_backend.rest_quest.item.service.ItemService;
import io.nh_backend.rest_quest.user.service.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ItemController 클래스의")
class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private ItemService itemService;

    private final String BASE_URL = "/api/v1/items";

    @Nested
    @DisplayName("Description: 전체 아이템 카탈로그 목록 조회 ( GET /api/v1/items )")
    class Description_with_get_all_items {

        @Nested
        @DisplayName("Context: 유니티 클라이언트가 평범하게 목록 조회를 요청하는 경우")
        class Context_normal_request {

            @Test
            @DisplayName("It: 200 OK 상태 코드와 함께 ApiResponse 성공 래퍼 규격을 반환한다.")
            void It_전체_목록_조회_성공() throws Exception {
                // given
                List<ItemResponse> mockList = List.of(
                        ItemResponse.builder()
                                .itemId(1L).rId("sword_001").itemName("연습용 검이다.")
                                .itemType("WEAPON").itemGrade("COMMON").description("설명")
                                .price(100).gemPrice(0).sellPrice(50)
                                .build(),
                        ItemResponse.builder()
                                .itemId(2L).rId("sword_002").itemName("초보자용 검이다.")
                                .itemType("WEAPON").itemGrade("COMMON").description("설명")
                                .price(200).gemPrice(0).sellPrice(100)
                                .build()
                );
                given(itemService.getAllItems()).willReturn(mockList);

                // when
                ResultActions perform = mockMvc.perform(
                        MockMvcRequestBuilders.get(BASE_URL).accept(MediaType.APPLICATION_JSON)
                );

                // then
                perform
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.message").value(SuccessCode.ITEM_READ.getSuccessMessage()))
                        .andExpect(jsonPath("$.data").isArray())
                        .andExpect(jsonPath("$.data.length()").value(2))
                        .andExpect(jsonPath("$.data[0].itemId").value(1L))
                        .andExpect(jsonPath("$.data[0].price").value(100))
                        .andExpect(jsonPath("$.data[1].itemId").value(2L))
                        .andExpect(jsonPath("$.data[1].price").value(200));

                then(itemService).should().getAllItems();
            }
        }
    }

    @Nested
    @DisplayName("Description: 아이템 단건 상세 조회 ( GET /api/v1/items/{id} )")
    class Description_with_get_item_by_id {

        @Nested
        @DisplayName("Context: 마스터 데이터에 등록되어 있는 유효한 식별자 ID(1L)를 넘긴 경우")
        class Context_with_valid_id {

            @Test
            @DisplayName("It: 200 OK 상태 코드와 유니티 바인딩 전용 필드명이 적용된 단건 JSON을 반환한다.")
            void It_단건_상세조회_성공() throws Exception {
                // given
                Long validId = 1L;
                ItemResponse mockResponse = ItemResponse.builder()
                        .itemId(validId).rId("sword_001").itemName("연습용 검")
                        .itemType("WEAPON").itemGrade("COMMON").description("설명")
                        .price(100).gemPrice(0).sellPrice(50)
                        .build();
                given(itemService.getItemById(validId)).willReturn(mockResponse);

                // when
                ResultActions perform = mockMvc.perform(
                        MockMvcRequestBuilders.get(BASE_URL + "/" + validId).accept(MediaType.APPLICATION_JSON)
                );

                // then
                perform
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.message").value(SuccessCode.ITEM_READ_SINGLE.getSuccessMessage()))
                        .andExpect(jsonPath("$.data.itemId").value(validId))
                        .andExpect(jsonPath("$.data.price").value(100))
                        .andExpect(jsonPath("$.data.gemPrice").value(0));

                then(itemService).should().getItemById(validId);
            }
        }

        @Nested
        @DisplayName("Context: 존재하지 않는 허구의 ID(20L)를 주소에 기입해 호출한 경우")
        class Context_with_invalid_id {

            @Test
            @DisplayName("It: 글로벌 핸들러가 예외를 인터셉트하여 404 에러와 함께 규격화된 ErrorBody를 리턴한다.")
            void It_단건_상세조회_실패_404_반환() throws Exception {
                // given
                Long invalidId = 20L;
                given(itemService.getItemById(invalidId))
                        .willThrow(new BusinessException(ErrorCode.ITEM_NOT_FOUND));

                // when
                ResultActions perform = mockMvc.perform(
                        MockMvcRequestBuilders.get(BASE_URL + "/" + invalidId).accept(MediaType.APPLICATION_JSON)
                );

                // then
                perform
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.message").value(ErrorCode.ITEM_NOT_FOUND.getDescription()))
                        .andExpect(jsonPath("$.data").isEmpty());

                then(itemService).should().getItemById(invalidId);
            }
        }
    }
}