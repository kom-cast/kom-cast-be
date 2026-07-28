package com.komcast.be.service;

import com.komcast.be.domain.*;
import com.komcast.be.dto.TtsRequestDto;
import com.komcast.be.repository.ScriptRepository;
import com.komcast.be.repository.ScriptSectionRepository;
import com.komcast.be.repository.SectionLineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScriptService {

    private final ScriptRepository scriptRepository;
    private final ScriptSectionRepository scriptSectionRepository;
    private final SectionLineRepository sectionLineRepository;

    public TtsRequestDto getTtsPayloadFromScript(UUID scriptId) {
        Script script = scriptRepository.findById(scriptId)
                .orElseThrow(() -> new IllegalArgumentException("Script not found in DB with id: " + scriptId));

        List<ScriptSection> scriptSections = scriptSectionRepository.findByScriptIdOrderBySectionOrderAsc(script.getId());

        List<TtsRequestDto.TtsSection> sections = new ArrayList<>();
        for (ScriptSection ss : scriptSections) {
            Section sec = ss.getSection();
            List<SectionLine> lines = sectionLineRepository.findBySectionIdOrderByLineOrderAsc(sec.getId());

            List<TtsRequestDto.TtsLine> ttsLines = lines.stream()
                    .map(l -> TtsRequestDto.TtsLine.builder()
                            .speaker(l.getTalker())
                            .text(l.getContent())
                            .build())
                    .collect(Collectors.toList());

            TtsRequestDto.TtsTarget target = TtsRequestDto.TtsTarget.builder()
                    .type(sec.getTargetType() != null ? sec.getTargetType().name() : null)
                    .stockCode(sec.getStockCode())
                    .industryCode(sec.getIndustryCode())
                    .build();

            sections.add(TtsRequestDto.TtsSection.builder()
                    .sectionType(sec.getSectionType() != null ? sec.getSectionType().name() : ss.getSectionType().name())
                    .target(target)
                    .lines(ttsLines)
                    .build());
        }

        return TtsRequestDto.builder()
                .scriptId(script.getId().toString())
                .sections(sections)
                .build();
    }
}
