package test;

import cn.zzu.ss.core.SS;

import java.io.Serializable;

@SS
public class Woman extends Person implements Serializable {
    private int skill;

    public int getSkill() {
        return skill;
    }

    public void setSkill(final int skill) {
        this.skill = skill;
    }

    @Override
    public String toString() {
        return "Woman{" + "skill=" + skill + ", name='" + name + '\'' + ", age=" + age + '}';
    }
}
