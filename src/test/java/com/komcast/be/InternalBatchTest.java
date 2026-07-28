package com.komcast.be;

import com.komcast.be.dto.BatchCompletionRequestDto;
import com.komcast.be.service.InternalBatchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class InternalBatchTest {

    @Autowired
    private InternalBatchService internalBatchService;

    @Test
    @DisplayName("데이터 서버 배치 완료 트리거 수신 시 페이로드가 있어도 정상 작동해야 함")
    void batchCompletionAsyncWebhookWithPayloadTest() {
        BatchCompletionRequestDto payload = BatchCompletionRequestDto.builder()
                .runDate("2026-07-27")
                .build();

        assertDoesNotThrow(() -> internalBatchService.processBatchCompletionAsync(payload));
    }

    @Test
    @DisplayName("데이터 서버 배치 완료 트리거 수신 시 페이로드(Null Body)가 없어도 정상 작동해야 함")
    void batchCompletionAsyncWebhookWithoutPayloadTest() {
        assertDoesNotThrow(() -> internalBatchService.processBatchCompletionAsync(null));
    }
}
