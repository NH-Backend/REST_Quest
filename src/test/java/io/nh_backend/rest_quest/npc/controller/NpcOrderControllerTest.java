package io.nh_backend.rest_quest.npc.controller;

import io.nh_backend.rest_quest.common.constant.ErrorCode;
import io.nh_backend.rest_quest.common.constant.SuccessCode;
import io.nh_backend.rest_quest.common.exception.BusinessException;
import io.nh_backend.rest_quest.item.dto.UserItemResponse;
import io.nh_backend.rest_quest.npc.dto.NpcPurchaseRequest;
import io.nh_backend.rest_quest.npc.dto.NpcPurchaseResponse;
import io.nh_backend.rest_quest.npc.service.NpcOrderService;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NpcOrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("NpcOrderController 클래스의")
class NpcOrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private NpcOrderService npcOrderService;

    private static final String BASE_URL = "/api/v1/users/me/npcs";
    private static final String LOGIN_EMAIL = "test@naver.com"; //
    private final Long npcId = 1L;
    private final Long npcItemId = 10L;

    @Nested
    @DisplayName("Describe: NPC 상점 아이템 구매 API(POST)는")
    class Describe_purchaseItem {

        @Nested
        @DisplayName("Context: 재화와 상점 매대 재고가 모두 충족하고 올바른 요청인 경우")
        class Context_purchase_success {

            @Test
            @DisplayName("It: 200 OK 성공 응답과 함께 지갑 잔액 및 획득한 상세 아이템 필드를 정확히 반환한다.")
            void It_returns_200_and_purchase_response() throws Exception {
                // given
                int purchaseQuantity = 3;
                NpcPurchaseRequest request = new NpcPurchaseRequest(purchaseQuantity);

                NpcPurchaseResponse mockResponse = new NpcPurchaseResponse(
                        new NpcPurchaseResponse.WalletDto(2700L, 100L),
                        new UserItemResponse(1L, 50L, "potion_hp_001", "HP 포션", "CONSUMABLE", "COMMON", "HP 회복", 100, 0, 10, purchaseQuantity, false, "2026-06-08T13:00:00")
                );

                given(npcOrderService.purchaseItem(eq(LOGIN_EMAIL), eq(npcId), eq(npcItemId), eq(purchaseQuantity)))
                        .willReturn(mockResponse);

                // when
                ResultActions actions = mockMvc.perform(
                        post(BASE_URL + "/{npcId}/items/{npcItemId}/purchase", npcId, npcItemId)
                                .principal(()->LOGIN_EMAIL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                );

                // then
                actions.andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.message").value(SuccessCode.NPC_ITEM_PURCHASE_SUCCESS.getSuccessMessage()))
                        .andExpect(jsonPath("$.data.wallet.gold").value(2700L))
                        .andExpect(jsonPath("$.data.wallet.gem").value(100L))
                        .andExpect(jsonPath("$.data.acquiredItem.quantity").value(purchaseQuantity))
                        .andExpect(jsonPath("$.data.acquiredItem.itemId").value(50L))
                        .andExpect(jsonPath("$.data.acquiredItem.rId").value("potion_hp_001"))
                        .andExpect(jsonPath("$.data.acquiredItem.itemName").value("HP 포션"))
                        .andDo(print());

                then(npcOrderService).should(times(1))
                        .purchaseItem(eq(LOGIN_EMAIL), eq(npcId), eq(npcItemId), eq(purchaseQuantity));
            }
        }

        @Nested
        @DisplayName("Context: 소지한 골드가 부족하여 비즈니스 레이어에서 실패하는 경우")
        class Context_insufficient_gold {

            @Test
            @DisplayName("It: 400 BadRequest 에러와 함께 소지 골드 부족 약속 메시지를 응답한다.")
            void It_returns_400_gold_shortage() throws Exception {
                // given
                NpcPurchaseRequest request = new NpcPurchaseRequest(5);

                given(npcOrderService.purchaseItem(eq(LOGIN_EMAIL), eq(npcId), eq(npcItemId), eq(5)))
                        .willThrow(new BusinessException(ErrorCode.INSUFFICIENT_GOLD));

                // when
                ResultActions actions = mockMvc.perform(
                        post(BASE_URL + "/{npcId}/items/{npcItemId}/purchase", npcId, npcItemId)
                                .principal(()->LOGIN_EMAIL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                );

                // then
                actions.andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.message").value(ErrorCode.INSUFFICIENT_GOLD.getDescription()))
                        .andDo(print());

                then(npcOrderService).should(times(1))
                        .purchaseItem(eq(LOGIN_EMAIL), eq(npcId), eq(npcItemId), eq(5));
            }
            }
        }

        @Nested
        @DisplayName("Context: 클라이언트가 바디의 수량 필드를 유실하거나 누락(null)하여 보낸 상황의 경우")
        class Context_quantity_is_null {

            @Test
            @DisplayName("It: @Valid 가드 절에 걸려 400 에러를 뿜으며, 비즈니스 레이어 접근을 차단한다.")
            void It_intercepts_null_quantity_at_controller() throws Exception {
                // when: 빈 JSON 객체("{}")
                ResultActions actions = mockMvc.perform(
                        post(BASE_URL + "/{npcId}/items/{npcItemId}/purchase", npcId, npcItemId)
                                .principal(() -> LOGIN_EMAIL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                );

                // then
                actions.andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.success").value(false))
                        .andDo(print());

                then(npcOrderService).shouldHaveNoInteractions();
            }
        }

    @Nested
    @DisplayName("Context: 유니티가 구매 수량을 경계값인 0개로 설정하여 패킷을 보낸 경우")
    class Context_quantity_is_zero {

        @Test
        @DisplayName("It: @Positive 검증선에 포착되어 400 에러를 반환하고, 프로세스를 즉시 차단한다.")
        void It_intercepts_zero_quantity_at_controller() throws Exception {
            // given
            NpcPurchaseRequest request = new NpcPurchaseRequest(0);

            // when
            ResultActions actions = mockMvc.perform(
                    post(BASE_URL + "/{npcId}/items/{npcItemId}/purchase", npcId, npcItemId)
                            .principal(() -> LOGIN_EMAIL) //
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
            );

            // then
            actions.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andDo(print());

            then(npcOrderService).shouldHaveNoInteractions();
        }
    }

        @Nested
        @DisplayName("Context: 유니티가 구매 수량을 음수(-5)로 조작하여 패킷을 보낸 경우")
        class Context_invalid_quantity_minus {

            @Test
            @DisplayName("It: 수량 유효성 가드 절에 포착되어 400 에러를 반환하고, 프로세스를 즉시 종료한다.")
            void It_intercepts_invalid_quantity_at_controller() throws Exception {
                // given
                NpcPurchaseRequest request = new NpcPurchaseRequest(-5);

                // when
                ResultActions actions = mockMvc.perform(
                        post(BASE_URL + "/{npcId}/items/{npcItemId}/purchase", npcId, npcItemId)
                                .principal(() -> LOGIN_EMAIL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                );

                // then
                actions.andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.success").value(false))
                        .andDo(print());

                then(npcOrderService).shouldHaveNoInteractions();
            }
        }

        @Nested
        @DisplayName("Context: 주소창 패스 변수인 npcId 식별자를 음수(-1)로 악의적으로 변조하여 쏜 경우")
        class Context_invalid_path_variable_minus {

            @Test
            @DisplayName("It: @Positive 규칙 위반으로 400 BadRequest 에러를 유발하고 하위 조회를 완벽히 엄금한다.")
            void It_intercepts_invalid_path_variable_at_controller() throws Exception {
                // given
                Long hackedNpcId = -1L;
                NpcPurchaseRequest request = new NpcPurchaseRequest(1);

                // when
                ResultActions actions = mockMvc.perform(
                        post(BASE_URL + "/{npcId}/items/{npcItemId}/purchase", hackedNpcId, npcItemId)
                                .principal(() -> LOGIN_EMAIL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                );

                // then
                actions.andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.success").value(false))
                        .andDo(print());

                then(npcOrderService).shouldHaveNoInteractions();
        }
    }
}