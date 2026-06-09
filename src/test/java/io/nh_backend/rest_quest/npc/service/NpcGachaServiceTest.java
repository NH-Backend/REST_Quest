package io.nh_backend.rest_quest.npc.service;

import io.nh_backend.rest_quest.common.constant.ErrorCode;
import io.nh_backend.rest_quest.common.exception.BusinessException;
import io.nh_backend.rest_quest.item.domain.Item;
import io.nh_backend.rest_quest.item.domain.ItemGrade;
import io.nh_backend.rest_quest.item.domain.ItemType;
import io.nh_backend.rest_quest.item.domain.UserItem;
import io.nh_backend.rest_quest.npc.dto.GachaResponse;
import io.nh_backend.rest_quest.item.repository.ItemRepository;
import io.nh_backend.rest_quest.item.repository.UserItemRepository;
import io.nh_backend.rest_quest.npc.domain.Npc;
import io.nh_backend.rest_quest.npc.domain.NpcItem;
import io.nh_backend.rest_quest.npc.repository.NpcItemRepository;
import io.nh_backend.rest_quest.npc.repository.NpcRepository;
import io.nh_backend.rest_quest.user.domain.Role;
import io.nh_backend.rest_quest.user.domain.User;
import io.nh_backend.rest_quest.user.domain.UserProfile;
import io.nh_backend.rest_quest.user.domain.Wallet;
import io.nh_backend.rest_quest.user.repository.UserProfileRepository;
import io.nh_backend.rest_quest.user.repository.UserRepository;
import io.nh_backend.rest_quest.user.repository.WalletRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("NpcGachaService 클래스의")
class NpcGachaServiceTest {
    @InjectMocks
    private NpcGachaService npcGachaService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private NpcRepository npcRepository;

    @Mock
    private NpcItemRepository npcItemRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserItemRepository userItemRepository;

    private final String email = "player@example.com";
    private final Long npcId = 1L;
    private final Long npcItemId = 10L;

    private User user;
    private Wallet wallet;
    private UserProfile profile;
    private Npc npc;
    private Item gachaItem;
    private NpcItem npcItem;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email(email)
                .password("password123")
                .nickname("용사")
                .role(Role.USER)
                .build();

        wallet = Wallet.builder()
                .user(user)
                .gold(5000)
                .gem(10)
                .build();

        profile = UserProfile.builder()
                .user(user)
                .level(1)
                .exp(100L)
                .build();

        npc = Npc.builder()
                .rId("npc_gacha_001")
                .name("뽑기 NPC")
                .description("뽑기 상점 NPC")
                .locationKey("town")
                .active(true)
                .build();

        gachaItem = Item.builder()
                .itemName("Golden Gacha Box")
                .rId("gacha_gold")
                .description("골드 등급 보상을 뽑는다.")
                .goldPrice(200)
                .gemPrice(0)
                .sellPrice(0)
                .expCoupon(0)
                .gemCoupon(0)
                .goldCoupon(0)
                .itemType(ItemType.GACHA)
                .itemGrade(ItemGrade.EPIC)
                .build();
        ReflectionTestUtils.setField(gachaItem, "id", 1L);

        npcItem = NpcItem.builder()
                .npc(npc)
                .item(gachaItem)
                .quantity(5)
                .sortOrder(1)
                .build();
        ReflectionTestUtils.setField(npcItem, "id", npcItemId);
    }

    @Nested
    @DisplayName("Describe: purchaseAndDraw 메서드는")
    class Describe_purchaseAndDraw {

        @Nested
        @DisplayName("Context: 일반 뽑기 상자를 구매하여 정상적으로 보상을 주사위질 하는 경우")
        class Context_normal_gacha_draw_flow {

            @Test
            @DisplayName("It: GEM_COUPON 보상이 당첨되면 가방 저장(save) 없이 지갑의 보석 잔액만 누적시킨다.")
            void It_gem_coupon_reward_success() {
                // given
                Item gemCoupon = Item.builder()
                        .itemName("Epic Gem Coupon")
                        .rId("gem_coupon_epic_001")
                        .description("보석을 지급한다.")
                        .goldPrice(0)
                        .gemPrice(0)
                        .sellPrice(0)
                        .expCoupon(0)
                        .gemCoupon(10)
                        .goldCoupon(0)
                        .itemType(ItemType.GEM_COUPON)
                        .itemGrade(ItemGrade.EPIC)
                        .build();
                ReflectionTestUtils.setField(gemCoupon, "id", 2L);

                givenBaseRepositories();

                given(itemRepository.findAllByItemGrade(any(ItemGrade.class))).willReturn(List.of(gemCoupon));

                // when
                GachaResponse response = npcGachaService.purchaseAndDraw(email, npcId, npcItemId);

                // then
                Assertions.assertThat(response.wallet().gold()).isEqualTo(4800); // 5000 - 200
                Assertions.assertThat(response.wallet().gem()).isEqualTo(20);    // 10 + 10
                Assertions.assertThat(response.reward().type()).isEqualTo("GEM");
                Assertions.assertThat(response.reward().amount()).isEqualTo(10);
                Assertions.assertThat(response.acquiredInventoryItem()).isNull();

                then(userItemRepository).should(never()).save(any(UserItem.class));
                Assertions.assertThat(npcItem.getQuantity()).isEqualTo(4); // 매대 재고 차감 실증
            }

            @Test
            @DisplayName("It: 일반 장비/소모품 아이템이 당첨되면 인벤토리 레포지토리에 반영하고 reward.type을 ITEM으로 래핑한다.")
            void It_normal_item_reward_success() {
                // given
                Item sword = Item.builder()
                        .itemName("명장의 카타나")
                        .rId("sword_007")
                        .description("명장이 만든 도이다.")
                        .goldPrice(12000)
                        .gemPrice(0)
                        .sellPrice(6000)
                        .expCoupon(0)
                        .gemCoupon(0)
                        .goldCoupon(0)
                        .itemType(ItemType.WEAPON)
                        .itemGrade(ItemGrade.RARE)
                        .build();
                ReflectionTestUtils.setField(sword, "id", 3L);

                UserItem savedUserItem = UserItem.builder()
                        .user(user)
                        .item(sword)
                        .quantity(1)
                        .equipped(false)
                        .build();

                givenBaseRepositories();
                given(itemRepository.findAllByItemGrade(any(ItemGrade.class))).willReturn(List.of(sword));
                given(userItemRepository.findByUserAndItemAndDeletedAtIsNull(user, sword)).willReturn(Optional.empty());
                given(userItemRepository.save(any(UserItem.class))).willReturn(savedUserItem);

                // when
                GachaResponse response = npcGachaService.purchaseAndDraw(email, npcId, npcItemId);

                // then
                Assertions.assertThat(response.wallet().gold()).isEqualTo(4800);
                Assertions.assertThat(response.reward().type()).isEqualTo("ITEM");
                Assertions.assertThat(response.reward().amount()).isEqualTo(1);
                Assertions.assertThat(response.acquiredInventoryItem()).isNotNull();
                Assertions.assertThat(response.acquiredInventoryItem().itemName()).isEqualTo("명장의 카타나");

                then(userItemRepository).should(times(1)).save(any(UserItem.class));
                Assertions.assertThat(npcItem.getQuantity()).isEqualTo(4);
            }
        }

        @Nested
        @DisplayName("Context: 가차 상자가 아닌 일반 상품이나 상점 매대 데이터를 변조하여 뽑기를 시도하는 경우")
        class Context_invalid_item_type_gacha {

            @Test
            @DisplayName("It: INVALID_PARAMETER 비즈니스 예외를 던지고 재화 차감 및 다이스 연산을 원천 봉쇄한다.")
            void It_throws_INVALID_PARAMETER_exception() {
                // given
                ReflectionTestUtils.setField(gachaItem, "itemType", ItemType.WEAPON); // GACHA나 GOLD_EXCHANGE가 아닌 타입 변조 시뮬레이션

                given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
                given(walletRepository.findByUser(user)).willReturn(Optional.of(wallet));
                given(userProfileRepository.findByUser(user)).willReturn(Optional.of(profile));
                given(npcRepository.findById(npcId)).willReturn(Optional.of(npc));
                given(npcItemRepository.findByIdAndNpc(npcItemId, npc)).willReturn(Optional.of(npcItem));

                // when & then
                Assertions.assertThatThrownBy(() -> npcGachaService.purchaseAndDraw(email, npcId, npcItemId))
                        .isInstanceOf(BusinessException.class)
                        .hasMessageContaining(ErrorCode.INVALID_PARAMETER.getDescription());

                // 원천 무효화 검증
                Assertions.assertThat(wallet.getGold()).isEqualTo(5000);
                Assertions.assertThat(npcItem.getQuantity()).isEqualTo(5);
                then(itemRepository).shouldHaveNoInteractions();
            }
        }

        @Nested
        @DisplayName("Context: 당첨 대상 아이템 타입이 GOLD_EXCHANGE(즉시 골드 환전 상품)로 들어온 경우")
        class Context_gold_exchange_flow {

            @Test
            @DisplayName("It: 다이스 다중 확률 연산을 패스하고, 골드 쿠폰에 명시된 고정 액수(goldCoupon)만큼 골드를 즉시 지갑에 꽂아준다.")
            void It_exchanges_gold_coupon_directly() {
                // given
                ReflectionTestUtils.setField(gachaItem, "itemType", ItemType.GOLD_EXCHANGE);
                ReflectionTestUtils.setField(gachaItem, "goldCoupon", 10000); // 1만 골드 즉시 환전 쿠폰

                givenBaseRepositories();

                // when
                GachaResponse response = npcGachaService.purchaseAndDraw(email, npcId, npcItemId);

                // then
                Assertions.assertThat(response.wallet().gold()).isEqualTo(14800);
                Assertions.assertThat(response.reward().type()).isEqualTo("GOLD");
                Assertions.assertThat(response.reward().amount()).isEqualTo(10000);

                then(itemRepository).shouldHaveNoInteractions();
                then(userItemRepository).shouldHaveNoInteractions();
                Assertions.assertThat(npcItem.getQuantity()).isEqualTo(4);
            }
        }
    }

    private void givenBaseRepositories() {
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(walletRepository.findByUser(user)).willReturn(Optional.of(wallet));
        given(userProfileRepository.findByUser(user)).willReturn(Optional.of(profile));
        given(npcRepository.findById(npcId)).willReturn(Optional.of(npc));
        given(npcItemRepository.findByIdAndNpc(npcItemId, npc)).willReturn(Optional.of(npcItem));
    }
}
