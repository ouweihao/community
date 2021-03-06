package com.ouweihao.community.dao;

import com.ouweihao.community.entity.Section;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SectionMapper {

    int selectSectionCount();

    int insertSection(Section section);

    Section selectSectionById(int id);

    Section selectSectionByName(String name);

    List<Section> selectSections(int offset, int limit);

    List<Section> getAllSections();

//    List<Section> getDiscusspostSection();

    int updateSection(Section section);

    int deleteSectionById(int id);

}
