package com.sysmin.core.jvm.service.impl;

import com.sysmin.core.jvm.domain.JstackDO;
import com.sysmin.core.jvm.domain.SnapShotDO;
import com.sysmin.core.jvm.enums.JstackType;
import com.sysmin.core.jvm.service.api.JstackApi;
import com.sysmin.global.GlobalConfig;
import com.sysmin.global.LayuiTableVO;
import com.sysmin.util.BashUtil;
import com.sysmin.util.DateUtil;
import com.sysmin.util.FileUtil;
import com.sysmin.util.StringUtil;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author:Li
 * @time: 2019/1/1 11:04
 * @version: 1.0.0
 */
@Service
public class JstackImpl implements JstackApi {

    /**
     * 线程前缀
     */
    private final static String PREFIX = "java.lang.Thread.State: ";

    @Override
    public JstackDO jstack(int id, JstackType type) {
        String data = "";
        data = BashUtil.exec(type.getType(id));
        return new JstackDO()
                .setPid(id)
                .setTotal(StringUtil.findNum(data, "nid"))
                .setRunning(StringUtil.findNum(data, PREFIX + "RUNNABLE"))
                .setWaiting(StringUtil.findNum(data, PREFIX + "WAITING"))
                .setTimeWaiting(StringUtil.findNum(data, PREFIX + "TIMED_WAITING"))
                .setBlocked(StringUtil.findNum(data, PREFIX + "BLOCKED"))
                .setVmTotal()
                .setDate(DateUtil.getNowDate(DateUtil.HOUR + "时" + DateUtil.MINUTE + "分"));
    }

    @Override
    public String threadSnap(int id, JstackType type) {
        String path = FileUtil.checkPath(id, "thread", "thread_", ".txt");
        String command = type.getType(id, path);
        if (((String) GlobalConfig.getValue("OSSystem")).contains("Linux")) {
            BashUtil.exec(BashUtil.toLinuxCommand(command));
        } else {
            int result = FileUtil.saveDataToFile(BashUtil.exec(command), path);
            if (result != 1) {
                return null;
            }
        }
        return path;
    }

    @Override
    public String[] listThreadSnap() {
        return FileUtil.listFile((String) GlobalConfig.getValue("dumpPath") + File.separatorChar + "thread");
    }

    public LayuiTableVO getThreadSnap(int page, int limit) {
        List<String> paths = Arrays.asList(FileUtil.listFile((String) GlobalConfig.getValue("dumpPath") + File.separatorChar + "thread"));
        ArrayList<SnapShotDO> list = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {
            if (i >= ((page - 1) * limit) && i < ((page - 1) * limit) + limit) {
                String s = paths.get(i);
                String[] names = s.substring(s.lastIndexOf(File.separator) + 1, s.length()).split("_");
                list.add(new SnapShotDO(Integer.valueOf(names[1]), new File(s).length(), DateUtil.dateFormat(names[2].substring(0, names[2].indexOf(".")), "yyyyMMddHHmmss", DateUtil.DEFAULT_FORMAT), s));
            }
        }
        list.forEach(System.out::println);
        return new LayuiTableVO(200, "", paths.size(), list);
    }

    public static void main(String[] args) {
        System.out.println(DateUtil.worldToDate("29/April/2016:17:38:20 +0800"));
        // new JstackImpl().getThreadSnap(1, 10);
    }

}
