package com.ouweihao.community.service;

import com.ouweihao.community.entity.Section;

import java.util.List;

public interface SectionService {

    int addSection(Section section);

    Section findSectionById(int id);

    Section findSectionByName(String name);

    List<Section> getAllSections();

//    List<Section> getDiscusspostSection();

    int updateSection(Section section);

    int deleteSection(int id);

}
