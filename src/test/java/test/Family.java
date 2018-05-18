package test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Family implements Serializable {
    private Man host;
    private Woman hostess;

    private List<Person> children;

    private List<Person> elders;

    public Family(final Man host, final Woman hostess) {
        this.host = host;
        this.hostess = hostess;

        this.children = new ArrayList<>();
        this.elders = new ArrayList<>();
    }

    public void addElder(Person elder) {
        elders.add(elder);
    }

    public void addChild(Person child) {
        children.add(child);
    }

    public Man getHost() {
        return host;
    }

    public Woman getHostess() {
        return hostess;
    }

    public List<Person> getChildren() {
        return children;
    }

    public List<Person> getElders() {
        return elders;
    }

    @Override
    public String toString() {
        return "Family{" + "host=" + host + ", hostess=" + hostess + ", children=" + children + ", elders=" + elders + '}';
    }
}
