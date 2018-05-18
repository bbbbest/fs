package test;

import cn.zzu.ss.core.SS;

import java.io.Serializable;

@SS
public class Man extends Person implements Serializable {
    private int strength;

    public int getStrength() {
        return strength;
    }

    public void setStrength(final int strength) {
        this.strength = strength;
    }

    @Override
    public String toString() {
        return "Man{" + "strength=" + strength + ", name='" + name + '\'' + ", age=" + age + '}';
    }
}
