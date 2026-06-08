package io.nh_backend.rest_quest.npc.service;

import io.nh_backend.rest_quest.common.constant.ErrorCode;
import io.nh_backend.rest_quest.common.exception.BusinessException;
import io.nh_backend.rest_quest.item.domain.Item;
import io.nh_backend.rest_quest.item.dto.UserItemQuantityRequest;
import io.nh_backend.rest_quest.item.dto.UserItemResponse;
import io.nh_backend.rest_quest.item.service.UserItemService;
import io.nh_backend.rest_quest.npc.domain.Npc;
import io.nh_backend.rest_quest.npc.domain.NpcItem;
import io.nh_backend.rest_quest.npc.dto.NpcPurchaseResponse;
import io.nh_backend.rest_quest.npc.repository.NpcItemRepository;
import io.nh_backend.rest_quest.user.domain.Role;
import io.nh_backend.rest_quest.user.domain.User;
import io.nh_backend.rest_quest.user.domain.Wallet;
import io.nh_backend.rest_quest.user.repository.UserRepository;
import io.nh_backend.rest_quest.user.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NpcOrderService 클래스의")
class NpcOrderServiceTest {
    @InjectMocks
    private NpcOrderService npcOrderService;

    @Mock
    private NpcItemRepository npcItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private UserItemService userItemService;

    private User user;
    private Wallet wallet;
    private Npc npc;
    private Item goldItem;
    private NpcItem npcGoldItem;

    private final String email = "test@naver.com";
    private final Long npcId = 1L;
    private final Long npcItemId = 10L;

    @BeforeEach
    void setUp() {
        user = new User(email, "password123", "테스트용", Role.USER);
        wallet = Wallet.builder().gold(3000).gem(100).user(user).build();

        npc = Npc.builder().name("잡화상인 아리").active(true).build();
        ReflectionTestUtils.setField(npc, "id", npcId);

        goldItem = Item.builder()
                .rId("potion_hp_001")
                .itemName("HP 포션")
                .goldPrice(100)
                .gemPrice(0)
                .sellPrice(10)
                .description("HP 회복")
                .build();
        ReflectionTestUtils.setField(goldItem, "id", 50L);

        npcGoldItem = NpcItem.builder().npc(npc).item(goldItem).quantity(5).build();
        ReflectionTestUtils.setField(npcGoldItem, "id", npcItemId);
    }

    @Nested
    @DisplayName("Describe: purchaseItem 메서드는")
    class Describe_purchaseItem {

        @Nested
        @DisplayName("Context: 재화와 상점 매대 재고가 모두 충분하게 주어지는 상황인 경우")
        class Context_gold_and_stock_are_sufficient {

            @Test
            @DisplayName("It: 2800L의 남은 골드 잔액과 차감된 매대 재고를 검증하고 가방 적재 서비스를 호출한다.")
            void It_reduces_gold_and_stock_and_adds_to_inventory() {
                // given
                int purchaseQuantity = 2;
                UserItemResponse mockInventoryResponse = new UserItemResponse(
                        1L, goldItem.getId(), goldItem.getRId(), goldItem.getItemName(),
                        "CONSUMABLE", "COMMON", goldItem.getDescription(),
                        goldItem.getGoldPrice(), goldItem.getGemPrice(), goldItem.getSellPrice(),
                        purchaseQuantity, false, "2026-06-08T13:00:00"
                );

                given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
                given(npcItemRepository.findPurchaseTarget(npcId, npcItemId)).willReturn(Optional.of(npcGoldItem));
                given(walletRepository.findByUser(user)).willReturn(Optional.of(wallet));
                given(userItemService.addItemToInventory(eq(email), any(UserItemQuantityRequest.class)))
                        .willReturn(mockInventoryResponse);

                // when
                NpcPurchaseResponse response = npcOrderService.purchaseItem(email, npcId, npcItemId, purchaseQuantity);

                // then
                assertThat(response).isNotNull();
                assertThat(response.wallet().gold()).isEqualTo(2800L);
                assertThat(response.acquiredItem().quantity()).isEqualTo(purchaseQuantity);
                assertThat(npcGoldItem.getQuantity()).isEqualTo(3);

                ArgumentCaptor<UserItemQuantityRequest> captor = ArgumentCaptor.forClass(UserItemQuantityRequest.class);
                then(userItemService).should(times(1)).addItemToInventory(eq(email), captor.capture());
                assertThat(captor.getValue().itemId()).isEqualTo(goldItem.getId());
                assertThat(captor.getValue().quantity()).isEqualTo(purchaseQuantity);
            }
        }

        @Nested
        @DisplayName("Context: 젬(Gem) 기반의 상품 결제를 진행하는 상황인 경우")
        class Context_gem_purchase_flow {

            @Test
            @DisplayName("It: 보석 재화 60젬을 정확히 차감하고 남은 잔액 40L을 실증한다.")
            void It_consumes_gem_successfully() {
                // given
                Long gemNpcItemId = 20L;
                Item gemItem = Item.builder()
                        .rId("weapon_sword_007")
                        .itemName("드래곤 슬레이어")
                        .goldPrice(0)
                        .gemPrice(30)
                        .sellPrice(500)
                        .description("전설의 검")
                        .build();
                ReflectionTestUtils.setField(gemItem, "id", 77L);

                NpcItem npcGemItem = NpcItem.builder().npc(npc).item(gemItem).quantity(5).build();
                ReflectionTestUtils.setField(npcGemItem, "id", gemNpcItemId);

                int purchaseQuantity = 2;
                UserItemResponse mockInventoryResponse = new UserItemResponse(
                        2L, gemItem.getId(), gemItem.getRId(), gemItem.getItemName(),
                        "WEAPON", "LEGENDARY", gemItem.getDescription(),
                        gemItem.getGoldPrice(), gemItem.getGemPrice(), gemItem.getSellPrice(),
                        purchaseQuantity, false, "2026-06-08T13:00:00"
                );

                given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
                given(npcItemRepository.findPurchaseTarget(npcId, gemNpcItemId)).willReturn(Optional.of(npcGemItem));
                given(walletRepository.findByUser(user)).willReturn(Optional.of(wallet));
                given(userItemService.addItemToInventory(eq(email), any(UserItemQuantityRequest.class)))
                        .willReturn(mockInventoryResponse);

                // when
                NpcPurchaseResponse response = npcOrderService.purchaseItem(email, npcId, gemNpcItemId, purchaseQuantity);

                // then
                assertThat(response).isNotNull();
                assertThat(response.wallet().gem()).isEqualTo(40L);
                assertThat(npcGemItem.getQuantity()).isEqualTo(3);
            }
        }

        @Nested
        @DisplayName("Context: 유저가 소지한 골드가 부족하여 결제가 성립되지 않는 상황인 경우")
        class Context_insufficient_gold {

            @Test
            @DisplayName("It: INSUFFICIENT_GOLD 예외를 던지고, 재고 차감 및 인벤토리 지급이 발생하지 않는다.")
            void It_throws_INSUFFICIENT_GOLD_exception() {
                // given
                Wallet lowGoldWallet = Wallet.builder().gold(100).gem(100).user(user).build();
                int purchaseQuantity = 2;

                given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
                given(npcItemRepository.findPurchaseTarget(npcId, npcItemId)).willReturn(Optional.of(npcGoldItem));
                given(walletRepository.findByUser(user)).willReturn(Optional.of(lowGoldWallet));

                // when & then
                assertThatThrownBy(() -> npcOrderService.purchaseItem(email, npcId, npcItemId, purchaseQuantity))
                        .isInstanceOf(BusinessException.class)
                        .hasMessageContaining(ErrorCode.INSUFFICIENT_GOLD.getDescription());

                assertThat(npcGoldItem.getQuantity()).isEqualTo(5);
                then(userItemService).shouldHaveNoInteractions();
            }
        }

        @Nested
        @DisplayName("Context: 상점 매대의 잔여 물품 수량보다 더 많이 구매를 신청한 상황인 경우")
        class Context_shop_stock_shortage {

            @Test
            @DisplayName("It: SHOP_STOCK_SHORTAGE 예외를 유발하고 인벤토리 서비스와의 소통을 일절 금지한다.")
            void It_throws_SHOP_STOCK_SHORTAGE_exception() {
                // given
                int purchaseQuantity = 10;

                given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
                given(npcItemRepository.findPurchaseTarget(npcId, npcItemId)).willReturn(Optional.of(npcGoldItem));
                given(walletRepository.findByUser(user)).willReturn(Optional.of(wallet));

                // when & then
                assertThatThrownBy(() -> npcOrderService.purchaseItem(email, npcId, npcItemId, purchaseQuantity))
                        .isInstanceOf(BusinessException.class)
                        .hasMessageContaining(ErrorCode.SHOP_STOCK_SHORTAGE.getDescription());

                then(userItemService).shouldHaveNoInteractions();
            }
        }

        @Nested
        @DisplayName("Context: 위조 패킷을 발송하여 상인ID와 상품ID 매핑을 비정상적으로 꼬아 보낸 경우")
        class Context_invalid_target_mapping {

            @Test
            @DisplayName("It: NPC_NOT_FOUND 예외를 유발하고 지갑 및 가방에 대한 부가 조회를 사전에 차단한다.")
            void It_intercepts_at_repository_join_line() {
                // given
                Long hackedNpcId = 999L;

                given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
                given(npcItemRepository.findPurchaseTarget(hackedNpcId, npcItemId)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> npcOrderService.purchaseItem(email, hackedNpcId, npcItemId, 1))
                        .isInstanceOf(BusinessException.class)
                        .hasMessageContaining(ErrorCode.NPC_NOT_FOUND.getDescription());

                then(walletRepository).shouldHaveNoInteractions();
                then(userItemService).shouldHaveNoInteractions();
            }
        }

        @Nested
        @DisplayName("Context: 거래하려는 NPC 상인이 마을에서 비활성화(active=false) 처리가 된 경우")
        class Context_inactive_npc_target {

            @Test
            @DisplayName("It: 상점을 이용할 수 없다는 의미의 NPC_NOT_FOUND 예외를 선포한다.")
            void It_throws_NPC_NOT_FOUND_when_npc_is_inactive() {
                // given
                ReflectionTestUtils.setField(npc, "active", false);

                given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
                given(npcItemRepository.findPurchaseTarget(npcId, npcItemId)).willReturn(Optional.of(npcGoldItem));

                // when & then
                assertThatThrownBy(() -> npcOrderService.purchaseItem(email, npcId, npcItemId, 1))
                        .isInstanceOf(BusinessException.class)
                        .hasMessageContaining(ErrorCode.NPC_NOT_FOUND.getDescription());

                then(walletRepository).shouldHaveNoInteractions();
            }
        }

        @Nested
        @DisplayName("Context: 세션 토큰 내부의 이메일 정보와 부합하는 인증 유저가 DB에 매핑되지 않는 경우")
        class Context_user_not_found {

            @Test
            @DisplayName("It: UNAUTHORIZED_USER 예외를 선언하고 즉각 세션을 파기(종료)한다.")
            void It_throws_UNAUTHORIZED_USER_exception() {
                // given
                given(userRepository.findByEmail(email)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> npcOrderService.purchaseItem(email, npcId, npcItemId, 1))
                        .isInstanceOf(BusinessException.class)
                        .hasMessageContaining(ErrorCode.UNAUTHORIZED_USER.getDescription());

                then(npcItemRepository).shouldHaveNoInteractions();
                then(walletRepository).shouldHaveNoInteractions();
                then(userItemService).shouldHaveNoInteractions();
            }
        }

        @Nested
        @DisplayName("Context: 유저 데이터는 로드에 성공했으나 무결성 결함으로 지갑 데이터가 통째로 증발한 상황인 경우")
        class Context_wallet_not_found_system_crash {

            @Test
            @DisplayName("It: 시스템 정합성 크래시 상황으로 간주하여 INTERNAL_SERVER_ERROR 예외를 던진다")
            void It_returns_500_internal_server_error() {
                // given
                given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
                given(npcItemRepository.findPurchaseTarget(npcId, npcItemId)).willReturn(Optional.of(npcGoldItem));
                given(walletRepository.findByUser(user)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> npcOrderService.purchaseItem(email, npcId, npcItemId, 1))
                        .isInstanceOf(BusinessException.class)
                        .hasMessageContaining(ErrorCode.INTERNAL_SERVER_ERROR.getDescription());

                then(userItemService).shouldHaveNoInteractions();
            }
        }
    }
}