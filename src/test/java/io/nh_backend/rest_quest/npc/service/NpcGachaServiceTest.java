package io.nh_backend.rest_quest.npc.service;

import io.nh_backend.rest_quest.item.domain.Item;
import io.nh_backend.rest_quest.item.domain.ItemGrade;
import io.nh_backend.rest_quest.item.domain.ItemType;
import io.nh_backend.rest_quest.item.domain.UserItem;
import io.nh_backend.rest_quest.item.dto.GachaResponse;
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
                .itemName("Epic Gem Box")
                .rId("gacha_epic_001")
                .description("EPIC 등급 보상을 뽑는다.")
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
                .quantity(1)
                .sortOrder(1)
                .build();
    }

    @Nested
    @DisplayName("Describe: purchaseAndDraw() 메서드는")
    class Describe_with_purchase_and_draw {

        @Test
        @DisplayName("It: GEM_COUPON이 뽑히면 인벤토리에 저장하지 않고 wallet.gem만 증가시킨다.")
        void It_gem_coupon_reward_success() {
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
            given(itemRepository.findAllByItemGrade(ItemGrade.EPIC)).willReturn(List.of(gemCoupon));

            GachaResponse response = npcGachaService.purchaseAndDraw(email, 1L, 1L);

            Assertions.assertThat(response.wallet().gold()).isEqualTo(4800);
            Assertions.assertThat(response.wallet().gem()).isEqualTo(20);
            Assertions.assertThat(response.reward().type()).isEqualTo("GEM");
            Assertions.assertThat(response.reward().amount()).isEqualTo(10);
            Assertions.assertThat(response.acquiredInventoryItem()).isNull();
            then(userItemRepository).should(never()).save(any(UserItem.class));
        }

        @Test
        @DisplayName("It: 일반 아이템이 뽑히면 인벤토리에 추가하고 reward.type을 ITEM으로 반환한다.")
        void It_normal_item_reward_success() {
            Item sword = Item.builder()
                    .itemName("명장의 카타나")
                    .rId("sword_007")
                    .description("한 시대를 풍미한 명장이 만든 도이다.")
                    .goldPrice(12000)
                    .gemPrice(0)
                    .sellPrice(6000)
                    .expCoupon(0)
                    .gemCoupon(0)
                    .goldCoupon(0)
                    .itemType(ItemType.WEAPON)
                    .itemGrade(ItemGrade.EPIC)
                    .build();
            ReflectionTestUtils.setField(sword, "id", 3L);

            UserItem savedUserItem = UserItem.builder()
                    .user(user)
                    .item(sword)
                    .quantity(1)
                    .equipped(false)
                    .build();

            givenBaseRepositories();
            given(itemRepository.findAllByItemGrade(ItemGrade.EPIC)).willReturn(List.of(sword));
            given(userItemRepository.findByUserAndItemAndDeletedAtIsNull(user, sword)).willReturn(Optional.empty());
            given(userItemRepository.save(any(UserItem.class))).willReturn(savedUserItem);

            GachaResponse response = npcGachaService.purchaseAndDraw(email, 1L, 1L);

            Assertions.assertThat(response.wallet().gold()).isEqualTo(4800);
            Assertions.assertThat(response.reward().type()).isEqualTo("ITEM");
            Assertions.assertThat(response.reward().amount()).isEqualTo(1);
            Assertions.assertThat(response.acquiredInventoryItem()).isNotNull();
            Assertions.assertThat(response.acquiredInventoryItem().itemName()).isEqualTo("명장의 카타나");
            then(userItemRepository).should().save(any(UserItem.class));
        }
    }

    private void givenBaseRepositories() {
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(walletRepository.findByUser(user)).willReturn(Optional.of(wallet));
        given(userProfileRepository.findByUser(user)).willReturn(Optional.of(profile));
        given(npcRepository.findById(1L)).willReturn(Optional.of(npc));
        given(npcItemRepository.findByIdAndNpc(1L, npc)).willReturn(Optional.of(npcItem));
    }
}
