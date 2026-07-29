package io.github.niestrat99.advancedteleport.commands.warp;

import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.payments.PaymentManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class SetWarpCommandTest {

    private MockedStatic<CustomMessages> mockMessages;
    private MockedStatic<AdvancedTeleportAPI> mockATApi;
    private MockedStatic<PaymentManager> mockPaymentManager;

    private PaymentManager paymentManager;

    @Before
    public void setUp() {
        mockMessages = Mockito.mockStatic(CustomMessages.class);
        mockATApi = Mockito.mockStatic(AdvancedTeleportAPI.class);
        mockPaymentManager = Mockito.mockStatic(PaymentManager.class);

        paymentManager = Mockito.mock(PaymentManager.class);

        mockPaymentManager.when(PaymentManager::getInstance).thenReturn(paymentManager);
    }

    @After
    public void teardown() {
        mockMessages.close();
        mockATApi.close();
    }

    @Test
    public void setWarp_onCommand_returnsSucceededMessage() {

    }
}
