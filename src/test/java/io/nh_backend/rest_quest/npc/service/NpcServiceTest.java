package io.nh_backend.rest_quest.npc.service;

import io.nh_backend.rest_quest.common.constant.ErrorCode;
import io.nh_backend.rest_quest.common.exception.BusinessException;
import io.nh_backend.rest_quest.item.domain.Item;
import io.nh_backend.rest_quest.item.domain.ItemGrade;
import io.nh_backend.rest_quest.item.domain.ItemType;
import io.nh_backend.rest_quest.npc.domain.Npc;
import io.nh_backend.rest_quest.npc.domain.NpcItem;
import io.nh_backend.rest_quest.npc.dto.NpcResponse;
import io.nh_backend.rest_quest.npc.repository.NpcRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("NpcService 클래스의")
class NpcServiceTest {
    @InjectMocks
    private NpcService npcService;

    @Mock
    private NpcRepository npcRepository;

    private Npc createMockNpc(Long id, String rId, String name, boolean active) {
        Npc npc = Npc.builder()
                .rId(rId)
                .name(name)
                .description("안녕하세요.")
                .locationKey("village_entrance")
                .active(active)
                .build();

        ReflectionTestUtils.setField(npc, "id", id);
        return npc;
    }

    private Item createMockItem() {
        Item item = Item.builder()
                .rId("potion_hp_001")
                .itemName("빨간 포션")
                .itemType(ItemType.CONSUMABLE)
                .itemGrade(ItemGrade.COMMON)
                .description("체력을 채워줍니다.")
                .goldPrice(100)
                .gemPrice(0)
                .sellPrice(10)
                .build();

        ReflectionTestUtils.setField(item, "id", 10L);
        return item;
    }

    @Nested
    @DisplayName("Describe: getAllActiveNpcs 메서드는")
    class Describe_getAllActiveNpcs {

        @Nested
        @DisplayName("Context: 활성화된 NPC가 존재하는 경우")
        class Context_has_active_npcs {

            @Test
            @DisplayName("It: NpcResponse DTO 리스트 형태로 반환하며 내부 상점 정렬 결과까지 검증한다.")
            void It_returns_active_npc_list() {
                // given
                Npc mockNpc = createMockNpc(1L, "npc_merchant_01", "상인 밥", true);
                NpcItem mockNpcItem = NpcItem.builder().npc(mockNpc).item(createMockItem()).quantity(10).sortOrder(1).build();

                ReflectionTestUtils.setField(mockNpcItem, "id", 100L);
                mockNpc.addShopItem(mockNpcItem);

                given(npcRepository.findAllActiveNpcsWithShopItems()).willReturn(List.of(mockNpc));

                // when
                List<NpcResponse> response = npcService.getAllActiveNpcs();

                // then
                assertThat(response).hasSize(1);
                assertThat(response.get(0).npcId()).isEqualTo(1L);
                assertThat(response.get(0).name()).isEqualTo("상인 밥");

                assertThat(response.get(0).shopItems()).hasSize(1);
                assertThat(response.get(0).shopItems().get(0).npcItemId()).isEqualTo(100L);
                assertThat(response.get(0).shopItems().get(0).itemId()).isEqualTo(10L);
                assertThat(response.get(0).shopItems().get(0).itemName()).isEqualTo("빨간 포션");
                assertThat(response.get(0).shopItems().get(0).quantity()).isEqualTo(10);
                assertThat(response.get(0).shopItems().get(0).sortOrder()).isEqualTo(1);

                then(npcRepository).should().findAllActiveNpcsWithShopItems();
            }
        }
    }

    @Nested
    @DisplayName("Describe: getNpcDetails 메서드는")
    class Describe_getNpcDetails {

        @Nested
        @DisplayName("Context: 존재하는 올바른 NPC 식별자 ID가 주어지면")
        class Context_valid_npc_id {

            @Test
            @DisplayName("It: 단건 NPC 정보와 해당 상점 아이템 리스트 DTO를 반환한다.")
            void It_returns_single_npc_details() {
                // given
                Long npcId = 1L;
                Npc mockNpc = createMockNpc(npcId, "npc_merchant_01", "상인 밥", true);
                NpcItem mockNpcItem = NpcItem.builder().npc(mockNpc).item(createMockItem()).quantity(50).sortOrder(1).build();

                ReflectionTestUtils.setField(mockNpcItem, "id", 100L);
                mockNpc.addShopItem(mockNpcItem);

                given(npcRepository.findNpcWithShopItemsById(npcId)).willReturn(Optional.of(mockNpc));

                // when
                NpcResponse response = npcService.getNpcDetails(npcId);

                // then
                assertThat(response).isNotNull();
                assertThat(response.npcId()).isEqualTo(npcId);
                assertThat(response.rId()).isEqualTo("npc_merchant_01");
                assertThat(response.name()).isEqualTo("상인 밥");
                assertThat(response.description()).isEqualTo("안녕하세요.");
                assertThat(response.locationKey()).isEqualTo("village_entrance");
                assertThat(response.active()).isTrue();

                assertThat(response.shopItems()).hasSize(1);
                assertThat(response.shopItems().get(0).npcItemId()).isEqualTo(100L);
                assertThat(response.shopItems().get(0).itemId()).isEqualTo(10L);
                assertThat(response.shopItems().get(0).itemName()).isEqualTo("빨간 포션");
                assertThat(response.shopItems().get(0).quantity()).isEqualTo(50);
                assertThat(response.shopItems().get(0).sortOrder()).isEqualTo(1);

                then(npcRepository).should().findNpcWithShopItemsById(npcId);
            }
        }

        @Nested
        @DisplayName("Context: 존재하긴 하지만 기획상 비활성화(active = false) 상태인 NPC 식별자 ID가 주어지면")
        class Context_inactive_npc_id {

            @Test
            @DisplayName("It: 실제 서비스 분기 로직에 걸려 BusinessException(NPC_NOT_FOUND) 예외를 발생시킨다.")
            void It_throws_npc_not_found_exception_when_inactive() {
                // given
                Long npcId = 1L;
                Npc inactiveNpc = createMockNpc(npcId, "npc_merchant_01", "상인 밥", false); // active = false
                given(npcRepository.findNpcWithShopItemsById(npcId)).willReturn(Optional.of(inactiveNpc));

                // when & then
                assertThatThrownBy(() -> npcService.getNpcDetails(npcId))
                        .isInstanceOf(BusinessException.class)
                        .hasMessageContaining(ErrorCode.NPC_NOT_FOUND.getDescription());

                then(npcRepository).should().findNpcWithShopItemsById(npcId);
            }
        }

        @Nested
        @DisplayName("Context: DB에 맵핑된 데이터 자체가 없는 존재하지 않는 NPC 식별자 ID가 주어지면")
        class Context_not_exists_npc_id {

            @Test
            @DisplayName("It: 비즈니스 규칙에 따라 BusinessException(NPC_NOT_FOUND) 예외를 터트린다.")
            void It_throws_npc_not_found_exception() {
                // given
                Long invalidId = 999L;
                given(npcRepository.findNpcWithShopItemsById(invalidId)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> npcService.getNpcDetails(invalidId))
                        .isInstanceOf(BusinessException.class)
                        .hasMessageContaining(ErrorCode.NPC_NOT_FOUND.getDescription());

                then(npcRepository).should().findNpcWithShopItemsById(invalidId);
            }
        }
    }
}