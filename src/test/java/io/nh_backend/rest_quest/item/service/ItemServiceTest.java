package io.nh_backend.rest_quest.item.service;

import io.nh_backend.rest_quest.common.constant.ErrorCode;
import io.nh_backend.rest_quest.common.exception.BusinessException;
import io.nh_backend.rest_quest.item.domain.Item;
import io.nh_backend.rest_quest.item.domain.ItemGrade;
import io.nh_backend.rest_quest.item.domain.ItemType;
import io.nh_backend.rest_quest.item.dto.ItemResponse;
import io.nh_backend.rest_quest.item.repository.ItemRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItemService 클래스의")
class ItemServiceTest {

    @InjectMocks
    private ItemService itemService;

    @Mock
    private ItemRepository itemRepository;

    private Item sampleItem;

    @BeforeEach
    void setUp() {
        sampleItem = new Item(
                "연습용 검", "sword_001", "연습용 검이다.",
                100, 0, 50,
                0, 0, 0,
                ItemType.WEAPON, ItemGrade.COMMON
        );
    }

    @Nested
    @DisplayName("Describe: getAllItems() 메서드는")
    class Describe_with_get_all_items {

        @Nested
        @DisplayName("Context: 아이템 전체 조회가 호출되는 경우")
        class Context_normal_flow {
            @Test
            @DisplayName("It: 리포지토리의 전체 리스트를 받아 누락 없이 전체 DTO 배열로 가공해 리턴한다.")
            void It_전체_조회_성공() {
                // given
                Item sampleItem2 = new Item(
                        "초보자 검", "sword_002", "초보자용 검이다.",
                        200, 0, 100, 0, 0, 0, ItemType.WEAPON, ItemGrade.COMMON
                );

                given(itemRepository.findAll()).willReturn(List.of(sampleItem, sampleItem2));

                // when
                List<ItemResponse> result = itemService.getAllItems();

                // then
                Assertions.assertThat(result).hasSize(2);
                Assertions.assertThat(result.get(0).itemName()).isEqualTo("연습용 검");
                Assertions.assertThat(result.get(1).itemName()).isEqualTo("초보자 검");
                then(itemRepository).should().findAll();
            }
        }
    }

    @Nested
    @DisplayName("Describe: getItemById() 메서드는")
    class Describe_with_get_item_by_id {

        @Nested
        @DisplayName("Context: 조회하려는 식별자 엔티티 ID가 데이터베이스에 실재하는 경우")
        class Context_with_exist_id {

            @Test
            @DisplayName("It: 엔티티의 실제 정보값들이 유니티 전용 리스폰스 템플릿 필드로 안전 변환되어 반환된다.")
            void It_단건_조회_성공() {
                // given
                Long targetId = 1L;
                given(itemRepository.findById(targetId)).willReturn(Optional.of(sampleItem));

                // when
                ItemResponse result = itemService.getItemById(targetId);

                // then
                Assertions.assertThat(result).isNotNull();
                Assertions.assertThat(result.itemName()).isEqualTo("연습용 검");

                Assertions.assertThat(result.goldPrice()).isEqualTo(100);
                Assertions.assertThat(result.gemPrice()).isEqualTo(0);

                then(itemRepository).should().findById(targetId);
            }
        }

        @Nested
        @DisplayName("Context: 조회하려는 타겟 식별자 ID가 존재하지 않아 비어있는 경우")
        class Context_with_empty_id {

            @Test
            @DisplayName("It: 기획안에 정의된 예외 규칙에 따라 BusinessException을 연쇄적으로 폭발시킨다.")
            void It_단건_조회_실패_예외발생() {
                // given
                Long invalidId = 20L;
                given(itemRepository.findById(invalidId)).willReturn(Optional.empty());

                // when & then
                Assertions.assertThatThrownBy(() -> itemService.getItemById(invalidId))
                        .isInstanceOf(BusinessException.class)
                        .hasMessageContaining(ErrorCode.ITEM_NOT_FOUND.getDescription());

                then(itemRepository).should().findById(invalidId);
            }
        }
    }
}



