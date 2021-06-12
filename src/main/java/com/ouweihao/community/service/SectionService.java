package com.ouweihao.community.service;

import com.ouweihao.community.entity.Section;

import java.util.List;
import java.util.Map;

public interface SectionService {

    int findSectionCount();

    Map<String, String> addSection(Section section);

    Section findSectionById(int id);

    Section findSectionByName(String name);

    List<Section> findSections(int offset, int limit);

    List<Section> getAllSections();

//    List<Section> getDiscusspostSection();

    Map<String, String> updateSection(Section section);

    int deleteSection(int id);

}
