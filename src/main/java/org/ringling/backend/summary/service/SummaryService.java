package org.ringling.backend.summary.service;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.stereotype.Service;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.ringling.backend.common.utils.http.ContentType;
import org.ringling.backend.common.utils.http.HttpClient;
import org.ringling.backend.common.utils.http.HttpMethod;
import org.ringling.backend.common.utils.http.HttpResponse;
import org.ringling.backend.summary.dto.TitleSummarizationPayload;
import org.ringling.backend.summary.dto.TitleSummarizationResult;
import org.ringling.backend.summary.entity.Summary;
import org.ringling.backend.summary.entity.SummaryStatus;
import org.ringling.backend.summary.repository.SummaryRepository;

@Slf4j
@Service
public class SummaryService {

    private final SummaryRepository summaryRepository;

    @Autowired
    public SummaryService(SummaryRepository summaryRepository) {
        this.summaryRepository = summaryRepository;
    }

    public Summary processSummary(String url) {
        Summary existingSummary = summaryRepository.findByUrl(url);
        if (existingSummary != null) {
            return existingSummary;
        }

        Summary newSummary = Summary.builder()
            .url(url)
            .summaryStatus(SummaryStatus.PENDING)
            .build();

        summaryRepository.save(newSummary);

        /**
         * TODO: 비동기 AI 요약 요청 수행
         * 요청 결과로 받은 summaryTitle 값으로 엔티티 필드 갱신 후 DB에 반영
         */

        try {
            sendInferRequestAsync(newSummary);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return newSummary;
    }

    public void sendInferRequestAsync(Summary summary) {
        CompletableFuture
            .supplyAsync(() -> {
                try {
                    return sendRestAPI(summary);
                } catch (Exception e) {
                    throw new RuntimeException(e); // CompletableFuture에 예외 전달
                }
            })
            .thenAccept(result -> {
                summary.updateSummaryTitle(result.getResponseBody().getSummaryTitle());
                summary.updateSummaryStatus(SummaryStatus.COMPLETED);
                summaryRepository.save(summary);
            })
            .exceptionally(e -> {
                summary.updateSummaryStatus(SummaryStatus.FAILED);
                summaryRepository.save(summary);
                log.error("비동기 요청 실패", e);
                return null;
            });
    }
    private static HttpResponse<TitleSummarizationResult> sendRestAPI(Summary summary) throws IOException {
        return HttpClient.send(
            HttpMethod.POST,
            "http://localhost:8000/api/summary",
            new TitleSummarizationPayload(summary.getUrl()),
            null,
            ContentType.JSON,
            TitleSummarizationResult.class
        );
    }

}
