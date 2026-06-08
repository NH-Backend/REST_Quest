package io.nh_backend.rest_quest.npc.repository;


import io.nh_backend.rest_quest.npc.domain.Npc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("NpcRepository 클래스의")
class NpcRepositoryTest {
    @Autowired
    private NpcRepository npcRepository;

    @Nested
    @DisplayName("findAllActiveNpcsWithShopItems 메서드는")
    class Describe_findAllActiveNpcsWithShopItems {

        @Nested
        @DisplayName("Context: data.sql에 기동 시점에 적재된 활성 NPC 마스터 데이터가 존재하는 경우")
        class Context_has_active_npcs {

            @Test
            @DisplayName("It: 순서와 무관하게 아리와 고든 엔티티를 페치조인하여 빈 가방 없이 온전히 조회한다.")
            void It_returns_all_active_npcs_safely() {
                // when
                List<Npc> result = npcRepository.findAllActiveNpcsWithShopItems();

                // then
                assertThat(result).hasSize(2);

                assertThat(result)
                        .extracting(Npc::getName)
                        .containsExactlyInAnyOrder("잡화상인 아리", "대장장이 고든");

                Npc ari = result.stream().filter(n -> n.getName().equals("잡화상인 아리")).findFirst().orElseThrow();
                Npc gordon = result.stream().filter(n -> n.getName().equals("대장장이 고든")).findFirst().orElseThrow();

                assertThat(ari.getShopItems()).isNotEmpty();   // Fetch Join으로 활 카탈로그를 정상 로드했는가?
                assertThat(gordon.getShopItems()).isNotEmpty(); // Fetch Join으로 검 카탈로그를 정상 로드했는가?
            }
        }
    }

    @Nested
    @DisplayName("findNpcWithShopItemsById 메서드는")
    class Describe_findNpcWithShopItemsById {

        @Nested
        @DisplayName("Context: 존재하는 올바른 대장장이 고든(ID: 2) 식별자가 주어지면")
        class Context_valid_blacksmith_id {

            @Test
            @DisplayName("It: 정렬 순서와 무관하게 고든이 판매하는 4종 무기 마스터 카탈로그와 일치하는 데이터셋을 페치조인해온다.")
            void It_returns_blacksmith_with_shop_items() {
                // given
                Long blacksmithId = 2L;

                // when
                Optional<Npc> resultOpt = npcRepository.findNpcWithShopItemsById(blacksmithId);

                // then
                assertThat(resultOpt).isPresent();
                Npc foundNpc = resultOpt.get();
                assertThat(foundNpc.getName()).isEqualTo("대장장이 고든");

                assertThat(foundNpc.getShopItems())
                        .extracting(si -> si.getItem().getItemName())
                        .containsExactlyInAnyOrder("연습용 검", "초보자 검", "녹슨 철검", "강철 검");
            }
        }

        @Nested
        @DisplayName("Context: DB 데이터 세트에 존재하지 않는 해킹성 식별자 ID(999)가 주어지면")
        class Context_not_exists_npc_id {

            @Test
            @DisplayName("It: 하이버네이트 페치조인 연산이 비어있는 Optional.empty() 객체를 정석대로 리턴한다.")
            void It_returns_empty_optional() {
                // given
                Long invalidId = 999L;

                // when
                Optional<Npc> resultOpt = npcRepository.findNpcWithShopItemsById(invalidId);

                // then
                assertThat(resultOpt).isEmpty();
            }
        }
    }
}