package io.nh_backend.rest_quest.npc.repository;

import io.nh_backend.rest_quest.item.domain.Item;
import io.nh_backend.rest_quest.item.domain.ItemGrade;
import io.nh_backend.rest_quest.item.domain.ItemType;
import io.nh_backend.rest_quest.npc.domain.Npc;
import io.nh_backend.rest_quest.npc.domain.NpcItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("NpcItemRepository 클래스의")
class NpcItemRepositoryTest {
    @Autowired
    private NpcItemRepository npcItemRepository;

    @PersistenceContext
    private EntityManager em; // 지연 로딩 및 영속성 컨텍스트 격리 검증을 위해 주입

    private Npc savedNpc;
    private Item savedItem;
    private NpcItem savedNpcItem;

    @BeforeEach
    void setUp() {
        Npc npc = Npc.builder()
                .rId("npc_merchant_001")
                .name("포션상인 엘레나")
                .description("포션을 판매하는 상인입니다.")
                .locationKey("village_entrance")
                .active(true)
                .build();
        em.persist(npc);
        this.savedNpc = npc;

        Item item = Item.builder()
                .rId("potion_hp_001")
                .itemName("맑은 생명수 포션")
                .goldPrice(150)
                .gemPrice(0)
                .sellPrice(15)
                .description("HP를 즉시 회복")
                .expCoupon(0)
                .gemCoupon(0)
                .goldCoupon(0)
                 .itemType(ItemType.CONSUMABLE)
                 .itemGrade(ItemGrade.COMMON)
                .build();
        em.persist(item);
        this.savedItem = item;

        NpcItem npcItem = NpcItem.builder()
                .npc(npc)
                .item(item)
                .quantity(10)
                .sortOrder(1)
                .build();
        em.persist(npcItem);
        this.savedNpcItem = npcItem;

        em.flush();
        em.clear();
    }

    @Nested
    @DisplayName("Describe: findPurchaseTarget 메서드는")
    class Describe_findPurchaseTarget {

        @Nested
        @DisplayName("Context: 올바르게 매핑된 npcId와 npcItemId를 파라미터로 상신하는 경우")
        class Context_valid_npc_and_item_mapping {

            @Test
            @DisplayName("It: Optional 내부 알맹이를 무사히 반환하고, fetch join된 연관 엔티티(Npc, Item) 데이터 그래프를 1차 캐시 없이 즉시 탐색한다.")
            void It_returns_valid_npc_item_with_fetch_join() {
                // when
                Optional<NpcItem> result = npcItemRepository.findPurchaseTarget(savedNpc.getId(), savedNpcItem.getId());

                // then
                assertThat(result).isPresent();
                NpcItem actualNpcItem = result.get();

                // 매대 정보 검증
                assertThat(actualNpcItem.getId()).isEqualTo(savedNpcItem.getId());
                assertThat(actualNpcItem.getQuantity()).isEqualTo(10);

                //fetch join 되었으므로 em.clear() 이후에도 LazyInitializationException 없이 조회되어야 함
                assertThat(actualNpcItem.getNpc().getName()).isEqualTo("포션상인 엘레나");
                assertThat(actualNpcItem.getItem().getItemName()).isEqualTo("맑은 생명수 포션");
            }
        }

        @Nested
        @DisplayName("Context: 유저가 패킷을 변조하여 존재하는 npcItemId 이지만 다른 NPC ID(999L)를 조합해 찌른 경우")
        class Context_hacked_cross_mapping_request {

            @Test
            @DisplayName("It: 데이터 교차 필터링 방어선(where절)에 걸려 Optional.empty() 빈값을 안전하게 리턴한다.")
            void It_returns_optional_empty_against_hacked_packet() {
                // given
                Long manipulatedNpcId = 999L; // 존재하지 않거나 엉뚱한 상인 식별자 강제 위조

                // when
                Optional<NpcItem> result = npcItemRepository.findPurchaseTarget(manipulatedNpcId, savedNpcItem.getId());

                // then
                assertThat(result).isEmpty();
            }
        }
    }
}