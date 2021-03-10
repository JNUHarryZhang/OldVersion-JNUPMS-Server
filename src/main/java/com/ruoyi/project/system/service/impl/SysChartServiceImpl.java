package com.ruoyi.project.system.service.impl;

import com.ruoyi.common.enums.TitleSource;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.project.system.domain.Tech;
import com.ruoyi.project.system.domain.vo.TechTotal;
import com.ruoyi.project.system.mapper.SysChartMapper;
import com.ruoyi.project.system.mapper.SysPaperInfoMapper;
import com.ruoyi.project.system.mapper.SysSkillTypeMapper;
import com.ruoyi.project.system.mapper.SysVocaMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.*;

@Service
public class SysChartServiceImpl {

    @Resource
    private SysChartMapper chartMapper;

    @Resource
    private SysSkillTypeMapper skillTypeMapper;

    @Resource
    private SysPaperInfoMapper paperInfoMapper;

    @Resource
    private SysVocaMapper vocaMapper;

    public List<Map<String, Object>> getTitleSource() {
        List<Map<String, Object>> titleSource = chartMapper.getTitleSource();
        for (Map<String, Object> map : titleSource) {
            map.put("name", TitleSource.get(map.get("name").toString()));
        }
        return titleSource;
    }

    public List<Map<String, Object>> getTeacher() {
        return chartMapper.getTeacher();
    }

    public Map<String, Object> getTechByYear() {

        Map<String, Object> result = new HashMap<>();

        Map<Long, String> skill = new HashMap<>();
        List<Map<String, Object>> skillList = skillTypeMapper.querySkillList("");
        for (Map<String, Object> map : skillList) {
            Long id = ((BigInteger) map.get("id")).longValue();
            String techType = (String) map.get("techType");
            skill.put(id, techType);
        }
        List<Tech> techByYear = chartMapper.getTechByYear();
        techByYear.sort(Comparator.comparing(Tech::getPaperYear));

        List<String> resultYear = new ArrayList<>();
        for (Tech tech : techByYear) {
            resultYear.add(tech.getPaperYear());
        }
        result.put("years", resultYear);

        Map<String, List<Integer>> skillTotalList = new HashMap<>();
        for (Tech tech : techByYear) {

            List<String> name = new ArrayList<>();
            List<TechTotal> techTotals = tech.getTechTotals();
            for (TechTotal techTotal : techTotals) {
                Long id = techTotal.getId();
                Integer total = techTotal.getTotal();
                String s = skill.get(id);
                name.add(s);
                if (skillTotalList.containsKey(s)) {
                    List<Integer> integers = skillTotalList.get(s);
                    integers.add(total);
                } else {
                    List<Integer> list = new ArrayList<>();
                    list.add(total);
                    skillTotalList.put(s, list);
                }
            }
            Set<Map.Entry<Long, String>> entries = skill.entrySet();
            for (Map.Entry<Long, String> entry : entries) {
                String value = entry.getValue();
                if (!name.contains(value)) {
                    if (skillTotalList.containsKey(value)) {
                        List<Integer> list = skillTotalList.get(value);
                        list.add(0);
                    } else {
                        List<Integer> list = new ArrayList<>();
                        list.add(0);
                        skillTotalList.put(value, list);
                    }
                }
            }
        }
        result.put("typeList", skillTotalList);
        return result;
    }

    private void getWorldCloud() {

        String url = "C:/Users/Xinke/Downloads/wordCloud/wordCloud";

        List<String> titles = paperInfoMapper.selectAllTitle();
        String lunwen = url + "/doc/lunwen.txt";
        FileUtils.writeLists(lunwen, titles);

        String dict = url + "/userdict/userdict.txt";
        List<String> nameList = vocaMapper.selectAllName();
        FileUtils.writeLists(dict, nameList);

        String executer = "python";
        String file_path = "C:/Users/Xinke/Downloads/wordCloud/wordCloud/demo.py"; // python绝对路径
        String[] command_line = new String[] {executer, file_path};
        Process process;
        try {
            process = Runtime.getRuntime().exec(command_line);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            line = in.readLine();
            in.close();
            if (Objects.equals(line, "1")) {
                System.out.println("yes");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    @Scheduled(cron = "0 0/20 * * * ?")
    private void cronTask() {
        getWorldCloud();
    }

}
