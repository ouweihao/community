package com.ouweihao.community.service.Impl;

import com.ouweihao.community.dao.SectionMapper;
import com.ouweihao.community.entity.Section;
import com.ouweihao.community.service.SectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SectionServiceImpl implements SectionService {

    @Autowired
    private SectionMapper sectionMapper;

    @Transactional
    @Override
    public int addSection(Section section) {
        return sectionMapper.insertSection(section);
    }

    @Override
    public Section findSectionById(int id) {
        return sectionMapper.selectSectionById(id);
    }

    @Override
    public Section findSectionByName(String name) {
        return sectionMapper.selectSectionByName(name);
    }

    @Override
    public List<Section> getAllSections() {
        return sectionMapper.getAllSections();
    }

//    @Override
//    public List<Section> getDiscusspostSection() {
//        return sectionMapper.getDiscusspostSection();
//    }

    @Override
    public int updateSection(Section section) {
        return sectionMapper.updateSection(section);
    }

    @Override
    public int deleteSection(int id) {
        return sectionMapper.deleteSectionById(id);
    }
}
