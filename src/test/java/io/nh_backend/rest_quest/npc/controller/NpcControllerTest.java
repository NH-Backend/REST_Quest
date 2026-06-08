package io.nh_backend.rest_quest.npc.controller;

import io.nh_backend.rest_quest.common.constant.ErrorCode;
import io.nh_backend.rest_quest.common.constant.SuccessCode;
import io.nh_backend.rest_quest.common.exception.BusinessException;
import io.nh_backend.rest_quest.npc.dto.NpcResponse;
import io.nh_backend.rest_quest.npc.dto.NpcShopItemResponse;
import io.nh_backend.rest_quest.npc.service.NpcService;
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

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NpcController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("NpcController 클래스의")
class NpcControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private NpcService npcService;

    private final String BASE_URL = "/api/v1/npcs";

    private NpcShopItemResponse createMockShopItem() {
        return new NpcShopItemResponse(
                1L, 10L, "potion_hp_001", "빨간 포션", "CONSUMABLE", "COMMON",
                "체력을 회복합니다.", 0, 100, 10, 50, 1
        );
    }

    @Nested
    @DisplayName("Describe: NPC 목록 조회 API(GET)는")
    class Describe_getAllActiveNpcs {

        @Nested
        @DisplayName("Context: 서버에 활성화된 NPC 목록이 존재하는 상황인 경우")
        class Context_has_npcs {

            @Test
            @DisplayName("It: 200 OK 응답과 함께 success=true 및 NPC 목록 배열 데이터 크기를 검증한다.")
            void It_returns_200_and_npc_list() throws Exception {
                // given
                NpcResponse mockResponse = new NpcResponse(
                        1L, "npc_merchant_001", "상인 밥", "마을 입구 상인입니다.",
                        "village_entrance", true, List.of(createMockShopItem())
                );
                given(npcService.getAllActiveNpcs()).willReturn(List.of(mockResponse));

                // when
                ResultActions actions = mockMvc.perform(get(BASE_URL).accept(MediaType.APPLICATION_JSON));

                // then
                actions.andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.message").value(SuccessCode.NPC_LIST_READ.getSuccessMessage()))
                        .andExpect(jsonPath("$.data").isArray())
                        .andExpect(jsonPath("$.data.size()").value(1))
                        .andExpect(jsonPath("$.data[0].npcId").value(1L))
                        .andExpect(jsonPath("$.data[0].rId").value("npc_merchant_001"))
                        .andExpect(jsonPath("$.data[0].name").value("상인 밥"));
            }
        }
    }

    @Nested
    @DisplayName("Describe: NPC 단건 조회 API(GET)는")
    class Describe_getNpcDetails {

        @Nested
        @DisplayName("Context: 존재하는 올바른 NPC 식별자 ID를 패스로 상신하면")
        class Context_valid_npc_id {

            @Test
            @DisplayName("It: 200 OK 성공 마크와 함께 매핑된 npcId 식별자 알맹이를 정밀 검증한다.")
            void It_returns_single_npc_details() throws Exception {
                // given
                Long npcId = 1L;
                NpcResponse mockResponse = new NpcResponse(
                        npcId, "npc_merchant_001", "상인 밥", "마을 입구 상인입니다.",
                        "village_entrance", true, List.of(createMockShopItem())
                );
                given(npcService.getNpcDetails(npcId)).willReturn(mockResponse);

                // when
                ResultActions actions = mockMvc.perform(get(BASE_URL + "/{id}", npcId).accept(MediaType.APPLICATION_JSON));

                // then
                actions.andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.message").value(SuccessCode.NPC_DETAIL_READ.getSuccessMessage()))
                        .andExpect(jsonPath("$.data.npcId").value(npcId))
                        .andExpect(jsonPath("$.data.shopItems[0].itemName").value("빨간 포션"))
                        .andExpect(jsonPath("$.data.shopItems[0].sortOrder").value(1));
            }
        }

        @Nested
        @DisplayName("Context: 양수의 형식을 맞췄으나 DB에 매핑된 데이터가 없는 ID(999)가 들어온 경우")
        class Context_not_found_npc_id {

            @Test
            @DisplayName("It: 404 Not Found 에러와 함께 글로벌 핸들러가 가동되어 NPC_NOT_FOUND 약속 메시지를 반환한다.")
            void It_존재하지_않는_npc_조회_실패_검증() throws Exception {
                // given
                Long invalidId = 999L;

                given(npcService.getNpcDetails(invalidId))
                        .willThrow(new BusinessException(ErrorCode.NPC_NOT_FOUND));

                // when
                ResultActions actions = mockMvc.perform(get(BASE_URL + "/{id}", invalidId).accept(MediaType.APPLICATION_JSON));

                // then
                actions.andExpect(status().isNotFound()) // 404 상태코드 검증
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.message").value(ErrorCode.NPC_NOT_FOUND.getDescription()));

                then(npcService).should().getNpcDetails(invalidId);
            }
        }

        @Nested
        @DisplayName("Context: 유니티 주소창에서 NPC 식별자를 음수(-1)로 악의적으로 변조해 쏜 경우")
        class Context_invalid_path_variable {

            @Test
            @DisplayName("It: @Positive 입구 컷에 걸려 400 에러 및 INVALID_PARAMETER 메시지를 반환하고, 서비스 레이어 진입을 원천 차단한다.")
            void It_음수_식별자_주소창_입구컷_방어_성공() throws Exception {
                // when
                ResultActions actions = mockMvc.perform(get(BASE_URL + "/{id}", -1L).accept(MediaType.APPLICATION_JSON));

                // then
                actions.andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_PARAMETER.getDescription()));

                then(npcService).shouldHaveNoInteractions();
            }
        }
    }

}