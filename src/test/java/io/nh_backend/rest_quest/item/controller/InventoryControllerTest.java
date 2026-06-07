package io.nh_backend.rest_quest.item.controller;

import io.nh_backend.rest_quest.common.constant.ErrorCode;
import io.nh_backend.rest_quest.common.constant.SuccessCode;
import io.nh_backend.rest_quest.item.dto.UserItemQuantityRequest;
import io.nh_backend.rest_quest.item.dto.UserItemResponse;
import io.nh_backend.rest_quest.item.service.UserItemService;
import io.nh_backend.rest_quest.user.service.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;


import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(InventoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("InventoryController 클래스의")
class InventoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserItemService userItemService;

    private final String BASE_URL = "/api/v1/users/me/inventory";
    private final String USER_EMAIL = "user"; // @WithMockUser의 기본 username 기본값과 일치시킵니다.

    @Nested
    @DisplayName("Describe: 내 인벤토리 전체 목록 조회 API(GET)는")
    class Describe_with_get_my_inventory {

        @Nested
        @DisplayName("Context: 정상적인 유저가 가방 조회를 요청하는 경우")
        class Context_normal_flow {
            @Test
            @DisplayName("It: 200 OK 성공 응답과 함께 가방 DTO 목록 리스트를 반환한다.")
            void It_인벤토리_조회_웹_성공() throws Exception {
                // given
                UserItemResponse mockResponse = new UserItemResponse(
                        1L, 10L, "sword_01", "연습용 목검", "WEAPON", "COMMON",
                        "목검", 100, 0, 50, 3, false, "2026-06-06T22:00:00"
                );
                given(userItemService.getMyInventory(USER_EMAIL)).willReturn(List.of(mockResponse));

                // when
                ResultActions actions = mockMvc.perform(get(BASE_URL).principal(() -> USER_EMAIL));

                // then
                actions.andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.message").value(SuccessCode.INVENTORY_READ.getSuccessMessage()))
                        .andExpect(jsonPath("$.data[0].itemName").value("연습용 목검"))
                        .andExpect(jsonPath("$.data[0].quantity").value(3));
            }
        }
    }

    @Nested
    @DisplayName("Describe: 아이템 즉시 획득 API(POST)는")
    class Describe_with_add_item_to_inventory {

        @Nested
        @DisplayName("Context: 올바른 형식의 JSON 바디 데이터가 주어지는 경우")
        class Context_valid_json_body {
            @Test
            @DisplayName("It: 200 OK 성공 피드백과 함께 획득된 아이템 응답 조각을 반환한다.")
            void It_아이템_픽업_웹_성공() throws Exception {
                // given
                UserItemQuantityRequest request = new UserItemQuantityRequest(10L, 5); // 수량 5개 픽업 요청
                UserItemResponse mockResponse = new UserItemResponse(
                        1L, 10L, "sword_01", "연습용 목검", "WEAPON", "COMMON",
                        "목검", 100, 0, 50, 5, false, "2026-06-06T22:00:00"
                );
                given(userItemService.addItemToInventory(eq(USER_EMAIL), any(UserItemQuantityRequest.class))).willReturn(mockResponse);

                // when
                ResultActions actions = mockMvc.perform(post(BASE_URL + "/pickup")
                        .principal(() -> USER_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))); // DTO -> JSON String 변환 주입

                // then
                actions.andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.message").value(SuccessCode.ITEM_GET.getSuccessMessage()))
                        .andExpect(jsonPath("$.data.quantity").value(5));
            }
        }

        @Nested
        @DisplayName("Context: 바디에 수량을 0개 이하의 음수(-5)로 위조하여 보낸 경우")
        class Context_invalid_json_body {
            @Test
            @DisplayName("It: @Valid 검증선에 걸려 서비스까지 가지 못하고 400 Bad Request 실패를 반환한다.")
            void It_음수_수량_입구컷_방어() throws Exception {
                // given
                UserItemQuantityRequest invalidRequest = new UserItemQuantityRequest(10L, -5);

                // when
                ResultActions actions = mockMvc.perform(post(BASE_URL + "/pickup")
                        .principal(() -> USER_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)));

                // then
                actions.andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.message").value("요청 수량은 1개 이상이어야 합니다."));

                then(userItemService).shouldHaveNoInteractions();
            }
        }
    }

    @Nested
    @DisplayName("Describe: 아이템 버리기 API(DELETE)는")
    class Describe_with_discard_item {

        @Nested
        @DisplayName("Context: userItemId 경로 + query수량 에 맞춰 정상 상신한 경우")
        class Context_valid_patch_note_spec {
            @Test
            @DisplayName("It: 주소창 파라미터 데이터를 안정적으로 가로채 가공 후 서비스에 DTO로 묶어 던지고 200 OK를 반환한다.")
            void It_패치노트규격_버리기_웹_성공() throws Exception {
                // when
                //  /me/inventory/{userItemId}/discard?quantity=2
                ResultActions actions = mockMvc.perform(delete(BASE_URL + "/{userItemId}/discard", 1L)
                        .principal(() -> USER_EMAIL)
                        .param("quantity", "2")); // Query Parameter 주입

                // then
                actions.andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.message").value(SuccessCode.ITEM_DISCARD.getSuccessMessage()));

                then(userItemService).should().discardItem(eq(USER_EMAIL), eq(new UserItemQuantityRequest(1L, 2)));
            }
        }

        @Nested
        @DisplayName("Context: 유니티에서 가방 식별자 주소를 음수(-1)로 오염시켜 보낸 경우")
        class Context_invalid_path_variable {
            @Test
            @DisplayName("It: 컨트롤러 단에 명시한 @Positive 규칙이 가동되어 곧바로 400 Bad Request 실패를 유발한다.")
            void It_잘못된_식별자_주소창_입구컷_성공() throws Exception {
                // when
                ResultActions actions = mockMvc.perform(delete(BASE_URL + "/{userItemId}/discard", -1L)
                        .principal(() -> USER_EMAIL)
                        .param("quantity", "2"));

                // then
                actions.andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_PARAMETER.getDescription()));

                then(userItemService).shouldHaveNoInteractions();
            }
        }
    }
}