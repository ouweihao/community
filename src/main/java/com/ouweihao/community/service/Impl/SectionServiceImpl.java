package com.ouweihao.community.service.Impl;

import com.ouweihao.community.dao.SectionMapper;
import com.ouweihao.community.entity.Section;
import com.ouweihao.community.service.SectionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SectionServiceImpl implements SectionService {

    @Autowired
    private SectionMapper sectionMapper;

    @Override
    public int findSectionCount() {
        return sectionMapper.selectSectionCount();
    }

    @Transactional
    @Override
    public Map<String, String> addSection(Section section) {
        Map<String, String> insertMsg = new HashMap<>();

        if (StringUtils.isBlank(section.getName())) {
            insertMsg.put("nameMsg", "版块名不能为空！！！");
            return insertMsg;
        }

        if (sectionMapper.selectSectionByName(section.getName()) != null) {
            insertMsg.put("nameMsg", "该版块名已存在！！！");
            return insertMsg;
        }

        sectionMapper.insertSection(section);

        insertMsg.put("nameMsg", "新增版块成功！！！");

        return insertMsg;
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
    public List<Section> findSections(int offset, int limit) {
        return sectionMapper.selectSections(offset, limit);
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
    public Map<String, String> updateSection(Section section) {

        Map<String, String> updateMsg = new HashMap<>();

        if (StringUtils.isBlank(section.getName())) {
            updateMsg.put("nameMsg", "分类名不能为空！！！");
            return updateMsg;
        }

        if (sectionMapper.selectSectionByName(section.getName()) != null) {
            updateMsg.put("nameMsg", "该分类名已存在！！！");
            return updateMsg;
        }

        sectionMapper.updateSection(section);

        updateMsg.put("nameMsg", "更新成功！！！");

        return updateMsg;
    }

    @Override
    public int deleteSection(int id) {
        return sectionMapper.deleteSectionById(id);
    }
}
