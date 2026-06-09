package io.nh_backend.rest_quest.item.repository;

import io.nh_backend.rest_quest.item.domain.Item;
import io.nh_backend.rest_quest.item.domain.ItemGrade;
import io.nh_backend.rest_quest.item.domain.ItemType;
import io.nh_backend.rest_quest.item.domain.UserItem;
import io.nh_backend.rest_quest.user.domain.Role;
import io.nh_backend.rest_quest.user.domain.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("UserItemRepository 인터페이스의")
class UserItemRepositoryTest {

    @Autowired
    private UserItemRepository userItemRepository;

    @Autowired
    private TestEntityManager entityManager; // 테스트 전용 영속성 관리자

    private User savedUser;
    private Item savedItem1;
    private Item savedItem2;
    private UserItem savedUserItem1;

    @BeforeEach
    void setUp() {
        // 1. 기초 유저 생성 및 영속화
        User user = User.builder()
                .email("repo_player@example.com")
                .password("pass123")
                .nickname("DB용사")
                .role(Role.USER)
                .build();
        savedUser = entityManager.persist(user);

        // 2. 기초 아이템 마스터 데이터 2개 생성 및 영속화 (다건 조회 검증용)
        Item item1 = new Item("연습용 목검", "sword_01", "목검", 100, 0, 50, 0, 0, 0, ItemType.WEAPON, ItemGrade.COMMON);
        Item item2 = new Item("초보자용 방패", "shield_01", "방패", 150, 0, 70, 0, 0, 0, ItemType.ARMOR, ItemGrade.COMMON);
        savedItem1 = entityManager.persist(item1);
        savedItem2 = entityManager.persist(item2);

        // 3. 유저 가방에 목검 1개 넣어두기
        UserItem userItem1 = UserItem.builder()
                .user(savedUser)
                .item(savedItem1)
                .quantity(3)
                .equipped(false)
                .build();
        savedUserItem1 = entityManager.persist(userItem1);

        entityManager.flush();
        entityManager.clear(); // 영속성 컨텍스트를 비워 실제 DB 쿼리가 날아가도록 유도
    }

    @Nested
    @DisplayName("Describe: findAllByUserAndDeletedAtIsNull 메서드는")
    class Describe_with_find_all_by_user {

        @Nested
        @DisplayName("Context: 삭제(소프트 딜리트)되지 않은 아이템들이 가방에 여러 개 존재할 때")
        class Context_with_active_items {
            @Test
            @DisplayName("It: 해당 유저의 유효한 가방 속 아이템 레코드만 전체 리스트로 정상 조회한다.")
            void It_정상_아이템_목록_조회_성공() {
                // given - 방패를 가방에 추가로 영속화 (총 2개 상태 조성)
                UserItem userItem2 = UserItem.builder()
                        .user(savedUser)
                        .item(savedItem2)
                        .quantity(1)
                        .equipped(false)
                        .build();
                userItemRepository.save(userItem2);

                // when
                List<UserItem> result = userItemRepository.findAllByUserAndDeletedAtIsNull(savedUser);

                // then
                Assertions.assertThat(result).hasSize(2);
                Assertions.assertThat(result).extracting(ui -> ui.getItem().getItemName())
                        .containsExactlyInAnyOrder("연습용 목검", "초보자용 방패");
            }
        }

        @Nested
        @DisplayName("Context: 가방 속 아이템이 Soft Delete(delete()) 처리된 경우")
        class Context_with_soft_deleted_item {
            @Test
            @DisplayName("It: 조건절(And DeletedAt Is Null)에 의해 조회 대상 목록에서 완벽히 제외된다.")
            void It_소프트딜리트_아이템_조회_제외_성공() {
                // given - 목검 전량 버리기 (Soft Delete 수행)
                UserItem userItem = userItemRepository.findById(savedUserItem1.getId()).orElseThrow();
                userItem.delete(); // deletedAt = LocalDateTime.now()
                userItemRepository.saveAndFlush(userItem);
                entityManager.clear();

                // when
                List<UserItem> result = userItemRepository.findAllByUserAndDeletedAtIsNull(savedUser);

                // then
                Assertions.assertThat(result).isEmpty(); // 삭제되었으므로 가방이 비어있어야 함!
            }
        }
    }

    @Nested
    @DisplayName("Describe: findByUserAndItemAndDeletedAtIsNull 메서드는")
    class Describe_with_find_by_user_and_item {

        @Nested
        @DisplayName("Context: 가방에 이미 실재하는 유저와 아이템 정보가 매핑되어 주어질 때")
        class Context_exist_user_and_item {
            @Test
            @DisplayName("It: 중복 지급 방지 판정을 위해 해당 인벤토리 슬롯 엔티티를 정확히 단건 반환한다.")
            void It_동일_슬롯_조회_성공() {
                // when
                Optional<UserItem> result = userItemRepository.findByUserAndItemAndDeletedAtIsNull(savedUser, savedItem1);

                // then
                Assertions.assertThat(result).isPresent();
                Assertions.assertThat(result.get().getQuantity()).isEqualTo(3);
            }
        }
    }

}