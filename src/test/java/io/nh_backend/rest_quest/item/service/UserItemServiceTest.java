package io.nh_backend.rest_quest.item.service;

import io.nh_backend.rest_quest.common.constant.ErrorCode;
import io.nh_backend.rest_quest.common.exception.BusinessException;
import io.nh_backend.rest_quest.item.domain.Item;
import io.nh_backend.rest_quest.item.domain.ItemGrade;
import io.nh_backend.rest_quest.item.domain.ItemType;
import io.nh_backend.rest_quest.item.domain.UserItem;
import io.nh_backend.rest_quest.item.dto.UserItemQuantityRequest;
import io.nh_backend.rest_quest.item.dto.UserItemResponse;
import io.nh_backend.rest_quest.item.repository.ItemRepository;
import io.nh_backend.rest_quest.item.repository.UserItemRepository;
import io.nh_backend.rest_quest.user.domain.Role;
import io.nh_backend.rest_quest.user.domain.User;
import io.nh_backend.rest_quest.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserItemService 클래스의")
class UserItemServiceTest {

    @InjectMocks
    private UserItemService userItemService;

    @Mock
    private UserItemRepository userItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    private User sampleUser;
    private Item sampleItem;
    private UserItem sampleUserItem;
    private final String EMAIL = "player@example.com";

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .email(EMAIL)
                .password("password123")
                .nickname("용사")
                .role(Role.USER)
                .build();

        sampleItem = new Item(
                "연습용 목검", "sword_001", "초보자용 목검이다.",
                100, 0, 50,
                0, 0, 0,
                ItemType.WEAPON, ItemGrade.COMMON
        );

        sampleUserItem = UserItem.builder()
                .user(sampleUser)
                .item(sampleItem)
                .quantity(3)
                .equipped(false)
                .build();
    }

    @Nested
    @DisplayName("Describe: getMyInventory() 메서드는")
    class Describe_with_get_my_inventory {

        @Nested
        @DisplayName("Context: 유효한 사용자의 이메일 세션 정보가 주어지는 경우")
        class Context_normal_flow {
            @Test
            @DisplayName("It: 가방 속 정상 보유 중인 여러 개의 아이템 목록을 누락 없이 전체 조회하여 유니티 규격 DTO 리스트로 파싱 리턴한다.")
            void It_인벤토리_다건_목록_조회_성공() {
                // given
                Item sampleItem2 = new Item(
                        "초보자용 방패", "shield_001", "나무로 된 방패다.",
                        150, 0, 70,
                        0, 0, 0,
                        ItemType.ARMOR, ItemGrade.COMMON
                );

                UserItem sampleUserItem2 = UserItem.builder()
                        .user(sampleUser)
                        .item(sampleItem2)
                        .quantity(1)
                        .equipped(false)
                        .build();

                List<UserItem> mockInventory = List.of(sampleUserItem, sampleUserItem2);

                given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(sampleUser));
                given(userItemRepository.findAllByUserAndDeletedAtIsNull(sampleUser)).willReturn(mockInventory);

                // when
                List<UserItemResponse> result = userItemService.getMyInventory(EMAIL);

                // then
                Assertions.assertThat(result).hasSize(2);


                Assertions.assertThat(result.get(0).itemName()).isEqualTo("연습용 목검");
                Assertions.assertThat(result.get(0).quantity()).isEqualTo(3);


                Assertions.assertThat(result.get(1).itemName()).isEqualTo("초보자용 방패");
                Assertions.assertThat(result.get(1).quantity()).isEqualTo(1);
                Assertions.assertThat(result.get(1).itemType()).isEqualTo("ARMOR");

                then(userRepository).should().findByEmail(EMAIL);
                then(userItemRepository).should().findAllByUserAndDeletedAtIsNull(sampleUser);
            }
        }

        @Nested
        @DisplayName("Context: 시스템에 존재하지 않는 이메일 주소로 조회를 요청하는 경우")
        class Context_invalid_email {
            @Test
            @DisplayName("It: UNAUTHORIZED_USER 비즈니스 예외를 던지며 프로세스를 차단한다.")
            void It_조회_인증_실패_예외_검증() {
                given(userRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

                Assertions.assertThatThrownBy(() -> userItemService.getMyInventory(EMAIL))
                        .isInstanceOf(BusinessException.class)
                        .hasMessageContaining(ErrorCode.UNAUTHORIZED_USER.getDescription());

                then(userItemRepository).should(never()).findAllByUserAndDeletedAtIsNull(any(User.class));
            }
        }
    }

    @Nested
    @DisplayName("Describe: addItemToInventory() 메서드는")
    class Describe_with_add_item_to_inventory {

        @Nested
        @DisplayName("Context: 인증이 완료되고 가방에 이미 동일한 아이템 슬롯이 실재하는 경우")
        class Context_with_existing_item {
            @Test
            @DisplayName("It: 중복 인서트를 차단하고 기존 슬롯의 수량을 누적 합산한다.")
            void It_아이템_수량_누적_성공() {
                UserItemQuantityRequest request = new UserItemQuantityRequest(1L, 5);
                given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(sampleUser));
                given(itemRepository.findById(request.itemId())).willReturn(Optional.of(sampleItem));
                given(userItemRepository.findByUserAndItemAndDeletedAtIsNull(sampleUser, sampleItem)).willReturn(Optional.of(sampleUserItem));

                UserItemResponse result = userItemService.addItemToInventory(EMAIL, request);

                Assertions.assertThat(result.quantity()).isEqualTo(8);
                then(userItemRepository).should(never()).save(any(UserItem.class));
            }
        }

        @Nested
        @DisplayName("Context: 가방에 처음 들어오는 신규 아이템 보상인 경우")
        class Context_with_new_item {
            @Test
            @DisplayName("It: 리포지토리의 save 트리거를 당기며, 서비스 내부에서 올바른 수량으로 빌드된 엔티티가 전달되는지 검증한다.")
            void It_신규_아이템_인서트_성공() {
                // given
                UserItemQuantityRequest request = new UserItemQuantityRequest(2L, 1);
                given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(sampleUser));
                given(itemRepository.findById(request.itemId())).willReturn(Optional.of(sampleItem));
                given(userItemRepository.findByUserAndItemAndDeletedAtIsNull(sampleUser, sampleItem)).willReturn(Optional.empty());


                given(userItemRepository.save(any(UserItem.class))).willReturn(sampleUserItem);

                // UserItem 엔티티를 가로챌 캡터(ArgumentCaptor) 선언
               ArgumentCaptor<UserItem> userItemCaptor =
                        ArgumentCaptor.forClass(UserItem.class);

                // when
                userItemService.addItemToInventory(EMAIL, request);

                // then
                // save가 호출될 때 전달된 실제 엔티티 조각을 가로챕니다(capture).
                then(userItemRepository).should().save(userItemCaptor.capture());

                // 가로챈 실제 엔티티의 상태 값을 검증합니다.
                UserItem savedUserItem = userItemCaptor.getValue();

                // 서비스 로직이 request에 적힌 '1'이라는 수량을 엔티티에 똑바로 세팅했는가?
                Assertions.assertThat(savedUserItem.getQuantity()).isEqualTo(1);
                Assertions.assertThat(savedUserItem.getUser()).isEqualTo(sampleUser);
                Assertions.assertThat(savedUserItem.getItem()).isEqualTo(sampleItem);
            }
        }

        @Nested
        @DisplayName("Context: 존재하지 않는 유저 이메일 정보로 지급을 요청하는 경우")
        class Context_add_invalid_user {
            @Test
            @DisplayName("It: UNAUTHORIZED_USER 비즈니스 예외를 던지며 프로세스를 완전히 차단한다.")
            void It_지급_인증_실패_예외_검증() {
                UserItemQuantityRequest request = new UserItemQuantityRequest(1L, 1);
                given(userRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

                Assertions.assertThatThrownBy(() -> userItemService.addItemToInventory(EMAIL, request))
                        .isInstanceOf(BusinessException.class)
                        .hasMessageContaining(ErrorCode.UNAUTHORIZED_USER.getDescription());

                then(itemRepository).should(never()).findById(any(Long.class));
            }
        }

        @Nested
        @DisplayName("Context: 마스터 데이터베이스에 존재하지 않는 itemId를 지급하려는 경우")
        class Context_invalid_item_id {
            @Test
            @DisplayName("It: ITEM_NOT_FOUND 비즈니스 예외를 던지며 아이템 지급을 거부한다.")
            void It_아이템_미조회_실패_검증() {
                UserItemQuantityRequest request = new UserItemQuantityRequest(999L, 1);
                given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(sampleUser));
                given(itemRepository.findById(request.itemId())).willReturn(Optional.empty());

                Assertions.assertThatThrownBy(() -> userItemService.addItemToInventory(EMAIL, request))
                        .isInstanceOf(BusinessException.class)
                        .hasMessageContaining(ErrorCode.ITEM_NOT_FOUND.getDescription());

                then(userItemRepository).should(never()).findByUserAndItemAndDeletedAtIsNull(any(User.class), any(Item.class));
            }
        }
    }

    @Nested
    @DisplayName("Describe: discardItem() 메서드는")
    class Describe_with_discard_item {

        @Nested
        @DisplayName("Context: 모든 검증을 통과하고 가방 속 보유량과 똑같은 수량만큼 버리겠다고 요청한 경우")
        class Context_with_equal_quantity {
            @Test
            @DisplayName("It: 엔티티의 delete() 트리거를 당겨 Soft Delete 타임스탬프를 마킹한다.")
            void It_전량_폐기_소프트딜리트_성공() {
                UserItemQuantityRequest request = new UserItemQuantityRequest(1L, 3);
                given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(sampleUser));
                given(userItemRepository.findByIdAndUserAndDeletedAtIsNull(request.itemId(), sampleUser)).willReturn(Optional.of(sampleUserItem));

                userItemService.discardItem(EMAIL, request);

                Assertions.assertThat(sampleUserItem.getDeletedAt()).isNotNull();
            }
        }

        @Nested
        @DisplayName("Context: 모든 검증을 통과하고 가방 속 보유량보다 적은 수량만큼만 버리겠다고 요청한 경우")
        class Context_with_partial_quantity {
            @Test
            @DisplayName("It: 소프트 딜리트를 수행하지 않고, 더티 체킹을 통해 가방 속 아이템 수량만 정확히 차감한다.")
            void It_부분_폐기_수량차감_성공() {
                // given
                UserItemQuantityRequest request = new UserItemQuantityRequest(1L, 1); // 3개 중 1개만 버리기 요청
                given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(sampleUser));
                given(userItemRepository.findByIdAndUserAndDeletedAtIsNull(request.itemId(), sampleUser)).willReturn(Optional.of(sampleUserItem));

                // when
                userItemService.discardItem(EMAIL, request);

                // then
                Assertions.assertThat(sampleUserItem.getDeletedAt()).isNull();
                Assertions.assertThat(sampleUserItem.getQuantity()).isEqualTo(2);
            }
        }

        @Nested
        @DisplayName("Context: 존재하지 않는 유저 이메일 정보로 폐기를 요청하는 경우")
        class Context_discard_invalid_user {
            @Test
            @DisplayName("It: UNAUTHORIZED_USER 비즈니스 예외를 던지며 접근을 차단한다.")
            void It_폐기_인증_실패_예외_검증() {
                UserItemQuantityRequest request = new UserItemQuantityRequest(1L, 1);
                given(userRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

                Assertions.assertThatThrownBy(() -> userItemService.discardItem(EMAIL, request))
                        .isInstanceOf(BusinessException.class)
                        .hasMessageContaining(ErrorCode.UNAUTHORIZED_USER.getDescription());

                then(userItemRepository).should(never()).findByIdAndUserAndDeletedAtIsNull(any(Long.class), any(User.class));
            }
        }

        @Nested
        @DisplayName("Context: 유저 인벤토리(가방 슬롯 PK)에 존재하지 않는 아이템 식별자로 폐기를 상신한 경우")
        class Context_invalid_user_item_id {
            @Test
            @DisplayName("It: 가방 속 아이템 조회 불가 규칙에 의거해 ITEM_NOT_FOUND 예외를 던진다.")
            void It_인벤토리_장비_미조회_실패_검증() {
                UserItemQuantityRequest request = new UserItemQuantityRequest(999L, 1); // 잘못된 가방 식별자
                given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(sampleUser));
                // 가방 리포지토리 조회 결과가 텅 비어있음(empty)으로 설정
                given(userItemRepository.findByIdAndUserAndDeletedAtIsNull(request.itemId(), sampleUser)).willReturn(Optional.empty());

                Assertions.assertThatThrownBy(() -> userItemService.discardItem(EMAIL, request))
                        .isInstanceOf(BusinessException.class)
                        .hasMessageContaining(ErrorCode.ITEM_NOT_FOUND.getDescription());
            }
        }

        @Nested
        @DisplayName("Context: 가방에 가진 실재 수량보다 더 많은 양의 소모 처리를 요청하는 경우")
        class Context_with_shortage_stock {
            @Test
            @DisplayName("It: 데이터 오염을 막기 위해 ITEM_STOCK_SHORTAGE 비즈니스 예외를 발동한다.")
            void It_재고_부족_실패_예외_검증() {
                UserItemQuantityRequest request = new UserItemQuantityRequest(1L, 999);
                given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(sampleUser));
                given(userItemRepository.findByIdAndUserAndDeletedAtIsNull(request.itemId(), sampleUser)).willReturn(Optional.of(sampleUserItem));

                Assertions.assertThatThrownBy(() -> userItemService.discardItem(EMAIL, request))
                        .isInstanceOf(BusinessException.class)
                        .hasMessageContaining(ErrorCode.ITEM_STOCK_SHORTAGE.getDescription());
            }
        }
    }

}