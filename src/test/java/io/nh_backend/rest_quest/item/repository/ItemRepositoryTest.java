package io.nh_backend.rest_quest.item.repository;

import io.nh_backend.rest_quest.item.domain.Item;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("ItemRepository 클래스의")
class ItemRepositoryTest {

    @Autowired
    ItemRepository itemRepository;

    @Nested
    @DisplayName("Describe: findById() 메서드는")
    class Describe_with_find_by_id {

        @Nested
        @DisplayName("Context: data.sql에 선언된 올바른 마스터 데이터 아이템 ID(1L)가 주어지는 경우")
        class Context_with_valid_id {

            Long targetId = 1L;

            @Test
            @DisplayName("It: 영속성 컨텍스트에서 해당 엔티티를 탐색하여 '연습용 검' 정보를 담은 Optional을 반환한다.")
            void It_아이템_기본조회_성공() {
                // when - data.sql에 탑재된 기초 데이터를 id를 기반으로 직접 질의합니다.
                Optional<Item> result = itemRepository.findById(targetId);

                // then
                Assertions.assertThat(result).isPresent();
                Assertions.assertThat(result.get().getItemName()).isEqualTo("연습용 검");
                Assertions.assertThat(result.get().getRId()).isEqualTo("sword_001");
                Assertions.assertThat(result.get().getGoldPrice()).isEqualTo(100);
            }
        }

        @Nested
        @DisplayName("Context: 카탈로그 범위를 벗어난 허구의 아이템 ID(999L)가 주어지는 경우")
        class Context_with_invalid_id {

            Long wrongId = 999L;

            @Test
            @DisplayName("It: SQL 질의 결과에 상응하는 데이터 로우가 없으므로 비어있는 Optional.empty()를 리턴한다.")
            void It_빈_옵셔널_반환() {
                // when
                Optional<Item> result = itemRepository.findById(wrongId);

                // then
                Assertions.assertThat(result).isEmpty();
            }
        }
    }

}