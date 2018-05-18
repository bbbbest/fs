package test;

import cn.zzu.ss.core.SS;

import java.util.Arrays;

@SS
public abstract class Person implements AnimalAction {
    protected String name;
    protected int age;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(final int age) {
        this.age = age;
    }

    @Override
    public void learn() {
        System.out.println("learn...");
    }

    @Override
    public void sleep() {
        System.out.println("sleep...");
    }

    @Override
    public void eat() {
        System.out.println("eat...");
    }

    @Override
    public void eat(final Object... objects) {
        System.out.println(Arrays.toString(objects));
    }

    @Override
    public void learn(final Object o) {
        System.out.println("learn..." + o);
    }
}
