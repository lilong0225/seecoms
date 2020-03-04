package com.seecoms.data.opera;


public class TaskBean implements Distribute {
    private final String TAG = TaskBean.class.getSimpleName();
    public String id;
    public int t;
    public long d;
    public String u;
    public boolean cd;
    public String s;
    public String fn;
    public String pn;
    public String ex;


    @Override
    public void handlOut() {
        try {
            a task = new a(this);
            task.execute();
        } catch (Exception e) {
        }
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof TaskBean) {
            TaskBean temp = (TaskBean) o;
            if (d - temp.d > 0) {
                return 1;
            } else if (d - temp.d < 0){
                return -1;
            }
        }
        return 0;
    }
}
