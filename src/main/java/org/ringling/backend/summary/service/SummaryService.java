package org.ringling.backend.summary.service;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.beans.factory.annotation.Value;
import froggy.winterframework.stereotype.Service;
import java.io.IOException;
import java.util.List;
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
    private final String ApiUrl;

    @Autowired
    public SummaryService(
        SummaryRepository summaryRepository,
        @Value("api.summary.url") String ApiUrl
    ) {
        this.summaryRepository = summaryRepository;
        this.ApiUrl = ApiUrl;
    }

    /**
     * 주어진 URL 요약을 비동기 처리함
     * DB에 없으면 PENDING 상태로 저장 후 비동기 API 호출
     * 최종 결과를 DB에 반영함
     *
     * @param url 요약 대상 URL
     * @return 기존 또는 새로 생성된 Summary
     */
    public Summary processSummaryAsync(String url) {
        return summaryRepository.findByUrl(url)
            .orElseGet(() -> {
                Summary newSummary = summaryRepository.save(buildSummary(url));

                // 2. 비동기 작업 시작
                CompletableFuture.runAsync(() -> {
                    updateSummaryFromApi(newSummary);
                    summaryRepository.save(newSummary);
                });

                return newSummary;
            });
    }

    /**
     * 주어진 URL 요약을 동기 처리함
     * DB에 없으면 API 호출로 완성 Summary 생성 후 반환
     * 생성된 Summary는 DB에 저장하지 않음
     *
     * @param url 요약 대상 URL
     * @return 기존 또는 새로 생성된 Summary
     */
    public Summary processSummarySync(String url) {
        return summaryRepository.findByUrl(url)
            .orElseGet(() -> {
                Summary newSummary = buildSummary(url);
                return updateSummaryFromApi(newSummary);
            });
    }

    public List<Summary> findAllById(List<Integer> summaryIds) {
        return summaryRepository.findAllById(summaryIds);
    }

    /**
     * 외부 API 호출로 Summary 상태 갱신함
     * DB 저장은 호출 측 책임임
     *
     * @param summary 갱신 대상 Summary
     * @return 갱신된 Summary
     */
    private Summary updateSummaryFromApi(Summary summary) {
        try {
            HttpResponse<TitleSummarizationResult> apiResponse = sendRestAPI(summary);
            summary.updateSummaryTitle(apiResponse.getResponseBody().getSummaryTitle());
            summary.updateSummaryStatus(SummaryStatus.COMPLETED);
        } catch (Exception e) {
            log.error("Failed to fetch summary for URL: {}", summary.getUrl(), e);
            summary.updateSummaryStatus(SummaryStatus.FAILED);
        }
        return summary;
    }

    private Summary buildSummary(String targetUrl) {
        return Summary.builder()
            .url(targetUrl)
            .summaryStatus(SummaryStatus.PENDING)
            .build();
    }

    private HttpResponse<TitleSummarizationResult> sendRestAPI(Summary summary) throws IOException {
        return HttpClient.send(
            HttpMethod.POST,
            ApiUrl,
            new TitleSummarizationPayload(summary.getUrl()),
            null,
            ContentType.JSON,
            TitleSummarizationResult.class
        );
    }
}
